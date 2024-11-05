package com.sundar.devtech;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.InputType;
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
import com.google.android.material.textfield.TextInputEditText;
import com.sundar.devtech.DatabaseController.RequestURL;
import com.sundar.devtech.Internet.NetworkChangeListener;
import com.sundar.devtech.Services.ActivityMoving;
import com.sundar.devtech.Services.CustomAlertDialog;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private ImageView BACK_PRESS,APPBAR_BTN;
    private TextView APPBAR_TITLE;
    private TextInputEditText USER_NAME,USER_PASS;
    private ImageView PASS_ICON;
    private boolean passwordShowing = false;
    private AppCompatButton LOGIN_BTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        APPBAR_BTN = findViewById(R.id.appbar_btn);
        APPBAR_TITLE = findViewById(R.id.appbarTitle);
        APPBAR_TITLE.setText(getApplicationContext().getString(R.string.app_name));

        //back press activity
        BACK_PRESS = findViewById(R.id.backPress);
        APPBAR_BTN.setVisibility(View.GONE);
        BACK_PRESS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ScannerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        USER_NAME = findViewById(R.id.user_name);
        USER_PASS = findViewById(R.id.user_pass);
        PASS_ICON = findViewById(R.id.password_icon);
        LOGIN_BTN = findViewById(R.id.login);

        PASS_ICON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // password is show or not
                if (passwordShowing){
                    passwordShowing = false;
                    USER_PASS.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    PASS_ICON.setImageResource(R.drawable.eye);
                }else {
                    passwordShowing = true;
                    USER_PASS.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    PASS_ICON.setImageResource(R.drawable.eye_off);
                }
                USER_PASS.setSelection(USER_PASS.length());
            }
        });

        LOGIN_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adminLogin();
            }
        });
    }

    public void adminLogin(){
        String UserName = USER_NAME.getText().toString().trim();
        String UserPass = USER_PASS.getText().toString().trim();

        // Show progress dialog
        CustomAlertDialog dialog = new CustomAlertDialog(this);
        AlertDialog progressDialog = dialog.pleaseWaitDialog();
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, RequestURL.admin_login,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equalsIgnoreCase("Invalid User")){
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                        }else {
                            SharedPreferences sharedPreferences = getSharedPreferences("adminUser", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_id", UserName);
                            editor.putString("user_role", response.trim());
                            editor.apply();

                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();
                            ActivityMoving.ActivityMoving(LoginActivity.this, MainActivity.class);

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                    Toast.makeText(LoginActivity.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "ENetwork error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_name", UserName);
                params.put("user_pass", UserPass);
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(request);

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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityMoving.ActivityMoving(LoginActivity.this, ScannerActivity.class);
    }
}