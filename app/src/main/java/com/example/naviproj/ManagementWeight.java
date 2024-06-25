//체중관리 위한 클래스. 추후 코드 수정해야됨

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
import android.widget.Toast;

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
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ManagementWeight extends AppCompatActivity implements DialogCloseListener {

    private LineChart lineChart; //그래프 그리기위한 변수

    private Context context;

    private DataBaseHelper dbHelper;

    private String showName, showBmi, showHeight,showKg;

    private double normalKg;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DataBaseHelper(this);
        setContentView(R.layout.weight_management); // XML 파일과 연결
        lineChart = findViewById(R.id.lineChart);

        calBMI();

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
                //HomeFragment homeFragment=new HomeFragment();
                setResult(Activity.RESULT_OK);
                finish();

            }
        });
        // 뒤로가기 동작을 처리하는 콜백을 등록
       

        CardView btnShowPopup = findViewById(R.id.card_view);
        btnShowPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 팝업 액티비티를 띄우기 위한 Intent 생성
                
                //체중 상세검색으로 이동
                Intent intent = new Intent(view.getContext(), ShowAllWeight.class);
                view.getContext().startActivity(intent);
            }
        });

        CardView btnShowPopup2 = findViewById(R.id.card_view2);
        btnShowPopup2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 팝업 액티비티를 띄우기 위한 Intent 생성
                input_weight customDialog = new input_weight(ManagementWeight.this,ManagementWeight.this);
                customDialog.showDialog();
            }
        });

        CardView btnShowPopup3 = findViewById(R.id.card_view3);
        btnShowPopup3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 팝업 액티비티를 띄우기 위한 Intent 생성
                modify_weight customDialog = new modify_weight(ManagementWeight.this,ManagementWeight.this);
                customDialog.showDialog();
            }
        });

        CardView btnShowPopup4 = findViewById(R.id.card_view4);
        btnShowPopup4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 팝업 액티비티를 띄우기 위한 Intent 생성
                delete_weight customDialog = new delete_weight(ManagementWeight.this,ManagementWeight.this);
                customDialog.showDialog("weight");
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
                Intent intent = new Intent(view.getContext(), alert_weight.class);
                view.getContext().startActivity(intent);
            }
        });
    }


    private List<AlertItem> getAlertDataWeight() {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        return AlertItem.getAlertDataWeight();
    }

    public void drawGraph() {
        // 그래프에 MarkerView 설정
        MyMarkerView markerView = new MyMarkerView(this, R.layout.custom_marker_view,"weight");
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
        //Cursor cursor = db.rawQuery("SELECT Date, Avg(Kg) AS AvgKg FROM userWeight GROUP BY Date ORDER BY Date ASC", null);
        //Cursor cursor = db.rawQuery("SELECT Date, Kg FROM userWeight GROUP BY Date ORDER BY Date ASC", null);

        //Cursor cursor = db.rawQuery("SELECT Date, Kg, AVG(Kg) OVER (PARTITION BY strftime('%Y-%m', Date)) AS MonthlyAvgKg FROM userWeight", null);
        Cursor cursor = db.rawQuery("SELECT Date, Kg FROM userWeight ORDER BY Date ASC", null);

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        //HashSet<String> uniqueDates = new HashSet<>(); // 중복을 체크하기 위한 Set

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow("Date"));
                double kg = cursor.getDouble(cursor.getColumnIndexOrThrow("Kg"));

                // Entry에 데이터 추가
                entries.add(new Entry(labels.size(), (float) kg));
                labels.add(date);
            } while (cursor.moveToNext());

            cursor.close();
            db.close();

            // LineDataSet 설정
            LineDataSet dataset = new LineDataSet(entries, "체중(Kg)");
            dataset.setColor(ContextCompat.getColor(this, R.color.DarkBlue)); // 그래프 색상 설정


            // LineData에 dataset 추가
            LineData data = new LineData(dataset);

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
            yAxisLeft.setGranularity(1f); // Y축 간격 설정

            // 가로막대선 추가
            LimitLine limitLine = new LimitLine((float) normalKg, "표준체중");
            limitLine.setLineColor(Color.RED);
            limitLine.setLineWidth(2f);

            yAxisLeft.addLimitLine(limitLine);


            lineChart.getAxisRight().setEnabled(false); //오른쪽에는 축활성화 x

            // 그래프에 데이터 설정
            lineChart.setData(data);

            dataset.setLineWidth(3); //라인 두께
            dataset.setCircleRadius(6); // 점 크기
            lineChart.setVisibleXRangeMaximum(5); //가로스크롤, 한번에 보일 개수

            // 최신 데이터의 인덱스 계산
            int latestDataIndex = entries.size() - 1;

            // 차트를 최신 데이터가 먼저 보이도록 오른쪽으로 이동
            lineChart.moveViewToX(latestDataIndex);

            // 그래프 갱신
            lineChart.invalidate();

        }
    }//체중 추이 그래프를 그리기 위한 함수

    private void calBMI() {
        SQLiteDatabase db = dbHelper.getReadableDatabase(); //db에서 체중 정보 읽어오기 위한 내용들
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo", null);
        if (cursor.moveToNext()) {
            showName = cursor.getString(0);
            showHeight=cursor.getString(2);
            showKg = cursor.getString(3);
            // 최근 날짜의 BMI 값을 showBMI에 저장
        } else {
            // 결과가 없을 경우 처리
        }
        double changeHeight=Double.parseDouble(showHeight)/100;
        double res=Double.parseDouble(showKg)/Math.pow(changeHeight,2);
        res=(int)(res*100)/100.0;

        BigDecimal halfRound = BigDecimal.valueOf(res).setScale(3, RoundingMode.HALF_UP);
        res= halfRound.doubleValue();

        TextView enterName = findViewById(R.id.showName);
        TextView enterBmi = findViewById(R.id.showBMI);
        enterName.setText(showName+"님의현재 bmi는");

        if(res<18.5){
            enterBmi.setText(Double.toString(res)+"로 저체중이에요");
        }
        else if(res>=18.5&&res<=22.99){
            enterBmi.setText(Double.toString(res)+"로 정상체중이에요");
        }
        else if(res>=23&&res<=24.99){
            enterBmi.setText(Double.toString(res)+"로 비만 전 단계에요");
        }
        else if(res>=25&&res<=29.99){
            enterBmi.setText(Double.toString(res)+"로 1단계 비만이에요");
        }
        else if(res>=30&&res<=34.99){
            enterBmi.setText(Double.toString(res)+"로 2단계 비만이에요");
        }
        else if(res>=35){
            enterBmi.setText(Double.toString(res)+"로\n3단계 비만이에요");
        }

        if(Double.parseDouble(showHeight)<=150){
            normalKg=Double.parseDouble(showHeight)-100;
        }
        else if(Double.parseDouble(showHeight)>=150 &&Double.parseDouble(showHeight)<=159){
            normalKg=(Double.parseDouble(showHeight)-150)*0.5+50;
        }
        else if(Double.parseDouble(showHeight)>=160){
            normalKg=(Double.parseDouble(showHeight)-100)*0.9;
        }
        else{
            Toast.makeText(context, "전달안됨", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDialogClose() {
        drawGraph();
    }

}