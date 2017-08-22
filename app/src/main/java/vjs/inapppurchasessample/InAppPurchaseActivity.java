package vjs.inapppurchasessample;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import needle.Needle;
import needle.UiRelatedProgressTask;
import vjs.inapppurchasessample.billing_utils.IabHelper;
import vjs.inapppurchasessample.billing_utils.IabResult;
import vjs.inapppurchasessample.billing_utils.Inventory;

import static vjs.inapppurchasessample.MainActivity.CONSUMPTION_PRODUCT_ID;
import static vjs.inapppurchasessample.MainActivity.HALF_YEARLY_PRODUCT_ID;
import static vjs.inapppurchasessample.MainActivity.MONTHLY_PRODUCT_ID;
import static vjs.inapppurchasessample.MainActivity.QUARTERLY_PRODUCT_ID;
import static vjs.inapppurchasessample.MainActivity.WEEKLY_PRODUCT_ID;
import static vjs.inapppurchasessample.MainActivity.YEARLY_PRODUCT_ID;


public class InAppPurchaseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = InAppPurchaseActivity.class.getSimpleName();
    private static final String EMAIL_ID = "jogiyavishalvj@gmail.com";
    public static String Base64EncodedPublicKey = "YOUR BASE 64 KEY";

    private IabHelper mHelper;
    private IInAppBillingService mService;
    TextView purchaseTypeTextView, subscriptionDateTextView;
    TextView purchaseYearlyButton, purchaseMonthlyButton, purchaseHalfYearlyButton, purchaseQuerterlyButton, purchsaeWeeklyButton, purchaseConsumption;
    LinearLayout purchaseTypeLayout;
    private String fromActivity;

    private boolean hasYearlyPurchased = false, hasMonthlyPurchased = false, hasHalfYearlyPurchased = false,
            hasQuarterlyPurchased = false, hasConsumptionPurchased = false, hasWeeklyPurhcases = false;
    private Inventory inventory;
    private ProgressDialog progress;


    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };


    //Query Inventory
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure()) {
                // handle error here
                alert("Unknown Error.");
            } else {
                InAppPurchaseActivity.this.inventory = inventory;

                hasYearlyPurchased = inventory.hasPurchase(YEARLY_PRODUCT_ID);
                hasHalfYearlyPurchased = inventory.hasPurchase(HALF_YEARLY_PRODUCT_ID);
                hasQuarterlyPurchased = inventory.hasPurchase(QUARTERLY_PRODUCT_ID);
                hasMonthlyPurchased = inventory.hasPurchase(MONTHLY_PRODUCT_ID);
                hasWeeklyPurhcases = inventory.hasPurchase(WEEKLY_PRODUCT_ID);
                hasConsumptionPurchased = inventory.hasPurchase(CONSUMPTION_PRODUCT_ID);


                /*Use Commented Conditions only if you have 1 or 2 Subscription */
//                if (inventory.getSkuDetails(MONTHLY_PRODUCT_ID) == null
//                        || inventory.getSkuDetails(YEARLY_PRODUCT_ID) == null)
                querySkuDetails();//to fetch prices and currency symbols from playstore
//                else {
//                    purchaseMonthlyButton.setText(inventory.getSkuDetails(MONTHLY_PRODUCT_ID).getPrice());
//                    purchaseYearlyButton.setText(inventory.getSkuDetails(YEARLY_PRODUCT_ID).getPrice());
//                    Log.d(TAG, "onPurchase Result " + "Monthly  = " + inventory.getSkuDetails(MONTHLY_PRODUCT_ID).getPrice()
//                            + " Yearly = " + inventory.getSkuDetails(YEARLY_PRODUCT_ID).getPrice());
//
//                    if (progress != null)
//                        progress.cancel();
//                }


                // does the user have the premium upgrade?
                Log.d(TAG, "QueryInventoryFinishedListener onPurchase: " + YEARLY_PRODUCT_ID + " " + hasMonthlyPurchased
                        + "\n" + MONTHLY_PRODUCT_ID + " " + hasYearlyPurchased
                        + "\n" + HALF_YEARLY_PRODUCT_ID + " " + hasYearlyPurchased
                        + "\n" + QUARTERLY_PRODUCT_ID + " " + hasYearlyPurchased
                        + "\n" + WEEKLY_PRODUCT_ID + " " + hasYearlyPurchased
                        + "\n" + CONSUMPTION_PRODUCT_ID + " " + hasYearlyPurchased);

                logsTextView.setText("\n" + logsTextView.getText().toString() + "\n" + YEARLY_PRODUCT_ID + " " + hasMonthlyPurchased + "\n"
                        + "\n" + MONTHLY_PRODUCT_ID + " " + hasYearlyPurchased
                        + "\n" + HALF_YEARLY_PRODUCT_ID + " " + hasYearlyPurchased
                        + "\n" + QUARTERLY_PRODUCT_ID + " " + hasYearlyPurchased
                        + "\n" + WEEKLY_PRODUCT_ID + " " + hasYearlyPurchased
                        + "\n" + CONSUMPTION_PRODUCT_ID + " " + hasYearlyPurchased);

                updatePurchaseUi();

                /*You can get purzchase details and */
//              String applePrice =inventory.getSkuDetails(YEARLY_PRODUCT_ID).getPrice();
//              String bananaPrice =inventory.getPurchase().getPurchaseTime();
//              getSkuDetails(MONTHLY_PRODUCT_ID).getgetPrice();
            }
        }
    };

    private TextView logsTextView;
    private TextView queryPurchases;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_purchase);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.app_bar_in_app);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.purchase));


        ((TextView) findViewById(R.id.sku_details)).setOnClickListener(this);
        ((TextView) findViewById(R.id.query_purchases)).setOnClickListener(this);

        ((TextView) findViewById(R.id.faq)).setOnClickListener(this);
        ((TextView) findViewById(R.id.contact_support)).setOnClickListener(this);
        ((TextView) findViewById(R.id.help)).setOnClickListener(this);
        ((TextView) findViewById(R.id.clear_logs)).setOnClickListener(this);


        logsTextView = (TextView) findViewById(R.id.logs_text_view);

        purchaseTypeTextView = (TextView) findViewById(R.id.purchase_type_TV);
        subscriptionDateTextView = (TextView) findViewById(R.id.subscription_date_TV);

        purchaseYearlyButton = (TextView) findViewById(R.id.purchase_yearly);
        purchaseHalfYearlyButton = (TextView) findViewById(R.id.purchase_six_months);
        purchaseQuerterlyButton = (TextView) findViewById(R.id.purchase_quarterly);
        purchaseMonthlyButton = (TextView) findViewById(R.id.purchase_monthly);
        purchsaeWeeklyButton = (TextView) findViewById(R.id.purchase_weekly);
        purchaseConsumption = (TextView) findViewById(R.id.purchase_consumption);

        purchaseTypeLayout = (LinearLayout) findViewById(R.id.purchase_type_LL);
        queryPurchases = (TextView) findViewById(R.id.query_purchases2);


        purchaseYearlyButton.setOnClickListener(this);
        purchaseHalfYearlyButton.setOnClickListener(this);
        purchaseQuerterlyButton.setOnClickListener(this);
        purchaseMonthlyButton.setOnClickListener(this);
        purchsaeWeeklyButton.setOnClickListener(this);
        purchaseConsumption.setOnClickListener(this);
        queryPurchases.setOnClickListener(this);

        progress = new ProgressDialog(InAppPurchaseActivity.this);
        progress.setMessage(getString(R.string.processing));
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();


        mHelper = new IabHelper(InAppPurchaseActivity.this, Base64EncodedPublicKey);
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {

            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    try {
                        onClick(queryPurchases);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });


    }

    private void updatePurchaseUi() {


        if (inventory == null)
            return;

        /*You can customise this code as per your number of subscriptions */
        if (hasMonthlyPurchased || hasYearlyPurchased || hasHalfYearlyPurchased
                || hasQuarterlyPurchased || hasWeeklyPurhcases || hasConsumptionPurchased) {

            purchaseTypeLayout.setVisibility(View.VISIBLE);
            String purchaseType = getString(R.string.purchase_type);
            String subsDate = getString(R.string.subscription_date);

            if (hasMonthlyPurchased) {
                purchaseType += ": <font color='#1597FF'> " + getString(R.string.monthly) + " </font>";
                subsDate += ": <font color='#1597FF'> " + getFormatedDate(inventory.getPurchase(MONTHLY_PRODUCT_ID).getPurchaseTime()) + "</font>";
            }
            if (hasYearlyPurchased) {
                purchaseType += ": <font color='#1597FF'> " + getString(R.string.yearly) + " </font>";
                subsDate += ": <font color='#1597FF'> " + getFormatedDate(inventory.getPurchase(YEARLY_PRODUCT_ID).getPurchaseTime()) + "</font>";
            }
            if (hasWeeklyPurhcases) {
                purchaseType += ": <font color='#1597FF'> " + getString(R.string.weekly_purchase) + " </font>";
                subsDate += ": <font color='#1597FF'> " + getFormatedDate(inventory.getPurchase(WEEKLY_PRODUCT_ID).getPurchaseTime()) + "</font>";
            }
            if (hasQuarterlyPurchased) {
                purchaseType += ": <font color='#1597FF'> " + getString(R.string.quarterly_package) + " </font>";
                subsDate += ": <font color='#1597FF'> " + getFormatedDate(inventory.getPurchase(QUARTERLY_PRODUCT_ID).getPurchaseTime()) + "</font>";
            }
            if (hasHalfYearlyPurchased) {
                purchaseType += ": <font color='#1597FF'> " + getString(R.string.six_months_package) + " </font>";
                subsDate += ": <font color='#1597FF'> " + getFormatedDate(inventory.getPurchase(HALF_YEARLY_PRODUCT_ID).getPurchaseTime()) + "</font>";
            }
            if (hasConsumptionPurchased) {
                purchaseType += ": <font color='#1597FF'> " + getString(R.string.consumption) + " </font>";
                subsDate += ": <font color='#1597FF'> " + getFormatedDate(inventory.getPurchase(CONSUMPTION_PRODUCT_ID).getPurchaseTime()) + "</font>";
            }
            purchaseTypeTextView.setText(Html.fromHtml(purchaseType), TextView.BufferType.SPANNABLE);
            subscriptionDateTextView.setText(Html.fromHtml(subsDate), TextView.BufferType.SPANNABLE);

        } else
            purchaseTypeLayout.setVisibility(View.GONE);


        if (progress != null)
            progress.cancel();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.faq:
                String url = "https://play.google.com/store/apps/details?id=vjs.inapppurchasessample";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;

            case R.id.contact_support:
                contactSupport();
                break;

            case R.id.help:
                url = "http://vjscrazzy.blogspot.in/";
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;


            case R.id.sku_details:
                querySkuDetails();
                break;

            case R.id.purchase_weekly:
                Bundle buyIntentBundle = null;
                try {
                    buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                            WEEKLY_PRODUCT_ID, "subs", "Response back to me");
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    assert pendingIntent != null;
                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                            1001, new Intent(), 0, 0, 0);
                } catch (RemoteException | IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
                break;


            case R.id.purchase_monthly:

                buyIntentBundle = null;
                try {
                    buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                            MONTHLY_PRODUCT_ID, "subs", "Response back to me");
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    assert pendingIntent != null;
                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                            1002, new Intent(), 0, 0, 0);
                } catch (RemoteException | IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.purchase_quarterly:

                buyIntentBundle = null;
                try {
                    buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                            QUARTERLY_PRODUCT_ID, "subs", "Response back to me");
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    assert pendingIntent != null;
                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                            1003, new Intent(), 0, 0, 0);
                } catch (RemoteException | IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.purchase_six_months:
                buyIntentBundle = null;
                try {
                    buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                            HALF_YEARLY_PRODUCT_ID, "subs", "Response back to me");
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    assert pendingIntent != null;
                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                            1004, new Intent(), 0, 0, 0);
                } catch (RemoteException | IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.purchase_yearly:
                buyIntentBundle = null;
                try {
                    buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                            "com.simpletime.yearly", "subs", "Response back to me");
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    assert pendingIntent != null;
                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                            1005, new Intent(), 0, 0, 0);

                } catch (RemoteException | IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
                break;


            case R.id.purchase_consumption:
                buyIntentBundle = null;
                try {
                    buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                            CONSUMPTION_PRODUCT_ID, "inapp", "Response back to me");
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    assert pendingIntent != null;
                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                            1006, new Intent(), 0, 0, 0);
                } catch (RemoteException | IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
                break;


         /*   case R.id.query_purchases:
                QueryPurchases2();
                break;*/
            case R.id.query_purchases2:
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.clear_logs:
                logsTextView.setText("");

        }
    }


    private void contactSupport() {

        String subject = getString(R.string.contact_supoort_subject);
        String text = "";

        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL_ID});
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            shareIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);    //"Invoice Send to you"
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setType("text/plain");
        // Start chooser activity
        startActivity(Intent.createChooser(shareIntent, getString(R.string.select)));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001 || requestCode == 1002) {

            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");


            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    long purchaseTime = jo.getLong("purchaseTime");

                    Log.d(TAG, "onPurchase onActivityResult: " + jo.toString());
                    logsTextView.setText("\n" + logsTextView.getText().toString() + "\n" + "onPurchase onActivityResult: " + jo.toString());


                    purchaseTypeLayout.setVisibility(View.VISIBLE);
                    String purchaseType = getString(R.string.purchase_type);
                    String subsDate = getString(R.string.subscription_date);

                    purchaseTypeTextView.setText(Html.fromHtml(purchaseType), TextView.BufferType.SPANNABLE);
                    subscriptionDateTextView.setText(Html.fromHtml(subsDate), TextView.BufferType.SPANNABLE);

                    if (sku.equals(MONTHLY_PRODUCT_ID)) {
                        hasMonthlyPurchased = true;
                        purchaseType += ": <font color='#1597FF'> " + getString(R.string.monthly) + " </font>";


                        alert(getString(R.string.purchase_type) + ": " +
                                getString(R.string.monthly) + "\n" +
                                getString(R.string.price) + ": " + purchaseMonthlyButton.getText().toString() + "\n" +
                                getString(R.string.thank_you_meassge));
                    }


                    if (sku.equals(YEARLY_PRODUCT_ID)) {
                        purchaseType += ": <font color='#1597FF'> " + getString(R.string.yearly) + " </font>";
                        alert(getString(R.string.purchase_type) + ": " +
                                getString(R.string.yearly) + "\n" +
                                getString(R.string.price) + ": " + purchaseYearlyButton.getText().toString() + "\n" +
                                getString(R.string.thank_you_meassge));
                    }


                    subsDate += ": <font color='#1597FF'> " + getFormatedDate(purchaseTime) + "</font>";
                    purchaseTypeTextView.setText(Html.fromHtml(purchaseType), TextView.BufferType.SPANNABLE);
                    subscriptionDateTextView.setText(Html.fromHtml(subsDate), TextView.BufferType.SPANNABLE);

                } catch (JSONException e) {
                    Log.e(TAG, "onActivityResult: " + e.getMessage());
                }
            } else {
                alert(getString(R.string.failed_to_purchase));
            }
        }
    }


    private void querySkuDetails() {
        Needle.onBackgroundThread().execute(new UiRelatedProgressTask<String[], Integer>() {
            @Override
            protected String[] doWork() {

                String[] pricingArry = new String[6];
                ArrayList<String> skuList = new ArrayList<>();

                skuList.add(YEARLY_PRODUCT_ID);
                skuList.add(HALF_YEARLY_PRODUCT_ID);
                skuList.add(QUARTERLY_PRODUCT_ID);
                skuList.add(MONTHLY_PRODUCT_ID);
                skuList.add(WEEKLY_PRODUCT_ID);

                skuList.add(CONSUMPTION_PRODUCT_ID);

                final Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

                Bundle skuSubscriptionDetails = null;
                Bundle skuConsumptionDetails = null;

                try {
                    skuSubscriptionDetails = mService.getSkuDetails(3, getPackageName(), "subs", querySkus);
                    skuConsumptionDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if (skuSubscriptionDetails != null) {

                    int response = skuSubscriptionDetails.getInt("RESPONSE_CODE");
                    if (response == 0) {
                        ArrayList<String> responseList = skuSubscriptionDetails.getStringArrayList("DETAILS_LIST");

                        assert responseList != null;
                        for (String thisResponse : responseList) {
                            JSONObject object = null;
                            try {
                                object = new JSONObject(thisResponse);

                                String sku = object.getString("productId");
                                String price = object.getString("price");//price including the currency sign

                                /*I think this is bad logic please if you can refine this comment or correct.*/
                                if (sku.equals(YEARLY_PRODUCT_ID))
                                    pricingArry[0] = price;
                                else {
                                    if (sku.equals(HALF_YEARLY_PRODUCT_ID))
                                        pricingArry[1] = price;
                                    else {
                                        if (sku.equals(QUARTERLY_PRODUCT_ID))
                                            pricingArry[2] = price;
                                        else {
                                            if (sku.equals(MONTHLY_PRODUCT_ID))
                                                pricingArry[3] = price;
                                            else if (sku.equals(WEEKLY_PRODUCT_ID))
                                                pricingArry[4] = price;
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }


                /*For In-app Consumptions */
                if (skuConsumptionDetails != null) {

                    int response = skuConsumptionDetails.getInt("RESPONSE_CODE");
                    if (response == 0) {
                        ArrayList<String> responseList = skuConsumptionDetails.getStringArrayList("DETAILS_LIST");

                        assert responseList != null;
                        for (String thisResponse : responseList) {
                            JSONObject object = null;
                            try {
                                object = new JSONObject(thisResponse);

                                String sku = object.getString("productId");
                                String price = object.getString("price");//price including the currency sign

                                if (sku.equals(CONSUMPTION_PRODUCT_ID))
                                    pricingArry[5] = price;

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                return pricingArry;
//                        return monthlyPurchase + yearlyPurchase;
            }

            @Override
            protected void thenDoUiRelatedWork(String[] result) {

                purchaseYearlyButton.setText(result[0]);
                purchaseHalfYearlyButton.setText(result[1]);
                purchaseQuerterlyButton.setText(result[2]);
                purchaseMonthlyButton.setText(result[3]);
                purchsaeWeeklyButton.setText(result[4]);
                purchaseConsumption.setText(result[5]);

                Log.d(TAG, "thenDoUiRelatedWork: Result " + "\n" +
                        "Yearly  = " + result[0] + "\n" +
                        " 6 Months  = " + result[1] + "\n" +
                        " 3 Months  = " + result[2] + "\n" +
                        " Monthly  = " + result[3] + "\n" +
                        " weekly   = " + result[4] + "\n" +
                        " Consumption   = " + result[5] + "\n");

                logsTextView.setText("\n" + logsTextView.getText().toString() + "\n" + " Result " +
                        "Yearly  = " + result[0] +
                        " 6 Months  = " + result[1] + "\n" +
                        " 3 Months  = " + result[2] + "\n" +
                        " Monthly  = " + result[3] + "\n" +
                        " weekly   = " + result[4] + "\n" +
                        " Consumption   = " + result[5] + "\n");
                if (progress != null)
                    progress.cancel();

            }

            @Override
            protected void onProgressUpdate(Integer progress) {

            }
        });

    }


    private void QueryPurchases2() {
        try {
            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "subs", null);
            ArrayList<String> ownedSkus = null;

            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ownedSkus =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String> purchaseDataList =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");//detailed info about purchase
                ArrayList<String> signatureList =
                        ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                String continuationToken =
                        ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                StringBuilder result = new StringBuilder();
                assert purchaseDataList != null;
                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
//                    assert signatureList != null;
//                    String signature = signatureList.get(i);
                    assert ownedSkus != null;
                    String sku = ownedSkus.get(i);

                    result.append(sku).append(" = ").append(purchaseData).append("\n");
                }

                assert ownedSkus != null;
                Log.d(TAG, "onPurchase: " + purchaseDataList.size() + "\n" + result);
                // if continuationToken != null, call getPurchases again
                // and pass in the token to retrieve more items
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public static String getFormatedDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }


    private void alert(String s) {
        Log.d(TAG, "alert: " + s);
        logsTextView.setText("\n" + logsTextView.getText().toString() + "\n" + "alert: " + s);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(s)
                .setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(this, MainActivity.class);
        if (fromActivity != null) {
            if (fromActivity.equals(MainActivity.TAG)) {
                homeIntent = new Intent(this, MainActivity.class);
                setResult(RESULT_OK);
            }
        }
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        this.finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                this.finish();
                break;

            //    case R.id.clients_menu:
            //        startActivity(new Intent(Clients.this, AddNewClient.class));
            //       break;
        }
        return (super.onOptionsItemSelected(menuItem));
    }


}
