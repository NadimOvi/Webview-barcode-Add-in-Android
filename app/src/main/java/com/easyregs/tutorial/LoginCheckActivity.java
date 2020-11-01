package com.easyregs.tutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginCheckActivity extends AppCompatActivity {

    TextView phoneTextView;
    EditText phoneNumber,androidId;
    Button submitButton;
    String phoneNumberText;
    String Android_ID;
    CheckBox chkRemember;

    String info;
    String strPhoneType="";
    static final int PERMISSION_READ_STATE =123;

    SharedPreferences sp;
    SharedPreferences.Editor sped;
    ProgressDialog progressDialog;
    LocationManager locationManager;
    private final int FINE_LOCATION_PERMISSION = 9999;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_check);

        permission();

        sp = getApplicationContext().getSharedPreferences("GTR", MODE_PRIVATE);
        sped = sp.edit();
        phoneNumber = findViewById(R.id.phoneNumber);
        androidId = findViewById(R.id.androidId);
        submitButton = findViewById(R.id.submitButton);

        progressDialog = new ProgressDialog(LoginCheckActivity.this);
        progressDialog.setMessage("অপেক্ষা করুন...");
        progressDialog.setCancelable(false);

      /*  progressDialog.show();*/
        prcGetRemember();


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndroidID();

            }
        });
    }

    private void permission() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(LoginCheckActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LoginCheckActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LoginCheckActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
        } else if (ActivityCompat.checkSelfPermission(LoginCheckActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LoginCheckActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(LoginCheckActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(LoginCheckActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    private void prcSetRemember() {
        String strRemember = "";
        strRemember = "Remember";
        sped.putString("PhoneNumber", phoneNumber.getText().toString().trim());
        sped.putString("AndroidId", androidId.getText().toString().trim());
        sped.putString("Remember", strRemember);
        sped.commit();
    }
    private void prcGetRemember() {

        if (sp.contains("PhoneNumber")) {
            progressDialog.show();
            String AndroidId = "";
            phoneNumber.setText(sp.getString("PhoneNumber", ""));
            androidId.setText(sp.getString("AndroidId", ""));
            AndroidId = sp.getString("Remember", "");
            if (AndroidId.length() != 0) {
            }
            prcValidateUser("Auto");
        }
    }

    private void prcValidateUser(String Flag) {
         progressDialog.show();
        //Validating User :: Using Async Task
        try {
            validationUser();

        } catch (Exception ex) {
            Log.d("ValUser", ex.getMessage());
        }
    }
    private void validationUser(){
        progressDialog.dismiss();
        phoneNumberText = phoneNumber.getText().toString().trim();
        Android_ID = androidId.getText().toString().trim();
        if (phoneNumberText.isEmpty()) {
            Toast.makeText(LoginCheckActivity.this, "দয়া করে আপনার ফোন নম্বরটি পূরণ করুন", Toast.LENGTH_LONG).show();
        }else if (phoneNumberText.length()<11){
            Toast.makeText(LoginCheckActivity.this, "১১ সংখ্যার মোবাইল নাম্বার দিন", Toast.LENGTH_LONG).show();
        }else {
            userLogin();
            progressDialog.show();
        }

    }
    private void userLogin(){
        progressDialog.dismiss();
        startActivity(new Intent(LoginCheckActivity.this, MainActivity.class)
                .putExtra("PhoneNumber", phoneNumberText)
                .putExtra("AndroidId", Android_ID));
        prcSetRemember();
        finish();
    }
    private void checkAndroidID(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission
                .READ_PHONE_STATE );

        if (permissionCheck == PackageManager.PERMISSION_GRANTED){
            MyTelephonyManager();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .READ_PHONE_STATE},PERMISSION_READ_STATE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_READ_STATE:
            {
                if(grantResults.length>=0 && grantResults[0] ==  PackageManager.PERMISSION_GRANTED){
                    MyTelephonyManager();
                }else{
                    Toast.makeText(this,"আপনার কাছে সম্পূর্ণ প্রয়োজনীয় অনুমতি নেই",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void MyTelephonyManager(){
        TelephonyManager manager=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        int phoneType = manager.getPhoneType();
        switch (phoneType){
            case (TelephonyManager.PHONE_TYPE_CDMA):
                strPhoneType = "CDMA";
                break;
            case (TelephonyManager.PHONE_TYPE_GSM):
                strPhoneType = "GSM";
                break;
            case (TelephonyManager.PHONE_TYPE_NONE):
                strPhoneType ="NONE";
                break;
        }
        Android_ID= Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        androidId.setText(Android_ID);
        validationUser();

    }
}