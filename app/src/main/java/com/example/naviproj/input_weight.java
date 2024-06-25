package com.example.naviproj;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class input_weight {

    private Context context;
    private DialogCloseListener listener;
    private Dialog dialog;
    //private EditText editText;
    private DataBaseHelper dbHelper;

    private String userName;
    private double userKg;
    private String userInput, userDate;

    private TextInputEditText inputKg, inputDate;

    private int minYear, minMonth, minDay,maxYear,maxMonth,maxDay;

    public input_weight(Context context, DialogCloseListener listener) {
        this.context = context;
        this.listener = listener;
    }


    public void showDialog() {
        // 다이얼로그를 생성하고 배경을 투명하게 설정
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.input_weight);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);//바깥쪽 눌러도 창이 사라지지 않게 함


        // 다이얼로그 안의 위젯들을 참조
        TextInputLayout kgInputLayout = dialog.findViewById(R.id.inputKg);//체중참조
        inputKg = kgInputLayout.findViewById(R.id.editKgInput);

        TextInputLayout dateInputLayout = dialog.findViewById(R.id.inputDate);//날짜 참조
        inputDate = dateInputLayout.findViewById(R.id.editDateInput);
        inputDate.setOnClickListener(new View.OnClickListener() {
            //AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllWeight.this);
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }

        });

        Button btnOk = dialog.findViewById(R.id.btnOk);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        // "OK" 버튼을 눌렀을 때의 동작을 정의
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput = inputKg.getText().toString();
                userDate=inputDate.getText().toString();

                if (!userInput.isEmpty()&&!userInput.contains("데이터")) { //"데이터"를 검사하는 이유는 이미 db에 저장됐을 경우 중복을 막기위함(중복시 '이미 데이터가 존재'한다고 뜸)
                    // 입력된 값이 비어있지 않은 경우에만 처리
                    findData();
                    insertDataToUserWeightTable(userName, userDate, Double.parseDouble(userInput));
                }
                else{
                    Toast.makeText(context, "모든 데이터를 입력 후 수정할 수 있습니다!", Toast.LENGTH_SHORT).show();
                }
                if (listener != null) {
                    listener.onDialogClose(); // 클래스 A에게 신호를 보냄
                }
                // 다이얼로그 닫기
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

    private void findData() { //데이터를 추가하기 위한 알고리즘
        dbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo", null);
        if (cursor.moveToFirst()) {
            userName = cursor.getString(0);
        }
        cursor.close();

        dbHelper.close();
    } //테이블에 요소를 추가하기 위한 함수

    private void insertDataToUserWeightTable(String userName, String userDate, double userWeight) { //데이터를 추가하기 위한 알고리즘
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Name", userName);
        values.put("Date", userDate);
        values.put("Kg", userWeight);

        long result = db.insert("userWeight", null, values);

        if (result != -1) {
            Toast.makeText(this.context, "Data added to userWeight table", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.context, "Failed to add data to userWeight table", Toast.LENGTH_SHORT).show();
        }

        db.close();

        //db = dbHelper.getWritableDatabase();
        //values = new ContentValues();


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");// 새로운 ContentValues 객체 생성, 전체 db인 userInfo에서 체중정보 업데이트, 현재 날짜에만 적용
        String currentDate = dateFormat.format(new Date());
        if(userDate.equals(currentDate)){
            db = dbHelper.getWritableDatabase();
            ContentValues valuesInfo = new ContentValues();
            valuesInfo.put("Kg", userWeight);
            db.update("userInfo", valuesInfo,null,null);
            db.close();
        }
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

                        dbHelper = new DataBaseHelper(context);
                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                        String query = "SELECT Kg FROM userWeight WHERE Date=?";
                        Cursor cursor = db.rawQuery(query, new String[]{selectedDate});
                        if(cursor.moveToFirst()) {
                            inputKg.setEnabled(false); //날짜 입력이 완료되지 않으면 비활성화
                            inputKg.setText("이미 데이터가 존재합니다!");
                        }
                        else{
                            inputKg.setText("");
                            inputKg.setEnabled(true);
                        }
                        db.close(); //240206 추가
                        cursor.close(); //240206 추가
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
