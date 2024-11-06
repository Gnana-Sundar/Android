package com.sundar.devtech;

import static com.sundar.devtech.Services.ActivityMoving.ActivityMoving;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
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
import com.sundar.devtech.DatabaseController.RequestURL;
import com.sundar.devtech.Interfaces.MotorCommandCallback;
import com.sundar.devtech.Internet.NetworkChangeListener;
import com.sundar.devtech.Masters.MotorMaster;
import com.sundar.devtech.Models.ProductModel;
import com.sundar.devtech.Models.SalesModel;
import com.sundar.devtech.R;
import com.sundar.devtech.Services.ActivityMoving;
import com.sundar.devtech.Services.ByteConvertor;
import com.sundar.devtech.Services.CustomAlertDialog;
import com.sundar.devtech.Services.MotorService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfirmActivity extends AppCompatActivity {

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private ImageView BACK_PRESS,APPBAR_BTN;
    private TextView APPBAR_TITLE;
    private MaterialButton BUY,CANCEL;
    private CustomAlertDialog customAlertDialog;
    private ImageView PROD_IMAGE;
    private TextView PROD_NAME,PROD_DESC,PROD_SPEC;
    private String motor_id,prod_id,emp_id,run_hex,status_hex;
    private MotorService motorService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        customAlertDialog = new CustomAlertDialog(ConfirmActivity.this);
        motorService = new MotorService(ConfirmActivity.this);

        APPBAR_BTN = findViewById(R.id.appbar_btn);
        APPBAR_TITLE = findViewById(R.id.appbarTitle);
        APPBAR_TITLE.setText(getApplicationContext().getString(R.string.app_name));

        //back press activity
        BACK_PRESS = findViewById(R.id.backPress);
        APPBAR_BTN.setVisibility(View.GONE);
        BACK_PRESS.setVisibility(View.GONE);

        PROD_IMAGE = findViewById(R.id.product_img);
        PROD_NAME = findViewById(R.id.product_name);
        PROD_DESC = findViewById(R.id.description);
        PROD_SPEC = findViewById(R.id.specification);

        BUY = findViewById(R.id.buy);
        CANCEL = findViewById(R.id.cancel);

        BUY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = customAlertDialog.confirmDialog();

                builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runCommand();
                    }
                });

                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        CANCEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = customAlertDialog.cancelDialog();

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityMoving.ActivityMoving(ConfirmActivity.this,ScannerActivity.class);
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });
    }

    public void fetchProduct(){
        ArrayList<SalesModel> salesModels = SlotDetailActivity.salesModels;

        for (SalesModel salesModel:salesModels){
            motor_id = salesModel.getMotor_id();
            Bitmap bitmap = decodeBase64(salesModel.getProd_image());
            PROD_IMAGE.setImageBitmap(bitmap);
            PROD_NAME.setText(salesModel.getProd_name().toUpperCase());
            PROD_DESC.setText(salesModel.getProd_desc().toUpperCase());
            PROD_SPEC.setText(salesModel.getProd_spec().toUpperCase());
            prod_id = salesModel.getProd_id();
        }

        Intent intent = getIntent();

        emp_id = intent.getStringExtra("emp_id");
        run_hex = intent.getStringExtra("run_hex");
        status_hex = intent.getStringExtra("status_hex");
    }

    private Bitmap decodeBase64(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void runCommand() {
        CustomAlertDialog alertDialog = new CustomAlertDialog(ConfirmActivity.this);
        AlertDialog progressDialog = alertDialog.pleaseWaitDialog();
        progressDialog.show();

        motorService.MotorOn(run_hex, status_hex, new MotorCommandCallback() {
            @Override
            public void onStatusCommandResult(String response) {

                String[] arr = response.split(" ");

                if (arr != null && arr.length >= 5) {
                    if (arr[2].equals("02") && arr[4].equals("00")) {
                        insert(progressDialog);
                    } else {
                        showErrorDialog(progressDialog, alertDialog);
                    }
                } else {
                    showErrorDialog(progressDialog, alertDialog);
                }
            }
        });
    }

    private void showErrorDialog(AlertDialog progressDialog, CustomAlertDialog alertDialog) {
        progressDialog.dismiss();
        AlertDialog.Builder okDialog = alertDialog.okDialog("Server Error. Please Contact Administrator");
        okDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityMoving.ActivityMoving(ConfirmActivity.this, ScannerActivity.class);
            }
        }).show();
    }


    public void insert(AlertDialog alertDialog) {
        StringRequest request = new StringRequest(Request.Method.POST, RequestURL.sales_insert,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase("success")){
                            alertDialog.dismiss();
                            ActivityMoving.ActivityMoving(ConfirmActivity.this,SuccessActivity.class);
                            Toast.makeText(ConfirmActivity.this, "Buying Success", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            alertDialog.dismiss();
                            Toast.makeText(ConfirmActivity.this, "Inserted Failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                    alertDialog.dismiss();
                    Toast.makeText(ConfirmActivity.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                } else {
                    alertDialog.dismiss();
                    Toast.makeText(ConfirmActivity.this, "ENetwork error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("motor_id", motor_id);
                params.put("prod_id", prod_id);
                params.put("emp_id",emp_id);
                params.put("qty", "1");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ConfirmActivity.this);
        requestQueue.add(request);
    }



    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter =new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,filter);
        fetchProduct();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityMoving.ActivityMoving(ConfirmActivity.this,ScannerActivity.class);
    }
}