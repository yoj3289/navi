//240214 현재 실 사용중인 클래스 절대 지우지 말것
package com.example.naviproj;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShowAllWeight extends AppCompatActivity {

    private Context context;

    private DataBaseHelper dbHelper;

    private String showBmi;

    private TextInputEditText startDate, endDate;

    private int minYear, minMonth, minDay,maxYear,maxMonth,maxDay;

    private List<ShowWeightItem> ShowWeightList;

    private ShowWeightAdapter showAdapter;


    /*public ShowAllWeight2(Context context) {
        this.context = context;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_all_weight2);

        Toolbar toolbar = findViewById(R.id.custom_toolbar);
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


        dbHelper = new DataBaseHelper(this);

        // RecyclerView 초기화 및 설정
        RecyclerView showRecyclerView = findViewById(R.id.showRecyclerView);

        // LinearLayoutManager를 가로 방향으로 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        showRecyclerView.setLayoutManager(layoutManager);

        // 체중정보 리사이클러뷰 어댑터 설정 (가상의 데이터 사용)
        showAdapter = new ShowWeightAdapter(getShowWeight());
        showRecyclerView.setAdapter(showAdapter);

        TextInputLayout startTextInputLayout = findViewById(R.id.dateStart);
        startDate = startTextInputLayout.findViewById(R.id.inputStartDate);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog("start");
            }

        });

        TextInputLayout endTextInputLayout = findViewById(R.id.dateEnd);
        endDate = endTextInputLayout.findViewById(R.id.inputEndDate);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog("end");
            }

        });


        Button search = findViewById(R.id.search);
        // "검색" 버튼을 눌렀을 때의 동작
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getShowWeight2();
                showAdapter.notifyDataSetChanged();
            }
        });
    }

    private List<ShowWeightItem> getShowWeight() {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        ShowWeightList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM userWeight ORDER BY Date ASC", null);
        int i = 0;

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(1);
                BigDecimal kg = new BigDecimal(cursor.getString(2));

                if (cursor.moveToPrevious()) {
                    BigDecimal previousKg = new BigDecimal(cursor.getString(2));
                    BigDecimal weightDifference = kg.subtract(previousKg);

                    if(weightDifference.compareTo(BigDecimal.ZERO)>0){
                        // DecimalFormat을 사용하여 형식을 지정
                        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                        String formattedDifference = decimalFormat.format(weightDifference);
                        ShowWeightList.add(new ShowWeightItem(date, Double.parseDouble(String.valueOf(kg)), "▲"+formattedDifference));
                    }
                    else if(weightDifference.compareTo(BigDecimal.ZERO)<0){
                        // DecimalFormat을 사용하여 형식을 지정
                        BigDecimal absoluteDifference = weightDifference.abs();
                        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                        String formattedDifference = decimalFormat.format(absoluteDifference);

                        ShowWeightList.add(new ShowWeightItem(date, Double.parseDouble(String.valueOf(kg)), "▼"+formattedDifference));
                    }
                    else{
                        // DecimalFormat을 사용하여 형식을 지정
                        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                        String formattedDifference = decimalFormat.format(weightDifference);
                        ShowWeightList.add(new ShowWeightItem(date, Double.parseDouble(String.valueOf(kg)), "±"+formattedDifference));
                    }
                    cursor.moveToNext();
                } else {
                    // 첫 번째 아이템은 차이가 없으므로 0으로 설정
                    ShowWeightList.add(new ShowWeightItem(date, Double.parseDouble(String.valueOf(kg)), "±00.00"));
                    cursor.moveToNext();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return ShowWeightList;
    }

    private void showDatePickerDialog(String point) { //point는 startDate와 endDate구분을 위한 것
        Calendar calendar = Calendar.getInstance();
        String dateVal[];
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Date FROM userWeight ORDER BY Date ASC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            dateVal=cursor.getString(0).split("-");
            minYear=Integer.parseInt(dateVal[0]);
            minMonth=Integer.parseInt(dateVal[1]);
            minDay=Integer.parseInt(dateVal[2]);
            cursor.close();
        }
        Cursor cursor2 = db.rawQuery("SELECT Date FROM userWeight ORDER BY Date DESC LIMIT 1", null);
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
                        if(point=="start"){
                            startDate.setText(selectedDate);
                        }
                        else{
                            endDate.setText(selectedDate);
                        }
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

    private List<ShowWeightItem> getShowWeight2() {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        ShowWeightList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String start=startDate.getText().toString();

        String fin=endDate.getText().toString();

        String query = "SELECT * FROM userWeight WHERE Date BETWEEN ? AND ? ORDER BY Date ASC";
        Cursor cursor = db.rawQuery(query, new String[]{start, fin});
        int i = 0;

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(1);
                BigDecimal kg = new BigDecimal(cursor.getString(2));

                if (cursor.moveToPrevious()) {
                    BigDecimal previousKg = new BigDecimal(cursor.getString(2));
                    BigDecimal weightDifference = kg.subtract(previousKg);

                    if(weightDifference.compareTo(BigDecimal.ZERO)>0){
                        // DecimalFormat을 사용하여 형식을 지정
                        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                        String formattedDifference = decimalFormat.format(weightDifference);
                        ShowWeightList.add(new ShowWeightItem(date, Double.parseDouble(String.valueOf(kg)), "▲"+formattedDifference));
                    }
                    else if(weightDifference.compareTo(BigDecimal.ZERO)<0){
                        // DecimalFormat을 사용하여 형식을 지정
                        BigDecimal absoluteDifference = weightDifference.abs();
                        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                        String formattedDifference = decimalFormat.format(absoluteDifference);

                        ShowWeightList.add(new ShowWeightItem(date, Double.parseDouble(String.valueOf(kg)), "▼"+formattedDifference));
                    }
                    else{
                        // DecimalFormat을 사용하여 형식을 지정
                        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                        String formattedDifference = decimalFormat.format(weightDifference);
                        ShowWeightList.add(new ShowWeightItem(date, Double.parseDouble(String.valueOf(kg)), "±"+formattedDifference));
                    }
                    cursor.moveToNext();
                } else {
                    // 첫 번째 아이템은 차이가 없으므로 0으로 설정
                    ShowWeightList.add(new ShowWeightItem(date, Double.parseDouble(String.valueOf(kg)), "±00.00"));
                    cursor.moveToNext();
                }
            } while (cursor.moveToNext()) ;
        }
        cursor.close();
        db.close();
        return ShowWeightList;
    }

    // 문자열을 Date 객체로 변환하는 메서드
    private static Date parseDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}