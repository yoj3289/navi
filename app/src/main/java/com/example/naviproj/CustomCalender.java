package com.example.naviproj;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CustomCalender extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDatePickerDialog();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                //android.R.style.Theme_Holo_Light_Dialog,
                R.style.Theme_CustomCalendar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        Intent intent = new Intent();
                        intent.putExtra("selectedDate", String.format("%d.%d.%d", year, monthOfYear + 1, dayOfMonth));
                        setResult(0, intent);

                        finish();
                    }
                },
                // 초기 날짜 설정 (년, 월, 일)
                currentYear, currentMonth, currentDay
        );

        datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                // 취소 버튼이 눌렸을 때의 동작 처리

                finish();
            }
        });

        Calendar maxDate = Calendar.getInstance();
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }
}
