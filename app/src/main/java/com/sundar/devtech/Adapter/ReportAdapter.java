package com.sundar.devtech.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sundar.devtech.Models.ReportModel;
import com.sundar.devtech.R;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private Context context;
    private List<ReportModel> reportModels = new ArrayList<>();

    public ReportAdapter(Context context, List<ReportModel> reportModels) {
        this.context = context;
        this.reportModels = reportModels;
    }

    @NonNull
    @Override
    public ReportAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report,parent,false);
        return new ReportAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.ViewHolder holder, int position) {
        final ReportModel reportModel = reportModels.get(position);
        holder.EMP_ID.setText(reportModel.getEmp_id());
        holder.EMP_NAME.setText(reportModel.getEmp_name());
        holder.PROD_NAME.setText(reportModel.getProd_name());
        holder.QTY.setText(reportModel.getQty());
        holder.DATE.setText(reportModel.getDate());
        holder.TIME.setText(reportModel.getTime());
    }

    @Override
    public int getItemCount() {
        return reportModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView EMP_ID,EMP_NAME,PROD_NAME,QTY,DATE,TIME;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            EMP_ID = itemView.findViewById(R.id.rep_emp_id);
            EMP_NAME = itemView.findViewById(R.id.rep_emp_name);
            PROD_NAME = itemView.findViewById(R.id.rep_prod_name);
            QTY = itemView.findViewById(R.id.rep_qty);
            DATE = itemView.findViewById(R.id.rep_date);
            TIME = itemView.findViewById(R.id.rep_time);
        }
    }
}
