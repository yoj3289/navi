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
import java.util.List;

public class alert_sugar extends AppCompatActivity {

    private DataBaseHelper dbHelper;

    private TextInputEditText criteriaDate; //기준날짜를 입력받기 위한 것

    private int minYear, minMonth, minDay, maxYear, maxMonth, maxDay;

    private List<AlertItem> ShowAlertList;

    private AlertAdapter AlertAdapter;

    private String strCriDate; //기준날짜를 쿼리문에 넣기 위한 것
    private double calCriSys, calCriSys2, calCriDia, calCriLeft, calCriRight; //기준날짜의 수축기 혈압

    private double calPulsePressure, calBothSide; //각각 맥박압 차이와 양팔차이 계산 위함

    private SimpleDateFormat dateFormat;

    private double empty_oneMonthBefore, empty_thMonthBefore, empty_sixMonthBefore, full_oneMonthBefore, full_thMonthBefore, full_sixMonthBefore; //각 1, 3, 6개월 전 평균을 저장 해 두기 위한 변수

    private Drawable drawable;

    private ImageView ic1,ic2,ic3,ic4,ic5,ic6,ic7;

    private TextView specific;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_page);

        Toolbar toolbar = findViewById(R.id.custom_toolbar);
        TextView altTitle=findViewById(R.id.alertTitle);
        altTitle.setText("혈당 이상 탐지");
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

        ic7=findViewById(R.id.icon7);
        ic7.setVisibility(View.INVISIBLE);


        dbHelper = new DataBaseHelper(this);


        TextInputLayout kgInputLayout = findViewById(R.id.dateStart);//혈압참조
        criteriaDate = kgInputLayout.findViewById(R.id.inputStartDate);
        criteriaDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }

        });

        Button search = findViewById(R.id.search);
        // "검색" 버튼을 눌렀을 때의 동작
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strCriDate=criteriaDate.getText().toString();
                getAlertDataPress("update");
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

        // 혈압정보 리사이클러뷰 어댑터 설정 (가상의 데이터 사용)
        AlertAdapter = new AlertAdapter(getAlertDataPress(""));
        showRecyclerView.setAdapter(AlertAdapter);

    }



    private void showDatePickerDialog() { //point는 startDate와 endDate구분을 위한 것
        Calendar calendar = Calendar.getInstance();
        String dateVal[];
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Date FROM userSugar ORDER BY Date ASC LIMIT 1", null); //날짜 최소값(데이터가 있는 날짜의 최소일자)
        if (cursor.moveToFirst()) {
            dateVal=cursor.getString(0).split("-");
            minYear=Integer.parseInt(dateVal[0]);
            minMonth=Integer.parseInt(dateVal[1]);
            minDay=Integer.parseInt(dateVal[2]);
            cursor.close();
        }
        Cursor cursor2 = db.rawQuery("SELECT Date FROM userSugar ORDER BY Date DESC LIMIT 1", null); //날짜 최대값(데이터가 있는 날짜의 최대일자)
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
        Cursor cursor = db.rawQuery("SELECT Date FROM userSugar ORDER BY Date DESC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            criteriaDate.setText(cursor.getString(0));

            strCriDate= cursor.getString(0);
        }
        cursor.close();
        db.close();
    }

    @SuppressLint("Range")
    private List<AlertItem> getAlertDataPress(String mode) {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        if(mode=="update"){
            ShowAlertList.clear();
        }
        else{
            ShowAlertList = new ArrayList<>();
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sqlQuery = "SELECT " + //1,3,6개월 전의 정보를 가져옴
                "AVG(CASE WHEN (STRFTIME('%Y-%m', Date) = STRFTIME('%Y-%m', date(?, '-1 month')) AND Time LIKE '%식전') THEN BloodSugar END) AS empty_one_month_ago_avg, " +
                "AVG(CASE WHEN (STRFTIME('%Y-%m', Date) = STRFTIME('%Y-%m', date(?, '-3 month')) AND Time LIKE '%식전') THEN BloodSugar END) AS empty_three_months_ago_avg, " +
                "AVG(CASE WHEN (STRFTIME('%Y-%m', Date) = STRFTIME('%Y-%m', date(?, '-6 month')) AND Time LIKE '%식전') THEN BloodSugar END) AS empty_six_months_ago_avg " + //공복 혈당 정보들
                "FROM userSugar";

        String sqlQueryA = "SELECT " + //1,3,6개월 전의 정보를 가져옴
                "AVG(CASE WHEN (STRFTIME('%Y-%m', Date) = STRFTIME('%Y-%m', date(?, '-1 month')) AND Time LIKE '%식후') THEN BloodSugar END) AS full_one_month_ago_avg, " +
                "AVG(CASE WHEN (STRFTIME('%Y-%m', Date) = STRFTIME('%Y-%m', date(?, '-3 month')) AND Time LIKE '%식후') THEN BloodSugar END) AS full_three_months_ago_avg, " +
                "AVG(CASE WHEN (STRFTIME('%Y-%m', Date) = STRFTIME('%Y-%m', date(?, '-6 month')) AND Time LIKE '%식후') THEN BloodSugar END) AS full_six_months_ago_avg " + //식후 혈당 정보들
                "FROM userSugar";
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {strCriDate, strCriDate, strCriDate});

        Cursor cursorA = db.rawQuery(sqlQueryA, new String[] {strCriDate, strCriDate, strCriDate});

        if(cursor.moveToFirst()) {
            empty_oneMonthBefore = cursor.getDouble(cursor.getColumnIndex("empty_one_month_ago_avg"));
            empty_thMonthBefore = cursor.getDouble(cursor.getColumnIndex("empty_three_months_ago_avg"));
            empty_sixMonthBefore = cursor.getDouble(cursor.getColumnIndex("empty_six_months_ago_avg"));
        }
        if(cursorA.moveToFirst()){
            full_oneMonthBefore=cursorA.getDouble(cursorA.getColumnIndex("full_one_month_ago_avg"));
            full_thMonthBefore=cursorA.getDouble(cursorA.getColumnIndex("full_three_months_ago_avg"));
            full_sixMonthBefore=cursorA.getDouble(cursorA.getColumnIndex("full_six_months_ago_avg"));
        }


        String sqlQuery2="SELECT BloodSugar FROM userSugar WHERE Time LIKE '%식전' AND Date=?"; //기준일의 정보를 가져옴
        Cursor cursor2=db.rawQuery(sqlQuery2, new String[] {strCriDate});
        if(cursor2.moveToFirst()) {
            calCriSys = cursor2.getDouble(cursor2.getColumnIndex("BloodSugar"));
            if (calCriSys != 0.0) {
                if (empty_oneMonthBefore != 0.0) {

                    if (calCriSys >= empty_oneMonthBefore * 0.1 + empty_oneMonthBefore) {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_up, null);
                        ShowAlertList.add(new AlertItem("공복혈당 1개월 비교", drawable, "기준일보다 10% 이상 증가했어요."));
                    } else if (calCriSys <= empty_oneMonthBefore - empty_oneMonthBefore * 0.1) {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_down, null);
                        ShowAlertList.add(new AlertItem("공복혈당 1개월 비교", drawable, "기준일보다 10% 이상 감소했어요."));
                    } else {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_smile, null);
                        ShowAlertList.add(new AlertItem("공복혈당 1개월 비교", drawable, "이상이 감지되지 않았어요!"));
                    }

                } else {
                    drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_empty, null);
                    ShowAlertList.add(new AlertItem("공복혈당 1개월 비교", drawable, "해당 기간에 기록된 혈당이 없어요."));
                }

                if (empty_thMonthBefore != 0.0) {
                    if (calCriSys >= empty_thMonthBefore * 0.1 + empty_thMonthBefore) {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_up, null);
                        ShowAlertList.add(new AlertItem("공복혈당 3개월 비교", drawable, "기준일보다 10% 이상 증가했어요."));
                    } else if (calCriSys <= empty_thMonthBefore - empty_thMonthBefore * 0.1) {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_down, null);
                        ShowAlertList.add(new AlertItem("공복혈당 3개월 비교", drawable, "기준일보다 10% 이상 감소했어요."));
                    } else {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_smile, null);
                        ShowAlertList.add(new AlertItem("공복혈당 3개월 비교", drawable, "이상이 감지되지 않았어요!"));
                    }

                } else {
                    drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_empty, null);
                    ShowAlertList.add(new AlertItem("공복혈당 3개월 비교", drawable, "해당 기간에 기록된 혈당이 없어요."));
                }

                if (empty_sixMonthBefore != 0.0) {
                    if (calCriSys >= empty_sixMonthBefore * 0.1 + empty_sixMonthBefore) {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_up, null);
                        ShowAlertList.add(new AlertItem("공복혈당 6개월 비교", drawable, "기준일보다 10% 이상 증가했어요."));
                    } else if (calCriSys <= empty_sixMonthBefore - empty_sixMonthBefore * 0.1) {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_down, null);
                        ShowAlertList.add(new AlertItem("공복혈당 6개월 비교", drawable, "기준일보다 10% 이상 감소했어요."));
                    } else {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_smile, null);
                        ShowAlertList.add(new AlertItem("공복혈당 6개월 비교", drawable, "이상이 감지되지 않았어요!"));
                    }

                } else {
                    drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_empty, null);
                    ShowAlertList.add(new AlertItem("공복혈당 6개월 비교", drawable, "해당 기간에 기록된 혈당이 없어요."));
                }



                //여기서부터는 식후혈당
                String sqlQuery3="SELECT BloodSugar FROM userSugar WHERE Time LIKE '%식후' AND Date=?"; //기준일의 정보를 가져옴
                Cursor cursor3=db.rawQuery(sqlQuery3, new String[] {strCriDate});
                if(cursor3.moveToFirst()) {
                    calCriSys2 = cursor3.getDouble(cursor3.getColumnIndex("BloodSugar"));
                    if (calCriSys2 != 0.0) {
                        if (full_oneMonthBefore != 0.0) {

                            if (calCriSys2 >= full_oneMonthBefore * 0.1 + full_oneMonthBefore) {
                                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_up, null);
                                ShowAlertList.add(new AlertItem("식후혈당 1개월 비교", drawable, "기준일보다 10% 이상 증가했어요."));
                            } else if (calCriSys2 <= full_oneMonthBefore - full_oneMonthBefore * 0.1) {
                                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_down, null);
                                ShowAlertList.add(new AlertItem("식후혈당 1개월 비교", drawable, "기준일보다 10% 이상 감소했어요."));
                            } else {
                                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_smile, null);
                                ShowAlertList.add(new AlertItem("식후혈당 1개월 비교", drawable, "이상이 감지되지 않았어요!"));
                            }

                        } else {
                            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_empty, null);
                            ShowAlertList.add(new AlertItem("식후혈당 1개월 비교", drawable, "해당 기간에 기록된 혈당이 없어요."));
                        }

                        if (full_thMonthBefore != 0.0) {
                            if (calCriSys2 >= full_thMonthBefore * 0.1 + full_thMonthBefore) {
                                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_up, null);
                                ShowAlertList.add(new AlertItem("식후혈당 3개월 비교", drawable, "기준일보다 10% 이상 증가했어요."));
                            } else if (calCriSys2 <= full_thMonthBefore - full_thMonthBefore * 0.1) {
                                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_down, null);
                                ShowAlertList.add(new AlertItem("식후혈당 3개월 비교", drawable, "기준일보다 10% 이상 감소했어요."));
                            } else {
                                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_smile, null);
                                ShowAlertList.add(new AlertItem("식후혈당 3개월 비교", drawable, "이상이 감지되지 않았어요!"));
                            }

                        } else {
                            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_empty, null);
                            ShowAlertList.add(new AlertItem("식후혈당 3개월 비교", drawable, "해당 기간에 기록된 혈당이 없어요."));
                        }

                        if (full_sixMonthBefore != 0.0) {
                            if (calCriSys2 >= full_sixMonthBefore * 0.1 + full_sixMonthBefore) {
                                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_up, null);
                                ShowAlertList.add(new AlertItem("식후혈당 6개월 비교", drawable, "기준일보다 10% 이상 증가했어요."));
                            } else if (calCriSys2 <= full_sixMonthBefore - full_sixMonthBefore * 0.1) {
                                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_down, null);
                                ShowAlertList.add(new AlertItem("식후혈당 6개월 비교", drawable, "기준일보다 10% 이상 감소했어요."));
                            } else {
                                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_smile, null);
                                ShowAlertList.add(new AlertItem("식후혈당 6개월 비교", drawable, "이상이 감지되지 않았어요!"));
                            }

                        } else {
                            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_empty, null);
                            ShowAlertList.add(new AlertItem("식후혈당 6개월 비교", drawable, "해당 기간에 기록된 혈당이 없어요."));
                        }
                    }
                }
                cursor3.close();
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
                ic5.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic6.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
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
                ic5.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic6.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
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
                ic5.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic6.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
        }

        else if(iconNum=="icon4"){
            ic4.setColorFilter(0);
            if(specific.getText()!=""&&specific.getText().toString().contains("꾸준")){
                specific.setText("");
                ic4.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
            else{
                specific.setText("해당 시기에 입력된 정보가 없어요\n꾸준히 기록하면 나비가 도움을 줄 수 있어요!");
                ic1.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic2.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic3.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic5.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic6.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
        }

        else if(iconNum=="icon5"){
            ic5.setColorFilter(0);
            if(specific.getText()!=""&&specific.getText().toString().contains("심부전")){
                specific.setText("");
                ic5.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
            else{
                specific.setText("수축기와 이완기의 차이가 크거나 작아요.\n심장질환, 심부전 등을 의심할 수 있어요.\n증상이 지속되면 반드시 의사와 상담하세요.");
                ic1.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic2.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic3.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic4.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic6.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
        }

        else{
            ic6.setColorFilter(0);
            if(specific.getText()!=""&&specific.getText().toString().contains("동맥경화")){
                specific.setText("");
                ic6.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
            else{
                specific.setText("양팔의 수축기 혈압 차이가 커요.\n동맥경화 등을 의심할 수 있어요.\n증상이 지속되면 반드시 의사와 상담하세요.");
                ic1.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic2.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic3.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic4.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic5.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
        }
    }
}
