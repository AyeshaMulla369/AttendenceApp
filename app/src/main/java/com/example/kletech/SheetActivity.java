package com.example.kletech;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SheetActivity extends AppCompatActivity {

    Toolbar toolbar;
    int pageHeight = 1120;
    int pagewidth = 792;
    String className;
    String subjectName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet);

        className = getIntent().getStringExtra("className");
        subjectName = getIntent().getStringExtra("subjectName");

        showTable();

        setToolbar();


    }

    private void showTable() {
        DbHelper dbHelper = new DbHelper(this);
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        long[] idArray = getIntent().getLongArrayExtra("idArray");
        int[] rollArray = getIntent().getIntArrayExtra("rollArray");
        String[] nameArray = getIntent().getStringArrayExtra("nameArray");
        String month = getIntent().getStringExtra("month");

        int DAY_IN_MONTH = getDayInMonth(month);
        Log.d("SheetActivity", "showTable: Inside the new page");

        //row setup
        int rowSize = idArray.length+1;
        TableRow[] rows = new TableRow[rowSize];
        TextView[] rolls_tvs = new TextView[rowSize];
        TextView[] name_tvs = new TextView[rowSize];
        TextView[][] status_tvs = new TextView[rowSize][DAY_IN_MONTH + 1];

        for (int i=0;i< rowSize ;i++)
        {
            rolls_tvs[i] = new TextView(this);
            name_tvs[i] = new TextView(this);
            for (int j=1;j<=DAY_IN_MONTH ; j++)
            {
                status_tvs[i][j] = new TextView(this);
            }
        }

        rolls_tvs[0].setText("Roll");
        rolls_tvs[0].setTypeface(rolls_tvs[0].getTypeface() , Typeface.BOLD);
        name_tvs[0].setText("Name");
        name_tvs[0].setTypeface(name_tvs[0].getTypeface() , Typeface.BOLD);
        for (int i=1;i<= DAY_IN_MONTH ;i++)
        {
            status_tvs[0][i].setText(String.valueOf(i));
            status_tvs[0][i].setTypeface(status_tvs[0][i].getTypeface() , Typeface.BOLD);
        }

        for (int i=1;i< rowSize ;i++)
        {
            rolls_tvs[i].setText(String.valueOf(rollArray[i-1]));
            name_tvs[i].setText(nameArray[i-1]);
            for (int j=1;j<=DAY_IN_MONTH ; j++)
            {
                String  day = String.valueOf(j);
                if(day.length() == 1) day = "0"+day;

                String date = day+"."+month;
                String status = dbHelper.getStatus(idArray[i-1] , date  );

                status_tvs[i][j].setText(status);
            }
        }

        for (int i = 0; i <rowSize ; i++) {

            rows[i] = new TableRow(this);
            if(i%2 == 0)
                rows[i].setBackgroundColor(Color.parseColor("#EEEEEE"));
            else
                rows[i].setBackgroundColor(Color.parseColor("#E4E4E4"));

            rolls_tvs[i].setPadding(16,16,16,16);
            name_tvs[i].setPadding(16,16,16,16);
            rows[i].addView(rolls_tvs[i]);
            rows[i].addView(name_tvs[i]);
            for (int j = 1; j <= DAY_IN_MONTH; j++) {
                status_tvs[i][j].setPadding(16,16,16,16);
                rows[i].addView(status_tvs[i][j]);
            }
            tableLayout.addView(rows[i]);
        }

        tableLayout.setShowDividers(TableLayout.SHOW_DIVIDER_MIDDLE);

    }

    private int getDayInMonth(String month) {
        int monthIndex = Integer.valueOf(month.substring(0,1));
        int year = Integer.valueOf(month.substring(4));

        Calendar calendar =Calendar.getInstance();
        calendar.set(calendar.MONTH , monthIndex);
        calendar.set(calendar.YEAR , year);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar_page);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.saver);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        title.setText(className);
        subtitle.setText(subjectName);
        save.setOnClickListener(v -> {
            Toast.makeText(this, "Clicked on save", Toast.LENGTH_SHORT).show();
            createPDF();
        });
        back.setOnClickListener(v -> onBackPressed());
    }

    private void createPDF() {



        PdfDocument document = new PdfDocument();

        // two variables for paint "paint" is used
        // for drawing shapes and we will use "title"
        // for adding text in our PDF file.
        Paint paint = new Paint();
        Paint title = new Paint();


        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1080,1920 , 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        //Canvas
        Canvas canvas = page.getCanvas();

        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        title.setTextSize(25);
        canvas.drawText("Attendence Sheet", 209, 100, title);
        canvas.drawText(" "+className, 209, 130, title);
        canvas.drawText(" "+subjectName, 209, 160, title);
        title.setTextAlign(Paint.Align.CENTER);


        //finish page
        document.finishPage(page);
        Toast.makeText(this, "Finished writing", Toast.LENGTH_SHORT).show();

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = "AttendenceSheet.pdf";
        File file = new File(downloadsDir , fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            Toast.makeText(this, "Written Succesfully", Toast.LENGTH_SHORT).show();
            }catch (FileNotFoundException e){
            throw new RuntimeException();
        }catch (IOException e){
            throw new RuntimeException();
        }



    }
}