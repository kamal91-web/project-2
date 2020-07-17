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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.foddez.service.Util.LoadingDialog;
import com.foddez.service.Util.SharedPrefManager;

public class PaymentMethod extends AppCompatActivity {
    Dialog popupDialig;
    Button closeImage;
    private ProgressDialog progressDialog;
    LoadingDialog loadingDialog;

    private TextView txtTotalCardAmt;

    private LinearLayout llGooglepay, llPhonepay,llNetBanking, llCod;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);
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
            loadingDialog = new LoadingDialog(PaymentMethod.this);
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("please wait...");


            txtTotalCardAmt=findViewById(R.id.txt_total_cart_payment);
            txtTotalCardAmt.setText("Item Total ₹"+getIntent().getStringExtra("total_cart_payment"));

            llGooglepay=findViewById(R.id.ll_google_pay);
            llPhonepay=findViewById(R.id.ll_paytm_pay);
            llNetBanking=findViewById(R.id.ll_netbanking_pay);
            llCod=findViewById(R.id.ll_cod_pay);
            llCod.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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
