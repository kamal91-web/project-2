package com.foddez.service;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.foddez.service.Adapter.SavedAddressAdapter;
import com.foddez.service.Model.SavedAddressModel;
import com.foddez.service.Util.LoadingDialog;
import com.foddez.service.Util.RequestHandler;
import com.foddez.service.Util.ServerURLs;
import com.foddez.service.Util.SharedPrefManager;
import com.foddez.service.Util.SuccessDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedAddress extends AppCompatActivity {
    Dialog popupDialig;
    Button closeImage;
    private ProgressDialog progressDialog;
    LoadingDialog loadingDialog;

    RecyclerView savedAddressRecyclerView;
    RecyclerView.LayoutManager savedAddressLayoutManager;
    List<SavedAddressModel> savedAddressModelList;
    SavedAddressAdapter savedAddressAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_address);
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
            myToolbar.setTitle("Saved Address");
            myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            loadingDialog = new LoadingDialog(SavedAddress.this);
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("please wait...");

            savedAddressModelList = new ArrayList<>();
            savedAddressRecyclerView = findViewById(R.id.recycle_view_saved_address);
            savedAddressRecyclerView.setHasFixedSize(true);
            savedAddressLayoutManager = new LinearLayoutManager(this);
            savedAddressRecyclerView.setLayoutManager(savedAddressLayoutManager);
            loadSavedAddress();
        }
    }

    private void loadSavedAddress() {
        loadingDialog.startLoadingDialog();
        final String userMobile=SharedPrefManager.getInstance(this).getUserMobile();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerURLs.SAVED_ADDRESS_URL,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONArray categoriesObject = new JSONArray(response);
                            //String tcat=String.valueOf(categoriesObject.length());
                            //TastyToast.makeText(Home.this,"Total Category: "+tcat,TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                            for (int j = 0; j < categoriesObject.length(); j++) {
                                JSONObject categoryObject = categoriesObject.getJSONObject(j);

                                String said = categoryObject.getString("said");
                                String address_type = categoryObject.getString("address_type");
                                String house_number = categoryObject.getString("house_number");
                                String area = categoryObject.getString("area");
                                String land_mark = categoryObject.getString("land_mark");
                                String state = categoryObject.getString("state");
                                String city = categoryObject.getString("city");
                                String country = categoryObject.getString("country");

                                SavedAddressModel savedAddress= new SavedAddressModel(said, address_type, house_number,area,land_mark,state,city,country);
                                savedAddressModelList.add(savedAddress);
                            }

                            savedAddressAdapter = new SavedAddressAdapter(SavedAddress.this, savedAddressModelList);
                            savedAddressAdapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                            savedAddressRecyclerView.setAdapter(savedAddressAdapter);
                            loadingDialog.closeDilog();
                        } catch (JSONException e) {
                            loadingDialog.closeDilog();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialog.closeDilog();
                //TastyToast.makeText(Home.this,"Category Error: "+error.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params=new HashMap<>();
                params.put("user_mobile", userMobile);
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
