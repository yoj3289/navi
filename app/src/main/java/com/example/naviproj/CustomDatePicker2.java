//커스텀으로 스피너 만들려고 한건데 아마 지워도 될듯
package com.example.naviproj;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class CustomDatePicker2 {

    private Context context;
    private AlertDialog dialog;
    private DatePicker yearPicker;
    private TextView cancelButton, okButton;

    private DateSelectedListener dateSelectedListener;

    public interface DateSelectedListener {
        void onDateSelected(String selectedDate);
    }

    public CustomDatePicker2(Context context, DateSelectedListener dateSelectedListener) {
        this.context = context;
        this.dateSelectedListener = dateSelectedListener;
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_date_picker4, null);
        Calendar calendar = Calendar.getInstance();

        yearPicker = view.findViewById(R.id.yearpicker);
        cancelButton = view.findViewById(R.id.time_btn_no);
        okButton = view.findViewById(R.id.time_btn_yes);

        yearPicker.init(2000, 0, 1, null);


        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DatePicker에서 선택한 날짜 가져오기
                int year = yearPicker.getYear();
                int month = yearPicker.getMonth() + 1; // 월은 0부터 시작
                int dayOfMonth = yearPicker.getDayOfMonth();

                // 날짜를 필요한 형식으로 포맷팅
                String selectedDate = String.format("%d년 %d월 %d일", year, month , dayOfMonth);

                // 선택한 날짜를 호출한 클래스에 알리기
                if (dateSelectedListener != null) {
                    dateSelectedListener.onDateSelected(selectedDate);
                }else {
                    Log.e("CustomDatePicker2", "DateSelectedListener is null");
                }

                // 다이얼로그 닫기
                dialog.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다이얼로그 닫기
                dialog.dismiss();
            }
        });


        builder.setView(view);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //datepicker의 여백을 지움
        dialog.show();
    }

}




