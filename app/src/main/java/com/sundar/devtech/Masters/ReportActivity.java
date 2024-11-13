package com.sundar.devtech.Masters;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.sundar.devtech.Adapter.ReportAdapter;
import com.sundar.devtech.DatabaseController.RequestURL;
import com.sundar.devtech.Internet.NetworkChangeListener;
import com.sundar.devtech.Models.ReportModel;
import com.sundar.devtech.R;
import com.sundar.devtech.Services.CreateExcelCsv;
import com.sundar.devtech.Services.CustomAlertDialog;
import com.sundar.devtech.Services.DateAndTime;
import com.sundar.devtech.Services.SendMail;
import com.sundar.devtech.SplashScreen;
import com.sundar.devtech.WeeklyReport.scheduleWeeklyEmail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;

public class ReportActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private ImageView BACK_PRESS,APPBAR_BTN;
    private TextView APPBAR_TITLE,FROM_DATE,TO_DATE;
    private MaterialButton PROCESS;
    private AppCompatButton CREATE_PDF,SEND_MAIL;
    private RecyclerView REPORT_RECYCLER;
    private RecyclerView.LayoutManager REPORT_MANAGER;
    private List<ReportModel> REPORT;
    private ReportAdapter REPORT_ADAPTER;
    private Calendar CALENDAR;
    String[] PERMISSION = {READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE};
    private ActivityResultLauncher<Intent> activityResultLauncher;
    Handler handler = new Handler();
    private int count = 0;
    private int totalEmails = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        APPBAR_BTN = findViewById(R.id.appbar_btn);
        APPBAR_TITLE = findViewById(R.id.appbarTitle);
        APPBAR_TITLE.setText("ReportActivity");

        //back press activity
        BACK_PRESS = findViewById(R.id.backPress);
        APPBAR_BTN.setVisibility(View.GONE);
        BACK_PRESS.setOnClickListener(view -> ReportActivity.super.onBackPressed());

        FROM_DATE = findViewById(R.id.rep_from_date);
        TO_DATE = findViewById(R.id.rep_to_date);
        PROCESS = findViewById(R.id.process);
        CALENDAR = Calendar.getInstance();

        CREATE_PDF = findViewById(R.id.create_excel);
        SEND_MAIL = findViewById(R.id.sendmail);

        REPORT_RECYCLER = findViewById(R.id.report_recycler);
        REPORT_MANAGER = new GridLayoutManager(ReportActivity.this, 1);
        REPORT_RECYCLER.setLayoutManager(REPORT_MANAGER);
        REPORT = new ArrayList<>();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                CALENDAR.set(Calendar.YEAR,year);
                CALENDAR.set(Calendar.MONTH,month);
                CALENDAR.set(Calendar.DAY_OF_MONTH,day);
                SimpleDateFormat date = DateAndTime.getDate(CALENDAR);
                FROM_DATE.setText(date.format(CALENDAR.getTime()));
            }
        };

        DatePickerDialog.OnDateSetListener toDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                CALENDAR.set(Calendar.YEAR,year);
                CALENDAR.set(Calendar.MONTH,month);
                CALENDAR.set(Calendar.DAY_OF_MONTH,day);
                SimpleDateFormat date = DateAndTime.getDate(CALENDAR);
                TO_DATE.setText(date.format(CALENDAR.getTime()));
            }
        };

        FROM_DATE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ReportActivity.this, date, CALENDAR.get(Calendar.YEAR),
                        CALENDAR.get(Calendar.MONTH),
                        CALENDAR.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        TO_DATE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ReportActivity.this, toDate, CALENDAR.get(Calendar.YEAR),
                        CALENDAR.get(Calendar.MONTH),
                        CALENDAR.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        PROCESS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String from_date = FROM_DATE.getText().toString().trim();
                String to_date = TO_DATE.getText().toString().trim();

                if (from_date.isEmpty()){
                    Toast.makeText(ReportActivity.this, "From Date Empty", Toast.LENGTH_SHORT).show();
                }else if (to_date.isEmpty()){
                    Toast.makeText(ReportActivity.this, "To Date Empty", Toast.LENGTH_SHORT).show();
                }else {
                    select();
                }
            }
        });

        SEND_MAIL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    String from_date = FROM_DATE.getText().toString().trim();
                    String to_date = TO_DATE.getText().toString().trim();
                    if (from_date.isEmpty()){
                        Toast.makeText(ReportActivity.this, "From Date Empty", Toast.LENGTH_SHORT).show();
                    }else if (to_date.isEmpty()){
                        Toast.makeText(ReportActivity.this, "To Date Empty", Toast.LENGTH_SHORT).show();
                    }else  if (REPORT.isEmpty()){
                        Toast.makeText(ReportActivity.this, "Data is Empty", Toast.LENGTH_SHORT).show();
                    }else {
                        saveReports(1);
                    }
                } else {
                    requestPermission();
                }
            }
        });
        CREATE_PDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    String from_date = FROM_DATE.getText().toString().trim();
                    String to_date = TO_DATE.getText().toString().trim();
                    if (from_date.isEmpty()){
                        Toast.makeText(ReportActivity.this, "From Date Empty", Toast.LENGTH_SHORT).show();
                    }else if (to_date.isEmpty()){
                        Toast.makeText(ReportActivity.this, "To Date Empty", Toast.LENGTH_SHORT).show();
                    }else  if (REPORT.isEmpty()){
                        Toast.makeText(ReportActivity.this, "Data is Empty", Toast.LENGTH_SHORT).show();
                    }else {
                        saveReports(0);
                    }
                } else {
                    requestPermission();
                }
            }
        });
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        Toast.makeText(ReportActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ReportActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void saveReports(int number) {
        if (number == 1){
            CreateExcelCsv.saveExcel(this, REPORT, "pentatvm_report.xlsx", 1);
            List<String> EMAIL_NAME = SplashScreen.EMAIL_NAME;

            totalEmails = EMAIL_NAME.size();
            count = 0;

            for (String email: EMAIL_NAME) {
               sendEmailInBackground(email.toString(), "Welcome to PENTA-TVM", "Report Date : "+ DateAndTime.getDate());
            }

        }else {
            CustomAlertDialog alertDialog = new CustomAlertDialog(ReportActivity.this);
            AlertDialog progressDialog = alertDialog.pleaseWaitDialog();
            progressDialog.show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String response = CreateExcelCsv.saveExcel(ReportActivity.this, REPORT, "pentatvm_report.xlsx", 1);
                    if (response.equals("1")) {
                        progressDialog.dismiss();
                        Toast.makeText(ReportActivity.this, "File saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ReportActivity.this, "Failed to save Excel", Toast.LENGTH_SHORT).show();
                    }
                }
            },2000);

        }


    }

    // THIS PERMISSION FOR ANDROID 11 AND ABOVE AND BELOW STORAGE PERMISSION
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int readCheck = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
            int writeCheck = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
            return readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                activityResultLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activityResultLauncher.launch(intent);
            }
        } else {
            ActivityCompat.requestPermissions(ReportActivity.this, PERMISSION, 30);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 30) {
            if (grantResults.length > 0) {
                boolean readPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean writePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (readPermission && writePermission) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void select() {
        String from_date = FROM_DATE.getText().toString().trim();
        String to_date = TO_DATE.getText().toString().trim();
        StringRequest request = new StringRequest(Request.Method.POST, RequestURL.reports,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            REPORT.clear();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);

                                String EMP_ID = object.getString("emp_id");
                                String EMP_NAME = object.getString("emp_name");
                                String PROD_NAME = object.getString("prod_name");
                                String QTY = object.getString("qty");
                                String DATE = object.getString("crea_date");
                                String TIME = object.getString("crea_time");

                                ReportModel reportModel = new ReportModel(EMP_ID, EMP_NAME, PROD_NAME, QTY, DATE, TIME);
                                REPORT.add(reportModel);
                            }

                            REPORT_ADAPTER = new ReportAdapter(ReportActivity.this, REPORT);
                            REPORT_RECYCLER.setAdapter(REPORT_ADAPTER);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ReportActivity.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                    Toast.makeText(ReportActivity.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ReportActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("from_date", from_date);
                params.put("to_date", to_date);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(ReportActivity.this);
        requestQueue.add(request);
    }
    private void sendEmailInBackground(String recipientEmail, String subject, String bodyText) {
        CustomAlertDialog alertDialog = new CustomAlertDialog(ReportActivity.this);
        AlertDialog progressDialog = alertDialog.pleaseWaitDialog();
        progressDialog.show();

        // Use an ExecutorService to run the email sending on a background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            SendMail mailSender = new SendMail();
            try {
                mailSender.sendEmailWithAttachment(recipientEmail, subject, bodyText, 1);

                // Update UI on the main thread after success
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    count++;
                    if (count == totalEmails) {
                        Toast.makeText(ReportActivity.this, "Email Send Success", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MessagingException e) {
                e.printStackTrace();

                // Update UI on the main thread after failure
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    count++;
                    if (count == totalEmails) {
                        Toast.makeText(ReportActivity.this, "Email Send Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter =new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,filter);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeListener);
    }

}