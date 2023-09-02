package com.example.kletech;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.LauncherActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


import java.lang.reflect.Array;
import java.util.ArrayList;

public class SheetListActivity extends AppCompatActivity {

    private ListView sheetView;
    private ArrayAdapter adapter;
    private ArrayList<String> listItems = new ArrayList();
    private long cid;
    private String className ;
    private String subjectName;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_list);




        cid = getIntent().getLongExtra("cid" , -1);
        className = getIntent().getStringExtra("className");
        subjectName = getIntent().getStringExtra("subjectName");

        loadListItems();
        sheetView = findViewById(R.id.sheet);
        adapter = new ArrayAdapter(this , R.layout.sheet_list , R.id.date_list_item , listItems);
        sheetView.setAdapter(adapter);

        sheetView.setOnItemClickListener((parent, view, position, id) -> openSheetActivity(position));
        setToolbar();
    }


    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar_page);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.saver);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        title.setText(className);
        subtitle.setText(subjectName);
        save.setVisibility(View.GONE);
        back.setOnClickListener(v -> onBackPressed());
    }
    private void openSheetActivity(int position) {
        long[] idArray = getIntent().getLongArrayExtra("idArray");
        int[] rollArray = getIntent().getIntArrayExtra("rollArray");
        String[] nameArray = getIntent().getStringArrayExtra("nameArray");
        Intent intent = new Intent(this , SheetActivity.class);
        intent.putExtra("className" , className);
        intent.putExtra("subjectName" , subjectName);
        intent.putExtra("idArray" , idArray);
        intent.putExtra("rollArray" , rollArray);
        intent.putExtra("nameArray" , nameArray);
        intent.putExtra("month",listItems.get(position));

        Log.d("SheetListActivity","THis is open sheet");

        startActivity(intent);

    }

    public void loadListItems(){
        Cursor cursor = new DbHelper(this).getDistinctMonths(cid);
        while(cursor.moveToNext()){
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.DATE_KEY));
            listItems.add(date.substring(3));
        }
    }
}