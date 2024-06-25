//혈압 전체보기(상세검색) 리사이클러뷰에서 사용되는 아이템
package com.example.naviproj;

import java.util.ArrayList;
import java.util.List;

public class ShowPressItem {
    private String Date;
    private Integer Press;
    private String UpDown;

    public ShowPressItem(String Date, Integer Press, String UpDown){

        this.Date=Date;

        this.Press=Press;

        this.UpDown=UpDown;

    }
    public String getDate() {
        return Date;
    }
    public Integer getPress() {
        return Press;
    }
    public String getUpDown() {
        return UpDown;
    }


    //체중 이상 감지에 사용될 리사이클러뷰
    public static List<ShowPressItem> getShowPress() {
        List<ShowPressItem> ShowPressList = new ArrayList<>();
        return ShowPressList;
    }

}
