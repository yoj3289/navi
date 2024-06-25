//이상감지 리사이클러뷰에 쓰임
package com.example.naviproj;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class AlertItem {

    private String alertTitle;

    private String alertCriteriaDate;

    private Drawable alertIcon; //0213추가
    private String alertDetail;

    public AlertItem(String alertTitle,Drawable Icon, String alertDetail){

        this.alertTitle=alertTitle;

        this.alertIcon=Icon;

        this.alertDetail=alertDetail;

    }
    public String getTitle() {
        return alertTitle;
    }


    public Drawable getAlertIcon() {
        return alertIcon;
    }

    public String getDetail() {
        return alertDetail;
    }


    //체중 이상 감지에 사용될 리사이클러뷰
    public static List<AlertItem> getAlertDataWeight() {
        List<AlertItem> alertList = new ArrayList<>();


        return alertList;
    }

    //혈압 이상 감지에 사용될 리사이클러뷰
    public static List<AlertItem> getAlertDataPress() {
        List<AlertItem> alertList = new ArrayList<>();


        return alertList;
    }


    //혈당 이상 감지에 사용될 리사이클러뷰
    public static List<AlertItem> getAlertDataSugar() {
        List<AlertItem> alertList = new ArrayList<>();


        return alertList;
    }


}
