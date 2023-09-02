package com.example.kletech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class StudentActivity extends AppCompatActivity {

    Toolbar toolbar;
    private String className,subjectName;
    private int position;
    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<StudentItem> studentItems = new ArrayList<>();
    private DbHelper dbHelper;
    private long cid;
    private MyCalender calender;
    private TextView subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);


        dbHelper = new DbHelper(this);
        Intent it = getIntent();
        className = it.getStringExtra("className");
        subjectName = it.getStringExtra("subjectName");
        position = it.getIntExtra("position",-1);
        cid = it.getLongExtra("cid" , -1);
        calender = new MyCalender();

        setToolbar();
        loadData();
        recyclerView = findViewById(R.id.student_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StudentAdapter(this , studentItems);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(position-> changeStatus(position));
        loadStatusData();

    }

    private void loadData() {

        Cursor cursor = dbHelper.getStudentTable(cid);
        studentItems.clear();
        while(cursor.moveToNext())
        {
            long sid = cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.S_ID));
            int roll = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.STUDENT_ROLL_KEY));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.STUDENT_NAME_KEY));

            studentItems.add(new StudentItem(sid , roll , name));
        }
        cursor.close();
    }

    private void changeStatus(int position) {

        String status = studentItems.get(position).getStatus();

        if(status.equals("P")) status = "A";
        else status = "P";

        studentItems.get(position).setStatus(status);
        adapter.notifyItemChanged(position);

    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar_page);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.saver);
        subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        title.setText(className);
        subtitle.setText(subjectName+" | "+calender.getDate());

        save.setOnClickListener(v -> saveStatus());

        back.setOnClickListener(v -> onBackPressed());
        toolbar.inflateMenu(R.menu.student_menu);
        toolbar.setOnMenuItemClickListener(menuItem -> onMenuItemClick(menuItem));

    }

    private void saveStatus() {
        for (StudentItem studentItem: studentItems) {
            String status = studentItem.getStatus();
            if(status != "P") status = "A";
            long value = dbHelper.addStatus(studentItem.getSid(),cid, calender.getDate(),status);

            if (value == -1) dbHelper.updateStatus(studentItem.getSid(), calender.getDate(),status);

        }
    }

    private void loadStatusData(){
        for (StudentItem studentItem: studentItems) {
            String status = dbHelper.getStatus(studentItem.getSid(), calender.getDate());
            if(status!= null) studentItem.setStatus(status);
            else studentItem.setStatus("");

        }
        adapter.notifyDataSetChanged();
    }
    private boolean onMenuItemClick(MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.add_student)
        {
            showAddStudentDialog();
        }
        else if (menuItem.getItemId() == R.id.show_calender)
        {
            showCalendar();
        }
        else if (menuItem.getItemId() == R.id.show_attendence_sheet)
        {
            openSheetList();
        }
        return true;
    }

    private void openSheetList() {
        long[] idArray = new long[studentItems.size()];
        int[] rollArray = new int[studentItems.size()];
        String[] nameArray = new String[studentItems.size()];

        for(int i=0;i<idArray.length;i++)
            idArray[i]= studentItems.get(i).getSid();

        for(int i=0;i<rollArray.length;i++)
            rollArray[i]= studentItems.get(i).getId();

        for(int i=0;i<nameArray.length;i++)
            nameArray[i]= studentItems.get(i).getName();

        Intent intent = new Intent(this , SheetListActivity.class);
        intent.putExtra("cid" , cid);
        intent.putExtra("className" , className);
        intent.putExtra("subjectName" , subjectName);
        intent.putExtra("idArray" , idArray);
        intent.putExtra("rollArray" , rollArray);
        intent.putExtra("nameArray" , nameArray);
        startActivity(intent);
    }

    private void showCalendar() {
        calender.show(getSupportFragmentManager(), "");
        calender.setOnCalenderClickListener(this::onCalenderOkClickListener);
    }

    private void onCalenderOkClickListener(int year, int month, int day) {
        calender.setDate(year, month, day);
        subtitle.setText(subjectName+" | "+calender.getDate());
        loadStatusData();
    }

    private void showAddStudentDialog() {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(),MyDialog.STUDENT_ADD_DIALOG);
        dialog.setListener((roll,name)-> addStudent(roll , name));
    }

    private void addStudent(String roll_string, String name) {
        int roll = Integer.valueOf(roll_string);
        long sid  = dbHelper.addStudent(cid , Integer.valueOf(roll) , name);
        StudentItem studentItem = new StudentItem(sid , roll , name);
        studentItems.add(studentItem);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case 0:
                showUpdateStudentDialog(item.getGroupId());
                break;
            case 1:
                deleteStudent(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateStudentDialog(int position) {
        MyDialog dialog = new MyDialog(studentItems.get(position).getId() , studentItems.get(position).getName());
        dialog.show(getSupportFragmentManager(), MyDialog.STUDENT_UPDATE_DIALOG);
        dialog.setListener(( roll_string , name)->{
            updateStudent(position ,name);
        });
    }

    private void updateStudent(int position, String name) {
        dbHelper.updateStudent(studentItems.get(position).getSid() , name);
        studentItems.get(position).setName(name);
        adapter.notifyItemChanged(position);
    }


    private void deleteStudent(int position) {
        dbHelper.deleteStudent(studentItems.get(position).getSid());
        studentItems.remove(position);
        adapter.notifyItemRemoved(position);
    }
}