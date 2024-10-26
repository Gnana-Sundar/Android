package com.sundar.devtech.Models;

public class StockModel {
    private String motor_no,prod_id,prod_name,qty,min_qty,active;

    public StockModel(String motor_no, String prod_id, String prod_name, String qty, String min_qty, String active) {
        this.motor_no = motor_no;
        this.prod_id = prod_id;
        this.prod_name = prod_name;
        this.qty = qty;
        this.min_qty = min_qty;
        this.active = active;
    }

    public String getMotor_no() {
        return motor_no;
    }

    public String getProd_id() {
        return prod_id;
    }

    public String getProd_name() {
        return prod_name;
    }

    public String getQty() {
        return qty;
    }

    public String getMin_qty() {
        return min_qty;
    }

    public String getActive() {
        return active;
    }
}
