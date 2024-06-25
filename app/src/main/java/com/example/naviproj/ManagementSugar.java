package com.example.naviproj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class ManagementSugar extends AppCompatActivity implements DialogCloseListener{
    public LineChart lineChart; //그래프 그리기위한 변수

    private Context context;

    private DataBaseHelper dbHelper;

    private String showName, showTime, showSugar,showName2, showTime2, showSugar2;

    private double normalKg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DataBaseHelper(this);
        setContentView(R.layout.sugar_management); // XML 파일과 연결
        lineChart = findViewById(R.id.lineChart);

        calSugar();

        // 그래프 그리기
        drawGraph();

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


        CardView btnShowPopup = findViewById(R.id.card_view);
        btnShowPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //혈당 상세검색으로 이동
                Intent intent = new Intent(view.getContext(), ShowAllSugar.class);
                view.getContext().startActivity(intent);
            }
        });

        CardView btnShowPopup2 = findViewById(R.id.card_view2);
        btnShowPopup2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 팝업 액티비티를 띄우기 위한 Intent 생성
                input_sugar customDialog = new input_sugar(ManagementSugar.this,ManagementSugar.this);
                customDialog.showDialog();
            }
        });

        CardView btnShowPopup3 = findViewById(R.id.card_view3);
        btnShowPopup3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 팝업 액티비티를 띄우기 위한 Intent 생성
                modify_sugar customDialog = new modify_sugar(ManagementSugar.this,ManagementSugar.this);
                customDialog.showDialog();
            }
        });

        CardView btnShowPopup4 = findViewById(R.id.card_view4);
        btnShowPopup4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 팝업 액티비티를 띄우기 위한 Intent 생성
                delete_weight customDialog = new delete_weight(ManagementSugar.this,ManagementSugar.this);
                customDialog.showDialog("sugar");
            }
        });

        CardView btnShowPopup5 = findViewById(R.id.card_view5);
        btnShowPopup5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 팝업 액티비티를 띄우기 위한 Intent 생성
                Intent intent = new Intent(view.getContext(), ShowManual.class);
                view.getContext().startActivity(intent);
            }
        });

        CardView btnShowPopup6 = findViewById(R.id.card_view6);
        btnShowPopup6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 팝업 액티비티를 띄우기 위한 Intent 생성
                Intent intent = new Intent(view.getContext(), alert_sugar.class);
                view.getContext().startActivity(intent);
            }
        });
    }

    public void drawGraph() {

        // 그래프에 MarkerView 설정
        MyMarkerView markerView = new MyMarkerView(this, R.layout.custom_marker_view,"sugar");
        lineChart.setMarker(markerView);

        // 그래프에 ValueSelectedListener 설정
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
                // 특정 점을 선택했을 때의 동작
                // 여기서는 MarkerView를 보여주도록 설정되어 있으므로 따로 처리할 내용은 없음
            }

            @Override
            public void onNothingSelected() {
                // 아무 점도 선택되지 않았을 때의 동작
            }
        });

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT Date, AVG(CASE WHEN Time LIKE '%전' THEN BloodSugar END) AS AvgBeforeSug,AVG(CASE WHEN Time LIKE '%후' THEN BloodSugar END) AS AvgAfterSug FROM userSugar GROUP BY Date HAVING COUNT(CASE WHEN Time LIKE '%전'THEN BloodSugar END)>0 AND COUNT(CASE WHEN Time LIKE '%후' THEN BloodSugar END)>0 ", null); //식전 평균


        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<Entry> entries2 = new ArrayList<>();

        ArrayList<String> labels = new ArrayList<>();


        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow("Date"));
                //String date2 = cursor2.getString(cursor2.getColumnIndexOrThrow("Date"));
                //if(date==date2) {
                double empty = cursor.getDouble(cursor.getColumnIndexOrThrow("AvgBeforeSug")); //공복혈당 평균
                    //double after = cursor2.getDouble(cursor2.getColumnIndexOrThrow("AvgAfterSug")); //식후혈당 평균
                double after = cursor.getDouble(cursor.getColumnIndexOrThrow("AvgAfterSug")); //식후혈당 평균


                    // Entry에 데이터 추가
                    entries.add(new Entry(labels.size(), (float) empty));
                    entries2.add(new Entry(labels.size(), (float) after));

                    labels.add(date);
                //}

            } while (cursor.moveToNext());



            cursor.close();
            db.close();

            // LineDataSet 설정
            LineDataSet dataset = new LineDataSet(entries2, "식후 혈당");
            dataset.setColor(ContextCompat.getColor(this, R.color.DarkBlue)); // 그래프 색상 설정

            LineDataSet dataset2 = new LineDataSet(entries, "공복 혈당");
            dataset2.setColor(ContextCompat.getColor(this, R.color.DarkRed)); // 그래프 색상 설정


            // 데이터셋을 하나의 리스트로 묶음
            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataset);
            dataSets.add(dataset2);

            // LineData 객체 생성시에 리스트를 전달
            LineData data = new LineData(dataSets);


            // X축 설정
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축을 하단에 표시
            xAxis.setGranularityEnabled(true);//중복 날짜는 표시하지 않음(이미 동일날짜 체중들은 평균으로 계산해서 문제 없음)
            xAxis.setValueFormatter(new DateValueFormatter(labels)); // Date 형식으로 X축 라벨 표시
            xAxis.setLabelRotationAngle(45f); // 라벨을 45도 회전
            xAxis.setSpaceMax(1f); // 여백을 조절
            xAxis.setSpaceMin(0.1f); // 핀치줌 최소 간격



            // Y축 설정
            YAxis yAxisLeft = lineChart.getAxisLeft();
            yAxisLeft.setGranularity(0.3f); // Y축 간격 설정

            // 가로막대선 추가
            LimitLine limitLine = new LimitLine((float) 100, "공복혈당 정상 최대치");
            limitLine.setLineColor(Color.RED);
            limitLine.setLineWidth(2f);
            yAxisLeft.addLimitLine(limitLine);

            LimitLine limitLine2 = new LimitLine((float) 140, "식후혈당 정상 최대치");
            limitLine2.setLineColor(Color.BLUE);
            limitLine2.setLineWidth(2f);
            yAxisLeft.addLimitLine(limitLine2);


            lineChart.getAxisRight().setEnabled(false); //오른쪽에는 축활성화 x

            // 그래프에 데이터 설정
            lineChart.setData(data);

            dataset.setLineWidth(3); //라인 두께
            dataset.setCircleRadius(6); // 점 크기

            dataset2.setLineWidth(3); //라인 두께
            dataset2.setCircleRadius(6); // 점 크기
            lineChart.setVisibleXRangeMaximum(5); //가로스크롤, 한번에 보일 개수

            lineChart.setVisibleYRangeMaximum(100, YAxis.AxisDependency.LEFT);//세로스크롤, 한번에 보일 개수

            // 최신 데이터의 인덱스 계산
            int latestDataIndex = entries.size() - 1;

            // 차트를 최신 데이터가 먼저 보이도록 오른쪽으로 이동
            lineChart.moveViewToX(latestDataIndex);

            // 그래프 갱신
            lineChart.invalidate();

        }
    }//체중 추이 그래프를 그리기 위한 함수

    private void calSugar() {
        String totalres="";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Name, Time, BloodSugar  FROM userSugar WHERE Time LIKE '%식후' ORDER BY Date DESC,BloodSugar DESC LIMIT 1", null);
        if (cursor.moveToNext()) {
            showName = cursor.getString(0);
            showTime=cursor.getString(1);
            showSugar=cursor.getString(2);
            //Log.d("time은",showTime);
            //Log.d("혈당",showSugar);
        } else {
            // 결과가 없을 경우 처리
        }

        //240623추가
        Cursor cursor2 = db.rawQuery("SELECT Name, Time, BloodSugar  FROM userSugar WHERE Time LIKE '%식전' ORDER BY Date DESC,BloodSugar DESC LIMIT 1", null);
        if (cursor2.moveToNext()) {
            showName2 = cursor2.getString(0);
            showTime2=cursor2.getString(1);
            showSugar2=cursor2.getString(2);
            //Log.d("time은",showTime);
            //Log.d("혈당",showSugar);
        } else {
            // 결과가 없을 경우 처리
        }

        TextView enterName = findViewById(R.id.showName);
        TextView enterSugar = findViewById(R.id.showSugar);

        if(showTime!=null&&showSugar!=null&&showTime2!=null&&showSugar2!=null) {
            double sugar = Double.parseDouble(showSugar);
            double sugar2=Double.parseDouble(showSugar2);

            enterName.setText(showName + "님은 현재");
            if(showTime2.contains("식전")&&sugar2<100){
                totalres+="공복-정상, ";
            }
            else if(showTime2.contains("식전") && (sugar2>=100&&sugar2<=125)){
                totalres+="공복-혈당 장애, ";
            }
            else if(showTime2.contains("식전")&&sugar2<=125){
                totalres+="공복-내당능 장애, ";
            }
            else if(showTime2.contains("식전")&&sugar2>=126){
                totalres+="공복-당뇨병, ";
            }
            if(showTime.contains("식후")&&sugar<140){
                //enterSugar.setText("식후 기준 정상이에요.");
                totalres+="식후-정상이에요.";
            }
            else if(showTime.contains("식후")&&(sugar>=140&&sugar<=199)){
                totalres+="식후-내당능 장애가 의심돼요.";
            }
            else if(showTime.contains("식후")&&sugar>=200){
                totalres+="식후-당뇨병이 의심돼요.";
            }
            enterSugar.setText(totalres);
        }
        else{
            enterName.setText("데이터가 저장되면 이곳이 활성화 돼요!");
            enterSugar.setText("");
        }
    }

    @Override
    public void onDialogClose() {
        drawGraph();
    }
}