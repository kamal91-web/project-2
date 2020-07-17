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
import android.support.v7.widget.Toolbar;
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
import com.foddez.service.Util.SuccessDialog;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {
    Dialog popupDialig;
    Button closeImage;
    private ProgressDialog progressDialog;

    private EditText txtFullName, txtEmailId;
    private Button btnUpdate;

    LoadingDialog loadingDialog;
    SuccessDialog successDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
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
            loadingDialog = new LoadingDialog(EditProfile.this);
            successDialog=new SuccessDialog(EditProfile.this);
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("please wait...");

            txtFullName=findViewById(R.id.txt_user_name);
            txtEmailId=findViewById(R.id.txt_user_email);
            loadUserDetail();
            btnUpdate=findViewById(R.id.btnUpdateProfile);

            txtFullName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(txtFullName.getText().toString().trim().equals("") && txtEmailId.getText().toString().trim().equals("")){
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            btnUpdate.setEnabled(false);
                            btnUpdate.setClickable(false);
                            btnUpdate.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_desable));
                        } else {
                            btnUpdate.setEnabled(false);
                            btnUpdate.setClickable(false);
                            btnUpdate.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_desable));
                        }
                    }else{
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            btnUpdate.setEnabled(true);
                            btnUpdate.setClickable(true);
                            btnUpdate.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        } else {
                            btnUpdate.setEnabled(true);
                            btnUpdate.setClickable(true);
                            btnUpdate.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        }

                    }
                }
            });

            txtEmailId.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(txtFullName.getText().toString().trim().equals("") && txtEmailId.getText().toString().trim().equals("")){
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            btnUpdate.setEnabled(false);
                            btnUpdate.setClickable(false);
                            btnUpdate.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_desable));
                        } else {
                            btnUpdate.setEnabled(false);
                            btnUpdate.setClickable(false);
                            btnUpdate.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_desable));
                        }
                    }else{
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            btnUpdate.setEnabled(true);
                            btnUpdate.setClickable(true);
                            btnUpdate.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        } else {
                            btnUpdate.setEnabled(true);
                            btnUpdate.setClickable(true);
                            btnUpdate.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background_enable));
                        }

                    }
                }
            });

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    verifyDetails();
                }
            });
        }
    }

    private void verifyDetails() {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(txtFullName.getText().toString().trim().equals("")){
            txtFullName.setError("please enter full name");
        }else if(!txtEmailId.getText().toString().trim().equals("") && !txtEmailId.getText().toString().trim().matches(emailPattern)){
            txtEmailId.setError("please enter valid email address!");
        }else{
            updateDetails();
        }

    }

    private void updateDetails() {
        final String userMobile=SharedPrefManager.getInstance(this).getUserMobile();
        final String full_name=txtFullName.getText().toString().trim();
        final String email_id=txtEmailId.getText().toString().trim();
        loadingDialog.startLoadingDialog();
        StringRequest stringRequest=new StringRequest(
                Request.Method.POST,
                ServerURLs.UPDATE_USER_DETAIL_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObj=new JSONObject(response);
                            if(!jObj.getBoolean("error")){
                                txtFullName.setText(jObj.getString("full_name"));
                                txtEmailId.setText(jObj.getString("email_id"));
                                loadingDialog.closeDilog();
                                successDialog.startSuccessDialog();
                            }else{
                                loadingDialog.closeDilog();
                            }
                        } catch (JSONException e) {
                            loadingDialog.closeDilog();
                            e.printStackTrace();
                            TastyToast.makeText(getApplicationContext(),e.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDialog.closeDilog();
                        TastyToast.makeText(getApplicationContext(),error.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("user_mobile",userMobile);
                params.put("full_name",full_name);
                params.put("email_id",email_id);
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void loadUserDetail() {
        final String userMobile=SharedPrefManager.getInstance(this).getUserMobile();
        loadingDialog.startLoadingDialog();
        StringRequest stringRequest=new StringRequest(
                Request.Method.POST,
                ServerURLs.USER_PERSONAL_DETAIL_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObj=new JSONObject(response);
                            if(!jObj.getBoolean("error")){
                                txtFullName.setText(jObj.getString("full_name"));
                                txtEmailId.setText(jObj.getString("email_id"));
                                loadingDialog.closeDilog();
                            }else{
                                loadingDialog.closeDilog();
                                TastyToast.makeText(getApplicationContext(),jObj.getString("message"),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();

                            }
                        } catch (JSONException e) {
                            loadingDialog.closeDilog();
                            e.printStackTrace();
                            TastyToast.makeText(getApplicationContext(),e.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDialog.closeDilog();
                        TastyToast.makeText(getApplicationContext(),error.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("user_mobile",userMobile);
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
