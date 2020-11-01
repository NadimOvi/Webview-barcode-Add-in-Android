package com.easyregs.tutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;

import static com.google.zxing.integration.android.IntentIntegrator.parseActivityResult;

public class MainActivity extends AppCompatActivity {

    String phoneNumber,androidId,address1,lat,lon;
    LocationManager locationManager;
    private final int FINE_LOCATION_PERMISSION = 9999;
    Double postlatitude = 0.00;
    Double postlongitude = 0.00;
    String locationname = "";
    ProgressDialog progressDialog;
    private String codeContent;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    SwipeRefreshLayout refreshLayout;

    /*public static final String MAIN_SITE_URL = "https://www.pqstec.com/EasyReg/",*/  //Main site
    public static final String MAIN_SITE_URL = "https://www.pqstec.com/EasyReg/",  //Testing Site

    /*STRING_TO_MATCH_FOR_BARCODE_SCAN = "https://www.pqstec.com/EasyReg/CustomerSerial/Create?BarcodeScan=1",*/ //Main site
            STRING_TO_MATCH_FOR_BARCODE_SCAN = "https://www.pqstec.com/EasyReg/CustomerSerial/Create?BarcodeScan=1",  //Testing Site
            DEALER_BARCODE_SCAN = "https://www.pqstec.com/EasyReg/DealerProductSerial/Create?DealerProductSerialNo=1",  //Testing Site

            Inventory_BARCODE_SCAN = "https://www.pqstec.com/EasyReg/ProductInventorySerial/Create?ProductInventorySerialNo=1",  //Testing Site

    /*Login_URL = "https://www.pqstec.com/EasyReg/Account/LoginBangla?dealerpoint=Walton",*/ //Main Site
    Login_URL = "https://www.pqstec.com/EasyReg/Account/LoginBangla?dealerpoint=Walton",  //Testing

   /* Login_Post_URL = "https://www.pqstec.com/EasyReg/CustomerSerial/Create", */ //Main Site
            Login_Post_URL = "https://www.pqstec.com/EasyReg/CustomerSerial/Create", // Testing

            /*POST_URL = "https://www.pqstec.com/EasyReg/CustomerSerial/Create";*/ //Main site
            POST_URL = "https://www.pqstec.com/EasyReg/CustomerSerial/Create", //Testing

    dealerProductSerial= "https://www.pqstec.com/EasyReg/DealerProductSerial/Create",
    inventoryProductSerial= "https://www.pqstec.com/EasyReg/ProductInventorySerial/Create";



    private WebView main_web_view;
    private Url url;
    boolean isHomePage = false;
    @SuppressLint("SetJavaScriptEnabled")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*refreshLayout = findViewById(R.id.swipe);*/
        phoneNumber = getIntent().getStringExtra("PhoneNumber");
        androidId = getIntent().getStringExtra("AndroidId");

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("অপেক্ষা করুন...");
        progressDialog.setCancelable(false);

        /*ActionBar action_bar = getSupportActionBar();
        action_bar.hide();*/

        // load site URL
        openUrl(MAIN_SITE_URL);


        HashMap<String, Object> defaultsRate = new HashMap<>();
        defaultsRate.put("new_version_code", String.valueOf(getVersionCode()));

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10) // change to 3600 on published app
                .build();

        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(defaultsRate);

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    final String new_version_code = mFirebaseRemoteConfig.getString("new_version_code");

                    if(Integer.parseInt(new_version_code) > getVersionCode())
                        showTheDialog("com.easyregs.tutorial", new_version_code );
                }
                else Log.e("MYLOG", "mFirebaseRemoteConfig.fetchAndActivate() NOT Successful");

            }
        });

        /*refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                main_web_view.reload();
                refreshLayout.setRefreshing(false);
            }
        });*/

    }

    private void showTheDialog(final String appPackageName, String versionFromRemoteConfig){
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("আপডেট!")
                .setMessage("নতুন আপডেট ভার্সনটি ইনস্টল করুন ।")
                .setPositiveButton("আপডেট করুন", null)
                .setNegativeButton("না", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .show();

        dialog.setCancelable(false);

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + appPackageName)));
                }
                catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
    }

    private PackageInfo pInfo;
    public int getVersionCode() {
        pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i("MYLOG", "NameNotFoundException: "+e.getMessage());
        }
        return pInfo.versionCode;
    }

    public void openUrl(String url){
        main_web_view = findViewById(R.id.mainWebView);
        //enable javascript
        main_web_view.getSettings().setJavaScriptEnabled(true);
        main_web_view.getSettings().setDisplayZoomControls(false);
        main_web_view.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        main_web_view.getSettings().setAppCacheEnabled(true);
        main_web_view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        main_web_view.setScrollbarFadingEnabled(true);



        main_web_view.getSettings().setLoadWithOverviewMode(true);
        main_web_view.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        main_web_view.getSettings().setSaveFormData(true);
        main_web_view.getSettings().setEnableSmoothTransition(true);

        main_web_view.getSettings().setDatabaseEnabled(true);
        main_web_view.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT );
        main_web_view.getSettings().setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );

        main_web_view.getSettings().setAllowFileAccess( true );
        main_web_view.getSettings().setDomStorageEnabled(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            main_web_view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            main_web_view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        // get the activity context
        final Activity activity = this;

        //set client to handle errors and intercept link clicks
        main_web_view.setWebViewClient(new WebViewClient(){

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                String msg = "error : "+description+" Request URL : "+failingUrl;
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // we will interrupt the link here
                  if(isURLMatching(url)) {
                        secondScanPage();
                        return true;

                    }else if(isSecondURLMatching(url)){
                      scanNow();
                      return true;
                  }else if(isThirdURLMatching(url)){
                      thirdScanPage();
                      return true;
                  }
                  else if (loginURLMatching(url)){
                    try {
                        currentPlaceLocation();
                    }catch (Exception e){
                        Log.e("Fail 2", e.toString());
                        //At the level Exception Class handle the error in Exception Table
                        // Exception Create That Error  Object and throw it
                        //E.g: FileNotFoundException ,etc
                        e.printStackTrace();
                    }


                }else if (loginMainURLMatching(url)){


                     /*Toast.makeText(MainActivity.this,"PhoneNumber"+phoneNumber+
                             "\nAndroidID"+androidId+"\naddress"+address1+
                             "\npostlatitude"+postlatitude+"\npostlongitude"+postlongitude,Toast.LENGTH_LONG).show();*/
                      postURL();
                }

                return super.shouldOverrideUrlLoading(view,url);
            }


        });

        //load the URL
        main_web_view.loadUrl(url);
    }




    protected boolean loginURLMatching(String url){
        return url.toLowerCase().contains(Login_URL.toLowerCase());
    }
    protected boolean loginMainURLMatching(String url){
        return url.toLowerCase().contains(Login_Post_URL.toLowerCase());
    }
    protected boolean isURLMatching(String url){
        return url.toLowerCase().contains(STRING_TO_MATCH_FOR_BARCODE_SCAN.toLowerCase());
    }
    protected boolean isSecondURLMatching(String url){
        return url.toLowerCase().contains(DEALER_BARCODE_SCAN.toLowerCase());
    }
    protected boolean isThirdURLMatching(String url){
        return url.toLowerCase().contains(Inventory_BARCODE_SCAN.toLowerCase());
    }

    /**
     * Initiate the barcode scan
     */
    public void scanNow(){
        /*IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt(String.valueOf("Scan Barcode"));
          // Wide scanning rectangle, may work better for 1D barcodes
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.initiateScan();
        integrator.setOrientationLocked(false);*/

        IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
        scanIntegrator.setPrompt("স্ক্যান করার জন্য বারকোডটি চতুষ্কোণ ঘরের ভিতরে রাখুন।");
        scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        scanIntegrator.setOrientationLocked(false);
        startActivityForResult(scanIntegrator.createScanIntent(), 10);

    }
    public void secondScanPage(){
        /*IntentIntegrator secondIntegrator = new IntentIntegrator(this);
        secondIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        secondIntegrator.setPrompt(String.valueOf("Scan Barcode"));
        // Wide scanning rectangle, may work better for 1D barcodes
        secondIntegrator.setCameraId(0);  // Use a specific camera of the device
        secondIntegrator.initiateScan();
        secondIntegrator.setOrientationLocked(false);*/
        IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
        scanIntegrator.setPrompt("স্ক্যান করার জন্য বারকোডটি চতুষ্কোণ ঘরের ভিতরে রাখুন।");
        scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        scanIntegrator.setOrientationLocked(false);
        startActivityForResult(scanIntegrator.createScanIntent(), 20);


    }
    public void thirdScanPage(){

        IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
        scanIntegrator.setPrompt("স্ক্যান করার জন্য বারকোডটি চতুষ্কোণ ঘরের ভিতরে রাখুন।");
        scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        scanIntegrator.setOrientationLocked(false);
        startActivityForResult(scanIntegrator.createScanIntent(), 30);

    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //retrieve scan result

             /*   final IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                if (scanningResult != null) {
                    codeContent = scanningResult.getContents();
                    String url = new String(POST_URL);
                    String secondUrl = new String(dealerProductSerial);
                    String webUrl = new String(main_web_view.getUrl());
                    if(secondUrl.equals(webUrl)){
                        openUrl(dealerProductSerial + "?DealerProductSerialNo=" + codeContent);

                    }
                    *//*checkPage();*//*

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "No scan data received!", Toast.LENGTH_SHORT);
                    toast.show();
                }*/

        if (requestCode == 10) {
            if (resultCode == RESULT_OK) {
                String scanContent = intent.getStringExtra("SCAN_RESULT");
                openUrl(dealerProductSerial + "?DealerProductSerialNo=" + scanContent);

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "বাতিল হয়েছে", Toast.LENGTH_SHORT).show();
            }

        }
        else if (requestCode == 20) {
            if (resultCode == RESULT_OK) {
                String scanContent = intent.getStringExtra("SCAN_RESULT");

                openUrl(POST_URL + "?CustomerSerialNo=" + scanContent);

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "বাতিল হয়েছে", Toast.LENGTH_SHORT).show();
            }

        }else if (requestCode == 30) {
            if (resultCode == RESULT_OK) {
                String scanContent = intent.getStringExtra("SCAN_RESULT");

                openUrl(inventoryProductSerial + "?ProductInventorySerialNo=" + scanContent);

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "বাতিল হয়েছে", Toast.LENGTH_SHORT).show();
            }

        }
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    String url = new String(POST_URL);
                    String webUrl = new String(main_web_view.getUrl());

                    if (url.equals(webUrl)) {
                        new AlertDialog.Builder(this)
                                .setIcon(android.R.drawable.alert_dark_frame)
                                .setTitle("সতর্কতা !")
                                .setMessage("আপনি কি লগআউট করতে চান ?")
                                .setPositiveButton("হ্যাঁ |", new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        openUrl("javascript:document.getElementById('logoutForm').submit()");
                                    }

                                })
                                .setNegativeButton("না |", null)
                                .show();
                    }
                    else {
                        url = new String(MAIN_SITE_URL);
                        webUrl = new String(main_web_view.getUrl());

                        if (url.equals(webUrl)) {
                            /*Toast.makeText(this, "Access Denied",
                                    Toast.LENGTH_SHORT).show();*/
                            new AlertDialog.Builder(this)
                                    .setIcon(android.R.drawable.alert_dark_frame)
                                    .setTitle("বাহির!")
                                    .setMessage("আপনি কি বের হতে চান ?")
                                    .setPositiveButton("হ্যাঁ |", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }

                                    })
                                    .setNegativeButton("না |", null)
                                    .show();
                        }


                        else
                        if (main_web_view.canGoBack()) {
                            Toast.makeText(this, "Back",
                                    Toast.LENGTH_SHORT);
                            main_web_view.goBack();
                        }
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        main_web_view.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        main_web_view.restoreState(savedInstanceState);
    }

    private void currentPlaceLocation() {
        try{

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
            } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }catch (Exception e) {
            Log.e("Fail 2", e.toString());

            e.printStackTrace();
        }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);
                        Geocoder geocoder = new Geocoder(getApplicationContext());
                        try {
                            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                            String str = addressList.get(0).getFeatureName();
                            str += "," + addressList.get(0).getSubLocality() + "," + addressList.get(0).getLocality() + "," + addressList.get(0).getCountryName();


                            locationname = str;
                            postlatitude = latitude;
                            postlongitude = longitude;

                            address1 =locationname;
                           /* placeTextView.setText(address1);
                            latTextView.setText(Double.toString(postlatitude));
                            longTextView.setText(Double.toString(postlongitude));*/
                            /*editTextAddress.setText(locationname);*/


                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);
                        Geocoder geocoder = new Geocoder(getApplicationContext());
                        try {
                            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                            String str = addressList.get(0).getFeatureName();
                            str += "," + addressList.get(0).getSubLocality() + "," + addressList.get(0).getLocality() + "," + addressList.get(0).getCountryName();

                            locationname = str;
                            postlatitude = latitude;
                            postlongitude = longitude;

                            address1 =locationname;
                            /*placeTextView.setText(address1);
                            latTextView.setText(Double.toString(postlatitude));
                            longTextView.setText(Double.toString(postlongitude));*/



                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });

            } else {
                Toast.makeText(getApplicationContext(), "Location Access Failed!!", Toast.LENGTH_SHORT).show();

            }

    }
    private void postURL() {
        /* prcValidateUser("");*/

        progressDialog.show();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.pqstec.com/Easyreg/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        Api api = retrofit.create(Api.class);

        JsonObject jsonObjectFinal = new JsonObject();

        JSONObject jsonObjectName = new JSONObject();


        try {

            /*Toast.makeText(MainActivity.this,"PhoneNumber"+phoneNumber+
                    "\nAndroidID"+androidId+"\naddress"+address1+
                    "\npostlatitude"+postlatitude+"\npostlongitude"+postlongitude,Toast.LENGTH_LONG).show();*/

            jsonObjectName.put("UserPhoneNo", phoneNumber);
            jsonObjectName.put("macAddress", androidId);
            jsonObjectName.put("Latitude", postlatitude);
            jsonObjectName.put("longitude", postlongitude);
            jsonObjectName.put("LocationName", address1);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonParser jsonParser = new JsonParser();
        jsonObjectFinal = (JsonObject) jsonParser.parse(jsonObjectName.toString());
        Call<Information> obj = api.postURLInfo(jsonObjectFinal);

        obj.enqueue(new Callback<Information>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<Information> obj, Response<Information> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    int counter = 0;

                    Information information= response.body();

                    Information isSuccessful=response.body();

                    if (isSuccessful != null) {
                        if (isSuccessful.equals("")) {
                            Toast.makeText(MainActivity.this, "আবার চেষ্টা করুন", Toast.LENGTH_LONG).show();
                        } else {
                            /*Toast.makeText(MainActivity.this, "লগইন হয়েছে", Toast.LENGTH_LONG).show();*/
                            /*startActivity(new Intent(LoginActivity.this, ProfileActivity.class)
                                    .putExtra("userId", a)
                                    .putExtra("PhoneNumber", afterOtpPhone));*/

                            /*preferenceConfig.writeLoginStatus(true);*/
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "আবার চেষ্টা করুন", Toast.LENGTH_LONG).show();


                    }

                } else {
                    progressDialog.dismiss();
                    Log.d("", "onResponse: ");
                    Toast.makeText(MainActivity.this,"আবার চেষ্টা করুন", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<Information> obj, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this,  "Failure", Toast.LENGTH_LONG).show();

            }
        });


    }
}
