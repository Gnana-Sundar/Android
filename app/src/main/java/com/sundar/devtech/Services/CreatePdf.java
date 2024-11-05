package com.sundar.devtech.Services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

import com.sundar.devtech.Models.ReportModel;
import com.sundar.devtech.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreatePdf {

    private static final int PAGE_WIDTH = 710;
    private static final int PAGE_HEIGHT = 1200;
    private static final int ALL_PAGE_WIDTH = 1000;

    public static void createPdf(Context context, List<ReportModel> reportModels, String from_date, String to_date) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint textPaint = new Paint();
        Paint linePaint = new Paint();
        Paint tableSize = new Paint();
        linePaint.setStrokeWidth(1f);

        int yPos;

        // Initialize the first page with header
        PdfDocument.Page page = initializePage(context, pdfDocument, 130, from_date, to_date, paint, textPaint, linePaint, true);
        Canvas canvas = page.getCanvas();
        yPos = 300;  // Starting position for entries on the first page

        int entryCount = 0;
        boolean isFirstPage = true;

        for (ReportModel value : reportModels) {
            // Set max entries per page based on the page type
            int currentMaxEntries = isFirstPage ? 28 : 34;
            entryCount++;

            // Check if a new page is needed
            if (entryCount > currentMaxEntries) {
                pdfDocument.finishPage(page);  // Finalize current page

                // Create a new page without header for subsequent pages
                page = initializePage(context, pdfDocument, 100, from_date, to_date, paint, textPaint, linePaint, false);
                canvas = page.getCanvas();
                yPos = 120;  // Starting position for entries on subsequent pages
                entryCount = 1;  // Reset entry count for new page
                isFirstPage = false;
            }

            // Draw the row data
            drawRow(canvas, value, yPos, tableSize);
            yPos += 30;
        }

        // Finalize the last page
        pdfDocument.finishPage(page);
        savePdf(context, pdfDocument, "pentatvm_report.pdf");
    }

    // Initialize page with headers and borders
    private static PdfDocument.Page initializePage(Context context, PdfDocument pdfDocument, int yPos, String from_date, String to_date, Paint paint, Paint textPaint, Paint linePaint, boolean isFirstPage) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfDocument.getPages().size() + 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(context.getResources().getColor(R.color.colordevider)); // You can adjust the color as needed
        canvas.drawRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT, backgroundPaint); // Fill the background

        if (isFirstPage) {
            // Draw header and date range only on the first page
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(35);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setColor(context.getResources().getColor(R.color.black));
            canvas.drawText("Devtech Reports", PAGE_WIDTH / 2, yPos, paint);
            yPos += 60;
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(20);
            textPaint.setColor(context.getResources().getColor(R.color.black));
            canvas.drawText("FROM DATE : " + from_date + "    TO DATE :" + to_date, 40, yPos, textPaint);
        }

        // Draw table headers for each page
        drawTableHeaders(canvas, paint, linePaint, isFirstPage ? 250 : 80);

        return page;
    }

    // Draw table headers and borders
    private static void drawTableHeaders(Canvas canvas, Paint paint, Paint linePaint, int headerYPos) {
        paint.setTextSize(11);
        paint.setColor(Color.BLACK);

        // Draw headers
        canvas.drawText("EMPLOYEE ID", 60, headerYPos, paint);
        canvas.drawText("EMPLOYEE NAME", 190, headerYPos, paint);
        canvas.drawText("PRODUCT NAME", 370, headerYPos, paint);
        canvas.drawText("QTY", 495, headerYPos, paint);
        canvas.drawText("DATE", 560, headerYPos, paint);
        canvas.drawText("TIME", 640, headerYPos, paint);

        // Draw column and row lines consistently for all pages
        int topLineY = headerYPos - 30;
        int bottomLineY = headerYPos + 20;

        canvas.drawLine(20, topLineY, 680, topLineY, linePaint);  // Top border
        canvas.drawLine(20, bottomLineY, 680, bottomLineY, linePaint);  // Header-bottom border
        canvas.drawLine(20, topLineY, 20, 1130, linePaint);  // Left border
        canvas.drawLine(680, topLineY, 680, 1130, linePaint); // Right border
        canvas.drawLine(110, topLineY, 110, 1130, linePaint); // Column divider
        canvas.drawLine(280, topLineY, 280, 1130, linePaint); // Column divider
        canvas.drawLine(470, topLineY, 470, 1130, linePaint); // Column divider
        canvas.drawLine(520, topLineY, 520, 1130, linePaint); // Column divider
        canvas.drawLine(600, topLineY, 600, 1130, linePaint); // Column divider
        canvas.drawLine(20, 1130, 680, 1130, linePaint); // Bottom border
    }

    // Draw a single row of data
    private static void drawRow(Canvas canvas, ReportModel value, int yPos, Paint tableSize) {
        tableSize.setTextSize(10);

        // Draw EMPLOYEE ID
        canvas.drawText(value.getEmp_id(), 50, yPos, tableSize);

        // Draw EMPLOYEE NAME with word wrap
        String[] empNameLines = getWrappedText(value.getEmp_name(), tableSize, 180);
        for (String line : empNameLines) {
            canvas.drawText(line, 120, yPos, tableSize);
        }

        // Draw PRODUCT NAME with word wrap
        String[] prodNameLines = getWrappedText(value.getProd_name(), tableSize, 200);
        for (String line : prodNameLines) {
            canvas.drawText(line, 310, yPos, tableSize);
        }

        // Draw QTY, DATE, TIME
        canvas.drawText(value.getQty(), 495, yPos, tableSize);
        canvas.drawText(value.getDate().toString(), 535, yPos, tableSize);
        canvas.drawText(value.getTime().toString(), 620, yPos, tableSize);
    }


    // Save the PDF document to the specified file
    private static void savePdf(Context context, PdfDocument pdfDocument, String fileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DEVTECH/" + fileName);
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(context, "PDF saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }


    private static String[] getWrappedText(String text, Paint paint, float maxWidth) {
        // Split text into words
        String[] words = text.split(" ");
        ArrayList<String> lines = new ArrayList<>();
        String currentLine = "";

        // Iterate through each word
        for (String word : words) {
            if (paint.measureText(currentLine + " " + word) <= maxWidth) {
                // Add word to the current line
                currentLine += (currentLine.isEmpty() ? "" : " ") + word;
            } else {
                // Add current line to the list and start a new line
                lines.add(currentLine);
                currentLine = word;
            }
        }

        // Add the last line to the list
        lines.add(currentLine);

        // Convert the list to an array
        return lines.toArray(new String[0]);
    }
}
