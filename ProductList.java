package com.foddez.service;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.foddez.service.Adapter.ProductAdapter;
import com.foddez.service.Adapter.SubCategoryAdapter;
import com.foddez.service.Model.BusinessModel;
import com.foddez.service.Model.ProductModel;
import com.foddez.service.Model.SubCategoryModel;
import com.foddez.service.Util.Database;
import com.foddez.service.Util.RequestHandler;
import com.foddez.service.Util.ServerURLs;
import com.foddez.service.Util.SharedPrefManager;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductList extends AppCompatActivity {
    Dialog popupDialig;
    Button closeImage;
    private ProgressDialog progressDialog;
    private RecyclerView subCategoryRecyclerView,productRecyclerView;
    private ArrayList<SubCategoryModel> subCategoryList;
    private ArrayList<ProductModel> productList;
    ProductAdapter productAdapter;
    private int cartQty=0;
    private float cartAmt= (float) 0.0;
    private TextView txtStoreName,txtStoreLocation, txtTotalCartItem, txtTotalCart, txtBottomSheetText;
    CollapsingToolbarLayout ctl;

    Database mDataBase;
    private RelativeLayout rlBottomCart;
    private Button btnViewCart;
    private String prevBusinessId="";
    private String prevBusinessName="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        popupDialig=new Dialog(this);
        if(!isConnected(getApplicationContext())){
            showPopup(getApplicationContext());
        }else {
            if (!SharedPrefManager.getInstance(this).isLogedIn()) {
                startActivity(new Intent(getApplicationContext(), EnterMobile.class));
                finish();
                return;
            }
            mDataBase = new Database(this);
            Toolbar myToolbar = (Toolbar) findViewById(R.id.z_toolbar);
            setSupportActionBar(myToolbar);
            myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setSubtitle("Testing");
            ctl = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);


            ctl.setCollapsedTitleTextAppearance(R.style.coll_toolbar_title);
            ctl.setExpandedTitleTextAppearance(R.style.exp_toolbar_title);
            progressDialog = new ProgressDialog(this);// Initializing ProgressBar Auto Loader
            progressDialog.setMessage("loading...");//
            subCategoryList = new ArrayList<>();
            productList = new ArrayList<>();
            txtStoreName=findViewById(R.id.txt_store_name);
            txtStoreLocation=findViewById(R.id.txt_store_location);

            loadStoreDetails(getIntent().getStringExtra("business_id"));
            //TastyToast.makeText(getApplicationContext(),getIntent().getStringExtra("cat_id")+", "+getIntent().getStringExtra("business_id")+", "+getIntent().getStringExtra("city_name"), TastyToast.LENGTH_LONG,TastyToast.WARNING).show();

            rlBottomCart=findViewById(R.id.rl_bottom_cart);
            txtTotalCartItem=findViewById(R.id.txt_total_cart_items);
            txtTotalCart=findViewById(R.id.txt_total_cart_amt);
            btnViewCart=findViewById(R.id.btn_view_cart);
            btnViewCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gotoppage=new Intent(ProductList.this,ConfirmOrder.class);
                    gotoppage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    gotoppage.putExtra("cat_id",getIntent().getStringExtra("cat_id"));
                    gotoppage.putExtra("business_id",getIntent().getStringExtra("business_id"));
                    gotoppage.putExtra("business_name",getIntent().getStringExtra("business_name"));
                    startActivity(gotoppage);
                }
            });

            if(mDataBase.cartProductCount()>0) {
                if (mDataBase.checkBusinessItem(getIntent().getStringExtra("business_id")) == 1) {
                    displayBottomCart();
                } else {
                    Cursor cursor = mDataBase.getBusinessName();
                    if (cursor.moveToFirst()) {
                        prevBusinessName = cursor.getString(18);
                    }
                    emptyCartPopup(prevBusinessName);
                }
            }else{
                displayBottomCart();
            }
        }
    }
    public void emptyCartPopup(String prevBusinesname){
        rlBottomCart.setVisibility(View.GONE);
        //TastyToast.makeText(getApplicationContext(),prevBusinessName+" NULL", TastyToast.LENGTH_LONG,TastyToast.WARNING).show();
        final BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(ProductList.this,R.style.BottomSheetDialogTheme);
        View bottomSheetView= LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_bottom_clear_cart,(LinearLayout)findViewById(R.id.bottom_sheet_layout));
        //bottomSheetView.findViewById(R.id.txt_t)
        bottomSheetView.findViewById(R.id.img_close_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetView.findViewById(R.id.btm_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetView.findViewById(R.id.btm_clear_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataBase.deleteAllProduct();
                bottomSheetDialog.dismiss();
            }
        });
        //bottomSheetView.findViewById(R.id.txt_text_detail);
        txtBottomSheetText=bottomSheetView.findViewById(R.id.txt_text_detail);
        txtBottomSheetText.setText("Your cart contains items from "+prevBusinesname+". Do you want to clear the cart and add items from "+getIntent().getStringExtra("business_name")+"?");
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    private void loadStoreDetails(final String business_id) {
        progressDialog.show();
        StringRequest stringRequest=new StringRequest(
                Request.Method.POST,
                ServerURLs.BUSINESS_DETAIL_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObj=new JSONObject(response);
                            if(!jObj.getBoolean("error")){
                                txtStoreName.setText(jObj.getString("business_name"));
                                ctl.setTitle(jObj.getString("business_name"));
                                txtStoreLocation.setText(jObj.getString("business_area"));
                                initializeComponents(getIntent().getStringExtra("cat_id"),getIntent().getStringExtra("business_id"),getIntent().getStringExtra("city_name"));
                            }else{
                                //TastyToast.makeText(getApplicationContext(),jObj.getString("message"),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();

                            }
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            //TastyToast.makeText(getApplicationContext(),e.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        //TastyToast.makeText(getApplicationContext(),error.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("business_id",business_id);
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }


    public void initializeComponents(String cat_id,String business_id,String city_name){
        subCategoryRecyclerView = findViewById(R.id.recycler_sub_category);
        subCategoryRecyclerView.hasFixedSize();
        subCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));

        productRecyclerView = findViewById(R.id.recycler_products);
        productRecyclerView.hasFixedSize();
        productRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        //productViewPager.setOffscreenPageLimit(0);
        //productViewPager.
        //productViewPager.setPageTransformer(true,new ZoomOutPageTransformer());

        getAllSubCategory(cat_id,business_id,city_name);
    }

    private void getAllSubCategory(final String cat_id,final String business_id,final String city_name) {

        //TastyToast.makeText(getApplicationContext(),"Function Loaded",TastyToast.LENGTH_LONG,TastyToast.WARNING).show();
        StringRequest stringRequest=new StringRequest(Request.Method.POST, ServerURLs.GET_SUB_CATEGORY_LIST_UTL,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        try {
                            subCategoryList.clear();
                            JSONArray subcategorylist=new JSONArray(response);
                            if(subcategorylist.length()>0) {
                                for (int i=0; i < subcategorylist.length(); i++) {
                                    JSONObject subcatObj=subcategorylist.getJSONObject(i);
                                    String scid=subcatObj.getString("sub_cat_id");
                                    String business_id=subcatObj.getString("business_id");
                                    String cat_id=subcatObj.getString("cat_id");
                                    String sub_cat_name=subcatObj.getString("sub_cat_name");
                                    String sub_cat_image=subcatObj.getString("sub_cat_image");
                                    SubCategoryModel subCategoryModel=new SubCategoryModel(scid,cat_id,sub_cat_name,sub_cat_image,business_id);
                                    subCategoryList.add(subCategoryModel);
                                    //Log.d("SUBCATNM=",sub_cat_name);
                                }
                                final SubCategoryAdapter subCatAdapter=new SubCategoryAdapter(ProductList.this,subCategoryList);
                                subCategoryRecyclerView.setAdapter(subCatAdapter);

                                //TastyToast.makeText(getApplicationContext(),"Package Find",TastyToast.LENGTH_LONG,TastyToast.SUCCESS).show();
                            }else{
                                //TastyToast.makeText(getApplicationContext(),"Package Not Find",TastyToast.LENGTH_LONG,TastyToast.WARNING).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //TastyToast.makeText(getApplicationContext(),e.getMessage(),TastyToast.LENGTH_LONG,TastyToast.WARNING).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //TastyToast.makeText(getApplicationContext(),error.getMessage(),TastyToast.LENGTH_LONG,TastyToast.WARNING).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params=new HashMap<>();
                params.put("cat_id", cat_id);
                params.put("business_id", business_id);
                params.put("city_name", city_name);
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void getProducts(final String businessId, final String categoryId, final String scid) {
        //Log.d("PRODFUNCALL=",businessId);
        productList.clear();
        StringRequest stringRequest=new StringRequest(Request.Method.POST, ServerURLs.GET_PRODUCT_LIST_UTL,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray productlistarray=new JSONArray(response);
                            if(productlistarray.length()>0) {
                                for (int i = 0; i < productlistarray.length(); i++) {
                                    Log.d("PRODROWID=",String.valueOf(i));
                                    JSONObject productObj=productlistarray.getJSONObject(i);

                                    String pid=productObj.getString("id");
                                    String cat_id=productObj.getString("cat_id");
                                    String sub_cat_id=productObj.getString("sub_cat_id");
                                    String business_id=productObj.getString("business_id");
                                    String product_name=productObj.getString("product_nm");
                                    String cPName= Html.fromHtml(product_name).toString();
                                    String pdesc=productObj.getString("product_desc");
                                    String cPDesc= Html.fromHtml(pdesc).toString();
                                    String punit=productObj.getString("product_unit");
                                    double pprice=productObj.getDouble("product_price");
                                    double poffprice=productObj.getDouble("offer_price");
                                    String item_sgst=productObj.getString("item_sgst");
                                    String item_cgst=productObj.getString("item_cgst");
                                    String item_stock=productObj.getString("item_stock");
                                    String pimage=productObj.getString("product_img");
                                    String punitname=productObj.getString("product_unit_name");

                                    ProductModel products =new ProductModel(pid,cat_id,sub_cat_id,business_id,cPName,cPDesc,punit,item_sgst,item_cgst,pimage,punitname,item_stock,pprice,poffprice);
                                    productList.add(products);
                                    //Log.d("PRODUCT=",product_name);
                                }
                                RecyclerView productRecycler=findViewById(R.id.recycler_products);
                                productRecycler.setHasFixedSize(true);
                                productRecycler.setNestedScrollingEnabled(false);
                                productRecycler.setLayoutManager(new LinearLayoutManager(ProductList.this));

                                productAdapter=new ProductAdapter(ProductList.this,productList,mDataBase,getIntent().getStringExtra("business_name"));
                                productRecycler.setAdapter(productAdapter);
                                productAdapter.notifyDataSetChanged();

                            }else{
                                try {
                                    productList.clear();
                                    productAdapter.notifyDataSetChanged();
                                }catch (Exception e){

                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            TastyToast.makeText(getApplicationContext(),e.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params=new HashMap<>();
                params.put("business_id", businessId);
                params.put("cat_id", categoryId);
                params.put("sub_cat_id", scid);
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void  displayBottomCart(){
        cartQty=mDataBase.cartProductQty();
        if(cartQty>0) {
            cartAmt=mDataBase.cartProductTotalAmt();
            txtTotalCartItem.setText(cartQty+" Items");
            txtTotalCart.setText("₹ "+cartAmt);
            rlBottomCart.setVisibility(View.VISIBLE);
        }else{
            cartAmt=mDataBase.cartProductTotalAmt();
            txtTotalCartItem.setText(cartQty+" Items");
            txtTotalCart.setText("₹ "+cartAmt);
            rlBottomCart.setVisibility(View.GONE);
        }
        //TastyToast.makeText(getApplicationContext(),String.valueOf(cartQty)+" Items", TastyToast.LENGTH_LONG,TastyToast.WARNING).show();
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
