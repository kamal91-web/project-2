package com.foddez.service;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.foddez.service.Util.LoadingDialog;
import com.foddez.service.Util.RequestHandler;
import com.foddez.service.Util.ServerURLs;
import com.foddez.service.Util.SharedPrefManager;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EnterMobile extends AppCompatActivity {
    private Button btnContinue;
    private EditText txtMobileNo;
    LoadingDialog loadingDialog;

    Dialog popupDialig;
    Button closeImage;
    private String fullOTP,mobileno,newOtp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_mobile);
        popupDialig=new Dialog(this);
        if(!isConnected(getApplicationContext())){
            showPopup(getApplicationContext());
        }else {
            if (SharedPrefManager.getInstance(this).isLogedIn()) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                finish();
                return;
            }
            btnContinue = findViewById(R.id.btn_continue);
            txtMobileNo = findViewById(R.id.txt_mobile_no);
            loadingDialog = new LoadingDialog(EnterMobile.this);
            txtMobileNo.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (txtMobileNo.getText().toString().trim().length() == 10) {
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            btnContinue.setEnabled(true);
                            btnContinue.setClickable(true);
                            btnContinue.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        } else {
                            btnContinue.setEnabled(true);
                            btnContinue.setClickable(true);
                            btnContinue.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        }
                    } else {
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            btnContinue.setEnabled(false);
                            btnContinue.setClickable(false);
                            btnContinue.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_desable));
                        } else {
                            btnContinue.setEnabled(false);
                            btnContinue.setClickable(false);
                            btnContinue.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_desable));
                        }
                    }
                }
            });

            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    checkUserMobile();
                }
            });
        }
    }

    private void checkUserMobile() {
        loadingDialog.startLoadingDialog();
        StringRequest stringRequest=new StringRequest(Request.Method.POST, ServerURLs.CHECK_MOBILE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject=new JSONObject(response);
                    if(jsonObject.getBoolean("error")){
                        sendOtp();
                        //
                        loadingDialog.closeDilog();
                        Intent gotologin=new Intent(getApplicationContext(),OtpVerification.class);
                        gotologin.putExtra("mobileno",txtMobileNo.getText().toString().trim());
                        gotologin.putExtra("send_otp",newOtp);
                        startActivity(gotologin);
                        finish();
                    }else{
                        loadingDialog.closeDilog();
                        TastyToast.makeText(getApplicationContext(),"Error: Internal Server Error",TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loadingDialog.closeDilog();
                    TastyToast.makeText(getApplicationContext(),"Error2: "+e.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialog.closeDilog();
                TastyToast.makeText(getApplicationContext(),"Error3: "+error.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("mobile_no",txtMobileNo.getText().toString().trim());
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void sendOtp() {
        Random r = new Random();
        newOtp = String.format("%04d", r.nextInt(10000));
        String mes="Your registration OTP is: "+newOtp;
        //sendSMS(mobileno,mes);
    }
    private boolean sendSMS(final String mobileNo, final String sendSms){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                String authkey = "12956AJFCd4Fb3u5d526422";
                String senderId = "URBSPL";
                String clientMessage = sendSms;
                String route="4";

                URLConnection clientUrlConnection=null;
                URL clientUrl=null;
                BufferedReader clientReader=null;

                String encode_client_sms= URLEncoder.encode(clientMessage);

                String mainUrl="http://login.yourbulksms.com/api/sendhttp.php?";

                StringBuilder sbPostData= new StringBuilder(mainUrl);
                sbPostData.append("authkey="+authkey);
                sbPostData.append("&mobiles=+91"+mobileNo);
                sbPostData.append("&message="+encode_client_sms);
                sbPostData.append("&route="+route);
                sbPostData.append("&sender="+senderId);
                mainUrl = sbPostData.toString();
                try{
                    //prepare connection
                    clientUrl = new URL(mainUrl);
                    clientUrlConnection = clientUrl.openConnection();
                    clientUrlConnection.connect();
                    clientReader= new BufferedReader(new InputStreamReader(clientUrlConnection.getInputStream()));
                    clientReader.close();
                }catch (IOException e){
                    e.printStackTrace();
                    //TastyToast.makeText(getApplicationContext(),"OTPERROR: "+e.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                }
            }
        });
        thread.start();
        return false;
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
