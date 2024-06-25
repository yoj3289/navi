//알람이 울렸을 때 noti를 누르면 보여질 화면
package com.example.naviproj;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medi_alarm_activity);  // medi_alarm_activity.xml 레이아웃을 사용
    }
}