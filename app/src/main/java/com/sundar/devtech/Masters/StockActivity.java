package com.sundar.devtech.Masters;

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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.sundar.devtech.Adapter.StockAdapter;
import com.sundar.devtech.DatabaseController.RequestURL;
import com.sundar.devtech.Internet.NetworkChangeListener;
import com.sundar.devtech.Models.EmployeeModel;
import com.sundar.devtech.Models.ProductModel;
import com.sundar.devtech.Models.StockModel;
import com.sundar.devtech.R;
import com.sundar.devtech.Services.DateAndTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private ImageView BACK_PRESS,APPBAR_BTN;
    private TextView APPBAR_TITLE, SLOT_NO, PROD_NAME;
    private TextInputEditText QTY,MIN_QTY;
    private Spinner ACTIVE;
    private ArrayAdapter<String> active_adapter;
    private MaterialButton SAVE,CANCEL,FULL_LOAD_BTN;
    private RecyclerView PRODSTOCK_RECYCLER;
    private RecyclerView.LayoutManager PRODSTOCK_MANAGER;
    public List<StockModel> PRODSTOCK;
    private StockAdapter PRODSTOCK_ADAPTER;
    private TextInputEditText SEARCHVIEW;
    public static String LOGGED_USER = null;
    public static String user_role = null;
    private String PROD_ID;
    private Dialog DIALOG;
    private ArrayAdapter<String> adapter;
    public static List<String> motorList = new ArrayList<>();
    public static HashMap<String, String> prodlists = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        APPBAR_BTN = findViewById(R.id.appbar_btn);
        APPBAR_TITLE = findViewById(R.id.appbarTitle);
        APPBAR_TITLE.setText("Slot Management");

        //back press activity
        BACK_PRESS = findViewById(R.id.backPress);
        APPBAR_BTN.setImageResource(R.drawable.add);
        BACK_PRESS.setOnClickListener(view -> StockActivity.super.onBackPressed());

        PRODSTOCK_RECYCLER = findViewById(R.id.stock_recycler);
        PRODSTOCK_MANAGER = new GridLayoutManager(this, 1);
        PRODSTOCK_RECYCLER.setLayoutManager(PRODSTOCK_MANAGER);
        PRODSTOCK = new ArrayList<>();

        /* filter from search bar start*/
        SEARCHVIEW = findViewById(R.id.searchView_stock);
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

                List<StockModel> filteredList = new ArrayList<>();
                for (StockModel item : PRODSTOCK) {
                    if (item.getSlot_no().contains(text.toLowerCase()) ||
                            item.getProd_name().toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(item);
                    }
                }

                if (filteredList.isEmpty()) {
                    Toast.makeText(StockActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                } else {
                    PRODSTOCK_ADAPTER.setFilteredList(filteredList);
                }
            }
        });

        /* filter from search bar End*/
        APPBAR_BTN.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingInflatedId")
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(StockActivity.this, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(StockActivity.this).inflate(R.layout.stock_prod_dialog,
                        (LinearLayout)findViewById(R.id.stock_prod_dialog));
                builder.setView(view);

                final AlertDialog alertDialog = builder.create();

                ((TextView) view.findViewById(R.id.dialog_title)).setText("Add New Slot");

                SLOT_NO = view.findViewById(R.id.stock_slot_no);
                PROD_NAME = view.findViewById(R.id.stock_prodName);
                QTY = view.findViewById(R.id.stock_prod_qty);
                MIN_QTY = view.findViewById(R.id.stock_min_qty);
                ACTIVE = view.findViewById(R.id.stock_prod_active);

                FULL_LOAD_BTN = view.findViewById(R.id.full_load_btn);
                SAVE = view.findViewById(R.id.stock_prod_insert_btn);
                CANCEL = view.findViewById(R.id.stock_prod_cancel_btn);

                if (!user_role.equals("1")) {
                    SLOT_NO.setEnabled(false);
                    PROD_NAME.setEnabled(false);
                    MIN_QTY.setEnabled(false);
                }

                active_adapter = new ArrayAdapter<>(StockActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.active));
                active_adapter.setDropDownViewResource(R.layout.item_drop_down);
                ACTIVE.setAdapter(active_adapter);

                SLOT_NO.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DIALOG = new Dialog(StockActivity.this);
                        DIALOG.setContentView(R.layout.dialog_search_spinner);

                        // Set dialog width to match parent
                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                        layoutParams.copyFrom(DIALOG.getWindow().getAttributes());
                        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                        layoutParams.height = 1000;
                        DIALOG.getWindow().setAttributes(layoutParams);

                        DIALOG.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        DIALOG.show();

                        TextView Tittle = DIALOG.findViewById(R.id.dialog_spinner_title);
                        Tittle.setText("Select Slot No");
                        EditText editText = DIALOG.findViewById(R.id.spinner_search);
                        ListView listView = DIALOG.findViewById(R.id.spinner_list);

                        // Initialize the list outside the loop
                        adapter = new ArrayAdapter<>(StockActivity.this, android.R.layout.simple_list_item_1, motorList);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                adapter.getFilter().filter(s);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {}
                        });

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String selectedSlot = adapter.getItem(position);

                                SLOT_NO.setText(selectedSlot);

                                DIALOG.dismiss();
                            }
                        });
                    }
                });
                PROD_NAME.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DIALOG = new Dialog(StockActivity.this);
                        DIALOG.setContentView(R.layout.dialog_search_spinner);

                        // Set dialog width to match parent
                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                        layoutParams.copyFrom(DIALOG.getWindow().getAttributes());
                        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                        layoutParams.height = 1000;
                        DIALOG.getWindow().setAttributes(layoutParams);

                        DIALOG.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        DIALOG.show();

                        TextView Tittle = DIALOG.findViewById(R.id.dialog_spinner_title);
                        Tittle.setText("Select Product");
                        EditText editText = DIALOG.findViewById(R.id.spinner_search);
                        ListView listView = DIALOG.findViewById(R.id.spinner_list);

                        List<String> product = new ArrayList<>(prodlists.values());
                        adapter = new ArrayAdapter<>(StockActivity.this, android.R.layout.simple_list_item_1, product);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                adapter.getFilter().filter(s);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {}
                        });

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                String selectedProdName = adapter.getItem(position);

                                String selectedProdId = null;

                                for (Map.Entry<String, String> entry : prodlists.entrySet()) {
                                    if (entry.getValue().equals(selectedProdName)) {
                                        selectedProdId = entry.getKey();
                                        break;
                                    }
                                }

                                if (selectedProdId != null) {
                                    PROD_NAME.setText(selectedProdName);
                                    PROD_ID = selectedProdId;
                                }

                                DIALOG.dismiss();
                            }
                        });
                    }
                });

                FULL_LOAD_BTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QTY.setText("11");
                    }
                });

                QTY.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s != null && s.length() > 1) {
                            try {
                                int input = Integer.parseInt(s.toString());

                                // Validate if the number is between 00 and 11
                                if (input < 1 || input > 11) {
                                    QTY.setError("Please enter a number between 01 and 11");
                                } else if (s.length() < 2) {
                                    QTY.setError("Enter a 2-digit number (e.g.,01,02,03)");
                                } else {
                                    QTY.setError(null); // Clear any previous error
                                }
                            } catch (NumberFormatException e) {
                                QTY.setError("Invalid input");
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                SAVE.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        insert();
                    }
                });

                CANCEL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View V) {
                        alertDialog.dismiss();
                    }
                });

                if (alertDialog.getWindow() != null) {
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                alertDialog.show();
            }
        });
    }

    public void select() {

        StringRequest request = new StringRequest(Request.Method.POST, RequestURL.prod_stock_fetch,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            PRODSTOCK.clear();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);

                                String SLOT_NO = object.getString("slot_no");
                                String PROD_ID = object.getString("prod_id");
                                String PROD_NAME = object.getString("prod_name").toUpperCase();
                                String QTY = object.getString("qty");
                                String MIN_QTY = object.getString("min_qty");
                                String ACTIVE = object.getString("active");

                                StockModel stockModel = new StockModel(SLOT_NO,PROD_ID,PROD_NAME,QTY,MIN_QTY,ACTIVE);
                                PRODSTOCK.add(stockModel);
                            }
                            PRODSTOCK_ADAPTER = new StockAdapter(StockActivity.this, PRODSTOCK);
                            PRODSTOCK_RECYCLER.setAdapter(PRODSTOCK_ADAPTER);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(StockActivity.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                    Toast.makeText(StockActivity.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StockActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(StockActivity.this);
        requestQueue.add(request);
    }

    public void insert() {
        String slot_no = SLOT_NO.getText().toString().trim().replace("Slot No - ","");
        String prod_id = PROD_ID;
        String prod_name = PROD_NAME.getText().toString().trim();
        String qty = QTY.getText().toString().trim();
        String min_qty = MIN_QTY.getText().toString().trim();
        String active = ACTIVE.getSelectedItem().toString().trim().equals("ENABLE") ? "1" : "2";

        if (slot_no.equals("")){
            Toast.makeText(StockActivity.this, "Slot No is Empty", Toast.LENGTH_SHORT).show();
        }else if (prod_name.equals("")){
            Toast.makeText(StockActivity.this, "Product Name is Empty", Toast.LENGTH_SHORT).show();
        }else if (qty.equals("") || qty.equals("0")){
            Toast.makeText(StockActivity.this, "Qty is Empty", Toast.LENGTH_SHORT).show();
        }else if (min_qty.equals("")){
            Toast.makeText(StockActivity.this, "Minimum Qty is Empty", Toast.LENGTH_SHORT).show();
        }else {
            StringRequest request = new StringRequest(Request.Method.POST, RequestURL.prod_stock_insert,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.equalsIgnoreCase("success")) {
                                Toast.makeText(StockActivity.this, "Inserted Successfully", Toast.LENGTH_SHORT).show();
                                PROD_NAME.setText("");
                                SLOT_NO.setText("");
                                QTY.setText("");
                                MIN_QTY.setText("");
                                SLOT_NO.requestFocus();

                                select();
                            } else {
                                Toast.makeText(StockActivity.this, response, Toast.LENGTH_SHORT).show();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                        Toast.makeText(StockActivity.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(StockActivity.this, "ENetwork error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("slot_no", slot_no);
                    params.put("prod_id", prod_id);
                    params.put("qty", qty);
                    params.put("min_qty", min_qty);
                    params.put("active", active);
                    params.put("user", LOGGED_USER);
                    params.put("date", DateAndTime.getDeviceDate());
                    params.put("time", DateAndTime.getDeviceTime());
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(StockActivity.this);
            requestQueue.add(request);
        }
    }

    public void fetchMotor() {
        StringRequest request = new StringRequest(Request.Method.POST, RequestURL.motorNo_fetch,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        motorList.clear();
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                String slot_no = object.getString("slot_no");

                                motorList.add("Slot No - "+slot_no);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(StockActivity.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                    Toast.makeText(StockActivity.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StockActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(StockActivity.this);
        requestQueue.add(request);
    }
    public void fetchProduct(){
        StringRequest request = new StringRequest(Request.Method.POST, RequestURL.prod_fetch,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        prodlists.clear();
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);

                                String prod_id = object.getString("prod_id");
                                String prod_name = object.getString("prod_name").toUpperCase();
                                String active = object.getString("active");

                                if (active.equals("1")) {
                                    prodlists.put(prod_id, prod_name);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(StockActivity.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                    Toast.makeText(StockActivity.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StockActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(StockActivity.this);
        requestQueue.add(request);
    }
    public void fetchLoggedUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("adminUser", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);
        user_role = sharedPreferences.getString("user_role", null);
        if (userId != null) {
            LOGGED_USER = userId;
        }else {
            LOGGED_USER = "admin";
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter =new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,filter);
        select();
        fetchMotor();
        fetchProduct();
        fetchLoggedUser();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeListener);
    }
}