package com.example.naviproj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShowNews extends AppCompatActivity {
    private String URL = "https://news.naver.com/breakingnews/section/103/241";

    private String URL2,URL3,URL4,URL5,URL6,URL7,URL8,URL9,URL10,URL11="";

    private boolean isEmpty;

    private String tem1,tem2,tem3,tem4,tem5,tem6,tem7,tem8,tem9,tem10;

    private String title1,title2,title3,title4,title5,title6,title7,title8,title9,title10;

    private String imgLink1,imgLink2,imgLink3,imgLink4,imgLink5,imgLink6,imgLink7,imgLink8,imgLink9,imgLink10;

    private String media1,media2,media3,media4,media5,media6,media7,media8,media9,media10;

    private Bundle bundle;

    private Intent browserIntent; //링크열기

    private RecyclerView newsRecyclerView;

    private LinearLayout mainGroup;

    private TextView loadMsg1,loadMsg2; //로딩용 메시지

    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_news);

        Toolbar toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);
        // 기본 제목을 숨김
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뒤로가기 버튼 클릭 시 수행할 동작 추가
                setResult(Activity.RESULT_OK);
                finish();
            }
        });


        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        mainGroup=findViewById(R.id.mainGroup);

        mainGroup.setVisibility(View.INVISIBLE);

        loadMsg1=findViewById(R.id.loadMessage1);
        loadMsg2=findViewById(R.id.loadMessage2);



        setNews();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뒤로가기 버튼 클릭 시 수행할 동작 추가
                finish();
            }
        });
    }
    // 가상의 데이터를 반환하는 메소드
    private List<NewsItem> getNewsData() {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        //return NewsItem.getSampleNewsData();

        loadMsg1.setVisibility(View.INVISIBLE);
        loadMsg2.setVisibility(View.INVISIBLE);
        mainGroup.setVisibility(View.VISIBLE);

        List<NewsItem> newsList = new ArrayList<>();
        newsList.add(new NewsItem(tem1, media1,imgLink1)); //차례로 제목 언론사 뉴스사진
        newsList.add(new NewsItem(tem2, media2,imgLink2));
        newsList.add(new NewsItem(tem3, media3,imgLink3));
        newsList.add(new NewsItem(tem4, media4,imgLink4));
        newsList.add(new NewsItem(tem5, media5,imgLink5));

        newsList.add(new NewsItem(tem6, media6,imgLink6)); //차례로 제목 언론사 뉴스사진
        newsList.add(new NewsItem(tem7, media7,imgLink7));

        return newsList;

    }

    private void setNews(){
        new Thread() {
            @Override
            public void run() {
                try {
                    //크롤링 할 구문
                    Document doc = Jsoup.connect(URL).get();    //URL 웹사이트에 있는 html 코드를 다 끌어오기(네이버 뉴스 건강 메인)

                    Elements temele1 = doc.select("a[data-rank='1'] strong.sa_text_strong");
                    Elements temele2 = doc.select("a[data-rank='2'] strong.sa_text_strong");
                    Elements temele3 = doc.select("a[data-rank='3'] strong.sa_text_strong");
                    Elements temele4 = doc.select("a[data-rank='4'] strong.sa_text_strong");//0304추가
                    Elements temele5 = doc.select("a[data-rank='5'] strong.sa_text_strong");//0304추가

                    Elements temele6 = doc.select("a[data-rank='6'] strong.sa_text_strong");
                    Elements temele7 = doc.select("a[data-rank='7'] strong.sa_text_strong");


                    String link1 = doc.select("div.sa_text > a[data-rank=1]").first().attr("href"); //첫번째 기사의 주소
                    String link2 = doc.select("div.sa_text > a[data-rank=2]").first().attr("href");
                    String link3 = doc.select("div.sa_text > a[data-rank=3]").first().attr("href");
                    String link4 = doc.select("div.sa_text > a[data-rank=4]").first().attr("href");
                    String link5= doc.select("div.sa_text > a[data-rank=5]").first().attr("href");

                    String link6 = doc.select("div.sa_text > a[data-rank=6]").first().attr("href");
                    String link7 = doc.select("div.sa_text > a[data-rank=7]").first().attr("href");


                    //여기까지는 기사 들어가기 전 화면에서 가져오는 정보들


                    URL2= String.valueOf(link1);
                    URL3= String.valueOf(link2);
                    URL4= String.valueOf(link3);

                    URL5= String.valueOf(link4);
                    URL6= String.valueOf(link5);

                    URL7= String.valueOf(link6);
                    URL8= String.valueOf(link7);

                    Document doc2= Jsoup.connect(URL2).get();
                    Document doc3= Jsoup.connect(URL3).get();
                    Document doc4= Jsoup.connect(URL4).get();

                    Document doc5= Jsoup.connect(URL5).get();
                    Document doc6= Jsoup.connect(URL6).get();


                    Document doc7= Jsoup.connect(URL7).get();
                    Document doc8= Jsoup.connect(URL8).get();

                    imgLink1 = doc2.select("div.nbd_a._LAZY_LOADING_ERROR_HIDE > img._LAZY_LOADING").attr("data-src");
                    imgLink2 = doc3.select("div.nbd_a._LAZY_LOADING_ERROR_HIDE > img._LAZY_LOADING").attr("data-src");
                    imgLink3 = doc4.select("div.nbd_a._LAZY_LOADING_ERROR_HIDE > img._LAZY_LOADING").attr("data-src");

                    imgLink4 = doc5.select("div.nbd_a._LAZY_LOADING_ERROR_HIDE > img._LAZY_LOADING").attr("data-src");
                    imgLink5 = doc6.select("div.nbd_a._LAZY_LOADING_ERROR_HIDE > img._LAZY_LOADING").attr("data-src");

                    imgLink6 = doc7.select("div.nbd_a._LAZY_LOADING_ERROR_HIDE > img._LAZY_LOADING").attr("data-src");
                    imgLink7 = doc8.select("div.nbd_a._LAZY_LOADING_ERROR_HIDE > img._LAZY_LOADING").attr("data-src");



                    media1= doc2.select("div.media_end_head.go_trans div.media_end_head_top._LAZY_LOADING_WRAP img").first().attr("data-src");//언론사 이미지
                    media2= doc3.select("div.media_end_head.go_trans div.media_end_head_top._LAZY_LOADING_WRAP img").first().attr("data-src");
                    media3= doc4.select("div.media_end_head.go_trans div.media_end_head_top._LAZY_LOADING_WRAP img").first().attr("data-src");

                    media4= doc5.select("div.media_end_head.go_trans div.media_end_head_top._LAZY_LOADING_WRAP img").first().attr("data-src");
                    media5= doc6.select("div.media_end_head.go_trans div.media_end_head_top._LAZY_LOADING_WRAP img").first().attr("data-src");

                    media6= doc7.select("div.media_end_head.go_trans div.media_end_head_top._LAZY_LOADING_WRAP img").first().attr("data-src");//언론사 이미지
                    media7= doc8.select("div.media_end_head.go_trans div.media_end_head_top._LAZY_LOADING_WRAP img").first().attr("data-src");

                    //여기까지는 가져온 링크 들어가서 정보를 다시 가져옴

                    if (isEmpty == false) { //null값이 아니면 크롤링 실행
                        tem1 = temele1.get(0).text();
                        tem2 = temele2.get(0).text();
                        tem3 = temele3.get(0).text();

                        tem4 = temele4.get(0).text();
                        tem5 = temele5.get(0).text();

                        tem6 = temele6.get(0).text();
                        tem7 = temele7.get(0).text();


                        title1=tem1;
                        title2=tem2;
                        title3=tem3;

                        title4=tem4;
                        title5=tem5;


                        title6=tem6;
                        title7=tem7;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // LinearLayoutManager를 가로 방향으로 설정
                            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                            newsRecyclerView.setLayoutManager(layoutManager);

                            // 뉴스 리사이클러뷰 어댑터 설정 (가상의 데이터 사용)
                            NewsAdapter newsAdapter = new NewsAdapter(getNewsData());
                            newsRecyclerView.setAdapter(newsAdapter);

                            newsAdapter.setOnItemClickListener(new NewsAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    // 클릭된 아이템의 위치(position)을 통해 해당 아이템을 가져옴
                                    NewsItem newsItem = getNewsData().get(position);
                                    if(position==0){
                                        // 링크를 열기 위한 코드를 추가
                                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link1));
                                    }
                                    else if(position==1){
                                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link2));
                                    }
                                    else if(position==2){
                                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link3));
                                    }
                                    else if(position==3){
                                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link4));
                                    }
                                    else if(position==4){
                                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link5));
                                    }
                                    else if(position==5){
                                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link6));
                                    }
                                    else if(position==6){
                                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link7));
                                    }

                                    startActivity(browserIntent);
                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }.start();

    }
}
