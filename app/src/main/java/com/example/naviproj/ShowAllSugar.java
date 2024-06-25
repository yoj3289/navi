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

public class ShowAllSugar extends AppCompatActivity {

    private Context context;

    private DataBaseHelper dbHelper;

    private String showBmi;

    private TextInputEditText startDate, endDate;

    private int minYear, minMonth, minDay, maxYear, maxMonth, maxDay;

    private String userSide = "";

    private TextView empty, full;

    private List<ShowPressItem> ShowPressList;

    private ShowPressAdapter showAdapter;

    private int age=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_all_sugar);

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

        setDefault(); //나이와 공복/식후를 설정하는 함수, 65세 미만은 공복혈당, 65세 이상은 식후 혈당이 기본

        // 혈당정보 리사이클러뷰 어댑터 설정 (가상의 데이터 사용)
        //ShowWeightAdapter showAdapter = new ShowWeightAdapter(getShowWeight());
        showAdapter = new ShowPressAdapter(getShowSugar());
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
            //AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllWeight.this);
            @Override
            public void onClick(View view) {
                showDatePickerDialog("end");
            }

        });


        empty = findViewById(R.id.Empty);
        full = findViewById(R.id.Full);

        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSide("empty");
            }
        });

        full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSide("full");
            }
        });

        //ImageView search = findViewById(R.id.search);
        Button search = findViewById(R.id.search);
        // "검색" 버튼을 눌렀을 때의 동작
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getShowSugar2();
                showAdapter.notifyDataSetChanged();
            }
        });
    }

    private int setDefault(){ //나이 가져오기
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Birth FROM userInfo", null);

        if(cursor.moveToFirst()){
            String birthDate = cursor.getString(0);

            Calendar current = Calendar.getInstance();

            int currentYear  = current.get(Calendar.YEAR);
            int currentMonth = current.get(Calendar.MONTH) + 1;
            int currentDay   = current.get(Calendar.DAY_OF_MONTH);

            // 만 나이 구하기 (현재년-태어난년)
            //substring은 (시작인덱스,끝인덱스+1) substring의 두번째 인자-1번까지의 값을 가져오기 때문
            int ageYear=Integer.parseInt(birthDate.substring(0,4));
            int ageMonth=Integer.parseInt(birthDate.substring(5,7));
            int ageDay=Integer.parseInt(birthDate.substring(8,10));
            age = currentYear - ageYear;
            // 만약 생일이 지나지 않았으면 -1
            if (ageMonth * 100 + ageDay > currentMonth * 100 + currentDay)
                age--;
            // 5월 26일 생은 526
            // 현재날짜 5월 25일은 525
            // 두 수를 비교 했을 때 생일이 더 클 경우 생일이 지나지 않은 것이다.
        }
        return age;
    }
    private List<ShowPressItem> getShowSugar() {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        ShowPressList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor;
        if(age<65){ //65세 미만은 공복혈당을 기본값
            cursor = db.rawQuery("SELECT * FROM userSugar WHERE Time LIKE '%식전' ORDER BY Date ASC", null);
        }
        else{ //65세 이상은 식후혈당을 기본값
            cursor = db.rawQuery("SELECT * FROM userSugar WHERE Time LIKE '%식후'ORDER BY Date ASC", null);
        }
        int i = 0;

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(1);
                BigDecimal kg = new BigDecimal(cursor.getString(3));

                if (cursor.moveToPrevious()) {
                    BigDecimal previousKg = new BigDecimal(cursor.getString(3));
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
        Cursor cursor = db.rawQuery("SELECT Date FROM userSugar ORDER BY Date ASC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            dateVal = cursor.getString(0).split("-");
            minYear = Integer.parseInt(dateVal[0]);
            minMonth = Integer.parseInt(dateVal[1]);
            minDay = Integer.parseInt(dateVal[2]);
            cursor.close();
        }
        Cursor cursor2 = db.rawQuery("SELECT Date FROM userSugar ORDER BY Date DESC LIMIT 1", null);
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

    private List<ShowPressItem> getShowSugar2() {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        ShowPressList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String start = startDate.getText().toString();

        String fin = endDate.getText().toString();

        String query;
        Cursor cursor;

        if(userSide==""){
            if(age<65){
                query = "SELECT Date, BloodSugar FROM userSugar WHERE (Date BETWEEN ? AND ?) AND (Time LIKE '%식전') ORDER BY Date ASC";
                //cursor = db.rawQuery("SELECT * FROM userSugar WHERE Time LIKE '%식전' ORDER BY Date ASC", null);
                cursor = db.rawQuery(query, new String[]{start, fin});
            }
            else{
                query = "SELECT Date, BloodSugar FROM userSugar WHERE (Date BETWEEN ? AND ?) AND (Time LIKE '%식후') ORDER BY Date ASC";
                cursor = db.rawQuery(query, new String[]{start, fin});
            }

        }
        else{
            if(userSide=="empty"){
                query = "SELECT Date, BloodSugar FROM userSugar WHERE ((Date BETWEEN ? AND ?) AND TIME LIKE '%식전') ORDER BY Date ASC";
                cursor = db.rawQuery(query, new String[]{start, fin});
            }
            else{
                query = "SELECT Date, BloodSugar FROM userSugar WHERE ((Date BETWEEN ? AND ?) AND TIME LIKE '%식후') ORDER BY Date ASC";
                cursor = db.rawQuery(query, new String[]{start, fin});
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

    private void setSide(String side) {
        if (startDate.getText().toString().isEmpty() || endDate.getText().toString().isEmpty()) {
            Toast.makeText(ShowAllSugar.this, "날짜와 시간대를 먼저 입력하세요", Toast.LENGTH_SHORT).show();
        } else {
            if (side == "empty") {
                ColorStateList colorStateList = empty.getTextColors();
                int color = colorStateList.getDefaultColor();
                if (color == Color.parseColor("#BDBDBD")) {
                    full.setTextColor(Color.parseColor("#BDBDBD"));
                    full.setTextSize(Dimension.SP, 20);
                    userSide = "empty";
                    empty.setTextColor(Color.parseColor("#58D3F7"));
                    empty.setTextSize(Dimension.SP, 25);
                    empty.setTypeface(null, Typeface.BOLD);
                } else { //이미 선택된걸 다시 선택하면 선택모드 초기화, 왼쪽 오른쪽 선택버튼을 원상복구
                    empty.setTextColor(Color.parseColor("#BDBDBD"));
                    empty.setTextSize(Dimension.SP, 25);
                    full.setTextSize(Dimension.SP, 25);
                    empty.setTypeface(null, Typeface.NORMAL);
                    full.setTypeface(null, Typeface.NORMAL);
                    userSide = "";
                }

            } else {
                ColorStateList colorStateList = full.getTextColors();
                int color = colorStateList.getDefaultColor();
                if (color == Color.parseColor("#BDBDBD")) {
                    empty.setTextColor(Color.parseColor("#BDBDBD"));
                    empty.setTextSize(Dimension.SP, 20);
                    userSide = "full";
                    full.setTextColor(Color.parseColor("#58D3F7"));
                    full.setTextSize(Dimension.SP, 25);
                    full.setTypeface(null, Typeface.BOLD);
                } else {
                    full.setTextColor(Color.parseColor("#BDBDBD"));
                    full.setTextSize(Dimension.SP, 25);
                    empty.setTextSize(Dimension.SP, 25);
                    empty.setTypeface(null, Typeface.NORMAL);
                    full.setTypeface(null, Typeface.NORMAL);
                    userSide = "";
                }
            }
        }
    }
}
