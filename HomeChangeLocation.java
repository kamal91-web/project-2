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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.foddez.service.Util.RequestHandler;
import com.foddez.service.Util.ServerURLs;
import com.foddez.service.Util.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeChangeLocation extends AppCompatActivity {
    Dialog popupDialig;
    Button closeImage;
    private ProgressDialog progressDialog;

    AutoCompleteTextView autoCompleteTextView;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    private LinearLayout llMapLocation;
    private ArrayList<String> areaListArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_change_location);
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });


            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("please wait...");
            areaListArray = new ArrayList<String>();
            //Setting recyclear view
            /*mRecyclerView = findViewById(R.id.recycle_view_search_address);
            mRecyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(layoutManager);*/
            llMapLocation = findViewById(R.id.ll_location_map);

            //-----------------------------------------------
            //initisilizing SearchBar

            autoCompleteTextView=findViewById(R.id.address_search_text);
            loadArea();
            llMapLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent gotologin = new Intent(getApplicationContext(), HomeGoogleMapLocation.class);
                    //gotologin.putExtra("mobileno",txtMobileNo.getText().toString().trim());
                    startActivity(gotologin);
                }
            });

            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    getAreaDetail(autoCompleteTextView.getText().toString().trim());

                }
            });
        }
    }
    private void getAreaDetail(final String search_area) {
        progressDialog.setMessage("please wait...");
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerURLs.AFTER_SEARCH_AREA_HOME_URL,
                new com.android.volley.Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObj=new JSONObject(response);
                            if(!jObj.getBoolean("error")){
                                String areaid=jObj.getString("areaid");
                                String cityid=jObj.getString("cityid");
                                String stateid=jObj.getString("stateid");
                                String countryid=jObj.getString("countryid");
                                String area_name=jObj.getString("area_name");
                                String city_name=jObj.getString("city_name");
                                String state_name=jObj.getString("state_name");
                                String country_name=jObj.getString("country_name");
                                //SharedPrefManager.getInstance(getApplicationContext()).userLogin(getIntent().getStringExtra("mobileno"),jObj.getString("aid"),jObj.getString("area"),jObj.getString("city"));
                                Intent gotologin=new Intent(getApplicationContext(),HomeGoogleMapLocation.class);
                                gotologin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if(getIntent().getStringExtra("return_type").equals("cart"))
                                {
                                    gotologin.putExtra("return_type","cart");
                                    gotologin.putExtra("areaid",areaid);
                                    gotologin.putExtra("cityid",cityid);
                                    gotologin.putExtra("stateid",stateid);
                                    gotologin.putExtra("countryid",countryid);
                                    gotologin.putExtra("area_name",area_name);
                                    gotologin.putExtra("city_name",city_name);
                                    gotologin.putExtra("state_name",state_name);
                                    gotologin.putExtra("country_name",country_name);
                                    startActivity(gotologin);
                                    finish();
                                }else{
                                    gotologin.putExtra("return_type","");
                                    gotologin.putExtra("areaid",areaid);
                                    gotologin.putExtra("cityid",cityid);
                                    gotologin.putExtra("stateid",stateid);
                                    gotologin.putExtra("countryid",countryid);
                                    gotologin.putExtra("area_name",area_name);
                                    gotologin.putExtra("city_name",city_name);
                                    gotologin.putExtra("state_name",state_name);
                                    gotologin.putExtra("country_name",country_name);
                                    startActivity(gotologin);
                                }

                            }
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("search_area", search_area);
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }
    // For Area List Spinner
    private void loadArea() {

        progressDialog.setMessage("loading...");
        progressDialog.show();
        areaListArray.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerURLs.AREA_LIST_URL,
                new com.android.volley.Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray allarealist = new JSONArray(response);
                            if (allarealist.length() > 0) {
                                for (int i = 0; i < allarealist.length(); i++) {
                                    JSONObject areaObj = allarealist.getJSONObject(i);
                                    String area_name = areaObj.getString("search");
                                    areaListArray.add(area_name);
                                }
                                //ddlArea.setAdapter(new ArrayAdapter<String>(DeliveryAddress.this, android.R.layout.simple_spinner_dropdown_item, areaListArray));
                                ArrayAdapter adapter=new ArrayAdapter(HomeChangeLocation.this,android.R.layout.simple_list_item_1,areaListArray);
                                autoCompleteTextView.setAdapter(adapter);
                                autoCompleteTextView.setThreshold(3);
                                autoCompleteTextView.setAdapter(adapter);
                            }
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("city_nm", "");
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
