package com.sundar.devtech;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
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
import com.google.android.material.textfield.TextInputEditText;
import com.sundar.devtech.DatabaseController.RequestURL;
import com.sundar.devtech.Internet.NetworkChangeListener;
import com.sundar.devtech.Masters.MotorMaster;
import com.sundar.devtech.Masters.ReportActivity;
import com.sundar.devtech.Models.MotorModel;
import com.sundar.devtech.Models.ProductModel;
import com.sundar.devtech.Models.SalesModel;
import com.sundar.devtech.Services.ActivityMoving;
import com.sundar.devtech.Services.CustomAlertDialog;
import com.sundar.devtech.Services.MotorService;
import com.sundar.devtech.Services.QtyAlertMail;
import com.sundar.devtech.WeeklyReport.scheduleWeeklyEmail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SlotDetailActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private ImageView BACK_PRESS,APPBAR_BTN;
    private TextView APPBAR_TITLE,LOGGED_USER;
    private TextInputEditText SLOT_NUMBERS;
    private String OLD_SLOT_NUMBER,EMP_ID;
    private MaterialButton NUM0,NUM1,NUM2,NUM3,NUM4,NUM5,NUM6,NUM7,NUM8,NUM9;
    private AppCompatButton CLEAR,CANCEL,CONFIRM;
    private MotorService motorService;
    public static ArrayList<SalesModel> salesModels = new ArrayList<>();
    CustomAlertDialog alertDialog;

    QtyAlertMail qtyAlertMail = new QtyAlertMail(SlotDetailActivity.this);
    private CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_slot_detail);

        motorService = new MotorService(SlotDetailActivity.this);
        scheduleWeeklyEmail.scheduleWeeklyEmail(SlotDetailActivity.this);

        APPBAR_BTN = findViewById(R.id.appbar_btn);
        APPBAR_TITLE = findViewById(R.id.appbarTitle);
        SLOT_NUMBERS = findViewById(R.id.slotNumber);
        APPBAR_TITLE.setText(getApplicationContext().getString(R.string.app_name));

        //back press activity
        BACK_PRESS = findViewById(R.id.backPress);
        BACK_PRESS.setVisibility(View.GONE);
        APPBAR_BTN.setVisibility(View.GONE);
        LOGGED_USER = findViewById(R.id.logged_user);

        NUM0 = findViewById(R.id.btn_0);
        NUM1 = findViewById(R.id.btn_1);
        NUM2 = findViewById(R.id.btn_2);
        NUM3 = findViewById(R.id.btn_3);
        NUM4 = findViewById(R.id.btn_4);
        NUM5 = findViewById(R.id.btn_5);
        NUM6 = findViewById(R.id.btn_6);
        NUM7 = findViewById(R.id.btn_7);
        NUM8 = findViewById(R.id.btn_8);
        NUM9 = findViewById(R.id.btn_9);
        CLEAR = findViewById(R.id.clear);
        CANCEL = findViewById(R.id.cancel);
        CONFIRM = findViewById(R.id.confirm);

        SLOT_NUMBERS.setText("");

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String employeeId = sharedPreferences.getString("EMPLOYEE_ID", null);

        if (employeeId != null) {
            EMP_ID = employeeId;
           LOGGED_USER.setText("Logged in Staff id - "+employeeId);
        }


        ArrayList<MaterialButton> numbers = new ArrayList<>();
        numbers.add(NUM0);
        numbers.add(NUM1);
        numbers.add(NUM2);
        numbers.add(NUM3);
        numbers.add(NUM4);
        numbers.add(NUM5);
        numbers.add(NUM6);
        numbers.add(NUM7);
        numbers.add(NUM8);
        numbers.add(NUM9);

        for (MaterialButton b : numbers) {
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String currentText = SLOT_NUMBERS.getText().toString();

                    if (currentText.length() < 2) {
                        if (currentText.isEmpty()) {
                            SLOT_NUMBERS.setText(b.getText().toString());
                        } else {
                            SLOT_NUMBERS.setText(currentText + b.getText().toString());
                        }
                        OLD_SLOT_NUMBER = SLOT_NUMBERS.getText().toString();
                    } else {
//                        Toast.makeText(SlotDetailActivity.this, "Maximum Allow Two Digits", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        CLEAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SLOT_NUMBERS.setText("");
            }
        });

        CANCEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                ActivityMoving.ActivityMoving(SlotDetailActivity.this,ScannerActivity.class);
            }
        });

        CONFIRM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String slot_number = SLOT_NUMBERS.getText().toString();
                if (slot_number.isEmpty()){
                    Toast.makeText(SlotDetailActivity.this, "Please Enter The Slot Number", Toast.LENGTH_SHORT).show();
                }else if (slot_number.length() < 2){
                    Toast.makeText(SlotDetailActivity.this, "Please Enter The Correct Slot Number", Toast.LENGTH_SHORT).show();
                }else {
                    productChecking();
                }
            }
        });
    }

    private void startTimer() {
        // Initialize the countdown timer for 1 minute
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }
            @Override
            public void onFinish() {
                ActivityMoving.ActivityMoving(SlotDetailActivity.this, ScannerActivity.class);
            }
        }.start();
    }
    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Cancel the timer
            countDownTimer = null;
        }
    }
    public void productChecking() {
        String slot_no = SLOT_NUMBERS.getText().toString();

        // Show progress dialog
        alertDialog = new CustomAlertDialog(this);
        AlertDialog progressDialog = alertDialog.pleaseWaitDialog();
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, RequestURL.product,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            if (jsonResponse.has("message")) {
                                errorDialog(jsonResponse.getString("message"));
                            } else if (jsonResponse.has("products")) {

                                JSONArray jsonArray = jsonResponse.getJSONArray("products");
                                salesModels.clear();

                                String run_hex = null;
                                String status_hex = null;

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);

                                    String MOTOR_ID = object.getString("motor_id");
                                    String PROD_ID = object.getString("prod_id");
                                    String PROD_NAME = object.getString("prod_name");
                                    String PROD_SPEC = object.getString("prod_spec");
                                    String PROD_DESC = object.getString("prod_desc");
                                    String IMAGE = object.getString("image");
                                    String RUN_HEX = object.getString("run_hex");
                                    String STATUS_HEX = object.getString("status_hex");

                                    run_hex = RUN_HEX;
                                    status_hex =STATUS_HEX;

                                    salesModels.add(new SalesModel(MOTOR_ID, PROD_ID, PROD_NAME, PROD_SPEC, PROD_DESC, IMAGE));
                                }

                                if (!salesModels.isEmpty()) {
                                    stopTimer();
                                    Intent intent = new Intent(SlotDetailActivity.this, ConfirmActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("emp_id", EMP_ID);
                                    intent.putExtra("run_hex",run_hex);
                                    intent.putExtra("status_hex",status_hex);
                                    startActivity(intent);
                                    qtyAlertMail.sendAlertMail(slot_no);
                                } else {
                                    errorDialog("No products available.");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            serverErrorDialog();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        serverErrorDialog();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("slot_no", slot_no);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
    public void errorDialog(String message){
        AlertDialog.Builder okDialog = alertDialog.okDialog(message);
        okDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
    public void serverErrorDialog(){
        AlertDialog.Builder okDialog = alertDialog.okDialog("Server Error. Please Contact Administrator");
        okDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityMoving.ActivityMoving(SlotDetailActivity.this,ScannerActivity.class);
            }
        }).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter =new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,filter);
        qtyAlertMail.select();
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeListener);
        stopTimer();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityMoving.ActivityMoving(SlotDetailActivity.this,ScannerActivity.class);
    }
}