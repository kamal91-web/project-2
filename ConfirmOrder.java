package com.foddez.service;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.foddez.service.Adapter.CartItemAdapter;
import com.foddez.service.Adapter.SavedAddressAdapter;
import com.foddez.service.Adapter.SavedAddressRadioAdapter;
import com.foddez.service.Model.CartItemModel;
import com.foddez.service.Model.SavedAddressModel;
import com.foddez.service.Util.Database;
import com.foddez.service.Util.LoadingDialog;
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

public class ConfirmOrder extends AppCompatActivity {
    Dialog popupDialig;
    Button closeImage;
    private ProgressDialog progressDialog;
    LoadingDialog loadingDialog;

    private Button btnAddDeliveryAddress;
    private TextView txtStoreName, txtAddMore, txtItemTotal,txtDeliveryCharge, txtTotalPay;

    private Database mDataBase;
    RecyclerView cartItemRecyclerView;
    RecyclerView.LayoutManager cartItemLayoutManager;
    ArrayList<CartItemModel> cartItemList;
    CartItemAdapter cartItemAdapter;
    BottomSheetDialog bottomSheetDialog,caBottomSheetDialog;
    // Address List
    RecyclerView savedAddressRecyclerView;
    RecyclerView.LayoutManager savedAddressLayoutManager;
    List<SavedAddressModel> savedAddressModelList;
    SavedAddressRadioAdapter savedAddressAdapter;


    private LinearLayout llProceed;
    private RelativeLayout rlAddAddress;
    private TextView txtSAddressType, txtSAddress, txtChangeAddress;
    private Button btnProceed,btnCompleteProfile;
    private float gtotalpay= (float) 0.0;

    // Edit Profile
    private EditText txtFullName, txtEmailId;
    private Button btnUpdateProfile;
    private String get_user_name, get_user_email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);
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
            myToolbar.setTitle("Confirm Order");
            myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            loadingDialog = new LoadingDialog(ConfirmOrder.this);
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("please wait...");

            mDataBase = new Database(this);
            txtStoreName=findViewById(R.id.txt_store_name);
            txtStoreName.setText(getIntent().getStringExtra("business_name"));
            txtAddMore=findViewById(R.id.txt_add_more);

            txtItemTotal=findViewById(R.id.txt_item_total);
            txtDeliveryCharge=findViewById(R.id.txt_delivery_charge);
            txtTotalPay=findViewById(R.id.txt_total_pay);
            btnAddDeliveryAddress=findViewById(R.id.btn_add_delivery_address);

            llProceed=findViewById(R.id.ll_continue);
            txtSAddressType=findViewById(R.id.txt_s_addr_type);
            txtSAddress=findViewById(R.id.txt_s_address);
            txtChangeAddress=findViewById(R.id.txt_change_address);
            btnCompleteProfile=findViewById(R.id.brn_complete_profile);

            btnProceed=findViewById(R.id.brn_pay_proceed);
            rlAddAddress=findViewById(R.id.rl_add_address);

            txtAddMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });


            // Cart Item List
            cartItemList = new ArrayList<>();
            cartItemRecyclerView = findViewById(R.id.recycler_cart_item);
            cartItemRecyclerView.setHasFixedSize(true);
            cartItemLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            cartItemRecyclerView.setLayoutManager(cartItemLayoutManager);
            loadCartItems(getIntent().getStringExtra("business_id"));

            // Address List

            savedAddressModelList = new ArrayList<>();
            loadSavedAddress();
            btnAddDeliveryAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog=new BottomSheetDialog(ConfirmOrder.this,R.style.BottomSheetDialogTheme);
                    View bottomSheetView= LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_bottom_select_address,(LinearLayout)findViewById(R.id.bottom_sheet_layout));
                    bottomSheetView.findViewById(R.id.img_close_dialog).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();
                        }
                    });
                    bottomSheetView.findViewById(R.id.rl_add_new_address).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent gotologin = new Intent(getApplicationContext(), HomeChangeLocation.class);
                            gotologin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            gotologin.putExtra("return_type","cart");
                            startActivity(gotologin);
                        }
                    });
                    savedAddressRecyclerView = bottomSheetView.findViewById(R.id.recycler_address);
                    savedAddressRecyclerView.setHasFixedSize(true);
                    savedAddressLayoutManager = new LinearLayoutManager(getApplicationContext());
                    savedAddressRecyclerView.setLayoutManager(savedAddressLayoutManager);

                    savedAddressAdapter = new SavedAddressRadioAdapter(ConfirmOrder.this, savedAddressModelList);
                    savedAddressAdapter.notifyDataSetChanged();
                    savedAddressRecyclerView.setAdapter(savedAddressAdapter);

                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();
                }
            });

            txtChangeAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog=new BottomSheetDialog(ConfirmOrder.this,R.style.BottomSheetDialogTheme);
                    View bottomSheetView= LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_bottom_select_address,(LinearLayout)findViewById(R.id.bottom_sheet_layout));
                    bottomSheetView.findViewById(R.id.img_close_dialog).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();
                        }
                    });
                    savedAddressRecyclerView = bottomSheetView.findViewById(R.id.recycler_address);
                    savedAddressRecyclerView.setHasFixedSize(true);
                    savedAddressLayoutManager = new LinearLayoutManager(getApplicationContext());
                    savedAddressRecyclerView.setLayoutManager(savedAddressLayoutManager);

                    savedAddressAdapter = new SavedAddressRadioAdapter(ConfirmOrder.this, savedAddressModelList);
                    savedAddressAdapter.notifyDataSetChanged();
                    savedAddressRecyclerView.setAdapter(savedAddressAdapter);

                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();
                }
            });
            loadUserDetail();

            btnCompleteProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    caBottomSheetDialog=new BottomSheetDialog(ConfirmOrder.this,R.style.BottomSheetDialogTheme);
                    final View bottomSheetView= LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_complete_profile,(LinearLayout)findViewById(R.id.bottom_sheet_layout));
                    bottomSheetView.findViewById(R.id.img_close_dialog).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            caBottomSheetDialog.dismiss();
                        }
                    });
                    txtFullName = bottomSheetView.findViewById(R.id.txt_user_name);
                    txtFullName.setText(get_user_name);
                    txtEmailId = bottomSheetView.findViewById(R.id.txt_user_email);
                    txtEmailId.setText(get_user_email);
                    bottomSheetView.findViewById(R.id.btnUpdateProfile).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetView.findViewById(R.id.btnUpdateProfile).setClickable(false);
                            bottomSheetView.findViewById(R.id.btnUpdateProfile).setEnabled(false);
                            progressDialog.show();
                            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                            if(txtFullName.getText().toString().equals("")){
                                txtFullName.setError("please enter full name");
                                progressDialog.dismiss();
                            }else if(!txtEmailId.getText().toString().trim().equals("") && !txtEmailId.getText().toString().trim().matches(emailPattern)){
                                txtEmailId.setError("please enter valid email address!");
                                progressDialog.dismiss();
                            }else{
                                updateUserProfile(txtFullName.getText().toString().trim(),txtEmailId.getText().toString().trim());
                                caBottomSheetDialog.dismiss();
                                progressDialog.dismiss();
                            }
                        }
                    });
                    caBottomSheetDialog.setContentView(bottomSheetView);
                    caBottomSheetDialog.show();
                }
            });
            btnProceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gotoppage=new Intent(ConfirmOrder.this,PaymentMethod.class);
                    gotoppage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    gotoppage.putExtra("total_cart_payment",gtotalpay);
                    startActivity(gotoppage);
                }
            });
        }
    }

    private void updateUserProfile(final String full_name, final String email_id) {
        final String userMobile=SharedPrefManager.getInstance(this).getUserMobile();
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
                                loadUserDetail();
                            }else{

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //TastyToast.makeText(getApplicationContext(),e.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //loadingDialog.closeDilog();
                        //TastyToast.makeText(getApplicationContext(),error.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
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

    public void loadCartItems(String business_id) {
        Cursor cursor=mDataBase.getAllCartProduct();
        double gtotal=0.00, pptotal=0.00;
        int counter=0,delivery_charge=0;
        cartItemList.clear();
        if(cursor.moveToFirst()) {
            do {
                counter++;
                String businessid=cursor.getString(1);
                String productid=cursor.getString(2);
                String categoryid=cursor.getString(3);
                String subcategoryid=cursor.getString(4);
                String productname=cursor.getString(5);
                String productdescription=cursor.getString(6);
                String productunit=cursor.getString(7);
                String productsgst=cursor.getString(8);
                String productcgst=cursor.getString(9);
                String productimage=cursor.getString(10);
                String productunitname=cursor.getString(11);
                String productstock=cursor.getString(12);
                String productprice=cursor.getString(13);
                String orderqty=cursor.getString(14);
                String offerid=cursor.getString(15);
                String offerprice=cursor.getString(16);
                String offerpercentage=cursor.getString(17);
                String businessname=cursor.getString(18);
                pptotal=Integer.parseInt(cursor.getString(14))*Float.parseFloat(cursor.getString(13));
                gtotal +=pptotal;
                CartItemModel cartItemModel=new CartItemModel(businessid,productid,categoryid,subcategoryid,productname,productdescription,productunit,productsgst,productcgst,productimage,productunitname,productstock,productprice,orderqty,offerid,offerprice,offerpercentage,businessname);
                cartItemList.add(cartItemModel);
            } while (cursor.moveToNext());

            cartItemAdapter=new CartItemAdapter(ConfirmOrder.this,cartItemList,mDataBase);
            cartItemRecyclerView.setAdapter(cartItemAdapter);
            txtItemTotal.setText("₹ "+String.valueOf(gtotal));
            txtDeliveryCharge.setText("₹ "+String.valueOf(delivery_charge));
            gtotalpay= (float) (gtotal+delivery_charge);
            txtTotalPay.setText("₹ "+String.valueOf(gtotalpay));
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

    private void loadSavedAddress() {
        //loadingDialog.startLoadingDialog();
        savedAddressModelList.clear();
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //loadingDialog.closeDilog();
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
    private void loadUserDetail() {
        final String userMobile=SharedPrefManager.getInstance(this).getUserMobile();
        StringRequest stringRequest=new StringRequest(
                Request.Method.POST,
                ServerURLs.CHECK_USER_PROFILE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObj=new JSONObject(response);
                            if(jObj.getBoolean("error")){

                                get_user_name=jObj.getString("full_name");
                                get_user_email=jObj.getString("email_id");
                                btnCompleteProfile.setVisibility(View.VISIBLE);
                                btnProceed.setVisibility(View.GONE);
                            }else{
                                get_user_name=jObj.getString("full_name");
                                get_user_email=jObj.getString("email_id");
                                btnProceed.setText("Pay \u20B9"+gtotalpay);
                                btnCompleteProfile.setVisibility(View.GONE);
                                btnProceed.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //TastyToast.makeText(getApplicationContext(),e.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //loadingDialog.closeDilog();
                        //TastyToast.makeText(getApplicationContext(),error.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
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
    public void addressSelected(String said, String address_type, String full_address){
        String sb = full_address.substring(0,37);
        String ssb=sb+"...";

        bottomSheetDialog.dismiss();
        rlAddAddress.setVisibility(View.GONE);
        llProceed.setVisibility(View.VISIBLE);
        txtSAddressType.setText("Delever to: "+address_type);
        txtSAddress.setText(ssb);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadSavedAddress();
        bottomSheetDialog.dismiss();
        //TastyToast.makeText(getApplicationContext(),"backkedddd..",TastyToast.LENGTH_LONG, TastyToast.ERROR).show();
    }
}
