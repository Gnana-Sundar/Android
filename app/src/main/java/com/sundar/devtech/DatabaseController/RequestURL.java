package com.sundar.devtech.DatabaseController;

public class RequestURL {

//    public final static String URL = "http://192.168.1.62:8080";
    public final static String URL = "https://vinsoftsolutions.in";

    // MOTOR MASTER URL
    public final static String motor_insert = URL +"/penta_tvm/motormaster/motorinsert.php";
    public final static String motor_fetch  = URL +"/penta_tvm/motormaster/motorfetch.php";
    public final static String motor_update = URL +"/penta_tvm/motormaster/motorupdate.php";
    public final static String motor_delete = URL +"/penta_tvm/motormaster/motordelete.php";
    //-------------------------------------------------------------------------------------------------------------
    // PRODUCT MASTER URL
    public final static String prod_insert = URL +"/penta_tvm/productmaster/prodinsert.php";
    public final static String prod_fetch  = URL +"/penta_tvm/productmaster/prodfetch.php";
    public final static String prod_update = URL +"/penta_tvm/productmaster/produpdate.php";
    public final static String prod_delete = URL +"/penta_tvm/productmaster/proddelete.php";
    //-------------------------------------------------------------------------------------------------------------
    //EMPLOYEE RATE URL
    public final static String emp_fetch  = URL +"/penta_tvm/employeemaster/empfetch.php";
    public final static String emp_insert = URL +"/penta_tvm/employeemaster/empinsert.php";
    public final static String emp_update = URL +"/penta_tvm/employeemaster/empupdate.php";
    public final static String emp_delete = URL +"/penta_tvm/employeemaster/empdelete.php";

    //-------------------------------------------------------------------------------------------------------------

    public final static String prod_stock_insert = URL +"/penta_tvm/prodstock/prodstockinsert.php";
    public final static String prod_stock_fetch  = URL +"/penta_tvm/prodstock/prodstockfetch.php";
    public final static String prod_stock_update = URL +"/penta_tvm/prodstock/prodstockupdate.php";
    public final static String prod_stock_delete = URL +"/penta_tvm/prodstock/prodstockdelete.php";
    public final static String motorNo_fetch = URL +"/penta_tvm/fetchmotor.php";


    //-------------------------------------------------------------------------------------------------------------
    public final static String emp_login = URL +"/penta_tvm/login.php";
    public final static String product = URL +"/penta_tvm/product.php";
    public final static String sales_insert = URL +"/penta_tvm/salesinsert.php";
    public final static String admin_login = URL +"/penta_tvm/adminlogin.php";
    public final static String reports = URL +"/penta_tvm/reportfetch.php";
    public final static String send_mail = URL +"/penta_tvm/sendmail.php";
    //-------------------------------------------------------------------------------------------------------------
}
