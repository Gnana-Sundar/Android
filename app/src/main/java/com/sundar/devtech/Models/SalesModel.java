package com.sundar.devtech.Models;

public class SalesModel {
    private String motor_id,prod_id,prod_name,prod_spec,prod_desc,prod_image;

    public SalesModel(String motor_id,String prod_id, String prod_name, String prod_spec, String prod_desc, String prod_image) {
        this.motor_id = motor_id;
        this.prod_id = prod_id;
        this.prod_name = prod_name;
        this.prod_spec = prod_spec;
        this.prod_desc = prod_desc;
        this.prod_image = prod_image;
    }

    public String getMotor_id() {
        return motor_id;
    }

    public String getProd_id() {
        return prod_id;
    }

    public String getProd_name() {
        return prod_name;
    }

    public String getProd_spec() {
        return prod_spec;
    }

    public String getProd_desc() {
        return prod_desc;
    }

    public String getProd_image() {
        return prod_image;
    }
}
