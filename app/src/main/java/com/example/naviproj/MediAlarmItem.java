package com.example.naviproj;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class MediAlarmItem {
    private String alarmTitle;

    private String alarmPeriod;

    private Drawable alarmTime1,alarmTime2,alarmTime3,alarmTime4;

    public MediAlarmItem(String alarmTitle,String alarmPeriod, Drawable Icon1, Drawable Icon2,Drawable Icon3, Drawable Icon4){

        this.alarmTitle=alarmTitle;

        this.alarmPeriod=alarmPeriod;

        this.alarmTime1=Icon1;

        this.alarmTime2=Icon2;

        this.alarmTime3=Icon3;

        this.alarmTime4=Icon4;


    }
    public String getTitle() {
        return alarmTitle;
    }


    public String getPeriod() {
        return alarmPeriod;
    }

    public Drawable getIcon1() {
        return alarmTime1;
    }

    public Drawable getIcon2() {
        return alarmTime2;
    }

    public Drawable getIcon3() {
        return alarmTime3;
    }

    public Drawable getIcon4() {
        return alarmTime4;
    }





    //체중 이상 감지에 사용될 리사이클러뷰
    public static List<MediAlarmItem> getMediAlarmData() {
        List<MediAlarmItem> mediAlarmList = new ArrayList<>();

        return mediAlarmList;
    }
}
