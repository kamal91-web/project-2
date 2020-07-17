package com.foddez.service;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
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
import android.widget.TextView;

import com.foddez.service.Util.SharedPrefManager;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class OtpVerification extends AppCompatActivity {
    private TextView txtUserMobile, txtTimer, txtSendOtpAgain;
    private EditText txtUserOtp;
    private Button btnContinue;
    Dialog popupDialig;
    Button closeImage;
    private String newOtp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        popupDialig=new Dialog(this);
        if(!isConnected(getApplicationContext())){
            showPopup(getApplicationContext());
        }else {
            if (SharedPrefManager.getInstance(this).isLogedIn()) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                finish();
                return;
            }
            txtUserMobile=findViewById(R.id.txt_mobile_no);
            txtUserOtp=findViewById(R.id.txt_user_otp);
            txtTimer=findViewById(R.id.txt_timer);
            txtUserMobile.setText("Enter the OTP send to "+getIntent().getStringExtra("mobileno"));
            newOtp=getIntent().getStringExtra("send_otp");
            txtSendOtpAgain=findViewById(R.id.txt_send_otp_again);
            btnContinue=findViewById(R.id.btn_continue);
            txtUserOtp.setText(newOtp);
            timeCounter();
            txtSendOtpAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txtTimer.setVisibility(View.VISIBLE);
                    txtSendOtpAgain.setVisibility(View.GONE);
                    sendOtp();
                    timeCounter();
                }
            });

            txtUserOtp.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (txtUserOtp.getText().toString().trim().length() == 4) {
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
                    checkotp();
                }
            });
        }
    }
    private void checkotp() {
        String enter_otp=txtUserOtp.getText().toString().trim();
        if(enter_otp.equals(newOtp)){
            //SharedPrefManager.getInstance(getApplicationContext()).userLogin(getIntent().getStringExtra("mobileno"));
            Intent gotologin=new Intent(getApplicationContext(),SearchAddress.class);
            gotologin.putExtra("mobileno",getIntent().getStringExtra("mobileno"));
            startActivity(gotologin);
            finish();
        }else{
            TastyToast.makeText(getApplicationContext(),"Please enter valid OTP",TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
        }
    }
    private void timeCounter(){
        new CountDownTimer(90000,1000){
            @Override
            public void onTick(long millisUntilFinished) {
                txtTimer.setText("Waite "+String.format("%d",
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)));
            }

            @Override
            public void onFinish() {
                txtTimer.setVisibility(View.GONE);
                txtSendOtpAgain.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void sendOtp() {
        Random r = new Random();
        newOtp = String.format("%04d", r.nextInt(10000));
        String mes="Your registration OTP is: "+newOtp;
        //sendSMS(getIntent().getStringExtra("mobileno"),mes);
        txtUserOtp.setText(newOtp);
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
