//혈압 수정 시 띄우는 dialog코드
package com.example.naviproj;

import android.app.DatePickerDialog;
import android.app.Dialog;
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

public class modify_press {
    private Context context;
    private DialogCloseListener listener;

    private Dialog dialog;
    //private EditText editText;
    private DataBaseHelper dbHelper;

    private String userName;
    private String userKg;
    private String userInput;

    private TextInputEditText inputKg, inputDate;

    private String userDate, userTime, userSide="", userSystolic, userDiastolic, userPulse; //각각 입력한 날짜, 시간대, 팔위치, 수축기와 이완기, 맥박 값을 가져오기 위한 변수

    private TextView spinnerTitle, left, right;//각각 스피너와 인팔 오른팔 값을 저장하기 위한 변수

    private Spinner spinner;

    private String timeEditText;
    private TextInputEditText inputSystolic, inputDiastolic, inputPulse; //Systolic은 수축기 Diastolic은 이완기


    public modify_press(Context context,DialogCloseListener listener) {
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

        changeText(); //input_press에서 사용하는 xml디자인 그대로 쓰고 텍스트만 바꿔주기 위한 코드

        //시간대 스피너
        spinnerTitle=dialog.findViewById(R.id.categoryTextView);
        spinner=dialog.findViewById(R.id.categorySpinner);

        //시간대 스피너에서 고른 내용을 저장
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent,View view, int position, long id ){
                //spinnerTitle.setText(""+parent.getItemAtPosition(position));
                timeEditText=(String)parent.getItemAtPosition(position);

                if(userSide!=""&&inputDate.getText().toString()!=""){
                    dbHelper = new DataBaseHelper(context);
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    String query = "SELECT Systolic, Diastolic, Pulse FROM userPress WHERE (Date=? AND Time=? AND Side=?)";
                    Cursor cursor = db.rawQuery(query, new String[]{inputDate.getText().toString(),timeEditText,userSide});
                    if(cursor.moveToFirst()) {
                        inputSystolic.setText(cursor.getString(0));
                        inputDiastolic.setText(cursor.getString(1));
                        inputPulse.setText(cursor.getString(2));
                        inputSystolic.setEnabled(true);
                        inputDiastolic.setEnabled(true);
                        inputPulse.setEnabled(true);
                    }
                    else{
                        inputSystolic.setEnabled(false); //날짜 입력이 완료되지 않으면 비활성화
                        inputDiastolic.setEnabled(false);
                        inputPulse.setEnabled(false);
                        inputPulse.setText("수정할 데이터가 없어요!");
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
        inputSystolic.setEnabled(false);//날짜/시간대/위치가 입력되기 전까지 비활성화

        TextInputLayout diastolicInputLayout = dialog.findViewById(R.id.inputDiastolic);//이완기 참조
        inputDiastolic = diastolicInputLayout.findViewById(R.id.editDiastolicInput);
        inputDiastolic.setEnabled(false);//날짜/시간대/위치가 입력되기 전까지 비활성화

        TextInputLayout pulseInputLayout = dialog.findViewById(R.id.inputPulse);//맥박 참조
        inputPulse = pulseInputLayout.findViewById(R.id.editPulseInput);
        inputPulse.setEnabled(false);//날짜/시간대/위치가 입력되기 전까지 비활성화


        TextInputLayout dateInputLayout = dialog.findViewById(R.id.inputDate);//날짜 참조
        inputDate = dateInputLayout.findViewById(R.id.editDateInput);
        inputDate.setOnClickListener(new View.OnClickListener() {
            //AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllWeight.this);
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

        // "OK" 버튼을 눌렀을 때의 동작을 정의합
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userDate=inputDate.getText().toString();
                userTime=timeEditText;
                //userSide는 이미 설정됐음
                userSystolic = inputSystolic.getText().toString();
                userDiastolic = inputDiastolic.getText().toString();
                userPulse = inputPulse.getText().toString();

                if (userSide!=""&&!userSystolic.isEmpty()&&!userDiastolic.isEmpty()&&!userPulse.isEmpty()&&!userPulse.contains("데이터")) {
                    // 입력된 값이 비어있지 않고 kg가 제대로 들어갔을때만 처리
                    findData();
                    insertDataToUserPressTable(userName, userDate,userTime,userSide, Integer.parseInt(userSystolic),Integer.parseInt(userDiastolic),Double.parseDouble(userPulse));
                }
                else{
                    Toast.makeText(context, "모든 데이터를 입력 후 수정할 수 있습니다!", Toast.LENGTH_SHORT).show();
                }

                ManagementPress mg=new ManagementPress();

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
                //showDatePickerDialog();
                dialog.dismiss();
            }
        });

        // 다이얼로그를 표시
        dialog.show();
    }

    private void findData() { //데이터를 추가하기 위해 쿼리문을 설정하는 알고리즘
        dbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo", null);
        if (cursor.moveToFirst()) {
            userName = cursor.getString(0);
        }
        cursor.close();

        dbHelper.close();
    } //테이블에 요소를 추가하기 위한 함수

    private void insertDataToUserPressTable(String userName, String userDate, String userTime, String userSide, Integer userSystolic, Integer userDiastolic, double userPulse) { //데이터를 추가하기 위한 알고리즘
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "UPDATE userPress SET Systolic=?, Diastolic=?,Pulse=? WHERE (Date=? AND Time=? AND Side=?)";//이렇게 변수를 직접 전달하지 말고 ?로 전달하여 SQLinjection방지
        db.execSQL(query, new Object[]{userSystolic, userDiastolic,userPulse,userDate,userTime,userSide});

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

                        if(userSide!=""&&inputDate.getText().toString()!="") {
                            dbHelper = new DataBaseHelper(context);
                            SQLiteDatabase db = dbHelper.getReadableDatabase();
                            String query = "SELECT Systolic, Diastolic, Pulse FROM userPress WHERE (Date=? AND Time=? AND Side=?)";
                            Cursor cursor = db.rawQuery(query, new String[]{inputDate.getText().toString(), timeEditText, userSide});
                            if(cursor.moveToFirst()) {
                                inputSystolic.setText(cursor.getString(0));
                                inputDiastolic.setText(cursor.getString(1));
                                inputPulse.setText(cursor.getString(2));
                                inputSystolic.setEnabled(true);
                                inputDiastolic.setEnabled(true);
                                inputPulse.setEnabled(true);
                            }
                            else{
                                inputSystolic.setEnabled(false); //날짜 입력이 완료되지 않으면 비활성화
                                inputDiastolic.setEnabled(false);
                                inputPulse.setEnabled(false);

                                inputSystolic.setText("");
                                inputDiastolic.setText("");
                                inputPulse.setText("수정할 데이터가 없어요!");
                            }
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

    private void changeText(){
        TextView title=dialog.findViewById(R.id.title);
        title.setText("혈압을 수정하세요!");
        TextView guide=dialog.findViewById(R.id.howTo);
        guide.setText("1. 먼저 날짜·시간·위치를 입력하세요.\n2.기존에 저장된 정보를 수정 하세요." +
                "\n3. 자세한 내용은 '설명'을 눌러보세요!");
        Button ok=dialog.findViewById(R.id.btnOk);
        ok.setText("수정");
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
                String query = "SELECT Systolic, Diastolic, Pulse FROM userPress WHERE (Date=? AND Time=? AND Side=?)";
                Cursor cursor = db.rawQuery(query, new String[]{inputDate.getText().toString(), timeEditText, userSide});
                if(cursor.moveToFirst()) {
                    inputSystolic.setText(cursor.getString(0));
                    inputDiastolic.setText(cursor.getString(1));
                    inputPulse.setText(cursor.getString(2));
                    inputSystolic.setEnabled(true);
                    inputDiastolic.setEnabled(true);
                    inputPulse.setEnabled(true);
                }
                else{
                    inputSystolic.setEnabled(false); //날짜 입력이 완료되지 않으면 비활성화
                    inputDiastolic.setEnabled(false);
                    inputPulse.setEnabled(false);

                    inputSystolic.setText("");
                    inputDiastolic.setText("");
                    inputPulse.setText("수정할 데이터가 없어요!");
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
                String query = "SELECT Systolic, Diastolic, Pulse FROM userPress WHERE (Date=? AND Time=? AND Side=?)";
                Cursor cursor = db.rawQuery(query, new String[]{inputDate.getText().toString(), timeEditText, userSide});
                if(cursor.moveToFirst()) {
                    inputSystolic.setText(cursor.getString(0));
                    inputDiastolic.setText(cursor.getString(1));
                    inputPulse.setText(cursor.getString(2));
                    inputSystolic.setEnabled(true);
                    inputDiastolic.setEnabled(true);
                    inputPulse.setEnabled(true);
                }
                else{
                    inputSystolic.setEnabled(false); //날짜 입력이 완료되지 않으면 비활성화
                    inputDiastolic.setEnabled(false);
                    inputPulse.setEnabled(false);

                    inputSystolic.setText("");
                    inputDiastolic.setText("");
                    inputPulse.setText("수정할 데이터가 없어요!");
                }
                db.close();
                cursor.close();

            }
        }

    }
}
