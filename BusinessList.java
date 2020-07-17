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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.foddez.service.Adapter.BusinessOpenAdapter;
import com.foddez.service.Model.BusinessModel;
import com.foddez.service.Util.RequestHandler;
import com.foddez.service.Util.ServerURLs;
import com.foddez.service.Util.SharedPrefManager;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessList extends AppCompatActivity {
    Dialog popupDialig;
    Button closeImage;
    private TextView toolbarText, txtTotalBusiness;
    private ProgressDialog progressDialog;
    private ImageView imgBack;
/// Business
    RecyclerView businessOpenRecyclerView;
    RecyclerView.LayoutManager businessOpenLayoutManager;
    ArrayList<BusinessModel> businessModelList;
    BusinessOpenAdapter businessOpenAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_list);
        popupDialig=new Dialog(this);
        if(!isConnected(getApplicationContext())){
            showPopup(getApplicationContext());
        }else {
            if (!SharedPrefManager.getInstance(this).isLogedIn()) {
                startActivity(new Intent(getApplicationContext(), EnterMobile.class));
                finish();
                return;
            }
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            Toolbar myToolbar = (Toolbar) findViewById(R.id.z_toolbar);
            setSupportActionBar(myToolbar);
            myToolbar.setTitle("Order from "+getIntent().getStringExtra("cat_nm"));
            myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            progressDialog = new ProgressDialog(this);
            txtTotalBusiness=findViewById(R.id.total_business);

            // Business List
            businessModelList = new ArrayList<>();
            businessOpenRecyclerView = findViewById(R.id.total_open_business_recycler);
            businessOpenRecyclerView.setHasFixedSize(true);
            businessOpenLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            businessOpenRecyclerView.setLayoutManager(businessOpenLayoutManager);
            loadBusiness(SharedPrefManager.getInstance(this).getCityName(),getIntent().getStringExtra("cat_id"));
        }
    }

    private void loadBusiness(final String city_name, final String cat_id) {
        progressDialog.setMessage("loading...");
        progressDialog.show();
        businessModelList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerURLs.BUSINESS_LIST_URL,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONArray categoriesObject = new JSONArray(response);
                            //String tcat=String.valueOf(categoriesObject.length());
                            //TastyToast.makeText(Home.this,"Total Category: "+tcat,TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                            if(cat_id.equals("1")){
                                txtTotalBusiness.setText(categoriesObject.length() + " Restaurants");
                            }else {
                                txtTotalBusiness.setText(categoriesObject.length() + " Stores");
                            }
                            //StringBuilder total = new StringBuilder();
                            for (int j = 0; j < categoriesObject.length(); j++) {
                                JSONObject categoryObject = categoriesObject.getJSONObject(j);
                                //total = String.valueOf(j)+", ";
                                //total.append(String.valueOf(j));
                                String bid = categoryObject.getString("business_id");
                                String business_name = categoryObject.getString("business_name");
                                String business_image = categoryObject.getString("business_image");
                                String business_desc = categoryObject.getString("business_description");
                                String business_dist = categoryObject.getString("business_distance");
                                String business_delivery_t = categoryObject.getString("delivery_time");

                                BusinessModel businessModel = new BusinessModel(bid, business_name, business_image,business_desc,business_dist,business_delivery_t);
                                businessModelList.add(businessModel);

                            }

                            businessOpenAdapter = new BusinessOpenAdapter(BusinessList.this, businessModelList);
                            businessOpenAdapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                            businessOpenRecyclerView.setAdapter(businessOpenAdapter);
                            businessOpenAdapter.setOnItemClickListener(new BusinessOpenAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    Intent gotoppage=new Intent(BusinessList.this,ProductList.class);
                                    gotoppage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    BusinessModel businessModel=businessModelList.get(position);
                                    gotoppage.putExtra("city_name",SharedPrefManager.getInstance(BusinessList.this).getCityName());
                                    gotoppage.putExtra("cat_id",getIntent().getStringExtra("cat_id"));
                                    gotoppage.putExtra("business_id",businessModel.getBid());
                                    gotoppage.putExtra("business_name",businessModel.getBusiness_name());
                                    startActivity(gotoppage);
                                }
                            });

                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                            TastyToast.makeText(getApplicationContext(),e.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                TastyToast.makeText(BusinessList.this,"Category Error: "+error.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params=new HashMap<>();
                params.put("city_name", city_name);
                params.put("cat_id", cat_id);
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
