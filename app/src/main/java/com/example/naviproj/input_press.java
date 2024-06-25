package com.example.naviproj;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Dimension;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class input_press {
    private Context context;

    private DialogCloseListener listener;
    private Dialog dialog;

    private DataBaseHelper dbHelper;

    private String userName;
    private double userKg;
    //private String userInput, userDate;

    private String userDate, userTime, userSide="", userSystolic, userDiastolic, userPulse; //각각 입력한 날짜, 시간대, 팔위치, 수축기와 이완기, 맥박 값을 가져오기 위한 변수

    private TextView spinnerTitle, left, right;//각각 스피너와 인팔 오른팔 값을 저장하기 위한 변수

    private Spinner spinner;

    private String timeEditText;
    private TextInputEditText inputDate, inputSystolic, inputDiastolic, inputPulse; //Systolic은 수축기 Diastolic은 이완기

    //private int minYear, minMonth, minDay,maxYear,maxMonth,maxDay;

    public input_press(Context context,DialogCloseListener listener) {
        this.context = context;
        this.listener = listener;
    }


    public void showDialog() {
        // 다이얼로그를 생성하고 배경을 투명하게 설정
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.input_press);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);//바깥쪽 눌러도 창이 사라지지 않게 함

        //시간대 스피너
        spinnerTitle=dialog.findViewById(R.id.categoryTextView);
        spinner=dialog.findViewById(R.id.categorySpinner);

        //시간대 스피너에서 고른 내용을 저장
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent,View view, int position, long id ){
                timeEditText=(String)parent.getItemAtPosition(position);

                if(userSide!=""&&inputDate.getText().toString()!=""){
                    dbHelper = new DataBaseHelper(context);
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    String query = "SELECT Pulse FROM userPress WHERE (Date=? AND Time=? AND Side=?)";
                    Cursor cursor = db.rawQuery(query, new String[]{inputDate.getText().toString(),timeEditText,userSide});
                    if(cursor.moveToFirst()) {
                        inputSystolic.setEnabled(false); //날짜 입력이 완료되지 않으면 비활성화
                        inputDiastolic.setEnabled(false);
                        inputPulse.setEnabled(false);
                        inputPulse.setText("이미 데이터가 존재합니다!");
                    }
                    else{
                        inputSystolic.setText("");
                        inputDiastolic.setText("");
                        inputPulse.setText("");
                        inputSystolic.setEnabled(true);
                        inputDiastolic.setEnabled(true);
                        inputPulse.setEnabled(true);
                    }
                    db.close();
                    cursor.close();
                }
            }
            public void onNothingSelected(AdapterView<?> parent){}

        });



        // 다이얼로그 안의 위젯들을 참조
        TextInputLayout systolicInputLayout = dialog.findViewById(R.id.inputSystolic);//수축기 참조
        inputSystolic = systolicInputLayout.findViewById(R.id.editSystolicInput);

        TextInputLayout diastolicInputLayout = dialog.findViewById(R.id.inputDiastolic);//이완기 참조
        inputDiastolic = diastolicInputLayout.findViewById(R.id.editDiastolicInput);

        TextInputLayout pulseInputLayout = dialog.findViewById(R.id.inputPulse);//맥박 참조
        inputPulse = pulseInputLayout.findViewById(R.id.editPulseInput);

        TextInputLayout dateInputLayout = dialog.findViewById(R.id.inputDate);//날짜 참조
        inputDate = dateInputLayout.findViewById(R.id.editDateInput);
        inputDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });


        left=dialog.findViewById(R.id.sideLeft);
        right=dialog.findViewById(R.id.sideRight);

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

        Button btnOk = dialog.findViewById(R.id.btnOk);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        // "OK" 버튼을 눌렀을 때의 동작을 정의
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userDate=inputDate.getText().toString();
                userTime=timeEditText;
                //userSide는 이미 설정됐음
                userSystolic = inputSystolic.getText().toString();
                userDiastolic = inputDiastolic.getText().toString();
                userPulse = inputPulse.getText().toString();

                if (userSide!=""&&!userSystolic.isEmpty()&&!userDiastolic.isEmpty()&&!userPulse.isEmpty()&&!userPulse.contains("데이터")) { //"데이터"를 검사하는 이유는 이미 db에 저장됐을 경우 중복을 막기위함(중복시 '이미 데이터가 존재'한다고 뜸)
                    // 입력된 값이 비어있지 않은 경우에만 처리
                    findData();
                    insertDataToUserPressTable(userName, userDate,userTime,userSide, Integer.parseInt(userSystolic),Integer.parseInt(userDiastolic),Double.parseDouble(userPulse));
                }
                else{
                    Toast.makeText(context, "모든 데이터를 입력 후 수정할 수 있습니다!", Toast.LENGTH_SHORT).show();
                }
                if (listener != null) {
                    listener.onDialogClose(); // 클래스 A에게 신호를 보냄
                }
                // 다이얼로그를 닫기
                dialog.dismiss();
            }
        });

        // "취소" 버튼을 눌렀을 때의 동작을 정의
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // 다이얼로그를 표시
        dialog.show();
    }

    private void setSide(String side){
        if(inputDate.getText().toString().isEmpty()){
            Toast.makeText(context, "날짜와 시간대를 먼저 입력하세요", Toast.LENGTH_SHORT).show();
        }
        else{
            if(side=="left"){
                right.setTextColor(Color.parseColor("#BDBDBD"));
                right.setTextSize(Dimension.SP,20);
                userSide="left";
                left.setTextColor(Color.parseColor("#58D3F7"));
                left.setTextSize(Dimension.SP,25);
                left.setTypeface(null, Typeface.BOLD);

                dbHelper = new DataBaseHelper(context);
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String query = "SELECT Pulse FROM userPress WHERE (Date=? AND Time=? AND Side=?)";
                Cursor cursor = db.rawQuery(query, new String[]{inputDate.getText().toString(),timeEditText,userSide});
                if(cursor.moveToFirst()) {
                    inputSystolic.setEnabled(false); //날짜 입력이 완료되지 않으면 비활성화
                    inputDiastolic.setEnabled(false);
                    inputPulse.setEnabled(false);
                    inputPulse.setText("이미 데이터가 존재합니다!");
                }
                else{
                    inputSystolic.setText("");
                    inputDiastolic.setText("");
                    inputPulse.setText("");
                    inputSystolic.setEnabled(true);
                    inputDiastolic.setEnabled(true);
                    inputPulse.setEnabled(true);
                }
                db.close();
                cursor.close();
            }
            else{
                left.setTextColor(Color.parseColor("#BDBDBD"));
                left.setTextSize(Dimension.SP,20);
                userSide="right";
                right.setTextColor(Color.parseColor("#58D3F7"));
                right.setTextSize(Dimension.SP,25);
                right.setTypeface(null, Typeface.BOLD);

                dbHelper = new DataBaseHelper(context);
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String query = "SELECT Pulse FROM userPress WHERE (Date=? AND Time=? AND Side=?)";
                Cursor cursor = db.rawQuery(query, new String[]{inputDate.getText().toString(),timeEditText,userSide});
                if(cursor.moveToFirst()) {
                    inputSystolic.setEnabled(false); //날짜 입력이 완료되지 않으면 비활성화
                    inputDiastolic.setEnabled(false);
                    inputPulse.setEnabled(false);
                    inputPulse.setText("이미 데이터가 존재합니다!");
                }
                else{
                    inputSystolic.setText("");
                    inputDiastolic.setText("");
                    inputPulse.setText("");
                    inputSystolic.setEnabled(true);
                    inputDiastolic.setEnabled(true);
                    inputPulse.setEnabled(true);
                }
                db.close();
                cursor.close();

            }
        }

    }
    private void findData() { //데이터를 추가하기 전 이름을 가져오기 위한 알고리즘
        dbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo", null);
        if (cursor.moveToFirst()) {
            userName = cursor.getString(0);
        }
        cursor.close();

        dbHelper.close();
    }

    private void insertDataToUserPressTable(String userName, String userDate, String userTime, String userSide, Integer userSystolic, Integer userDiastolic, double userPulse) { //데이터를 추가하기 위한 알고리즘
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Name", userName);
        values.put("Date", userDate);
        values.put("Time", userTime);
        values.put("Side", userSide);
        values.put("Systolic", userSystolic);
        values.put("Diastolic", userDiastolic);
        values.put("Pulse", userPulse);

        long result = db.insert("userPress", null, values);

        if (result != -1) {
            Toast.makeText(this.context, "Data added to userWeight table", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.context, "Failed to add data to userWeight table", Toast.LENGTH_SHORT).show();
        }

        db.close();

    } //테이블에 요소를 추가하기 위한 함수

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int currentYear=calendar.get(Calendar.YEAR);
        int currentMonth=calendar.get(Calendar.MONTH);
        int currentDate=calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                R.style.Theme_CustomCalendar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 사용자가 선택한 날짜 처리
                        String selectedDate = String.format("%02d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                        inputDate.setText(selectedDate);

                        if(userSide!=""){
                            dbHelper = new DataBaseHelper(context);
                            SQLiteDatabase db = dbHelper.getReadableDatabase();
                            String query = "SELECT Pulse FROM userPress WHERE (Date=? AND Time=? AND Side=?)";
                            Cursor cursor = db.rawQuery(query, new String[]{inputDate.getText().toString(),timeEditText,userSide});
                            if(cursor.moveToFirst()) {
                                inputSystolic.setEnabled(false); //날짜 입력이 완료되지 않으면 비활성화
                                inputDiastolic.setEnabled(false);
                                inputPulse.setEnabled(false);
                                inputPulse.setText("이미 데이터가 존재합니다!");
                            }
                            else{
                                inputSystolic.setText("");
                                inputDiastolic.setText("");
                                inputPulse.setText("");
                                inputSystolic.setEnabled(true);
                                inputDiastolic.setEnabled(true);
                                inputPulse.setEnabled(true);
                            }
                            db.close();
                            cursor.close();
                        }


                    }
                },
                // 초기 날짜 설정 (년, 월, 일)
                currentYear, currentMonth, currentDate
        );


        Calendar maxDate = Calendar.getInstance();
        maxDate.set(currentYear,currentMonth,currentDate);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.setCancelable(false);
        datePickerDialog.show();
    }
}
