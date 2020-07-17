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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.foddez.service.Adapter.HomeCategoryAdapter;
import com.foddez.service.Adapter.ViewpagerAdapter;
import com.foddez.service.Model.CategoryModel;
import com.foddez.service.Model.SliderUtils;
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
import java.util.Timer;

public class Home extends AppCompatActivity {
    private Toolbar mTopToolbar;
    Dialog popupDialig;
    Button closeImage;
    private TextView toolbarText;
    private ProgressDialog progressDialog;
    private ImageView imgFruits, imgVegetables, imgPickup, imgMeet;
    private RelativeLayout rlChangeAddress;
    //
    RecyclerView catTopRecyclerView;
    RecyclerView.LayoutManager catTopLayoutManager;
    List<CategoryModel> categoryModelList;
    HomeCategoryAdapter homeCategoryAdapter;

    private ViewPager viewPager;
    List<SliderUtils> sliderImg;
    ViewpagerAdapter viewpagerAdapter;
    RequestQueue rq;
    private String at="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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
            mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mTopToolbar);
            toolbarText=findViewById(R.id.toolbar_area);
            /*at=SharedPrefManager.getInstance(this).getAddressType();
            if(at.equals("") || at.equals(null)){
                toolbarText.setText(SharedPrefManager.getInstance(this).getAreaName());
            }else{*/
            toolbarText.setText(SharedPrefManager.getInstance(this).getAreaName());
            //}
            //TastyToast.makeText(getApplicationContext(),SharedPrefManager.getInstance(this).getAddressType(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
            progressDialog = new ProgressDialog(this);
            rlChangeAddress=findViewById(R.id.rl_change_address);
            rlChangeAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gotoppage=new Intent(Home.this,HomeChangeLocation.class);
                    gotoppage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(gotoppage);
                }
            });
            imgFruits=findViewById(R.id.img_fruit);
            imgFruits.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gotoppage=new Intent(Home.this,BusinessList.class);
                    gotoppage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    gotoppage.putExtra("cat_id","3");
                    gotoppage.putExtra("cat_nm","Fruits and Vegetables");
                    startActivity(gotoppage);
                }
            });
            imgVegetables=findViewById(R.id.img_grocery);
            imgVegetables.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gotoppage=new Intent(Home.this,BusinessList.class);
                    gotoppage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    gotoppage.putExtra("cat_id","2");
                    gotoppage.putExtra("cat_nm","Grocery");
                    startActivity(gotoppage);
                }
            });
            imgPickup=findViewById(R.id.img_pickup);
            /*imgPickup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gotoppage=new Intent(Home.this,BusinessList.class);
                    gotoppage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    gotoppage.putExtra("cat_id","");
                    gotoppage.putExtra("cat_nm","");
                    startActivity(gotoppage);
                }
            });*/
            imgMeet=findViewById(R.id.img_meat);
            imgMeet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gotoppage=new Intent(Home.this,BusinessList.class);
                    gotoppage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    gotoppage.putExtra("cat_id","4");
                    gotoppage.putExtra("cat_nm","Meat and Fish");
                    startActivity(gotoppage);
                }
            });
            // Category First
            categoryModelList = new ArrayList<>();
            catTopRecyclerView = findViewById(R.id.home_category_recycler);
            catTopRecyclerView.setHasFixedSize(true);
            catTopLayoutManager = new GridLayoutManager(this, 4);
            catTopRecyclerView.setLayoutManager(catTopLayoutManager);
            loadTopCategories();

            //GET Slider IMAGE
            rq = Volley.newRequestQueue(this);
            sliderImg = new ArrayList<>();
            viewPager = findViewById(R.id.view_pager_main);
            getSlider();


        }
    }

    private void getSlider() {
        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(Request.Method.GET, ServerURLs.SLIDER_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for(int i=0; i< response.length(); i++){
                            SliderUtils sliderUtils=new SliderUtils();
                            try {
                                JSONObject jsonObject=response.getJSONObject(i);
                                sliderUtils.setSliderImageUrl(jsonObject.getString("cat_image"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            sliderImg.add(sliderUtils);
                        }
                        viewpagerAdapter=new ViewpagerAdapter(sliderImg,Home.this);
                        viewPager.setAdapter(viewpagerAdapter);
                        viewPager.setClipToPadding(false);
                        viewPager.setClipChildren(false);
                        viewPager.setOffscreenPageLimit(3);
                        viewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

                        //CompositePageTransformer
                        /*Timer timer=new Timer();
                        timer.scheduleAtFixedRate(new MyTimeTask(),4000,4000);*/
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TastyToast.makeText(getApplicationContext(),error.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<String, String>();
                headers.put("Content-Type","application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    private void loadTopCategories() {
        progressDialog.setMessage("loading...");
        progressDialog.show();
        categoryModelList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerURLs.HOME_CATEGORY_URL,
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

                                int id = categoryObject.getInt("cat_id");
                                String cat_name = categoryObject.getString("cat_nm");
                                String cat_image = categoryObject.getString("cat_image");

                                CategoryModel categories = new CategoryModel(id, cat_name, cat_image);
                                categoryModelList.add(categories);
                            }

                            homeCategoryAdapter = new HomeCategoryAdapter(Home.this, categoryModelList);
                            homeCategoryAdapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                            catTopRecyclerView.setAdapter(homeCategoryAdapter);
                            homeCategoryAdapter.setOnItemClickListener(new HomeCategoryAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    Intent gotoppage=new Intent(Home.this,BusinessList.class);
                                    gotoppage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    CategoryModel clickedItemCategory=categoryModelList.get(position);
                                    //TastyToast.makeText(Home.this,"Category Error: "+clickedItemCategory.getCategory_name(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
                                    gotoppage.putExtra("cat_id",String.valueOf(clickedItemCategory.getCid()));
                                    gotoppage.putExtra("cat_nm",clickedItemCategory.getCategory_name());
                                    startActivity(gotoppage);
                                }
                            });

                        } catch (JSONException e) {
                            progressDialog.dismiss();

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                //TastyToast.makeText(Home.this,"Category Error: "+error.getMessage(),TastyToast.LENGTH_LONG,TastyToast.ERROR).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params=new HashMap<>();
                params.put("cat_id", "all");
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_user) {
            startActivity(new Intent(getApplicationContext(), UserProfile.class));
            return true;
        }else if(id == R.id.action_cart){
            //
            return true;
        }

        return super.onOptionsItemSelected(item);
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
