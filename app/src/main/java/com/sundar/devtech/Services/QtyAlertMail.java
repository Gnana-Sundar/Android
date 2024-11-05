package com.sundar.devtech.Services;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sundar.devtech.DatabaseController.RequestURL;
import com.sundar.devtech.Models.StockModel;
import com.sundar.devtech.SplashScreen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

public class QtyAlertMail {
    private Context context;
    public static List<StockModel> PRODSTOCK = new ArrayList<>();

    public QtyAlertMail(Context context) {
        this.context = context;
    }

    public void sendAlertMail(String slot_no) {
        List<String> EMAIL_NAME = SplashScreen.EMAIL_NAME;

        if (PRODSTOCK.isEmpty()) {
            Toast.makeText(context, "Value is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String email : EMAIL_NAME) {
            StringBuilder emailContent = new StringBuilder("The following items have low stock:\n\n");
            boolean hasLowStock = false;

            for (StockModel stock : PRODSTOCK) {

                if (slot_no.equals(stock.getSlot_no())) {

                    int qty = Integer.parseInt(stock.getQty());
                    int min_qty = Integer.parseInt(stock.getMin_qty());

                    if (qty <= min_qty) {
                        emailContent.append("Slot No          : ").append(stock.getSlot_no())
                                .append("\nProduct          : ").append(stock.getProd_name())
                                .append("\nAlert Quantity   : ").append(min_qty)
                                .append("\nCurrent Quantity : ").append(qty)
                                .append("\n\n");
                        hasLowStock = true;
                    }
                }
            }

            // Send email only if there are items with low stock
            if (hasLowStock) {
                sendEmailInBackground(email, "Low qty alert", emailContent.toString());
            }
        }
    }

    // Send email in a background thread
    private void sendEmailInBackground(String recipientEmail, String subject, String bodyText) {
        new Thread(() -> {
            SendMail mailSender = new SendMail();
            try {
                mailSender.sendEmailWithAttachment(recipientEmail, subject, bodyText, 0);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }).start();
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

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                    Toast.makeText(context, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }
}
