package com.example.naviproj;

import java.util.ArrayList;
import java.util.List;

public class NewsItem {
    private String title;
    private String content;

    private String img;
    // 추가적인 필드들을 필요에 따라 정의

    public NewsItem(String title, String content, String img) {
        this.title = title;
        this.content = content;
        this.img=img;
        // 추가적인 필드들을 초기화
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImg() {
        return img;
    }

    // 추가적인 필드들의 getter 메소드들을 정의

    // 가상의 뉴스 데이터를 반환하는 메소드
    public static List<NewsItem> getSampleNewsData() {
        List<NewsItem> newsList = new ArrayList<>();

        // 필요에 따라 더 많은 가상 데이터를 추가

        return newsList;
    }
}




