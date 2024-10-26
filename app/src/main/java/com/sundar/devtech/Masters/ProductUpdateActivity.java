package com.sundar.devtech.Masters;

import static okio.ByteString.decodeBase64;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import com.sundar.devtech.ConfirmActivity;
import com.sundar.devtech.DatabaseController.RequestURL;
import com.sundar.devtech.Internet.NetworkChangeListener;
import com.sundar.devtech.R;
import com.sundar.devtech.SuccessActivity;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProductUpdateActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private ImageView BACK_PRESS,APPBAR_BTN;
    private TextView APPBAR_TITLE;
    private String PROD_ID,UPDATE = "1";
    private TextInputEditText PRODUCT_NAME, SPECIFICATION, DESCRIPTION;
    private ImageView PROD_IMAGE;
    private Button PRODUCT_IMAGE_BTN;
    private Spinner ACTIVE;
    private ArrayAdapter<String> active_adapter;
    private MaterialButton SAVE,CANCEL;
    public static final int GALLERY_REQ_CODE = 1;
    private static final int CROP_IMAGE_REQ_CODE = 2;
    private Bitmap bitmap;
    public static String LOGGED_USER = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_update);

        // Initialize your views
        APPBAR_BTN = findViewById(R.id.appbar_btn);
        APPBAR_TITLE = findViewById(R.id.appbarTitle);
        APPBAR_TITLE.setText("Add New Product"); // Change title to 'Update Product'

        // Back press functionality
        BACK_PRESS = findViewById(R.id.backPress);
        BACK_PRESS.setOnClickListener(view -> ProductUpdateActivity.super.onBackPressed());
        APPBAR_BTN.setVisibility(View.GONE);

        // Load logged user information
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefer", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);
        LOGGED_USER = (userId != null) ? userId : "admin";


        PRODUCT_NAME = findViewById(R.id.prod_name);
        SPECIFICATION = findViewById(R.id.prod_sepc);
        DESCRIPTION = findViewById(R.id.prod_desc);
        PROD_IMAGE = findViewById(R.id.prod_image);
        PRODUCT_IMAGE_BTN = findViewById(R.id.prod_image_btn);
        ACTIVE = findViewById(R.id.prod_active);

        SAVE = findViewById(R.id.prod_insert_btn);
        CANCEL = findViewById(R.id.prod_cancel_btn);

        active_adapter = new ArrayAdapter<>(ProductUpdateActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.active));
        active_adapter.setDropDownViewResource(R.layout.item_drop_down);
        ACTIVE.setAdapter(active_adapter);

        PRODUCT_IMAGE_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_REQ_CODE);
            }
        });

        SAVE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UPDATE.equals("0")){
                    insert();
                }else {
                    update();
                }
            }
        });

        CANCEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PRODUCT_NAME.setText("");
                SPECIFICATION.setText("");
                DESCRIPTION.setText("");
                PRODUCT_NAME.requestFocus();
            }
        });

    }

    public void fetchUpdateValue(){
        Intent intent = getIntent();
        if (intent != null) {
            PROD_ID  = intent.getStringExtra("prod_id");
            String prodName = intent.getStringExtra("prod_name");
            String prodSpec = intent.getStringExtra("prod_spec");
            String prodDesc = intent.getStringExtra("prod_desc");
            String prodImg = intent.getStringExtra("prod_img");
            String activeStatus = intent.getStringExtra("active");
            UPDATE = intent.getStringExtra("update");
            APPBAR_TITLE.setText("Update Product");
            SAVE.setText("UPDATE");

            // Set the retrieved data to your views
            PRODUCT_NAME.setText(prodName);
            SPECIFICATION.setText(prodSpec);
            DESCRIPTION.setText(prodDesc);

            if (prodImg != null && !prodImg.isEmpty()) {
                bitmap = decodeBase64(prodImg);
                PROD_IMAGE.setImageBitmap(bitmap);
            }

            if ("1".equals(activeStatus)) {
                ACTIVE.setSelection(0);
            } else {
                ACTIVE.setSelection(1);
            }
        }
    }

    public void insert() {
        String prod_name = PRODUCT_NAME.getText().toString().trim();
        String prod_spec = SPECIFICATION.getText().toString().trim();
        String prod_desc = DESCRIPTION.getText().toString().trim();
        String active = ACTIVE.getSelectedItem().toString().trim().equals("ENABLE") ? "1" : "2";

        StringRequest request = new StringRequest(Request.Method.POST, RequestURL.prod_insert,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase("success")){
                            Toast.makeText(ProductUpdateActivity.this, "Inserted Successfully", Toast.LENGTH_SHORT).show();
                            PRODUCT_NAME.setText("");
                            SPECIFICATION.setText("");
                            DESCRIPTION.setText("");
                            PROD_IMAGE.setImageDrawable(getResources().getDrawable(R.drawable.upload_icon));
                            PRODUCT_NAME.requestFocus();
                        }
                        else {
                            Toast.makeText(ProductUpdateActivity.this, response, Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                    Toast.makeText(ProductUpdateActivity.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductUpdateActivity.this, "ENetwork error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("prod_name", prod_name);
                params.put("prod_spec", prod_spec);
                params.put("prod_desc", prod_desc);
                params.put("prod_image",imageString(bitmap));
                params.put("active", active);
                params.put("user",LOGGED_USER);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ProductUpdateActivity.this);
        requestQueue.add(request);
    }
    public void update() {
        String prod_name = PRODUCT_NAME.getText().toString().trim();
        String prod_spec = SPECIFICATION.getText().toString().trim();
        String prod_desc = DESCRIPTION.getText().toString().trim();
        String active = ACTIVE.getSelectedItem().toString().trim().equals("ENABLE") ? "1" : "2";
        StringRequest request = new StringRequest(Request.Method.POST, RequestURL.prod_update,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equalsIgnoreCase("Update Successfully!")) {

                            Intent intent = new Intent(ProductUpdateActivity.this, ProductMaster.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            Toast.makeText(ProductUpdateActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProductUpdateActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                    Toast.makeText(ProductUpdateActivity.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("prod_id", PROD_ID);
                params.put("prod_name", prod_name);
                params.put("prod_spec", prod_spec);
                params.put("prod_desc", prod_desc);
                params.put("prod_image", imageString(bitmap));
                params.put("active", active);
                params.put("user", LOGGED_USER);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(ProductUpdateActivity.this);
        requestQueue.add(request);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQ_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            if (selectedImageUri != null) {
                UCrop.of(selectedImageUri, Uri.fromFile(new File(getCacheDir(), "croppedImage.jpg")))
                        .withAspectRatio(1, 1)
                        .withMaxResultSize(800, 800)
                        .start(this, CROP_IMAGE_REQ_CODE);
            }
        }

        if (requestCode == CROP_IMAGE_REQ_CODE && resultCode == RESULT_OK) {
            Uri croppedImageUri = UCrop.getOutput(data);
            if (croppedImageUri != null) {
                PROD_IMAGE.setImageURI(croppedImageUri);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), croppedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String imageString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);

        byte[] imaByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imaByte,Base64.DEFAULT).trim();
    }

    // Helper method to decode base64 string to a Bitmap
    private Bitmap decodeBase64(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchUpdateValue();
    }
}