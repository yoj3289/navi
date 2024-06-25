//체중 전체보기(상세검색) 리사이클러뷰에서 사용되는 아이템
package com.example.naviproj;

import java.util.ArrayList;
import java.util.List;

public class ShowWeightItem {
    private String Date;
    private double Gram;
    private String UpDown;

    public ShowWeightItem(String Date, double Gram, String UpDown){

        this.Date=Date;

        this.Gram=Gram;

        this.UpDown=UpDown;

    }
    public String getDate() {
        return Date;
    }
    public double getGram() {
        return Gram;
    }
    public String getUpDown() {
        return UpDown;
    }


    //체중 이상 감지에 사용될 리사이클러뷰
    public static List<ShowWeightItem> getShowWeight() {
        List<ShowWeightItem> ShowWeightList = new ArrayList<>();
        return ShowWeightList;
    }

}

