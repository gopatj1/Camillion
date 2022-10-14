package ru.irev.camillionforinst.subscribe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.irev.camillionforinst.BaseActivity;
import ru.irev.camillionforinst.BuildConfig;
import ru.irev.camillionforinst.R;
import ru.irev.camillionforinst.controller.UserSettingsController;
import ru.irev.camillionforinst.subscribe.util.IabHelper;
import ru.irev.camillionforinst.subscribe.util.IabResult;
import ru.irev.camillionforinst.subscribe.util.Inventory;
import ru.irev.camillionforinst.subscribe.util.Purchase;
import ru.irev.camillionforinst.subscribe.util.SkuDetails;
import ru.irev.camillionforinst.utils.DialogUtils;

import static ru.irev.camillionforinst.utils.Constants.CAMILLION_TAG;
import static ru.irev.camillionforinst.utils.Constants.IN_APP_KEY;

public class PaymentActivity extends BaseActivity {

    private static final int RC_REQUEST = 15567;
    private final String SKU_PURCHASE = "sku_purchase";
    private final String[] AVAILABLE_SKU_IDS = new String[]{ SKU_PURCHASE }; //TODO внести названия покупок
    private HashMap<String, String> prices = new HashMap<>();
    IabHelper mHelper;
    private String mSubscribeItem;
    private String payload;
    private boolean isRestoreOnly = false;
    private boolean hasPurchase = false;
    private boolean playMarketPurchaseWindowOpened = false;

    @BindView(R.id.paymentBackgroundImage)
    ImageView paymentBackground;
    @BindView(R.id.purchase)
    View purchaseBtn;

    @OnClick(R.id.paymentBtnClose)
    void onPaymentBtnClose() {
        analyticsLogEvent("Закрытие окна подписки");
        finish();
    }

    @OnClick(R.id.purchase)
    void onPurchaseClick() {
        subscribeClick(SKU_PURCHASE);
    }

    @OnClick(R.id.restorePurchase)
    void onPurchaseRestoreClick() {
        isRestoreOnly = true;
        getInventory();
    }

    public static void start(Activity context) {
        Intent intent = new Intent(context, PaymentActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        ButterKnife.bind(this);
        String key = getValidKey();
        mHelper = new IabHelper(this, key);
        mHelper.enableDebugLogging(BuildConfig.DEBUG);

        mHelper.startSetup(result -> {
            if (!result.isSuccess()) {
                // Oh noes, there was a problem.
                Log.d(CAMILLION_TAG, "Start setup result success false: " + result.getMessage());
                PaymentActivity.this.finish();
                return;
            }
            getInventory();
        });

        purchaseBtn.setTag(SKU_PURCHASE);
        purchaseBtn.setOnClickListener(subscribeListener);

        // if device language is russian, then set russian background image. Default - english image
        if (Locale.getDefault().getLanguage().equals("ru"))
            paymentBackground.setImageDrawable(getResources().getDrawable(R.drawable.payment_background_rus));
    }

    private void getInventory() {
        if (mHelper == null) return;
        try {
            mHelper.queryInventoryAsync(true, Arrays.asList(AVAILABLE_SKU_IDS), mGotInventoryListener);
            if (isRestoreOnly) {
                if (hasPurchase) DialogUtils.alert(this, getResources().getString(R.string.payment_restore_success), dialog -> finish());
                else DialogUtils.alert(this, getResources().getString(R.string.payment_no_restore));
                isRestoreOnly = false;
            }
        } catch (Exception ignored) {
            Log.e(CAMILLION_TAG, ignored.getMessage());
        }
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            hasPurchase = false;
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                DialogUtils.showNoInternetInShopDialog(PaymentActivity.this, buttonId -> {
                    if (buttonId == 1) {
                        getInventory();
                    } else PaymentActivity.this.finish();
                });
                return;
            }

            for (String sku : AVAILABLE_SKU_IDS) {
                prices.remove(sku);
                SkuDetails details = inventory.getSkuDetails(sku);
                if (details != null) {
                    prices.put(sku, inventory.getSkuDetails(sku).getPrice());
                    Log.d(CAMILLION_TAG, "SkuDetails: " + details.toString());
                }
                Purchase purchase = inventory.getPurchase(sku);
                if (purchase != null) {
                    Log.d(CAMILLION_TAG, "Purchase: " + purchase.toString());
                    hasPurchase = true;
                    DialogUtils.alert(PaymentActivity.this, getResources().getString(R.string.payment_restore_success), dialog -> finish());
                }
            }
            UserSettingsController.setUserPaid(realm, hasPurchase);
        }
    };

    void complain(String message) {
        if (BuildConfig.DEBUG) DialogUtils.alert(this, "Error: " + message);
        Log.e(CAMILLION_TAG, "Error: " + message);
    }

    private String getValidKey() {
        return IN_APP_KEY;
    }

    View.OnClickListener subscribeListener = v -> {
        String sku = (String) v.getTag();
        analyticsLogEvent("Приобретение подписки " + sku);
        if (BuildConfig.DEBUG) {
            UserSettingsController.setUserPaid(realm, true);
            DialogUtils.alert(this, "Совершена \"Тестовая\" подписка для удобного тестирования. Автоматически отменится при перезагрузке приложения", dialog -> finish());
            return;
        }
        Log.d(CAMILLION_TAG, "Try to purchase: " + sku);
        subscribeClick(sku);
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (playMarketPurchaseWindowOpened) {
            if (!hasPurchase)
                DialogUtils.alert(this, getResources().getString(R.string.payment_purchase_stop), dialog -> finish());
            playMarketPurchaseWindowOpened = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            try {
                mHelper.dispose();
            } catch (Exception ignored) {
                Log.e(CAMILLION_TAG, ignored.getMessage());
            }
            mHelper = null;
        }
    }

    // Subscribe button clicked. Explain to user, then start purchase
    public void subscribeClick(String sku) {
        mSubscribeItem = sku;
        payload = generatePayload();

        int index = -1;
        for (int i = 0; i < AVAILABLE_SKU_IDS.length; i++) {
            if (AVAILABLE_SKU_IDS[i].equals(sku)) {
                index = i;
                break;
            }
        }
        if (index == -1) return;
        try {
            if (index == 0) {
                mHelper.launchPurchaseFlow(this, sku, RC_REQUEST, mPurchaseFinishedListener, payload);
                playMarketPurchaseWindowOpened = false;
            } else {
                mHelper.launchPurchaseFlow(this, sku, IabHelper.ITEM_TYPE_SUBS, RC_REQUEST, mPurchaseFinishedListener, payload);
                getInventory();
                playMarketPurchaseWindowOpened = true;
            }
        } catch (Exception ignored) {
            Log.e(CAMILLION_TAG, ignored.getMessage());
        }
    }

    private String generatePayload() {
        StringBuilder sb = new StringBuilder();

        char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
                'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

        for (int i = 0; i < 32; i++) {
            sb.append(alphabet[((int) (Math.random() * alphabet.length)) % alphabet.length]);
        }
        sb.insert(15, "$$$");
        return sb.toString();
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        if (payload.equals(this.payload) && payload.contains("$$$")) {
            return this.payload.charAt(15) == '$';
        }
        return false;
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                if (result.getResponse() != IabHelper.IABHELPER_USER_CANCELLED) {
                    DialogUtils.alert(PaymentActivity.this, getResources().getString(R.string.payment_restore_error) + result.getMessage(), dialogInterface -> PaymentActivity.this.finish());
                }
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                DialogUtils.alert(PaymentActivity.this, getResources().getString(R.string.payment_restore_error_auth), dialogInterface -> PaymentActivity.this.finish());
                return;
            }
            if (purchase.getSku().equals(mSubscribeItem)) {
                UserSettingsController.setUserPaid(realm, true);
            }
        }
    };

    @Override
    public String getScreenName() {
        return "Окно подписки";
    }
}