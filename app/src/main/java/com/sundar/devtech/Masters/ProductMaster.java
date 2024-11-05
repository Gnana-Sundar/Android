package com.sundar.devtech.Masters;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sundar.devtech.Adapter.ProductAdapter;
import com.sundar.devtech.DatabaseController.RequestURL;
import com.sundar.devtech.Internet.NetworkChangeListener;
import com.sundar.devtech.MainActivity;
import com.sundar.devtech.Models.ProductModel;
import com.sundar.devtech.R;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductMaster extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private ImageView BACK_PRESS,APPBAR_BTN;
    private TextView APPBAR_TITLE;
    private RecyclerView PROD_RECYCLER;
    private RecyclerView.LayoutManager PROD_MANAGER;
    private List<ProductModel> PROD;
    private ProductAdapter PROD_ADAPTER;
    private TextInputEditText SEARCHVIEW;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_master);

        APPBAR_BTN = findViewById(R.id.appbar_btn);
        APPBAR_TITLE = findViewById(R.id.appbarTitle);
        APPBAR_TITLE.setText("Product List");

        //back press activity
        BACK_PRESS = findViewById(R.id.backPress);
        APPBAR_BTN.setImageResource(R.drawable.add);
        BACK_PRESS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductMaster.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        PROD_RECYCLER = findViewById(R.id.product_recycler);
        PROD_MANAGER = new GridLayoutManager(ProductMaster.this, 1);
        PROD_RECYCLER.setLayoutManager(PROD_MANAGER);
        PROD = new ArrayList<>();

        /* filter from search bar start*/
        SEARCHVIEW = findViewById(R.id.searchView_prod);
        SEARCHVIEW.clearFocus();
        SEARCHVIEW.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                fileList(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) {
                    SEARCHVIEW.setCursorVisible(false);
                    // Close the keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(SEARCHVIEW.getWindowToken(), 0);
                } else {
                    // Enable the cursor pointer when there is text in the search view
                    SEARCHVIEW.setCursorVisible(true);
                }
            }

            private void fileList(String text) {

                List<ProductModel> filteredList = new ArrayList<>();
                for (ProductModel item : PROD) {
                    if (item.getProd_name().toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(item);
                    }
                }

                if (filteredList.isEmpty()) {
                    Toast.makeText(ProductMaster.this, "No data found", Toast.LENGTH_SHORT).show();
                } else {
                    PROD_ADAPTER.setFilteredList(filteredList);
                }
            }
        });
        /* filter from search bar End*/

        APPBAR_BTN.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingInflatedId")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductMaster.this,ProductUpdateActivity.class);
                intent.putExtra("prod_id", "");
                intent.putExtra("prod_name", "");
                intent.putExtra("prod_spec","");
                intent.putExtra("prod_desc","");
                intent.putExtra("prod_img", "");
                intent.putExtra("active", "1");
                intent.putExtra("update", "0");
                startActivity(intent);
            }
        });
    }

    public void select() {

        StringRequest request = new StringRequest(Request.Method.POST, RequestURL.prod_fetch,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            PROD.clear();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);

                                String PROD_ID = object.getString("prod_id");
                                String PROD_NAME = object.getString("prod_name").toUpperCase();
                                String PROD_SPEC = object.getString("prod_spec").toUpperCase();
                                String PROD_DESC = object.getString("prod_desc").toUpperCase();
                                String IMAGE = object.getString("image");
                                String ACTIVE = object.getString("active");

                                ProductModel productModel = new ProductModel(PROD_ID, PROD_NAME, PROD_SPEC, PROD_DESC, ACTIVE, IMAGE);
                                PROD.add(productModel);
                            }

                            PROD_ADAPTER = new ProductAdapter(ProductMaster.this, PROD);
                            PROD_RECYCLER.setAdapter(PROD_ADAPTER);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ProductMaster.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                    Toast.makeText(ProductMaster.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductMaster.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(ProductMaster.this);
        requestQueue.add(request);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter =new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,filter);
        select();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ProductMaster.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}