package com.example.naviproj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class alert_weight extends AppCompatActivity {

    private DataBaseHelper dbHelper;

    private TextInputEditText criteriaDate; //기준날짜를 입력받기 위한 것

    private int minYear, minMonth, minDay, maxYear, maxMonth, maxDay;

    private List<AlertItem> ShowAlertList;

    private AlertAdapter AlertAdapter;

    private String strCriDate; //기준날짜를 쿼리문에 넣기 위한 것
    private double calCriKg; //기준날짜의 체중

    private SimpleDateFormat dateFormat;

    private double oneMonthBefore, thMonthBefore, sixMonthBefore; //각 1, 3, 6개월 전 평균을 저장 해 두기 위한 변수

    private Drawable drawable;

    private ImageView ic1,ic2,ic3,ic4,ic5,ic6,ic7;

    private TextView specific;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_page);

        Toolbar toolbar = findViewById(R.id.custom_toolbar);
        TextView altTitle=findViewById(R.id.alertTitle);
        altTitle.setText("체중 이상 탐지");
        setSupportActionBar(toolbar);
        // 기본 제목을 숨김
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뒤로가기 버튼 클릭 시 수행할 동작 추가
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        specific=findViewById(R.id.speText); //아이콘을 누르면 보일 textview

        ic1=findViewById(R.id.icon1); //ic1부터 4까지는 각 아이콘을 뜻함
        ic1.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
        ic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExplain("icon1");
            }

        });

        ic2=findViewById(R.id.icon2);
        ic2.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
        ic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExplain("icon2");
            }

        });

        ic3=findViewById(R.id.icon3);
        ic3.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
        ic3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExplain("icon3");
            }

        });

        ic4=findViewById(R.id.icon4);
        ic4.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
        ic4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExplain("icon4");
            }

        });

        ic5=findViewById(R.id.icon5);
        ic5.setVisibility(View.INVISIBLE);

        ic6=findViewById(R.id.icon6);
        ic6.setVisibility(View.INVISIBLE);



        dbHelper = new DataBaseHelper(this);

        // 현재 날짜 가져오기
        Date currentDate = new Date();

        // 날짜 포맷 설정 (예: "yyyy-MM-dd HH:mm:ss" 또는 다른 포맷)
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        TextInputLayout kgInputLayout = findViewById(R.id.dateStart);//체중참조
        criteriaDate = kgInputLayout.findViewById(R.id.inputStartDate);
        criteriaDate.setOnClickListener(new View.OnClickListener() {
            //AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllWeight.this);
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }

        });

        //ImageView search = findViewById(R.id.search);
        Button search = findViewById(R.id.search);
        // "검색" 버튼을 눌렀을 때의 동작
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strCriDate=criteriaDate.getText().toString();
                getAlertDataWeight("update");
                AlertAdapter.notifyDataSetChanged();
            }
        });

        // 날짜를 문자열로 변환하여 가져오기

        getLatestData();



        // RecyclerView 초기화 및 설정
        RecyclerView showRecyclerView = findViewById(R.id.showRecyclerView);

        // LinearLayoutManager를 세로 방향으로 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        showRecyclerView.setLayoutManager(layoutManager);

        // 체중정보 리사이클러뷰 어댑터 설정 (가상의 데이터 사용)
        AlertAdapter = new AlertAdapter(getAlertDataWeight(""));
        showRecyclerView.setAdapter(AlertAdapter);

    }



    private void showDatePickerDialog() { //point는 startDate와 endDate구분을 위한 것
        Calendar calendar = Calendar.getInstance();
        String dateVal[];
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Date FROM userWeight ORDER BY Date ASC LIMIT 1", null); //날짜 최소값(데이터가 있는 날짜의 최소일자)
        if (cursor.moveToFirst()) {
            dateVal=cursor.getString(0).split("-");
            minYear=Integer.parseInt(dateVal[0]);
            minMonth=Integer.parseInt(dateVal[1]);
            minDay=Integer.parseInt(dateVal[2]);
            cursor.close();
        }
        Cursor cursor2 = db.rawQuery("SELECT Date FROM userWeight ORDER BY Date DESC LIMIT 1", null); //날짜 최대값(데이터가 있는 날짜의 최대일자)
        if (cursor2.moveToFirst()) {
            dateVal=cursor2.getString(0).split("-");
            maxYear=Integer.parseInt(dateVal[0]);
            maxMonth=Integer.parseInt(dateVal[1]);
            maxDay=Integer.parseInt(dateVal[2]);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.Theme_CustomCalendar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 사용자가 선택한 날짜 처리
                        String selectedDate = String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                        criteriaDate.setText(selectedDate);
                    }
                },
                // 초기 날짜 설정 (년, 월, 일)
                maxYear, maxMonth, maxDay
        );

        cursor.close();
        db.close();
        Calendar minDate= Calendar.getInstance();
        minDate.set(minYear,minMonth-1,minDay);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(maxYear,maxMonth-1,maxDay);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.setCancelable(false);
        datePickerDialog.show();
    }

    private void getLatestData(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Date FROM userWeight ORDER BY Date DESC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            criteriaDate.setText(cursor.getString(0));

            strCriDate= cursor.getString(0);
        }
        cursor.close();
        db.close();
    }

    @SuppressLint("Range")
    private List<AlertItem> getAlertDataWeight(String mode) {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        if(mode=="update"){
            ShowAlertList.clear(); //내용이 변경되면 clear로 한번 지우고 다시 그려야 하기 때문
        }
        else{
            ShowAlertList = new ArrayList<>();
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sqlQuery = "SELECT " +
                "AVG(CASE WHEN STRFTIME('%Y-%m', Date) = STRFTIME('%Y-%m', date(?, '-1 month')) THEN Kg END) AS one_month_ago_avg, " +
                "AVG(CASE WHEN STRFTIME('%Y-%m', Date) = STRFTIME('%Y-%m', date(?, '-3 month')) THEN Kg END) AS three_months_ago_avg, " +
                "AVG(CASE WHEN STRFTIME('%Y-%m', Date) = STRFTIME('%Y-%m', date(?, '-6 month')) THEN Kg END) AS six_months_ago_avg " +
                "FROM userWeight";
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {strCriDate, strCriDate, strCriDate});
        if(cursor.moveToFirst()){
            oneMonthBefore=cursor.getDouble(cursor.getColumnIndex("one_month_ago_avg"));
            thMonthBefore=cursor.getDouble(cursor.getColumnIndex("three_months_ago_avg"));
            sixMonthBefore=cursor.getDouble(cursor.getColumnIndex("six_months_ago_avg"));
        }


        String sqlQuery2="SELECT * FROM userWeight WHERE Date=?";
        Cursor cursor2=db.rawQuery(sqlQuery2, new String[] {strCriDate});

        if(cursor2.moveToFirst()){
            calCriKg=Double.parseDouble(cursor2.getString(2));

            if(oneMonthBefore!=0.0){
                if(calCriKg>=oneMonthBefore*0.1+oneMonthBefore){
                    drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.graph_up,null);
                    ShowAlertList.add(new AlertItem("1개월 비교",drawable,"기준일보다 10% 이상 증가했어요." ));
                }
                else if(calCriKg<=oneMonthBefore-oneMonthBefore*0.1){
                    drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.graph_down,null);
                    ShowAlertList.add(new AlertItem("1개월 비교",drawable,"기준일보다 10% 이상 감소했어요." ));
                }
                else{
                    drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.graph_smile,null);
                    ShowAlertList.add(new AlertItem("1개월 비교",drawable,"이상이 감지되지 않았어요!"));
                }

            }
            else{
                drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.graph_empty,null);
                ShowAlertList.add(new AlertItem("1개월 비교",drawable,"해당 기간에 기록된 체중이 없어요."));
            }

            if(thMonthBefore!=0.0){
                if(calCriKg>=thMonthBefore*0.1+thMonthBefore){
                    drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.graph_up,null);
                    ShowAlertList.add(new AlertItem("3개월 비교",drawable,"기준일보다 10% 이상 증가했어요." ));
                }
                else if(calCriKg<=thMonthBefore-thMonthBefore*0.1){
                    drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.graph_down,null);
                    ShowAlertList.add(new AlertItem("3개월 비교",drawable,"기준일보다 10% 이상 감소했어요." ));
                }
                else{
                    drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.graph_smile,null);
                    ShowAlertList.add(new AlertItem("3개월 비교",drawable,"이상이 감지되지 않았어요!"));
                }

            }
            else{
                drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.graph_empty,null);
                ShowAlertList.add(new AlertItem("3개월 비교",drawable,"해당 기간에 기록된 체중이 없어요."));
            }

            if(sixMonthBefore!=0.0){
                if(calCriKg>=sixMonthBefore*0.1+sixMonthBefore){
                    drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.graph_up,null);
                    ShowAlertList.add(new AlertItem("6개월 비교",drawable,"기준일보다 10% 이상 증가했어요." ));
                }
                else if(calCriKg<=sixMonthBefore-sixMonthBefore*0.1){
                    drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.graph_down,null);
                    ShowAlertList.add(new AlertItem("6개월 비교",drawable,"기준일보다 10% 이상 감소했어요." ));
                }
                else{
                    drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.graph_smile,null);
                    ShowAlertList.add(new AlertItem("6개월 비교",drawable,"이상이 감지되지 않았어요!"));
                }

            }
            else{
                drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.graph_empty,null);
                ShowAlertList.add(new AlertItem("6개월 비교",drawable,"해당 기간에 기록된 체중이 없어요."));
            }
        }
        cursor.close();
        cursor2.close();
        db.close();
        return ShowAlertList;
    }

    private void showExplain(String iconNum){//아이콘을 누르면 아이콘과 관련된 설명이 나옴
        if(iconNum=="icon1"){
            ic1.setColorFilter(0);
            if(specific.getText()!=""&&specific.getText().toString().contains("차이")){
                specific.setText("");
                ic1.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
            else{
                specific.setText("기준일과 비교했을 때 큰 차이가 없어요.\n지금과 같은 상태를 유지하세요!");
                ic2.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic3.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic4.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }

        }

        else if(iconNum=="icon2"){
            ic2.setColorFilter(0);
            if(specific.getText()!=""&&specific.getText().toString().contains("증가")){
                specific.setText("");
                ic2.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
            else{
                specific.setText("기준일의 n개월 전보다 10%이상 증가했어요.\n증상이 지속되면 의사와 상담하세요.");
                ic1.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic3.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic4.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
        }

        else if(iconNum=="icon3"){
            ic3.setColorFilter(0);
            if(specific.getText()!=""&&specific.getText().toString().contains("감소")){
                specific.setText("");
                ic3.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
            else{
                specific.setText("기준일의 n개월 전보다 10%이상 감소했어요.\n증상이 지속되면 의사와 상담하세요.");
                ic1.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic2.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic4.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
        }

        else{
            ic4.setColorFilter(0);
            if(specific.getText()!=""&&specific.getText().toString().contains("꾸준")){
                specific.setText("");
                ic4.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
            else{
                specific.setText("해당 시기에 입력된 체중이 없어요\n꾸준히 기록하면 나비가 도움을 줄 수 있어요!");
                ic1.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic2.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic3.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
        }
    }


}
