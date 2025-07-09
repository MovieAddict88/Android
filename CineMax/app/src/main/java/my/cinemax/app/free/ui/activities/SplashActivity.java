package my.cinemax.app.free.ui.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.congle7997.google_iap.BillingSubs;
import com.congle7997.google_iap.CallBackCheck;
import com.greenfrvr.rubberloader.RubberLoaderView;
import my.cinemax.app.free.Provider.PrefManager;
import my.cinemax.app.free.R;
import my.cinemax.app.free.api.apiClient;
import my.cinemax.app.free.api.apiRest;
import my.cinemax.app.free.config.Global;
import my.cinemax.app.free.entity.ApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import my.cinemax.app.free.*;

public class SplashActivity extends AppCompatActivity {

    private PrefManager prf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        check();
        prf= new PrefManager(getApplicationContext());
        ( (RubberLoaderView) findViewById(R.id.loader1)).startLoading();
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // If you want to modify a view in your Activity
                SplashActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        checkAccount();


                    }
                });
            }
        }, 3000);

        prf.setString("ADMIN_REWARDED_ADMOB_ID","");

        prf.setString("ADMIN_INTERSTITIAL_ADMOB_ID","");
        prf.setString("ADMIN_INTERSTITIAL_FACEBOOK_ID","");
        prf.setString("ADMIN_INTERSTITIAL_TYPE","FALSE");
        prf.setInt("ADMIN_INTERSTITIAL_CLICKS",3);

        prf.setString("ADMIN_BANNER_ADMOB_ID","");
        prf.setString("ADMIN_BANNER_FACEBOOK_ID","");
        prf.setString("ADMIN_BANNER_TYPE","FALSE");

        prf.setString("ADMIN_NATIVE_FACEBOOK_ID","");
        prf.setString("ADMIN_NATIVE_ADMOB_ID","");
        prf.setString("ADMIN_NATIVE_LINES","6");
        prf.setString("ADMIN_NATIVE_TYPE","FALSE");
        // APP_LOGIN_REQUIRED is no longer used, so remove setting it.
        // prf.setString("APP_LOGIN_REQUIRED","FALSE");

        // Payment related preferences are no longer used.
        // prf.setString("APP_STRIPE_ENABLED","FALSE");
        // prf.setString("APP_PAYPAL_ENABLED","FALSE");
        // prf.setString("APP_PAYPAL_CLIENT_ID","");
        // prf.setString("APP_CASH_ENABLED","FALSE");
    }

    // public void check(){ // Billing/Subscription check is removed
    //     List<String> listSkuStoreSubs = new ArrayList<>();
    //     listSkuStoreSubs.add(Global.SUBSCRIPTION_ID);
    //     new BillingSubs(SplashActivity.this, listSkuStoreSubs, new CallBackCheck() {
    //         @Override
    //         public void onPurchase() {
    //             PrefManager prefManager= new PrefManager(getApplicationContext());
    //             prefManager.setString("SUBSCRIBED","TRUE");
    //         }
    //
    //         @Override
    //         public void onNotPurchase() {
    //             PrefManager prefManager= new PrefManager(getApplicationContext());
    //             prefManager.setString("SUBSCRIBED","FALSE");
    //         }
    //     });
    // }
    private void checkAccount() {

        Integer version = -1;
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (version!=-1){
            Integer id_user = 0; // Default to 0 or an anonymous user ID if your API supports it
            // if (prf.getString("LOGGED").toString().equals("TRUE")) { // LOGGED check removed
            //      id_user = Integer.parseInt(prf.getString("ID_USER")); // ID_USER will be removed
            // }
            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            Call<ApiResponse> call = service.check(version,id_user); // Pass 0 or anonymous ID
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getValues() != null){
                        for (int i = 0; i < response.body().getValues().size(); i++) {
                            // Keep admin settings
                            if ( response.body().getValues().get(i).getName().equals("ADMIN_REWARDED_ADMOB_ID") ){
                                if (response.body().getValues().get(i).getValue()!=null)
                                    prf.setString("ADMIN_REWARDED_ADMOB_ID",response.body().getValues().get(i).getValue());
                            }
                            if ( response.body().getValues().get(i).getName().equals("ADMIN_INTERSTITIAL_ADMOB_ID") ){
                                if (response.body().getValues().get(i).getValue()!=null)
                                    prf.setString("ADMIN_INTERSTITIAL_ADMOB_ID",response.body().getValues().get(i).getValue());
                            }
                            if ( response.body().getValues().get(i).getName().equals("ADMIN_INTERSTITIAL_FACEBOOK_ID") ){
                                if (response.body().getValues().get(i).getValue()!=null)
                                    prf.setString("ADMIN_INTERSTITIAL_FACEBOOK_ID",response.body().getValues().get(i).getValue());
                            }
                            if ( response.body().getValues().get(i).getName().equals("ADMIN_INTERSTITIAL_TYPE") ){
                                if (response.body().getValues().get(i).getValue()!=null)
                                    prf.setString("ADMIN_INTERSTITIAL_TYPE",response.body().getValues().get(i).getValue());
                            }
                            if ( response.body().getValues().get(i).getName().equals("ADMIN_INTERSTITIAL_CLICKS") ){
                                if (response.body().getValues().get(i).getValue()!=null)
                                    prf.setInt("ADMIN_INTERSTITIAL_CLICKS",Integer.parseInt(response.body().getValues().get(i).getValue()));
                            }
                            if ( response.body().getValues().get(i).getName().equals("ADMIN_BANNER_ADMOB_ID") ){
                                if (response.body().getValues().get(i).getValue()!=null)
                                    prf.setString("ADMIN_BANNER_ADMOB_ID",response.body().getValues().get(i).getValue());
                            }
                            if ( response.body().getValues().get(i).getName().equals("ADMIN_BANNER_FACEBOOK_ID") ){
                                if (response.body().getValues().get(i).getValue()!=null)
                                    prf.setString("ADMIN_BANNER_FACEBOOK_ID",response.body().getValues().get(i).getValue());
                            }
                            if ( response.body().getValues().get(i).getName().equals("ADMIN_BANNER_TYPE") ){
                                if (response.body().getValues().get(i).getValue()!=null)
                                    prf.setString("ADMIN_BANNER_TYPE",response.body().getValues().get(i).getValue());
                            }
                            if ( response.body().getValues().get(i).getName().equals("ADMIN_NATIVE_FACEBOOK_ID") ){
                                if (response.body().getValues().get(i).getValue()!=null)
                                    prf.setString("ADMIN_NATIVE_FACEBOOK_ID",response.body().getValues().get(i).getValue());
                            }
                            if ( response.body().getValues().get(i).getName().equals("ADMIN_NATIVE_ADMOB_ID") ){
                                if (response.body().getValues().get(i).getValue()!=null)
                                    prf.setString("ADMIN_NATIVE_ADMOB_ID",response.body().getValues().get(i).getValue());
                            }
                            if ( response.body().getValues().get(i).getName().equals("ADMIN_NATIVE_LINES") ){
                                if (response.body().getValues().get(i).getValue()!=null)
                                    prf.setString("ADMIN_NATIVE_LINES",response.body().getValues().get(i).getValue());
                            }
                            if ( response.body().getValues().get(i).getName().equals("ADMIN_NATIVE_TYPE") ){
                                if (response.body().getValues().get(i).getValue()!=null)
                                    prf.setString("ADMIN_NATIVE_TYPE",response.body().getValues().get(i).getValue());
                            }
                            // Remove payment/subscription related settings
                            // if ( response.body().getValues().get(i).getName().equals("APP_CURRENCY") ){...}
                            // if ( response.body().getValues().get(i).getName().equals("APP_CASH_ACCOUNT") ){...}
                            // if ( response.body().getValues().get(i).getName().equals("APP_STRIPE_PUBLIC_KEY") ){...}
                            // if ( response.body().getValues().get(i).getName().equals("APP_CASH_ENABLED") ){...}
                            // if ( response.body().getValues().get(i).getName().equals("APP_PAYPAL_ENABLED") ){...}
                            // if ( response.body().getValues().get(i).getName().equals("APP_PAYPAL_CLIENT_ID") ){...}
                            // if ( response.body().getValues().get(i).getName().equals("APP_STRIPE_ENABLED") ){...}

                            // APP_LOGIN_REQUIRED will be effectively false
                            // if ( response.body().getValues().get(i).getName().equals("APP_LOGIN_REQUIRED") ){...}

                            // NEW_SUBSCRIBE_ENABLED will be effectively false
                            // if ( response.body().getValues().get(i).getName().equals("subscription") ){...}
                        }

                        // Remove account disabled check (403) as it's tied to user login
                        // if (response.body().getValues().get(1).getValue().equals("403")){
                        //     prf.remove("ID_USER"); ... // All this user data removal is handled later
                        //     Toasty.error(getApplicationContext(),getResources().getString(R.string.account_disabled), Toast.LENGTH_SHORT, true).show();
                        // }

                        // App update check (202) can remain if it's a general app update mechanism
                        if (response.body().getCode().equals(200)) {
                            redirect();
                        } else if (response.body().getCode().equals(202)) {
                            String title_update=response.body().getValues().get(0).getValue();
                            String featurs_update=response.body().getMessage();
                            View v = (View)  getLayoutInflater().inflate(R.layout.update_message,null);
                            TextView update_text_view_title=(TextView) v.findViewById(R.id.update_text_view_title);
                            TextView update_text_view_updates=(TextView) v.findViewById(R.id.update_text_view_updates);
                            update_text_view_title.setText(title_update);
                            update_text_view_updates.setText(featurs_update);
                            AlertDialog.Builder builder;
                            builder = new AlertDialog.Builder(SplashActivity.this);
                            builder.setTitle("New Update")
                                    //.setMessage(response.body().getValue())
                                    .setView(v)
                                    .setPositiveButton(getResources().getString(R.string.update_now), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            final String appPackageName=getApplication().getPackageName();
											
										startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MyApi.API_URL)));
                                            finish();
                                        }
                                    })
								.setNegativeButton(getResources().getString(R.string.open_web), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //redirect();
										startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Global.API_URL)));
                                        }
                                    })
                                    .setCancelable(false)
                                    .setIcon(R.drawable.ic_update)
                                    .show();
                        } else {
                            redirect();
                        }
                    }else {
                        redirect();
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {

                }
            });
        }else{
            redirect();
        }

    }



    public void redirect(){
        // LOGIN_REQUIRED and LOGGED checks are removed.
        // The app will always behave as if login is not required and the user is effectively anonymous.
        if (!prf.getBoolean("first", false)) { // Use getBoolean with a default for "first"
            Intent intent = new Intent(SplashActivity.this,IntroActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
            finish();
            prf.setBoolean("first",true); // Set it as boolean
        }else{
            Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
            finish();
        }
    }



}
