//실제 사용중인 코드. makeMediAlarm2는 나중에 지우기
package com.example.naviproj;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class makeMediAlarm extends AppCompatActivity {

    private int year, month, dayOfMonth, year3, month3, dayOfMonth3, year5, month5, dayOfMonth5, year7, month7, dayOfMonth7; //각각 오늘/3일 후/5일 후/7일 후 날짜를 가져오기 위한 연,월,일 값
    private TextView day3, day5, day7, dayEvery, daySet; //복용 기간을 선택 시 배경 바꾸기 위해 선언

    private TextView cardview, cardtime, cardview2, cardtime2, cardview3, cardtime3, cardview4, cardtime4; //복용 기간을 선택 시 배경 바꾸기 위해 선언

    private TextView showDate; //사용자가 복용기간 선택 후 보여 줄 화면

    private TextView save;//알람 저장

    private String startDate, endDate, endDate3, endDate5, endDate7; //복용기간의 시작과 끝을 지정할 변수, 복용 끝은 3 5 7일로 나눔

    private CardView morning, noon, evening, night; //시간을 선택하는 카드뷰 값을 담기 위한 변수

    private String selectedTime; //선택된 시간을 보여주기 위함

    private boolean isCardViewOn1, isCardViewOn2, isCardViewOn3, isCardViewOn4 = false; // 상태를 저장할 변수

    private DataBaseHelper dbHelper;

    private TextInputEditText editNameInput;

    Calendar calendar = Calendar.getInstance();

    boolean[] isCardOn = {isCardViewOn1, isCardViewOn2, isCardViewOn3, isCardViewOn4};// 각 TextView의 상태를 저장할 배열

    boolean admitSave = false; //저장 허가 여부 결정

    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.input_medialarm2);

        Toolbar toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뒤로가기 버튼 클릭 시 수행할 동작 추가
                finish();
            }
        });

        dbHelper = new DataBaseHelper(this);


        //알람저장
        save = findViewById(R.id.btnSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userAlarmName = editNameInput.getText().toString();
                String timeSet[] = {"A", "B", "C", "D"};//db에 저장될 시기를 담기 위한 배열(아침, 점심, 저녁, 밤)
                String timeInput[] = {"", "", "", ""};//실제 알람에 시간을 등록하기 위한 배열

                if (isCardOn[0] == true) {
                    timeSet[0] = "morning";
                    timeInput[0] = (String) cardtime.getText();
                } else {
                    timeSet[0] = "";
                }
                if (isCardOn[1] == true) {
                    timeSet[1] = "noon";
                    timeInput[1] = (String) cardtime2.getText();
                } else {
                    timeSet[1] = "";
                }
                if (isCardOn[2] == true) {
                    timeSet[2] = "evening";
                    timeInput[2] = (String) cardtime3.getText();
                } else {
                    timeSet[2] = "";
                }
                if (isCardOn[3] == true) {
                    timeSet[3] = "night";
                    timeInput[3] = (String) cardtime4.getText();
                } else {
                    timeSet[3] = "";
                }

                insertDataToUserMediAlarmTable(userAlarmName, startDate, endDate, timeSet[0], timeSet[1], timeSet[2], timeSet[3]);
                if (admitSave == true) {
                    insertDataToAlamSystemTable(userAlarmName, startDate, endDate, timeInput);
                    //MediAlarmMain mediAlarmMain=new MediAlarmMain();
                }
                MediAlarmMain mediAlarmMain = new MediAlarmMain(makeMediAlarm.this, userAlarmName);
                mediAlarmMain.setAlarm();
                MedicationFragment medicationFragment = new MedicationFragment();

                finish();

            }
        });


        takeValue(); //findViewById로 값들 정의하기(코드가 길어져서 함수로 정의하고 한번에 정의함)
        TextView[] textViews = {day3, day5, day7, dayEvery, daySet};
        CardView[] cardViews = {morning, noon, evening, night};
        TextView[] cardNameViews = {cardview, cardview2, cardview3, cardview4};
        TextView[] cardTimeViews = {cardtime, cardtime2, cardtime3, cardtime4};

        startDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);


        for (int i = 0; i < textViews.length; i++) {
            final int finalI = i; // 클로저(closure)를 사용하여 인덱스를 클릭 이벤트 핸들러 내에서 참조할 수 있도록

            textViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 모든 TextView에 대해 기본 배경 설정
                    for (int j = 0; j < textViews.length; j++) {
                        if (j == finalI) {
                            // 클릭된 TextView에 대해 원하는 배경 설정
                            textViews[j].setBackground(ContextCompat.getDrawable(makeMediAlarm.this, R.drawable.border_background_select_date));
                            textViews[j].setTextColor(ContextCompat.getColor(makeMediAlarm.this, R.color.white));
                            if (textViews[j] == daySet) {
                                showDatePickerDialog();
                            } else {
                                if (textViews[j] == day3) {
                                    showTakeDate("3");
                                } else if (textViews[j] == day5) {
                                    showTakeDate("5");
                                } else if (textViews[j] == day7) {
                                    showTakeDate("7");
                                } else if (textViews[j] == dayEvery) {
                                    showTakeDate("every");
                                }
                            }
                        } else {
                            // 나머지 TextView에 대해 다른 배경 설정
                            textViews[j].setBackground(ContextCompat.getDrawable(makeMediAlarm.this, R.drawable.border_background_date));
                            //textViews[j].setTextColor(ContextCompat.getColor(makeMediAlarm.this, R.color.black);
                            textViews[j].setTextColor(Color.parseColor("#585858")); // 검은색
                        }
                    }
                }
            });
        }

        for (int i = 0; i < cardViews.length; i++) {
            final int finalI = i; // 클로저(closure)를 사용하여 인덱스를 클릭 이벤트 핸들러 내에서 참조할 수 있도록

            cardViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 모든 TextView에 대해 기본 배경 설정
                    for (int j = 0; j < cardViews.length; j++) {
                        if (j == finalI) {


                            if (isCardOn[j] == false) {
                                // 클릭된 TextView에 대해 원하는 배경 설정
                                cardViews[j].setBackground(ContextCompat.getDrawable(makeMediAlarm.this, R.drawable.border_background_select_date));
                                cardNameViews[j].setTextColor(ContextCompat.getColor(makeMediAlarm.this, R.color.white));
                                cardTimeViews[j].setTextColor(ContextCompat.getColor(makeMediAlarm.this, R.color.white));
                                isCardOn[j] = true;
                            } else if (isCardOn[j] == true) {
                                // 나머지 TextView에 대해 다른 배경 설정
                                cardViews[j].setBackground(ContextCompat.getDrawable(makeMediAlarm.this, R.drawable.border_background_date));
                                cardNameViews[j].setTextColor(Color.parseColor("#585858"));
                                cardTimeViews[j].setTextColor(Color.parseColor("#585858"));
                                isCardOn[j] = false;

                            }
                            if (cardViews[j] == morning && isCardOn[j] == true) {
                                showTimePickerDialog("morning");
                            } else if (cardViews[j] == noon && isCardOn[j] == true) {
                                showTimePickerDialog("noon");

                            } else if (cardViews[j] == evening && isCardOn[j] == true) {
                                showTimePickerDialog("evening");

                            } else if (cardViews[j] == night && isCardOn[j] == true) {
                                showTimePickerDialog("night");

                            }

                        } else {

                        }
                    }
                }
            });
        }
    }

    private void takeValue() { //xml 요소들을 정의

        TextInputLayout nameInputLayout = findViewById(R.id.inputName);
        editNameInput = nameInputLayout.findViewById(R.id.editNameInput);

        //takeMedi = findViewById(R.id.takeMedi); 240519 추가주석

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        day3 = findViewById(R.id.day3);
        day3.setTag("textView1");

        day5 = findViewById(R.id.day5);
        day5.setTag("textView2");

        day7 = findViewById(R.id.day7);
        day7.setTag("textView3");

        dayEvery = findViewById(R.id.dayEvery);
        dayEvery.setTag("textView4");

        daySet = findViewById(R.id.daySet);
        daySet.setTag("textView5");

        showDate = findViewById(R.id.showDate);
        //여기까지 기간설정에 쓰이는 변수 정의-----------------------------------------


        //여기부터------------------------------------------
        morning = findViewById(R.id.card_morning);
        morning.setTag("cardView1");

        noon = findViewById(R.id.card_noon);
        noon.setTag("cardView2");

        evening = findViewById(R.id.card_evening);
        evening.setTag("cardView3");

        night = findViewById(R.id.card_night);
        night.setTag("cardView4");

        //cardview는 아침, 점심, 저녁, 취침 전의 text들
        cardview = findViewById(R.id.cardName);
        cardview.setTag("cardNameView1");

        cardview2 = findViewById(R.id.cardName2);
        cardview2.setTag("cardNameView2");

        cardview3 = findViewById(R.id.cardName3);
        cardview3.setTag("cardNameView3");

        cardview4 = findViewById(R.id.cardName4);
        cardview4.setTag("cardNameView4");

        //cardtime은 카드뷰의 오른쪽에 표기되는 시간
        cardtime = findViewById(R.id.cardTime);
        cardtime.setTag("cardTimeView1");

        cardtime2 = findViewById(R.id.cardTime2);
        cardtime2.setTag("cardTimeView2");

        cardtime3 = findViewById(R.id.cardTime3);
        cardtime3.setTag("cardNameView3");

        cardtime4 = findViewById(R.id.cardTime4);
        cardtime4.setTag("cardNameView4");

        //여기까지 시간 설정에 쓰이는 변수 정의------------------------------------------


    }

    private void showDatePickerDialog() { //복용날짜에서 직접선택을 클릭 시 보여 줄 datepicker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                //android.R.style.Theme_Holo_Light_Dialog,
                R.style.Theme_CustomCalendar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 사용자가 선택한 날짜 처리
                        String selectedDate = String.format("%02d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                        endDate = selectedDate;
                        showTakeDate("free");
                    }
                },
                // 초기 날짜 설정 (년, 월, 일)
                year, month, dayOfMonth
        );

        Calendar minDate = Calendar.getInstance();
        minDate.set(year, month, dayOfMonth);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePickerDialog(String time) {
        // 현재 시간 가져오기
        int hour, minute;
        //Calendar currentTime = Calendar.getInstance();
        if (time == "morning") {
            hour = 6;
            minute = 0;
        } else if (time == "noon") {
            hour = 12;
            minute = 0;
        } else if (time == "evening") {
            hour = 18;
            minute = 0;
        } else {
            hour = 0;
            minute = 0;
        }

        // TimePickerDialog 생성
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                R.style.Theme_CustomTime,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // 사용자가 선택한 시간 처리
                        if (time == "morning") {
                            if (hourOfDay >= 12) {
                                hourOfDay = 11;
                                minute = 59;
                            } else if (hourOfDay < 6) {
                                hourOfDay = 6;
                                minute = 0;
                            }
                            selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                            cardtime.setText(selectedTime);
                            isCardViewOn1 = true;
                        } else if (time == "noon") {
                            if (hourOfDay >= 18) {
                                hourOfDay = 17;
                                minute = 59;
                            } else if (hourOfDay < 12) {
                                hourOfDay = 12;
                                minute = 0;
                            }
                            selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                            cardtime2.setText(selectedTime);
                            isCardViewOn2 = true;
                        } else if (time == "evening") {
                            if (hourOfDay < 18) {
                                hourOfDay = 23;
                                minute = 59;
                            }
                            selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                            cardtime3.setText(selectedTime);
                            isCardViewOn3 = true;
                        } else {
                            if (hourOfDay > 6) {
                                hourOfDay = 5;
                                minute = 59;
                            }
                            selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                            cardtime4.setText(selectedTime);
                            isCardViewOn4 = true;
                        }

                    }
                },
                hour, // 기본 시간 - 현재 시간
                minute, // 기본 분 - 현재 분
                true // 24시간 형식 여부 (true: 24시간, false: AM/PM)
        );
        timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) { //취소를 누를 경우 시간선택 버튼들이 파란색이 아닌 하얀색이 되게 바꿔주고 on/off의 상태를 다시 off로 바꿈
                // 'Cancel' 버튼을 눌렀을 때의 동작 정의
                if (time == "morning") {
                    morning.setBackground(ContextCompat.getDrawable(makeMediAlarm.this, R.drawable.border_background_date));
                    cardview.setTextColor(Color.parseColor("#585858"));
                    cardtime.setTextColor(Color.parseColor("#585858"));
                    isCardOn[0] = false;
                    Toast.makeText(makeMediAlarm.this, String.valueOf(isCardViewOn1), Toast.LENGTH_SHORT).show();

                } else if (time == "noon") {
                    noon.setBackground(ContextCompat.getDrawable(makeMediAlarm.this, R.drawable.border_background_date));
                    cardview2.setTextColor(Color.parseColor("#585858"));
                    cardtime2.setTextColor(Color.parseColor("#585858"));
                    isCardOn[1] = false;
                } else if (time == "evening") {
                    evening.setBackground(ContextCompat.getDrawable(makeMediAlarm.this, R.drawable.border_background_date));
                    cardview3.setTextColor(Color.parseColor("#585858"));
                    cardtime3.setTextColor(Color.parseColor("#585858"));
                    isCardOn[2] = false;
                } else {
                    night.setBackground(ContextCompat.getDrawable(makeMediAlarm.this, R.drawable.border_background_date));
                    cardview4.setTextColor(Color.parseColor("#585858"));
                    cardtime4.setTextColor(Color.parseColor("#585858"));
                    isCardOn[3] = false;
                }

            }
        });

        // TimePickerDialog 보여주기
        timePickerDialog.setTitle("시간을 선택하세요");
        timePickerDialog.setCancelable(false); //여백을 눌러도 꺼지지 않게 함
        timePickerDialog.show();
    }


    private void showTakeDate(String end) { //복용일자를 보여주기 위한 함수


        if (end == "free") {
            showDate.setText("복용기간: " + startDate + "~" + endDate);
        } else if (end == "3") {
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 2);

            year3 = calendar.get(Calendar.YEAR);
            month3 = calendar.get(Calendar.MONTH);
            dayOfMonth3 = calendar.get(Calendar.DAY_OF_MONTH);
            //endDate3=year3+"-"+(month3+1)+"-"+dayOfMonth3;
            //endDate = year3 + "-" + (month3 + 1) + "-" + dayOfMonth3;
            endDate = String.format("%04d-%02d-%02d", year3, month3 + 1, dayOfMonth3);
            //endDate=endDate3;
            showDate.setText("복용기간: " + startDate + "~" + endDate);

        } else if (end == "5") {
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 4);

            year5 = calendar.get(Calendar.YEAR);
            month5 = calendar.get(Calendar.MONTH);
            dayOfMonth5 = calendar.get(Calendar.DAY_OF_MONTH) - 1;
            dayOfMonth5 = calendar.get(Calendar.DAY_OF_MONTH);

            endDate = String.format("%04d-%02d-%02d", year5, month5 + 1, dayOfMonth5);
            showDate.setText("복용기간: " + startDate + "~" + endDate);
        } else if (end == "7") {
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 6);

            year7 = calendar.get(Calendar.YEAR);
            month7 = calendar.get(Calendar.MONTH);
            dayOfMonth7 = calendar.get(Calendar.DAY_OF_MONTH);
            endDate = String.format("%04d-%02d-%02d", year7, month7 + 1, dayOfMonth7);
            showDate.setText("복용기간: " + startDate + "~" + endDate);
        } else if (end == "every") {
            calendar = Calendar.getInstance();
            endDate = 2099 + "-" + 12 + "-" + 31;
            showDate.setText("복용기간: " + startDate + "~" + endDate);
        }
    }


    private void insertDataToUserMediAlarmTable(String alarm, String start, String end, String morning, String noon, String evening, String night) { //데이터를 추가하기 위한 알고리즘

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("alarmName", alarm);
        values.put("alarmStart", start);
        values.put("alarmEnd", end);
        values.put("alarmMorning", morning);
        values.put("alarmNoon", noon);
        values.put("alarmEvening", evening);
        values.put("alarmNight", night);

        // 중복 체크
        String selection = "alarmName=?";
        String[] selectionArgs = {alarm};
        Cursor cursor = db.query("userMediAlarm", null, selection, selectionArgs, null, null, null);

        if (cursor.getCount() == 0) {
            long result = db.insert("userMediAlarm", null, values);
            if (result != -1) {
                Toast.makeText(makeMediAlarm.this, "Data added to userMediAlarm table", Toast.LENGTH_SHORT).show();
                admitSave = true;
            } else {
                Toast.makeText(makeMediAlarm.this, "Failed to add data to userMediAlarm table", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(makeMediAlarm.this, "이미 동일한 이름의 알람이 존재합니다. 알람 이름을 바꿔주세요", Toast.LENGTH_SHORT).show();
        }


        db.close();
    } //테이블에 요소를 추가하기 위한 함수

    private void insertDataToAlamSystemTable(String alarm, String start, String end, String[] time) { //데이터를 추가하기 위한 알고리즘

        //각 아침,점심,저녁,밤 알람용 코드 생성을 위한것
        String code[] = new String[4];

        for (int i = 0; i < 4; i++) {
            if (time[i] != "") {
                code[i] = (i + 1) + start.substring(2, 4) + start.substring(5, 7) + start.substring(8) + 0;
                code[i] = findSame(code[i]);
            } else {
                code[i] = "0";
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("alarmName", alarm); //알람 이름

        values.put("codeMorning", code[0]); //아침 알람코드
        values.put("codeNoon", code[1]); //점심 알람 코드
        values.put("codeEvening", code[2]); //저녁 알람 코드
        values.put("codeNight", code[3]); //밤 알람 코드

        values.put("morningTime", time[0]);
        values.put("noonTime", time[1]);
        values.put("eveningTime", time[2]);
        values.put("nightTime", time[3]);


        long result = db.insert("MediAlarmSystem", null, values);

        if (result != -1) {
            Toast.makeText(makeMediAlarm.this, "Data added to MediAlarmSystem table", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(makeMediAlarm.this, "Failed to add data to MediAlarmSystem table", Toast.LENGTH_SHORT).show();
        }

        db.close();
    } //테이블에 요소를 추가하기 위한 함수

    private String findSame(String oldCode) {
        int count = 0;
        Random random = new Random();
        dbHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] selectionArgs = {oldCode, oldCode, oldCode, oldCode}; // oldCode를 4번 넣어줌

        while (true) {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM MediAlarmSystem WHERE codeMorning=? OR codeNoon=? OR codeEvening=? OR codeNight=?", selectionArgs);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0); // 첫 번째 열의 값을 가져옴
                cursor.close();
                if (count == 0) {
                    // 중복된 코드가 없으면 바로 반환
                    dbHelper.close();
                    return oldCode;
                } else {
                    // 중복된 코드가 있으면 새로운 코드를 생성하여 다시 확인
                    int newCode = random.nextInt(1000031) + 31; //중복이 없을 때까지 31부터 1,000,031까지의 랜덤 숫자를 더해 요청코드가 겹치는 상황을 원천차단. 31부터인 이유는 1을 더할 경우 다음날 요청 코드에 영향을 주기 때문
                    oldCode = Integer.toString(Integer.parseInt(oldCode) + newCode);
                    selectionArgs = new String[] {oldCode, oldCode, oldCode, oldCode}; // 새로운 코드를 selectionArgs에 업데이트
                }
            } else {
                cursor.close();
                dbHelper.close();
                return oldCode; // 예외 처리
            }
        }
    }
    }

