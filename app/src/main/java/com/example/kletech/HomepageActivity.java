package com.example.kletech;

import static com.example.kletech.DbHelper.C_ID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class HomepageActivity extends AppCompatActivity{



    FloatingActionButton fab;
    RecyclerView recyclerView;
    ClassAdapter classAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<ClassItem> classItems = new ArrayList<>();
    EditText class_edit;
    EditText subject_edit;
    DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        dbHelper = new DbHelper(this);
        fab = findViewById(R.id.fab1);
        fab.setOnClickListener(v -> showDialog());

        loadData();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        classAdapter = new ClassAdapter(this,classItems);
        recyclerView.setAdapter(classAdapter);
        classAdapter.setOnItemClickListener(position -> gotoItemActivity(position));


    }

    private void loadData() {

        Cursor cursor = dbHelper.getClassTable();

        classItems.clear();
        while(cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(C_ID));
            String className = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.CLASS_NAME_KEY));
            String subjectName = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.SUBJECT_NAME_KEY));

            classItems.add(new ClassItem(id , className, subjectName));
        }

    }

    private void gotoItemActivity(int position) {
        Intent i = new Intent(this , StudentActivity.class);

        i.putExtra("className",classItems.get(position).getClassName());
        i.putExtra("subjectName",classItems.get(position).getSubjectName());
        i.putExtra("position" , position);
        i.putExtra("cid" , classItems.get(position).getCid());
        startActivity(i);
    }

    private void showDialog() {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(),MyDialog.CLASS_ADD_DIALOG);
        dialog.setListener((className , subjectName)->{
            addClass(className,subjectName);
        });

    }

    private void addClass(String className , String subjectName) {
        long cid = dbHelper.addClass(className,subjectName);
        ClassItem classItem = new ClassItem(cid, className,subjectName);
        classItems.add(classItem);
        classAdapter.notifyDataSetChanged();


    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case 0:
                showUpdateDialog(item.getGroupId());
                break;
            case 1:
                deleteClass(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(int position) {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager() , MyDialog.CLASS_UPDATE_DIALOG);
        dialog.setListener((className , subjectName)->{
            updateTheClass(position , className , subjectName);
        });
    }

    private void updateTheClass(int position , String className , String subjectName)
    {
        dbHelper.updateClass(classItems.get(position).getCid() , className , subjectName);
        classItems.get(position).setClassName(className);
        classItems.get(position).setSubjectName(subjectName);
        classAdapter.notifyItemChanged(position);
    }
    private void deleteClass(int position) {
        dbHelper.deleteClass(classItems.get(position).getCid());
        classItems.remove(position);
        classAdapter.notifyItemRemoved(position);
    }

}

