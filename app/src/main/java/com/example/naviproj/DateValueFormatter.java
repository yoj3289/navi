package com.example.naviproj;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateValueFormatter extends ValueFormatter {

    private final List<String> labels;

    public DateValueFormatter(List<String> labels) {
        this.labels = labels;
    }

    @Override
    public String getFormattedValue(float value) {
        int index = (int) value;

        if (index >= 0 && index < labels.size()) {
            String dateString = labels.get(index);

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(dateString);

                // 변환된 날짜를 원하는 형식으로 포맷
                SimpleDateFormat newFormat = new SimpleDateFormat("yy-MM-dd", Locale.getDefault());
                return newFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return ""; // 에러 발생 시 빈 문자열 반환
    }
}
