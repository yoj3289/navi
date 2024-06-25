//혈압 전체보기에 사용되는 클래스. 240216사용중 지우지 말것
package com.example.naviproj;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Dimension;
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

public class ShowAllPress extends AppCompatActivity {

    private Context context;

    private DataBaseHelper dbHelper;

    private String showBmi;

    private TextInputEditText startDate, endDate;

    private int minYear, minMonth, minDay, maxYear, maxMonth, maxDay;

    private String userSide = ""; //왼팔 오른팔에 사용
    private String userSide2=""; //수축기, 이완기, 맥박에 사용

    private TextView left, right;

    private TextView sys,dia,pul;//각 수축기 이완기 맥박

    private List<ShowPressItem> ShowPressList;

    private ShowPressAdapter showAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_all_press);

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
        showAdapter = new ShowPressAdapter(getShowPress());
        showRecyclerView.setAdapter(showAdapter);

        TextInputLayout startTextInputLayout = findViewById(R.id.dateStart);
        startDate = startTextInputLayout.findViewById(R.id.inputStartDate);
        startDate.setOnClickListener(new View.OnClickListener() {
            //AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllWeight.this);
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


        left = findViewById(R.id.sideLeft);
        right = findViewById(R.id.sideRight);

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSide("left");
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSide("right");
            }
        });

        //ImageView search = findViewById(R.id.search);
        Button search = findViewById(R.id.search);
        // "검색" 버튼을 눌렀을 때의 동작
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getShowPress2();
                showAdapter.notifyDataSetChanged();
            }
        });

        sys = findViewById(R.id.Systolic);
        dia = findViewById(R.id.Diastolic);
        pul = findViewById(R.id.Pulse);

        sys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSide2("sys");
            }
        });
        dia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSide2("dia");
            }
        });
        pul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSide2("pul");
            }
        });

    }




    private List<ShowPressItem> getShowPress() {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        ShowPressList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM userPress ORDER BY Date ASC", null);
        int i = 0;

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(1);
                BigDecimal kg = new BigDecimal(cursor.getString(4));

                if (cursor.moveToPrevious()) {
                    BigDecimal previousKg = new BigDecimal(cursor.getString(4));
                    BigDecimal weightDifference = kg.subtract(previousKg);

                    if (weightDifference.compareTo(BigDecimal.ZERO) > 0) {
                        // DecimalFormat을 사용하여 형식을 지정
                        DecimalFormat decimalFormat = new DecimalFormat("#00");
                        String formattedDifference = decimalFormat.format(weightDifference);
                        ShowPressList.add(new ShowPressItem(date, Integer.parseInt(String.valueOf(kg)), "▲" + formattedDifference));
                    } else if (weightDifference.compareTo(BigDecimal.ZERO) < 0) {
                        // DecimalFormat을 사용하여 형식을 지정
                        BigDecimal absoluteDifference = weightDifference.abs();
                        DecimalFormat decimalFormat = new DecimalFormat("#00");
                        String formattedDifference = decimalFormat.format(absoluteDifference);

                        ShowPressList.add(new ShowPressItem(date, Integer.parseInt(String.valueOf(kg)), "▼" + formattedDifference));
                    } else {
                        // DecimalFormat을 사용하여 형식을 지정
                        DecimalFormat decimalFormat = new DecimalFormat("#00");
                        String formattedDifference = decimalFormat.format(weightDifference);
                        ShowPressList.add(new ShowPressItem(date, Integer.parseInt(String.valueOf(kg)), "±" + formattedDifference));
                    }
                    cursor.moveToNext();
                } else {
                    // 첫 번째 아이템은 차이가 없으므로 0으로 설정
                    ShowPressList.add(new ShowPressItem(date, Integer.parseInt(String.valueOf(kg)), "±00"));
                    cursor.moveToNext();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return ShowPressList;
    }

    private void showDatePickerDialog(String point) { //point는 startDate와 endDate구분을 위한 것
        Calendar calendar = Calendar.getInstance();
        String dateVal[];
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Date FROM userPress ORDER BY Date ASC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            dateVal = cursor.getString(0).split("-");
            minYear = Integer.parseInt(dateVal[0]);
            minMonth = Integer.parseInt(dateVal[1]);
            minDay = Integer.parseInt(dateVal[2]);
            cursor.close();
        }
        Cursor cursor2 = db.rawQuery("SELECT Date FROM userPress ORDER BY Date DESC LIMIT 1", null);
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
                        if (point == "start") {
                            startDate.setText(selectedDate);
                        } else {
                            endDate.setText(selectedDate);
                        }
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

    private List<ShowPressItem> getShowPress2() {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        ShowPressList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String start = startDate.getText().toString();

        String fin = endDate.getText().toString();

        String query;
        Cursor cursor;


        if(userSide==""){
            if(userSide2==""){
                query = "SELECT Date, Systolic FROM userPress WHERE Date BETWEEN ? AND ? ORDER BY Date ASC";
                cursor = db.rawQuery(query, new String[]{start, fin});
            }
            else{
                switch (userSide2) {
                    case "sys":
                        query = "SELECT Date, Systolic FROM userPress WHERE Date BETWEEN ? AND ? ORDER BY Date ASC";
                        cursor = db.rawQuery(query, new String[]{start, fin});
                        break;
                    case "dia":
                        query = "SELECT Date, Diastolic FROM userPress WHERE Date BETWEEN ? AND ? ORDER BY Date ASC";
                        cursor = db.rawQuery(query, new String[]{start, fin});
                        break;
                    default:
                        query = "SELECT Date, Pulse FROM userPress WHERE Date BETWEEN ? AND ? ORDER BY Date ASC";
                        cursor = db.rawQuery(query, new String[]{start, fin});
                        break;
                }
            }
        }
        else{
            if(userSide2==""){
                query = "SELECT Date, Systolic FROM userPress WHERE ((Date BETWEEN ? AND ?) AND Side=?) ORDER BY Date ASC";
                cursor = db.rawQuery(query, new String[]{start, fin, userSide});
            }
            else{
                switch (userSide2) {
                    case "sys":
                        query = "SELECT Date, Systolic FROM userPress WHERE ((Date BETWEEN ? AND ?) AND Side=?) ORDER BY Date ASC";
                        cursor = db.rawQuery(query, new String[]{start, fin, userSide});
                        break;
                    case "dia":
                        query = "SELECT Date, Diastolic FROM userPress WHERE ((Date BETWEEN ? AND ?) AND Side=?) ORDER BY Date ASC";
                        cursor = db.rawQuery(query, new String[]{start, fin, userSide});
                        break;
                    default:
                        query = "SELECT Date, Pulse FROM userPress WHERE ((Date BETWEEN ? AND ?) AND Side=?) ORDER BY Date ASC";
                        cursor = db.rawQuery(query, new String[]{start, fin, userSide});
                        break;
                }
            }
        }

        int i = 0;

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                BigDecimal kg = new BigDecimal(cursor.getString(1));

                if (cursor.moveToPrevious()) {
                    BigDecimal previousKg = new BigDecimal(cursor.getString(1));
                    BigDecimal weightDifference = kg.subtract(previousKg);

                    if (weightDifference.compareTo(BigDecimal.ZERO) > 0) {
                        // DecimalFormat을 사용하여 형식을 지정
                        DecimalFormat decimalFormat = new DecimalFormat("#00");
                        String formattedDifference = decimalFormat.format(weightDifference);
                        ShowPressList.add(new ShowPressItem(date, Integer.parseInt(String.valueOf(kg)), "▲" + formattedDifference));
                    } else if (weightDifference.compareTo(BigDecimal.ZERO) < 0) {
                        // DecimalFormat을 사용하여 형식을 지정
                        BigDecimal absoluteDifference = weightDifference.abs();
                        DecimalFormat decimalFormat = new DecimalFormat("#00");
                        String formattedDifference = decimalFormat.format(absoluteDifference);

                        ShowPressList.add(new ShowPressItem(date, Integer.parseInt(String.valueOf(kg)), "▼" + formattedDifference));
                    } else {
                        // DecimalFormat을 사용하여 형식을 지정
                        DecimalFormat decimalFormat = new DecimalFormat("#00");
                        String formattedDifference = decimalFormat.format(weightDifference);
                        ShowPressList.add(new ShowPressItem(date, Integer.parseInt(String.valueOf(kg)), "±" + formattedDifference));
                    }
                    //cursor.moveToNext();
                    cursor.moveToNext();
                } else {
                    // 첫 번째 아이템은 차이가 없으므로 0으로 설정
                    ShowPressList.add(new ShowPressItem(date, (int) Double.parseDouble(String.valueOf(kg)), "±00"));
                    cursor.moveToNext();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return ShowPressList;
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

    //왼팔 오른팔 UI 설정
    private void setSide(String side) {
        if (startDate.getText().toString().isEmpty() || endDate.getText().toString().isEmpty()) {
            Toast.makeText(ShowAllPress.this, "날짜와 시간대를 먼저 입력하세요", Toast.LENGTH_SHORT).show();
        } else {
            if (side == "left") {
                ColorStateList colorStateList = left.getTextColors();
                int color = colorStateList.getDefaultColor();
                if (color == Color.parseColor("#BDBDBD")) {
                    right.setTextColor(Color.parseColor("#BDBDBD")); //선택 안된것은 회색으로
                    right.setTextSize(Dimension.SP, 20); //글씨 크기도 작게
                    userSide = "left";
                    left.setTextColor(Color.parseColor("#58D3F7"));
                    left.setTextSize(Dimension.SP, 25);
                    left.setTypeface(null, Typeface.BOLD);
                } else { //이미 선택된걸 다시 선택하면 선택모드 초기화, 왼쪽 오른쪽 선택버튼을 원상복구
                    left.setTextColor(Color.parseColor("#BDBDBD"));
                    left.setTextSize(Dimension.SP, 25);
                    right.setTextSize(Dimension.SP, 25);
                    left.setTypeface(null, Typeface.NORMAL);
                    right.setTypeface(null, Typeface.NORMAL);
                    userSide = "";
                }

            } else {
                ColorStateList colorStateList = right.getTextColors();
                int color = colorStateList.getDefaultColor();
                if (color == Color.parseColor("#BDBDBD")) {
                    left.setTextColor(Color.parseColor("#BDBDBD"));
                    left.setTextSize(Dimension.SP, 20);
                    userSide = "right";
                    right.setTextColor(Color.parseColor("#58D3F7"));
                    right.setTextSize(Dimension.SP, 25);
                    right.setTypeface(null, Typeface.BOLD);
                } else {
                    right.setTextColor(Color.parseColor("#BDBDBD"));
                    right.setTextSize(Dimension.SP, 25);
                    left.setTextSize(Dimension.SP, 25);
                    right.setTypeface(null, Typeface.NORMAL);
                    left.setTypeface(null, Typeface.NORMAL);
                    userSide = "";
                }
            }
        }
    }

    //수축기, 이완기, 맥박 설정 UI
    private void setSide2(String side) {
        if (startDate.getText().toString().isEmpty() || endDate.getText().toString().isEmpty()) {
            Toast.makeText(ShowAllPress.this, "날짜와 시간대를 먼저 입력하세요", Toast.LENGTH_SHORT).show();
        } else {
            if (side == "sys") {
                ColorStateList colorStateList = sys.getTextColors();
                int color = colorStateList.getDefaultColor();
                if (color == Color.parseColor("#BDBDBD")) {
                    dia.setTextColor(Color.parseColor("#BDBDBD")); //선택 안된것은 회색으로
                    dia.setTextSize(Dimension.SP, 20); //글씨 크기도 작게
                    pul.setTextColor(Color.parseColor("#BDBDBD"));
                    pul.setTextSize(Dimension.SP, 20);
                    userSide2 = "sys";
                    sys.setTextColor(Color.parseColor("#58D3F7"));
                    sys.setTextSize(Dimension.SP, 25);
                    sys.setTypeface(null, Typeface.BOLD);
                } else { //이미 선택된걸 다시 선택하면 선택모드 초기화, 왼쪽 오른쪽 선택버튼을 원상복구
                    sys.setTextColor(Color.parseColor("#BDBDBD"));
                    sys.setTextSize(Dimension.SP, 25);
                    dia.setTextSize(Dimension.SP, 25);
                    pul.setTextSize(Dimension.SP, 25);
                    sys.setTypeface(null, Typeface.NORMAL);
                    dia.setTypeface(null, Typeface.NORMAL);
                    pul.setTypeface(null, Typeface.NORMAL);
                    userSide2 = "";
                }

            } else if(side=="dia"){
                ColorStateList colorStateList = dia.getTextColors();
                int color = colorStateList.getDefaultColor();
                if (color == Color.parseColor("#BDBDBD")) {
                    sys.setTextColor(Color.parseColor("#BDBDBD"));
                    sys.setTextSize(Dimension.SP, 20);
                    pul.setTextColor(Color.parseColor("#BDBDBD"));
                    pul.setTextSize(Dimension.SP, 20);
                    userSide2 = "dia";
                    dia.setTextColor(Color.parseColor("#58D3F7"));
                    dia.setTextSize(Dimension.SP, 25);
                    dia.setTypeface(null, Typeface.BOLD);
                } else {
                    dia.setTextColor(Color.parseColor("#BDBDBD"));
                    sys.setTextSize(Dimension.SP, 25);
                    dia.setTextSize(Dimension.SP, 25);
                    pul.setTextSize(Dimension.SP, 25);
                    sys.setTypeface(null, Typeface.NORMAL);
                    dia.setTypeface(null, Typeface.NORMAL);
                    pul.setTypeface(null, Typeface.NORMAL);
                    userSide2 = "";
                }
            }
            else{
                ColorStateList colorStateList = pul.getTextColors();
                int color = colorStateList.getDefaultColor();
                if (color == Color.parseColor("#BDBDBD")) {
                    sys.setTextColor(Color.parseColor("#BDBDBD"));
                    sys.setTextSize(Dimension.SP, 20);
                    dia.setTextColor(Color.parseColor("#BDBDBD"));
                    dia.setTextSize(Dimension.SP, 20);
                    userSide2 = "pul";
                    pul.setTextColor(Color.parseColor("#58D3F7"));
                    pul.setTextSize(Dimension.SP, 25);
                    pul.setTypeface(null, Typeface.BOLD);
                } else {
                    pul.setTextColor(Color.parseColor("#BDBDBD"));
                    sys.setTextSize(Dimension.SP, 25);
                    dia.setTextSize(Dimension.SP, 25);
                    pul.setTextSize(Dimension.SP, 25);
                    sys.setTypeface(null, Typeface.NORMAL);
                    dia.setTypeface(null, Typeface.NORMAL);
                    pul.setTypeface(null, Typeface.NORMAL);
                    userSide2 = "";
                }
            }
        }
    }
}
