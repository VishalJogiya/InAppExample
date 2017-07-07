package vjs.inapppurchasessample;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import needle.Needle;
import needle.UiRelatedProgressTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    IInAppBillingService mService;
    public static final String WEEKLY_PRODUCT_ID = "com.vj.weekly";
    public static final String MONTHLY_PRODUCT_ID = "com.vj.monthly";
    public static final String YEARLY_PRODUCT_ID = "com.vj.yearly";
    public static final String QUARTERLY_PRODUCT_ID = "com.vj.quarterly";

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        ((Button) findViewById(R.id.hello_purchase)).setOnClickListener(this);
        ((Button) findViewById(R.id.purchase_monthly)).setOnClickListener(this);
        ((Button) findViewById(R.id.purchase_yearly)).setOnClickListener(this);
        ((Button) findViewById(R.id.query_purchases)).setOnClickListener(this);

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

            case R.id.hello_purchase:
                Needle.onBackgroundThread().execute(new UiRelatedProgressTask<String, Integer>() {
                    @Override
                    protected String doWork() {
                        String yearlyPurchase = "";
                        String monthlyPurchase = "";
                        String weeklyPurchase = "";


                        ArrayList<String> skuList = new ArrayList<>();

                        skuList.add(WEEKLY_PRODUCT_ID);
                        skuList.add(MONTHLY_PRODUCT_ID);
                        skuList.add(YEARLY_PRODUCT_ID);

                        final Bundle querySkus = new Bundle();
                        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

                        Bundle skuDetails = null;
                        try {
                            skuDetails = mService.getSkuDetails(3, getPackageName(), "subs", querySkus);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                        if (skuDetails != null) {

                            int response = skuDetails.getInt("RESPONSE_CODE");
                            if (response == 0) {
                                ArrayList<String> responseList
                                        = skuDetails.getStringArrayList("DETAILS_LIST");

                                assert responseList != null;
                                for (String thisResponse : responseList) {
                                    JSONObject object = null;
                                    try {
                                        object = new JSONObject(thisResponse);

                                        String sku = object.getString("productId");
                                        String price = object.getString("price");

                                        if (sku.equals(MONTHLY_PRODUCT_ID))
                                            monthlyPurchase = price;
                                        else if (sku.equals(YEARLY_PRODUCT_ID))
                                            yearlyPurchase = price;
                                        else if (sku.equals(WEEKLY_PRODUCT_ID))
                                            weeklyPurchase = price;


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        return weeklyPurchase + monthlyPurchase + yearlyPurchase;
                    }

                    @Override
                    protected void thenDoUiRelatedWork(String result) {
//                        Toast.makeText(MainActivity.this, "Result: " + result, Toast.LENGTH_SHORT).show();

                        alert(result);
                        Log.d(TAG, "thenDoUiRelatedWork: Result " + result);
                    }

                    @Override
                    protected void onProgressUpdate(Integer progress) {
//                        Toast.makeText(MainActivity.this, "progress " + progress, Toast.LENGTH_SHORT).show();
                    }
                });

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
                            1001, new Intent(), 0, 0, 0);
                } catch (RemoteException | IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.purchase_yearly:

                buyIntentBundle = null;
                try {
                    buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                            YEARLY_PRODUCT_ID, "subs", "Response back to me");
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    assert pendingIntent != null;
                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                            1001, new Intent(), 0, 0, 0);

                } catch (RemoteException | IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.query_purchases:
                try {
                    Bundle ownedItems = mService.getPurchases(3, getPackageName(), "subs", null);

                    int response = ownedItems.getInt("RESPONSE_CODE");
                    if (response == 0) {
                        ArrayList<String> ownedSkus =
                                ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                        ArrayList<String> purchaseDataList =
                                ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        ArrayList<String> signatureList =
                                ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                        String continuationToken =
                                ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                        StringBuilder result = new StringBuilder();

                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            String purchaseData = purchaseDataList.get(i);
                            String signature = signatureList.get(i);
                            String sku = ownedSkus.get(i);

                            result.append(sku).append(" = ").append(purchaseData).append("\n");
                            // do something with this purchase information
                            // e.g. display the updated list of products owned by user
                        }
                        alert(result.toString());
                        // if continuationToken != null, call getPurchases again
                        // and pass in the token to retrieve more items
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    alert("You have bought the " + sku + ". Excellent choice,adventurer!");
                } catch (JSONException e) {
                    alert("Failed to parse purchase data.");
                    e.printStackTrace();
                }
            }
        }
    }

    private void alert(String s) {
        Log.e(TAG, "alert: " + s);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(s)
                .setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        dialog.dismiss();
                    }
                }).show();


    }
}
