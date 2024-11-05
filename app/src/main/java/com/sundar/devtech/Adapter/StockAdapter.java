package com.sundar.devtech.Adapter;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

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
import com.sundar.devtech.Masters.ProductMaster;
import com.sundar.devtech.Masters.StockActivity;
import com.sundar.devtech.Models.ProductModel;
import com.sundar.devtech.Models.StockModel;
import com.sundar.devtech.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {
    private Context context;
    private List<StockModel> stockModels = new ArrayList<>();
    private AlertDialog alertDialog;
    private String PROD_ID;
    private TextView SLOT_NO, PROD_NAME;
    private TextInputEditText QTY,MIN_QTY;
    private Spinner ACTIVE;
    private ArrayAdapter<String> active_adapter;
    private MaterialButton SAVE,CANCEL,FULL_LOAD_BTN;
    private Dialog DIALOG;
    private ArrayAdapter<String> adapter;

    public StockAdapter(Context context, List<StockModel> stockModels) {
        this.context = context;
        this.stockModels = stockModels;
    }
    public void setFilteredList(List<StockModel> filteredList){
        this.stockModels = filteredList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public StockAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prodstock,parent,false);
        return new StockAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockAdapter.ViewHolder holder, int position) {
        final StockModel stockModel = stockModels.get(position);

        holder.SLOT_NO.setText(stockModel.getSlot_no());
        holder.PROD_NAME.setText(stockModel.getProd_name());
        holder.QTY.setText(stockModel.getQty());
        holder.MIN_QTY.setText(stockModel.getMin_qty());

        if (stockModel.getActive().equals("1")) {
            holder.ACTIVE.setText("ENABLE");
        } else {
            holder.ACTIVE.setText("DISABLE");
        }

        holder.DELETE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(context).inflate(R.layout.warning_dialog,
                        (ConstraintLayout) holder.itemView.findViewById(R.id.warning_dialog));

                builder.setView(view);
                alertDialog = builder.create();
                ((TextView) view.findViewById(R.id.dialog_title)).setText("DELETE");
                ((TextView) view.findViewById(R.id.dialog_message)).setText("Are you sure you want to Delete");
                ((Button) view.findViewById(R.id.dialog_cancel)).setText("NO");
                ((Button) view.findViewById(R.id.dialog_submit)).setText("YES");

                view.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                view.findViewById(R.id.dialog_submit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Delete(stockModel.getSlot_no(),stockModel.getProd_id());
                    }
                });

                if (alertDialog.getWindow() != null) {
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                alertDialog.show();

            }

        });
        holder.EDIT.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingInflatedId")
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(context).inflate(R.layout.stock_prod_dialog,
                        (LinearLayout)holder.itemView.findViewById(R.id.stock_prod_dialog));
                builder.setView(view);

                alertDialog = builder.create();

                SLOT_NO = view.findViewById(R.id.stock_slot_no);
                PROD_NAME = view.findViewById(R.id.stock_prodName);
                QTY = view.findViewById(R.id.stock_prod_qty);
                MIN_QTY = view.findViewById(R.id.stock_min_qty);
                ACTIVE = view.findViewById(R.id.stock_prod_active);

                FULL_LOAD_BTN = view.findViewById(R.id.full_load_btn);
                SAVE = view.findViewById(R.id.stock_prod_insert_btn);
                CANCEL = view.findViewById(R.id.stock_prod_cancel_btn);

                if (!StockActivity.user_role.equals("1")) {
                    SLOT_NO.setEnabled(false);
                    PROD_NAME.setEnabled(false);
                    MIN_QTY.setEnabled(false);
                }

                active_adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, context.getResources().getStringArray(R.array.active));
                active_adapter.setDropDownViewResource(R.layout.item_drop_down);
                ACTIVE.setAdapter(active_adapter);

                ((TextView) view.findViewById(R.id.dialog_title)).setText("Update Slot");
                SAVE.setText("Update");

                SLOT_NO.setText("Slot No - "+stockModel.getSlot_no());
                PROD_ID = stockModel.getProd_id();
                PROD_NAME.setText(stockModel.getProd_name());
                QTY.setText(stockModel.getQty());
                MIN_QTY.setText(stockModel.getMin_qty());
                
                String statusValue = stockModel.getActive();
                if (statusValue.equals("1")) {
                    statusValue = "ENABLE";
                } else if (statusValue.equals("2")) {
                    statusValue = "DISABLE";
                }
                
                int position = active_adapter.getPosition(statusValue);
                ACTIVE.setSelection(position);

                SLOT_NO.setEnabled(false);
                SLOT_NO.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DIALOG = new Dialog(context);
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
                        adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, StockActivity.motorList);
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
                        DIALOG = new Dialog(context);
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

                        List<String> product = new ArrayList<>(StockActivity.prodlists.values());
                        adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, product);
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

                                for (Map.Entry<String, String> entry : StockActivity.prodlists.entrySet()) {
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
                        update(stockModel.getSlot_no());
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

    @Override
    public int getItemCount() {
        return stockModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView SLOT_NO,PROD_NAME,QTY, MIN_QTY, ACTIVE;
        private ImageButton EDIT, DELETE;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            SLOT_NO = itemView.findViewById(R.id.stock_slot_no);
            PROD_NAME = itemView.findViewById(R.id.stock_prod_name);
            QTY = itemView.findViewById(R.id.stock_qty);
            MIN_QTY = itemView.findViewById(R.id.stock_min_qty);
            ACTIVE = itemView.findViewById(R.id.stock_active);
            EDIT = itemView.findViewById(R.id.stock_edit);
            DELETE = itemView.findViewById(R.id.stock_delete);
        }
    }

    public void Delete(String slot_no,String prod_id){
        StringRequest request = new StringRequest(Request.Method.POST, RequestURL.prod_stock_delete,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase("Deleted Successfully!")){
                            Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                            ((StockActivity) context).select();
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(context, "Item deleted Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                    Toast.makeText(context, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("slot_no", slot_no);
                params.put("prod_id", prod_id);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }
    public void update(String slot_no) {

        String prod_name = PROD_NAME.getText().toString().trim();
        String qty = QTY.getText().toString().trim();
        String min_qty = MIN_QTY.getText().toString().trim();
        String active = ACTIVE.getSelectedItem().toString().trim().equals("ENABLE") ? "1" : "2";

        if (slot_no.equals("")){
            Toast.makeText(context, "Slot No is Empty", Toast.LENGTH_SHORT).show();
        }else if (prod_name.equals("")){
            Toast.makeText(context, "Product Name is Empty", Toast.LENGTH_SHORT).show();
        }else if (qty.equals("") || qty.equals("0")){
            Toast.makeText(context, "Qty is Empty", Toast.LENGTH_SHORT).show();
        }else if (min_qty.equals("")){
            Toast.makeText(context, "Minimum Qty is Empty", Toast.LENGTH_SHORT).show();
        }else {
            StringRequest request = new StringRequest(Request.Method.POST, RequestURL.prod_stock_update,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.trim().equalsIgnoreCase("Update Successfully!")) {
                                Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                ((StockActivity) context).select();
                                alertDialog.dismiss();
                            } else {
                                Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                        Toast.makeText(context, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("slot_no", slot_no);
                    params.put("prod_id", PROD_ID);
                    params.put("qty", qty);
                    params.put("min_qty", min_qty);
                    params.put("active", active);
                    params.put("user", StockActivity.LOGGED_USER);
                    return params;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(request);
        }
    }
}
