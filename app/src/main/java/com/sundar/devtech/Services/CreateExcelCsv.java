package com.sundar.devtech.Services;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.sundar.devtech.Models.ReportModel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CreateExcelCsv {
    public static void saveExcel(Context context, List<ReportModel> reportModels, String fileName, int fileNum) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Report");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("EMPLOYEE ID");
        headerRow.createCell(1).setCellValue("EMPLOYEE NAME");
        headerRow.createCell(2).setCellValue("PRODUCT NAME");
        headerRow.createCell(3).setCellValue("QTY");
        headerRow.createCell(4).setCellValue("DATE");
        headerRow.createCell(5).setCellValue("TIME");

        // Fill data
        int rowIndex = 1;
        for (ReportModel report : reportModels) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(report.getEmp_id());
            row.createCell(1).setCellValue(report.getEmp_name());
            row.createCell(2).setCellValue(report.getProd_name());
            row.createCell(3).setCellValue(report.getQty());
            row.createCell(4).setCellValue(report.getDate().toString());
            row.createCell(5).setCellValue(report.getTime().toString());
        }

        // Save the Excel file
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DEVTECH");
        if (!directory.exists()) {
            directory.mkdirs(); // Create directory if it doesn't exist
        }

        File excelFile = new File(directory, fileName);
        try (FileOutputStream fos = new FileOutputStream(excelFile)) {
            workbook.write(fos);
            if (fileNum == 1) {
                Toast.makeText(context, "Excel saved successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (fileNum == 1) {
                Toast.makeText(context, "Failed to save Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveCsv(Context context, List<ReportModel> reportModels, String fileName, int fileNum) {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DEVTECH");
        if (!directory.exists()) {
            directory.mkdirs(); // Create directory if it doesn't exist
        }

        File csvFile = new File(directory, fileName);
        try (FileWriter writer = new FileWriter(csvFile)) {
            // Write header
            writer.append("EMPLOYEE ID,EMPLOYEE NAME,PRODUCT NAME,QTY,DATE,TIME\n");

            // Write data
            for (ReportModel report : reportModels) {
                writer.append(report.getEmp_id()).append(',')
                        .append(report.getEmp_name()).append(',')
                        .append(report.getProd_name()).append(',')
                        .append(report.getQty()).append(',')
                        .append(report.getDate().toString()).append(',')
                        .append(report.getTime().toString()).append('\n');
            }
            if (fileNum == 1) {
                Toast.makeText(context, "CSV saved successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (fileNum == 1) {
                Toast.makeText(context, "Failed to save CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
