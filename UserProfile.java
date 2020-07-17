package com.foddez.service;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.foddez.service.Util.LoadingDialog;
import com.foddez.service.Util.SharedPrefManager;

public class UserProfile extends AppCompatActivity {
    Dialog popupDialig;
    Button closeImage;
    private ProgressDialog progressDialog;
    private RelativeLayout rlSavedAddress, rlReferEarn, rlSupport, rlAbout;
    private TextView txtUserMobile,txtEditProfile,txtContactUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        popupDialig=new Dialog(this);
        if(!isConnected(getApplicationContext())){
            showPopup(getApplicationContext());
        }else {
            if (!SharedPrefManager.getInstance(this).isLogedIn()) {
                startActivity(new Intent(getApplicationContext(), EnterMobile.class));
                finish();
                return;
            }

            Toolbar myToolbar = (Toolbar) findViewById(R.id.z_toolbar);
            setSupportActionBar(myToolbar);
            myToolbar.setTitle("");
            myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("please wait...");

            txtUserMobile=findViewById(R.id.txt_user_mobile);
            txtUserMobile.setText("+91"+SharedPrefManager.getInstance(this).getUserMobile());
            txtEditProfile=findViewById(R.id.txt_user_edit_profile);
            txtContactUs=findViewById(R.id.txt_support_title);
            rlSavedAddress=findViewById(R.id.rl_saved_address);
            rlReferEarn=findViewById(R.id.rl_refer_earn);
            rlSupport=findViewById(R.id.rl_support);
            rlAbout=findViewById(R.id.rl_about);

            txtEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gotologin = new Intent(getApplicationContext(), EditProfile.class);
                    startActivity(gotologin);
                }
            });

            rlSavedAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gotologin = new Intent(getApplicationContext(), SavedAddress.class);
                    startActivity(gotologin);
                }
            });
            rlSupport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gotologin = new Intent(getApplicationContext(), ContactUs.class);
                    startActivity(gotologin);
                }
            });
            rlAbout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gotologin = new Intent(getApplicationContext(), AboutUs.class);
                    startActivity(gotologin);
                }
            });
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;
            else return false;
        } else return false;
    }
    public void showPopup(final Context b){
        popupDialig.setContentView(R.layout.popup_window_no_internet_connection);
        closeImage=popupDialig.findViewById(R.id.close_connection_btn);

        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        popupDialig.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupDialig.show();
        Window window=popupDialig.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);
    }
}
