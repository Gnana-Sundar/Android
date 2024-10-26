package com.sundar.devtech.Adapter;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
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
import com.sundar.devtech.Masters.EmployeeMaster;
import com.sundar.devtech.Masters.ProductMaster;
import com.sundar.devtech.Masters.ProductUpdateActivity;
import com.sundar.devtech.Models.ProductModel;
import com.sundar.devtech.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.viewHolder> {

    private Context context;
    private List<ProductModel> productModels = new ArrayList<>();

    private AlertDialog alertDialog;

    public ProductAdapter(Context context, List<ProductModel> productModels) {
        this.context = context;
        this.productModels = productModels;
    }
    public void setFilteredList(List<ProductModel> filteredList){
        this.productModels = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product,parent,false);
        return new ProductAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.viewHolder holder, int position) {
        final ProductModel productModel = productModels.get(position);

        holder.PROD_NAME.setText(productModel.getProd_name());
        holder.PROD_DESC.setText(productModel.getProd_desc());
        holder.PROD_SPEC.setText(productModel.getProd_spec());

        if (productModel.getProd_image() != null && !productModel.getProd_image().isEmpty()) {
            Bitmap bitmap = decodeBase64(productModel.getProd_image());
            holder.PROD_IMAGE.setImageBitmap(bitmap);
        } else {
            holder.PROD_IMAGE.setImageResource(R.drawable.logo);
        }

        if (productModel.getActive().equals("1")) {
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
                        Delete(productModel.getProd_id());
                    }
                });

                if (alertDialog.getWindow() != null) {
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                alertDialog.show();

            }

        });
        holder.EDIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ProductUpdateActivity.class);
                intent.putExtra("prod_id", productModel.getProd_id());
                intent.putExtra("prod_name", productModel.getProd_name());
                intent.putExtra("prod_spec", productModel.getProd_spec());
                intent.putExtra("prod_desc", productModel.getProd_desc());
                intent.putExtra("prod_img", productModel.getProd_image());
                intent.putExtra("active", productModel.getActive());
                intent.putExtra("update", "1");
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productModels.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        private TextView PROD_NAME, PROD_DESC, PROD_SPEC, ACTIVE;
        private ImageView PROD_IMAGE;
        private ImageButton EDIT, DELETE;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            PROD_NAME = itemView.findViewById(R.id.prod_name);
            PROD_DESC = itemView.findViewById(R.id.prod_spec);
            PROD_SPEC = itemView.findViewById(R.id.prod_desc);
            PROD_IMAGE = itemView.findViewById(R.id.prod_image);
            ACTIVE = itemView.findViewById(R.id.prod_active);
            EDIT = itemView.findViewById(R.id.prod_edit);
            DELETE = itemView.findViewById(R.id.prod_delete);
        }
    }

    // Helper method to decode base64 string to a Bitmap
    private Bitmap decodeBase64(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void Delete(String prod_id){

        StringRequest request = new StringRequest(Request.Method.POST, RequestURL.prod_delete,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase("Deleted Successfully!")){
                            Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                            ((ProductMaster) context).select();
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
                params.put("prod_id", prod_id);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }

}
