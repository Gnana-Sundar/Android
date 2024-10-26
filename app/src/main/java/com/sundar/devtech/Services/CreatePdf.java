package com.sundar.devtech.Services;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import com.sundar.devtech.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreatePdf {

    private static final int PAGE_WIDTH = 704;
    private static final int PAGE_HEIGHT = 1200;

    private static final int ALL_PAGE_WIDTH = 1000;

    public static void createPdfDailyExpense(Context context, int ProdId, String prod_name) {
        prod_name = prod_name.replace(" ", "").replace("/", "");

        // Initialize variables
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint paintSize = new Paint();
        Paint tableSize = new Paint();
        Paint line = new Paint();
        line.setStrokeWidth(2f);
        int yPos = 100; // Initial y-position for drawing text
        int initialYPos = 150; // Store the initial y-position



        // Prepare PDF document
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Draw background color
        int grayColor = context.getResources().getColor(R.color.colordevider);
        canvas.drawColor(grayColor); // Set background color to the gray color defined in resources


        // Draw header
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(35);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(context.getResources().getColor(R.color.black));
        canvas.drawText("Report For Daily Expense", PAGE_WIDTH / 2, yPos, paint);
        yPos += 60; // Increment y-position for the next line



        paintSize.setTextAlign(Paint.Align.LEFT);
        paintSize.setTextSize(25);

        //Define a new paint object with the desired color
        Paint partyNamePaint = new Paint();
        partyNamePaint.setColor(context.getResources().getColor(R.color.smoke_purple)); // Change to your desired color
        partyNamePaint.setTextSize(25);
        //Draw "PARTY NAME -" with the new paint object
        canvas.drawText("PRODUCT NAME : ", 80, yPos, partyNamePaint);
        //Draw the rest of the text with the existing paintSize object
        canvas.drawText(product_name, 80 + partyNamePaint.measureText("PRODUCT NAME : "), yPos, paintSize);
        yPos += 45;

        paintSize.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paintSize.setColor(context.getResources().getColor(R.color.black));
        canvas.drawText("TOTAL AMOUNT  - ₹ " + String.valueOf(total), 80, yPos, paintSize);
        paintSize.setColor(context.getResources().getColor(android.R.color.black));
        yPos += 130; // Increment y-position for the next line

        // Draw table Line
        canvas.drawLine(50, 250, 654, 250, line); // Adjusted x-coordinate to fit the full width (704 - 50)
        canvas.drawLine(50, 300, 654, 300, line);

        canvas.drawLine(50, 250, 50, 1130, line); // Left margin
        canvas.drawLine(654, 250, 654, 1130, line); // Right margin

        canvas.drawLine(50, 1130, 654, 1130, line);

        // Draw table headers
        paint.setTextSize(22);
        paint.setColor(context.getResources().getColor(android.R.color.black));
        canvas.drawText("Date", 110, 280, paint);
        canvas.drawText("Amount", 270, 280, paint);
        canvas.drawText("Remarks", 460, 280, paint);

        // Draw vertical lines for each column header
        canvas.drawLine(180, 250, 180, 1130, line); // Vertical line for "Date"
        canvas.drawLine(380, 250, 380, 1130, line); // Vertical line for "Amount"

        tableSize.setTextAlign(Paint.Align.LEFT); // Align text to the left
        tableSize.setTextSize(18);

        // Maximum number of entries per page
        int maxEntriesPerPage = 13;

        // Initialize a counter for the number of entries drawn
        int entryCount = 0;

        Cursor d = db.rawQuery("SELECT a.prod_id, b.prod_name, a.date, a.amount, a.remarks FROM dailyexpense " +
                "AS a LEFT JOIN productmaster AS b ON a.prod_id = b.prod_id WHERE a.prod_id = ?", new String[]{String.valueOf(ProdId)});

        while (d.moveToNext()) {
            // Increment the entry count
            entryCount++;

            // If the maximum number of entries per page is reached, start a new page
            if (entryCount > maxEntriesPerPage) {
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfDocument.getPages().size() + 1).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                yPos = initialYPos; // Reset yPos to the initial position
                entryCount = 0; // Reset the entry count


                canvas.drawColor(grayColor);

                // Draw table Line for the new page
                canvas.drawLine(50, 30, 654, 30, line); // Adjusted x-coordinate to fit the full width (704 - 50)
                canvas.drawLine(50, 90, 654, 90, line);

                canvas.drawLine(50, 30, 50, 1130, line); // Left margin
                canvas.drawLine(654, 30, 654, 1130, line); // Right margin

                canvas.drawLine(50, 1130, 654, 1130, line);

                // Draw table headers
                paint.setTextSize(22);
                paint.setColor(context.getResources().getColor(android.R.color.black));
                canvas.drawText("Date", 110, 70, paint);
                canvas.drawText("Amount", 270, 70, paint);
                canvas.drawText("Remarks", 460, 70, paint);

                // Draw vertical lines for each column header
                canvas.drawLine(180, 30, 180, 1130, line); // Vertical line for "Date"
                canvas.drawLine(380, 30, 380, 1130, line); // Vertical line for "Amount"

                maxEntriesPerPage = 15;
            }

            String date = d.getString(c.getColumnIndex("date"));
            double amount = d.getDouble(c.getColumnIndex("amount"));
            String remarks = d.getString(c.getColumnIndex("remarks"));

            // Draw data in tabular format
            canvas.drawText(date, 60, yPos, tableSize); // Adjusted x-coordinate for "Date"
            canvas.drawText(String.valueOf(amount), 200, yPos, tableSize); // Adjusted x-coordinate for "Amount"

            float remarksX = 400; // Adjust the x-coordinate for "Remarks"
            float remarksY = yPos; // Initial y-coordinate for "Remarks"

            String[] remarkLines = getWrappedText(remarks, tableSize, 200); // Split remarks into lines
            for (String line1 : remarkLines) {
                canvas.drawText(line1, remarksX, remarksY, tableSize);
                remarksY += tableSize.getTextSize(); // Move to the next line
            }

            // Increment y-position for the next line, adjust as needed
            yPos += 60;
        }


        // Finish PDF document
        pdfDocument.finishPage(page);

        // Save PDF document
        // Save PDF document with a name based on the party's name
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ExpenseApp/" + prod_name + ".pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(context, "Pdf Saved Successfully " + prod_name, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }
}
