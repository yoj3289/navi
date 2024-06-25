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

public class alert_press extends AppCompatActivity {

    private DataBaseHelper dbHelper;

    private TextInputEditText criteriaDate; //기준날짜를 입력받기 위한 것

    private int minYear, minMonth, minDay, maxYear, maxMonth, maxDay;

    private List<AlertItem> ShowAlertList;

    private AlertAdapter AlertAdapter;

    private String strCriDate; //기준날짜를 쿼리문에 넣기 위한 것
    private double calCriSys, calCriSys2, calCriDia, calCriLeft, calCriRight; //기준날짜의 수축기 혈압

    private double calPulsePressure, calBothSide; //각각 맥박압 차이와 양팔차이 계산 위함

    private double calCriPul; //맥박 계산

    private SimpleDateFormat dateFormat;

    private double oneMonthBefore, thMonthBefore, sixMonthBefore; //각 1, 3, 6개월 전 평균을 저장 해 두기 위한 변수

    private Drawable drawable;

    private ImageView ic1, ic2, ic3, ic4, ic5, ic6, ic7;

    private TextView specific;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_page);

        Toolbar toolbar = findViewById(R.id.custom_toolbar);
        TextView altTitle=findViewById(R.id.alertTitle);
        altTitle.setText("혈압 이상 탐지");
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

        specific = findViewById(R.id.speText); //아이콘을 누르면 보일 textview

        ic1 = findViewById(R.id.icon1); //ic1부터 4까지는 각 아이콘을 뜻함
        ic1.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
        ic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExplain("icon1");
            }

        });

        ic2 = findViewById(R.id.icon2);
        ic2.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
        ic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExplain("icon2");
            }

        });

        ic3 = findViewById(R.id.icon3);
        ic3.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
        ic3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExplain("icon3");
            }

        });

        ic4 = findViewById(R.id.icon4);
        ic4.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
        ic4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExplain("icon4");
            }

        });


        ic5 = findViewById(R.id.icon5);
        ic5.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
        ic5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExplain("icon5");
            }

        });

        ic6 = findViewById(R.id.icon6);
        ic6.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
        ic6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExplain("icon6");
            }

        });

        ic7 = findViewById(R.id.icon7);
        ic7.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
        ic7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExplain("icon7");
            }

        });


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
                strCriDate = criteriaDate.getText().toString();
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
        Cursor cursor = db.rawQuery("SELECT Date FROM userPress ORDER BY Date ASC LIMIT 1", null); //날짜 최소값(데이터가 있는 날짜의 최소일자)
        if (cursor.moveToFirst()) {
            dateVal = cursor.getString(0).split("-");
            minYear = Integer.parseInt(dateVal[0]);
            minMonth = Integer.parseInt(dateVal[1]);
            minDay = Integer.parseInt(dateVal[2]);
            cursor.close();
        }
        Cursor cursor2 = db.rawQuery("SELECT Date FROM userPress ORDER BY Date DESC LIMIT 1", null); //날짜 최대값(데이터가 있는 날짜의 최대일자)
        if (cursor2.moveToFirst()) {
            dateVal = cursor2.getString(0).split("-");
            maxYear = Integer.parseInt(dateVal[0]);
            maxMonth = Integer.parseInt(dateVal[1]);
            maxDay = Integer.parseInt(dateVal[2]);
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
        Calendar minDate = Calendar.getInstance();
        minDate.set(minYear, minMonth - 1, minDay);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(maxYear, maxMonth - 1, maxDay);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.setCancelable(false);
        datePickerDialog.show();
    }

    private void getLatestData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Date FROM userPress ORDER BY Date DESC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            criteriaDate.setText(cursor.getString(0));

            strCriDate = cursor.getString(0);
        }
        cursor.close();
        db.close();
    }

    @SuppressLint("Range")
    private List<AlertItem> getAlertDataPress(String mode) {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        if (mode == "update") {
            ShowAlertList.clear();
        } else {
            ShowAlertList = new ArrayList<>();
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sqlQuery = "SELECT " +
                "AVG(CASE WHEN STRFTIME('%Y-%m', Date) = STRFTIME('%Y-%m', date(?, '-1 month')) THEN Systolic END) AS one_month_ago_avg, " +
                "AVG(CASE WHEN STRFTIME('%Y-%m', Date) = STRFTIME('%Y-%m', date(?, '-3 month')) THEN Systolic END) AS three_months_ago_avg, " +
                "AVG(CASE WHEN STRFTIME('%Y-%m', Date) = STRFTIME('%Y-%m', date(?, '-6 month')) THEN Systolic END) AS six_months_ago_avg " +
                "FROM userPress";
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{strCriDate, strCriDate, strCriDate});

        if (cursor.moveToFirst()) {
            oneMonthBefore = cursor.getDouble(cursor.getColumnIndex("one_month_ago_avg"));
            thMonthBefore = cursor.getDouble(cursor.getColumnIndex("three_months_ago_avg"));
            sixMonthBefore = cursor.getDouble(cursor.getColumnIndex("six_months_ago_avg"));
        }

        String sqlQuery2 = "SELECT AVG(Systolic) AS avgSys FROM userPress WHERE Date=?";
        Cursor cursor2 = db.rawQuery(sqlQuery2, new String[]{strCriDate});
        if (cursor2.moveToFirst()) {
            calCriSys = cursor2.getDouble(cursor2.getColumnIndex("avgSys"));
            if (calCriSys != 0.0) {
                if (oneMonthBefore != 0.0) {

                    if (calCriSys >= oneMonthBefore * 0.1 + oneMonthBefore) {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_up, null);
                        ShowAlertList.add(new AlertItem("1개월 비교", drawable, "기준일보다 10% 이상 증가했어요."));
                    } else if (calCriSys <= oneMonthBefore - oneMonthBefore * 0.1) {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_down, null);
                        ShowAlertList.add(new AlertItem("1개월 비교", drawable, "기준일보다 10% 이상 감소했어요."));
                    } else {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_smile, null);
                        ShowAlertList.add(new AlertItem("1개월 비교", drawable, "이상이 감지되지 않았어요!"));
                    }

                } else {
                    drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_empty, null);
                    ShowAlertList.add(new AlertItem("1개월 비교", drawable, "해당 기간에 기록된 혈압이 없어요."));
                }

                if (thMonthBefore != 0.0) {
                    if (calCriSys >= thMonthBefore * 0.1 + thMonthBefore) {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_up, null);
                        ShowAlertList.add(new AlertItem("3개월 비교", drawable, "기준일보다 10% 이상 증가했어요."));
                    } else if (calCriSys <= thMonthBefore - thMonthBefore * 0.1) {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_down, null);
                        ShowAlertList.add(new AlertItem("3개월 비교", drawable, "기준일보다 10% 이상 감소했어요."));
                    } else {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_smile, null);
                        ShowAlertList.add(new AlertItem("3개월 비교", drawable, "이상이 감지되지 않았어요!"));
                    }

                } else {
                    drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_empty, null);
                    ShowAlertList.add(new AlertItem("3개월 비교", drawable, "해당 기간에 기록된 혈압이 없어요."));
                }

                if (sixMonthBefore != 0.0) {
                    if (calCriSys >= sixMonthBefore * 0.1 + sixMonthBefore) {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_up, null);
                        ShowAlertList.add(new AlertItem("6개월 비교", drawable, "기준일보다 10% 이상 증가했어요."));
                    } else if (calCriSys <= sixMonthBefore - sixMonthBefore * 0.1) {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_down, null);
                        ShowAlertList.add(new AlertItem("6개월 비교", drawable, "기준일보다 10% 이상 감소했어요."));
                    } else {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_smile, null);
                        ShowAlertList.add(new AlertItem("6개월 비교", drawable, "이상이 감지되지 않았어요!"));
                    }

                } else {
                    drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_empty, null);
                    ShowAlertList.add(new AlertItem("6개월 비교", drawable, "해당 기간에 기록된 혈압이 없어요."));
                }

                //여기서부터는 맥압과 양팔차이 리사이클러뷰 띄우기
                String sqlQuery3 = "SELECT AVG(Systolic) AS avgSys,AVG(Diastolic) AS avgDia FROM userPress WHERE Date=?"; //맥박압
                Cursor cursor3 = db.rawQuery(sqlQuery3, new String[]{strCriDate});

                String sqlQuery4 = "SELECT AVG(CASE WHEN Side=? THEN Systolic END) AS avgLeft," +
                        "AVG(CASE WHEN Side=? THEN Systolic END) AS avgRight FROM userPress WHERE Date=?"; //양팔 차이
                Cursor cursor4 = db.rawQuery(sqlQuery4, new String[]{"left", "right", strCriDate});

                if (cursor3.moveToFirst() && cursor4.moveToFirst()) {
                    calCriSys2 = cursor3.getDouble(cursor3.getColumnIndex("avgSys"));

                    calCriDia = cursor3.getDouble(cursor3.getColumnIndex("avgDia")); //맥박압 계산

                    calCriLeft = cursor4.getDouble(cursor4.getColumnIndex("avgLeft"));

                    calCriRight = cursor4.getDouble(cursor4.getColumnIndex("avgRight")); //양팔차이 계산

                    if (calCriSys2 != 0 && calCriDia != 0 && calCriLeft != 0 && calCriRight != 0) {
                        calPulsePressure = calCriSys2 - calCriDia;//맥박압 계산

                        calBothSide = (calCriLeft - calCriRight > 0) ? calCriLeft - calCriRight : calCriRight - calCriLeft; //양팔차이 계산 왼쪽 오른쪽 차이 구하고 양수면 왼-오, 아니라면 오-왼(삼항연산자 사용)

                        if (calPulsePressure >= 60) {
                            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_up_down1, null);

                            ShowAlertList.add(new AlertItem("맥박압", drawable, "맥박압이 지나치게 높아요."));
                        } else if (calPulsePressure <= 20) {
                            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_up_down2, null);
                            ShowAlertList.add(new AlertItem("맥박압", drawable, "맥박압이 지나치게 낮아요."));
                        } else {
                            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_smile, null);
                            ShowAlertList.add(new AlertItem("맥박압", drawable, "이상이 감지되지 않았어요."));
                        }

                        if (calBothSide >= 10 && calBothSide < 20) {
                            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_left_right1, null);
                            ShowAlertList.add(new AlertItem("양쪽 차이", drawable, "양팔의 혈압차이가 조금 높아요."));
                        } else if (calBothSide >= 20 && calBothSide < 40) {
                            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_left_right2, null);
                            ShowAlertList.add(new AlertItem("양쪽 차이", drawable, "양팔의 혈압차이가 높아요."));
                        } else if (calBothSide >= 40) {
                            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_left_right3, null);
                            ShowAlertList.add(new AlertItem("양쪽 차이", drawable, "양팔의 혈압차이가 매우 높아요."));
                        } else {
                            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_smile, null);
                            ShowAlertList.add(new AlertItem("양쪽 차이", drawable, "이상이 감지되지 않았어요."));
                        }


                    } else {
                        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_empty, null);
                        ShowAlertList.add(new AlertItem("정보없음", drawable, "양팔 모두 혈압을 측정하세요."));
                    }

                    //여기서부터는 맥박 리사이클러뷰 띄우기
                    String sqlQuery5 = "SELECT AVG(Pulse) AS avgPul FROM userPress WHERE Date=?"; //맥박압
                    Cursor cursor5 = db.rawQuery(sqlQuery5, new String[]{strCriDate});

                    if (cursor5.moveToFirst()) {
                        calCriPul = cursor5.getDouble(cursor5.getColumnIndex("avgPul"));


                        if (calCriPul != 0) {
                            if (calCriPul >= 100) {
                                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_high_pulse, null);

                                ShowAlertList.add(new AlertItem("맥박", drawable, "맥박이 지나치게 높아요."));
                            } else if (calCriPul <= 50) {
                                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_low_pulse, null);
                                ShowAlertList.add(new AlertItem("맥박", drawable, "맥박이 지나치게 낮아요."));
                            } else {
                                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_smile, null);
                                ShowAlertList.add(new AlertItem("맥박", drawable, "이상이 감지되지 않았어요."));
                            }
                        } else {
                            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.graph_empty, null);
                            ShowAlertList.add(new AlertItem("정보없음", drawable, "양팔 모두 혈압을 측정하세요."));
                        }

                    }
                    cursor3.close();
                    cursor4.close();
                    cursor5.close();

                }
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
                ic7.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
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
                ic7.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
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
                ic7.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
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
                ic7.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
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
                ic7.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
        }

        else if(iconNum=="icon6"){
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
                ic7.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
        }

        else{
            ic7.setColorFilter(0);
            if(specific.getText()!=""&&specific.getText().toString().contains("맥박이")){
                specific.setText("");
                ic6.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
            else{
                specific.setText("맥박이 너무 빠르거나 느려요.\n증상이 지속되거나 다른 증상이 동반될 경우 반드시 의사와 상담하세요. ");
                ic1.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic2.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic3.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic4.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic5.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
                ic6.setColorFilter(Color.rgb(150, 150, 150), PorterDuff.Mode.MULTIPLY);
            }
        }
    }
}

