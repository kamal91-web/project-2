package com.foddez.service;

import android.app.Dialog;
import android.app.ProgressDialog;
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

import java.util.HashMap;
import java.util.Map;

public class GoogleMapLocation extends AppCompatActivity {
    Button btnHome, btnOffice, btnOther, btnNext;
    Dialog popupDialig;
    Button closeImage;
    private ProgressDialog progressDialog;
    private EditText txtFlatName, txtHowToReach, txtContactDetal;
    LoadingDialog loadingDialog;
    private String addressType="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map_location);
        popupDialig=new Dialog(this);
        if(!isConnected(getApplicationContext())){
            showPopup(getApplicationContext());
        }else {
            if (SharedPrefManager.getInstance(this).isLogedIn()) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                finish();
                return;
            }

            loadingDialog = new LoadingDialog(GoogleMapLocation.this);
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("please wait...");

            btnHome = findViewById(R.id.btn_home);
            btnOffice = findViewById(R.id.btn_office);
            btnOther = findViewById(R.id.btn_other);

            btnNext= findViewById(R.id.btnHomeNext);

            txtFlatName= findViewById(R.id.txt_building_name);
            txtHowToReach= findViewById(R.id.txt_land_mark);
            txtContactDetal= findViewById(R.id.txt_contact);

            btnHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addressType="HOME";
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        btnHome.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        btnOffice.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
                        btnOther.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
                    } else {
                        btnHome.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        btnOffice.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
                        btnOther.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
                    }
                }
            });

            btnOffice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addressType="OFFICE";
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        btnOffice.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        btnHome.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
                        btnOther.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
                    } else {
                        btnOffice.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        btnHome.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
                        btnOther.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
                    }
                }
            });

            btnOther.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addressType="OTHER";
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        btnOther.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        btnHome.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
                        btnOffice.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
                    } else {
                        btnOther.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        btnHome.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
                        btnOffice.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_border));
                    }
                }
            });

            txtFlatName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(txtFlatName.getText().toString().trim().equals("")){
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            btnNext.setEnabled(false);
                            btnNext.setClickable(false);
                            btnNext.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_desable));
                        } else {
                            btnNext.setEnabled(false);
                            btnNext.setClickable(false);
                            btnNext.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_desable));
                        }
                    }else{
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            btnNext.setEnabled(true);
                            btnNext.setClickable(true);
                            btnNext.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        } else {
                            btnNext.setEnabled(true);
                            btnNext.setClickable(true);
                            btnNext.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        }

                    }
                }
            });
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveUserAddress();
                }
            });
        }
    }

    private void saveUserAddress() {
        loadingDialog.startLoadingDialog();
        StringRequest stringRequest=new StringRequest(Request.Method.POST, ServerURLs.SAVE_USER_ADDRESS_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj=new JSONObject(response);
                    if(jObj.getBoolean("error")){
                        loadingDialog.closeDilog();
                        SharedPrefManager.getInstance(getApplicationContext()).userLogin(getIntent().getStringExtra("mobileno"),jObj.getString("aid"),jObj.getString("area"),jObj.getString("city"),jObj.getString("address_type"));
                        Intent gotologin=new Intent(getApplicationContext(),Home.class);
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
                params.put("user_mobile",getIntent().getStringExtra("mobileno"));
                params.put("areaid",getIntent().getStringExtra("areaid"));
                params.put("cityid",getIntent().getStringExtra("cityid"));
                params.put("stateid",getIntent().getStringExtra("stateid"));
                params.put("flat_name",txtFlatName.getText().toString().trim());
                params.put("land_mark",txtHowToReach.getText().toString().trim());
                params.put("contact_no",txtContactDetal.getText().toString().trim());
                params.put("address_type",addressType);
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
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
