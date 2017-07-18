package vjs.inapppurchasessample;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.vending.billing.IInAppBillingService;

import needle.Needle;
import vjs.inapppurchasessample.billing_utils.IabHelper;
import vjs.inapppurchasessample.billing_utils.IabResult;
import vjs.inapppurchasessample.billing_utils.Inventory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String WEEKLY_PRODUCT_ID = "com.vj.weekly";
    public static final String MONTHLY_PRODUCT_ID = "com.vj.monthly";
    public static final String QUARTERLY_PRODUCT_ID = "com.vj.quarterly";
    public static final String YEARLY_PRODUCT_ID = "com.vj.yearly";
    public static final String HALF_YEARLY_PRODUCT_ID = "com.vj.halfyearly";
    public static final String CONSUMPTION_PRODUCT_ID = "com.vj.consumption2";


    IInAppBillingService mService;
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
    private IabHelper mHelper;

    private boolean hasYearlyPurchased = false, hasMonthlyPurchased = false;
    private Inventory inventory;
    private ProgressDialog progress;
    String base64EncodedPublicKey = "";



    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Button) findViewById(R.id.purchase_activity)).setOnClickListener(this);
    }


    private void queryPurchase() {
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                //To check In App purchases
                mHelper = new IabHelper(MainActivity.this, base64EncodedPublicKey);
                Intent serviceIntent =
                        new Intent("com.android.vending.billing.InAppBillingService.BIND");
                serviceIntent.setPackage("com.android.vending");
                bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
                mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    public void onIabSetupFinished(IabResult result) {

                        if (result.isSuccess()) {
                            try {
                                Inventory inventory = mHelper.queryInventory();
                                hasYearlyPurchased = inventory.hasPurchase(YEARLY_PRODUCT_ID);
                                hasMonthlyPurchased = inventory.hasPurchase(MONTHLY_PRODUCT_ID);

                                Log.d(TAG, "onPurchase: Yearly " + hasYearlyPurchased + " Monthly " + hasMonthlyPurchased);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryPurchase();

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
            case R.id.purchase_activity:
                startActivity(new Intent(MainActivity.this, InAppPurchaseActivity.class));
                this.finish();

        }

    }

}
