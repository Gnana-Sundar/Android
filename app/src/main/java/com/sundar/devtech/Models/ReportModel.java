package com.sundar.devtech.Models;

import java.sql.Time;
import java.util.Date;

public class ReportModel {
    private String emp_id,emp_name,prod_name,qty,date,time;

    public ReportModel(String emp_id, String emp_name, String prod_name, String qty, String date, String time) {
        this.emp_id = emp_id;
        this.emp_name = emp_name;
        this.prod_name = prod_name;
        this.qty = qty;
        this.date = date;
        this.time = time;
    }

    public String getEmp_id() {
        return emp_id;
    }

    public String getEmp_name() {
        return emp_name;
    }

    public String getProd_name() {
        return prod_name;
    }

    public String getQty() {
        return qty;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
