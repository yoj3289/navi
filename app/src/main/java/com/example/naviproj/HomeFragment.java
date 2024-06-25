package com.example.naviproj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private Bundle bundle;


    private LineChart weightChart, pressChart, sugarChart; //각각 체중, 혈압, 혈당 그래프 그리기위한 변수

    private TextView showGreet, briefButton,newsButton; //각각 인삿말 보여주고 브리핑과 뉴스 버튼에 사용

    private RelativeLayout btnWeight,btnPress,btnSugar; //각각 체중 혈압 혈당 관리 버튼

    private DataBaseHelper dbHelper;

    private String name="";

    private ImageView newsIcon;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_home, container, false);


        ActivityResultLauncher<Intent> startActivityResultLauncher = registerForActivityResult( //메인으로 돌아오면 그래프 새로고치기 위함
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Intent data = result.getData(); // 결과 데이터가 필요한 경우 사용
                        setWeightGraph(); // 돌아올 때 drawGraph() 메서드 실행
                        setPressGraph();
                        setSugarGraph();
                    }
                });

        showGreet=view.findViewById(R.id.showName);

        weightChart = view.findViewById(R.id.lineChart);
        pressChart = view.findViewById(R.id.lineChart2);
        sugarChart = view.findViewById(R.id.lineChart3);

        btnWeight = view.findViewById(R.id.managementWeight);
        btnPress = view.findViewById(R.id.managementPress);
        btnSugar = view.findViewById(R.id.managementSugar);

        dbHelper = new DataBaseHelper(requireContext());

        newsIcon=view.findViewById(R.id.newsIcon);

        setName();

        setWeightGraph();
        setPressGraph();
        setSugarGraph();


        newsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //뉴스로 이동
                Intent intent = new Intent(view.getContext(), ShowNews.class);
                view.getContext().startActivity(intent);
            }
        });


        btnWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //체중 상세검색으로 이동
                Intent intent = new Intent(view.getContext(), ManagementWeight.class);
                startActivityResultLauncher.launch(intent);
            }
        });

        btnPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //혈압 상세검색으로 이동
                Intent intent = new Intent(view.getContext(), ManagementPress.class);
                startActivityResultLauncher.launch(intent);
            }
        });

        btnSugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //혈당 상세검색으로 이동
                Intent intent = new Intent(view.getContext(), ManagementSugar.class);
                startActivityResultLauncher.launch(intent);
            }
        });


        return view;

    }


    private void setName(){
        SQLiteDatabase db = dbHelper.getReadableDatabase(); //db에서 체중 정보 읽어오기 위한 내용들
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo", null);
        if (cursor.moveToNext()) {
            name = cursor.getString(0);
            // 최근 날짜의 BMI 값을 showBMI에 저장
        } else {
            // 결과가 없을 경우 처리
        }
        showGreet.setText(name+"님 반가워요!\n도움이 필요하면 눌러보세요!");
    }

    public void setWeightGraph(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Date, Kg FROM userWeight ORDER BY Date ASC", null);

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

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
            dataset.setDrawValues(false); //점 위에 숫자 안보이게
            dataset.setColor(ContextCompat.getColor(requireContext(), R.color.DarkBlue)); // 그래프 색상 설정

            // 선을 부드럽게 만들기 위한 설정
            dataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);


            // LineData에 dataset 추가
            LineData data = new LineData(dataset);

            // X축 설정
            XAxis xAxis = weightChart.getXAxis();
            xAxis.setDrawAxisLine(false); // X축 선 숨기기
            xAxis.setDrawGridLines(false); // X축 그리드 라인 숨기기
            xAxis.setDrawLabels(false); // X축 라벨 숨기기

            // 왼쪽 Y축 설정
            YAxis yAxisLeft = weightChart.getAxisLeft();
            yAxisLeft.setDrawAxisLine(false); // 왼쪽 Y축 선 숨기기
            yAxisLeft.setDrawGridLines(false); // 왼쪽 Y축 그리드 라인 숨기기
            yAxisLeft.setDrawLabels(false); // 왼쪽 Y축 라벨 숨기기


            // 오른쪽 Y축 설정 (이미 사용하지 않도록 설정되어 있지만, 세부 설정을 위해 추가)
            YAxis yAxisRight = weightChart.getAxisRight();
            yAxisRight.setDrawAxisLine(false); // 오른쪽 Y축 선 숨기기
            yAxisRight.setDrawGridLines(false); // 오른쪽 Y축 그리드 라인 숨기기
            yAxisRight.setDrawLabels(false); // 오른쪽 Y축 라벨 숨기기

            // 그래프에 데이터 설정
            weightChart.setData(data);

            dataset.setLineWidth(3); //라인 두께
            dataset.setCircleRadius(6); // 점 크기
            weightChart.setVisibleXRangeMaximum(4); //가로스크롤, 한번에 보일 개수

            // 최신 데이터의 인덱스 계산
            int latestDataIndex = entries.size() - 1;

            // 차트를 최신 데이터가 먼저 보이도록 오른쪽으로 이동
            weightChart.moveViewToX(latestDataIndex);
            weightChart.getLegend().setEnabled(false); // 레전드 비활성화
            weightChart.getDescription().setEnabled(false);

            // 이 설정들을 적용한 후 차트를 새로고침
            weightChart.invalidate();

        }
    }

    private void setPressGraph(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT Date, Avg(Systolic) AS AvgSys, Avg(Diastolic) AS AvgDia FROM userPress GROUP BY Date ORDER BY Date ASC", null);

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<Entry> entries2 = new ArrayList<>();

        ArrayList<String> labels = new ArrayList<>();


        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow("Date"));
                double systolic = cursor.getDouble(cursor.getColumnIndexOrThrow("AvgSys"));
                double diastolic = cursor.getDouble(cursor.getColumnIndexOrThrow("AvgDia"));

                // Entry에 데이터 추가
                entries.add(new Entry(labels.size(), (float) systolic));
                entries2.add(new Entry(labels.size(), (float) diastolic));

                labels.add(date);
            } while (cursor.moveToNext());


            cursor.close();
            db.close();

            // LineDataSet 설정
            LineDataSet dataset = new LineDataSet(entries, "수축기");
            dataset.setColor(ContextCompat.getColor(requireContext(), R.color.DarkBlue)); // 그래프 색상 설정
            dataset.setDrawValues(false); //점 위에 숫자 안보이게

            LineDataSet dataset2 = new LineDataSet(entries2, "이완기");
            dataset2.setColor(ContextCompat.getColor(requireContext(), R.color.DarkRed)); // 그래프 색상 설정
            dataset2.setDrawValues(false); //점 위에 숫자 안보이게

            // 선을 부드럽게 만들기 위한 설정
            dataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataset2.setMode(LineDataSet.Mode.CUBIC_BEZIER);


            // 데이터셋을 하나의 리스트로 묶음
            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataset);
            dataSets.add(dataset2);

            // LineData 객체 생성시에 리스트를 전달
            LineData data = new LineData(dataSets);

            // X축 설정
            XAxis xAxis = pressChart.getXAxis();
            xAxis.setDrawAxisLine(false); // X축 선 숨기기
            xAxis.setDrawGridLines(false); // X축 그리드 라인 숨기기
            xAxis.setDrawLabels(false); // X축 라벨 숨기기

            // 왼쪽 Y축 설정
            YAxis yAxisLeft = pressChart.getAxisLeft();
            yAxisLeft.setDrawAxisLine(false); // 왼쪽 Y축 선 숨기기
            yAxisLeft.setDrawGridLines(false); // 왼쪽 Y축 그리드 라인 숨기기
            yAxisLeft.setDrawLabels(false); // 왼쪽 Y축 라벨 숨기기


            // 오른쪽 Y축 설정 (이미 사용하지 않도록 설정되어 있지만, 세부 설정을 위해 추가)
            YAxis yAxisRight = pressChart.getAxisRight();
            yAxisRight.setDrawAxisLine(false); // 오른쪽 Y축 선 숨기기
            yAxisRight.setDrawGridLines(false); // 오른쪽 Y축 그리드 라인 숨기기
            yAxisRight.setDrawLabels(false); // 오른쪽 Y축 라벨 숨기기



            pressChart.getAxisRight().setEnabled(false); //오른쪽에는 축활성화 x

            // 그래프에 데이터 설정
            pressChart.setData(data);

            dataset.setLineWidth(3); //라인 두께
            dataset.setCircleRadius(6); // 점 크기

            dataset2.setLineWidth(3); //라인 두께
            dataset2.setCircleRadius(6); // 점 크기
            pressChart.setVisibleXRangeMaximum(3); //가로스크롤, 한번에 보일 개수

            pressChart.setVisibleYRangeMaximum(100, YAxis.AxisDependency.LEFT);//세로스크롤, 한번에 보일 개수

            // 최신 데이터의 인덱스 계산
            int latestDataIndex = entries.size() - 1;

            // 차트를 최신 데이터가 먼저 보이도록 오른쪽으로 이동
            pressChart.moveViewToX(latestDataIndex);


            pressChart.getLegend().setEnabled(false); // 레전드 비활성화
            pressChart.getDescription().setEnabled(false);

            // 그래프 갱신
            pressChart.invalidate();
        }
    }

    private void setSugarGraph(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT Date, AVG(CASE WHEN Time LIKE '%전' THEN BloodSugar END) AS AvgBeforeSug,AVG(CASE WHEN Time LIKE '%후' THEN BloodSugar END) AS AvgAfterSug FROM userSugar GROUP BY Date HAVING COUNT(CASE WHEN Time LIKE '%전'THEN BloodSugar END)>0 AND COUNT(CASE WHEN Time LIKE '%후' THEN BloodSugar END)>0 ", null); //식전 평균


        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<Entry> entries2 = new ArrayList<>();

        ArrayList<String> labels = new ArrayList<>();


        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow("Date"));
                double empty = cursor.getDouble(cursor.getColumnIndexOrThrow("AvgBeforeSug")); //공복혈당 평균
                double after = cursor.getDouble(cursor.getColumnIndexOrThrow("AvgAfterSug")); //식후혈당 평균


                // Entry에 데이터 추가
                entries.add(new Entry(labels.size(), (float) empty));
                entries2.add(new Entry(labels.size(), (float) after));

                labels.add(date);

            } while (cursor.moveToNext());



            cursor.close();
            db.close();

            // LineDataSet 설정
            LineDataSet dataset = new LineDataSet(entries2, "식후 혈당");
            dataset.setColor(ContextCompat.getColor(requireContext(), R.color.DarkBlue)); // 그래프 색상 설정
            dataset.setDrawValues(false); //점 위에 숫자 안보이게

            LineDataSet dataset2 = new LineDataSet(entries, "공복 혈당");
            dataset2.setColor(ContextCompat.getColor(requireContext(), R.color.DarkRed)); // 그래프 색상 설정
            dataset2.setDrawValues(false); //점 위에 숫자 안보이게

            // 선을 부드럽게 만들기 위한 설정
            dataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataset2.setMode(LineDataSet.Mode.CUBIC_BEZIER);


            // 데이터셋을 하나의 리스트로 묶음
            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataset);
            dataSets.add(dataset2);

            // LineData 객체 생성시에 리스트를 전달
            LineData data = new LineData(dataSets);

            // X축 설정
            XAxis xAxis = sugarChart.getXAxis();
            xAxis.setDrawAxisLine(false); // X축 선 숨기기
            xAxis.setDrawGridLines(false); // X축 그리드 라인 숨기기
            xAxis.setDrawLabels(false); // X축 라벨 숨기기

            // 왼쪽 Y축 설정
            YAxis yAxisLeft = sugarChart.getAxisLeft();
            yAxisLeft.setDrawAxisLine(false); // 왼쪽 Y축 선 숨기기
            yAxisLeft.setDrawGridLines(false); // 왼쪽 Y축 그리드 라인 숨기기
            yAxisLeft.setDrawLabels(false); // 왼쪽 Y축 라벨 숨기기


            // 오른쪽 Y축 설정 (이미 사용하지 않도록 설정되어 있지만, 세부 설정을 위해 추가)
            YAxis yAxisRight = sugarChart.getAxisRight();
            yAxisRight.setDrawAxisLine(false); // 오른쪽 Y축 선 숨기기
            yAxisRight.setDrawGridLines(false); // 오른쪽 Y축 그리드 라인 숨기기
            yAxisRight.setDrawLabels(false); // 오른쪽 Y축 라벨 숨기기

            sugarChart.getAxisRight().setEnabled(false); //오른쪽에는 축활성화 x

            // 그래프에 데이터 설정
            sugarChart.setData(data);

            dataset.setLineWidth(3); //라인 두께
            dataset.setCircleRadius(6); // 점 크기

            dataset2.setLineWidth(3); //라인 두께
            dataset2.setCircleRadius(6); // 점 크기
            sugarChart.setVisibleXRangeMaximum(3); //가로스크롤, 한번에 보일 개수

            sugarChart.setVisibleYRangeMaximum(100, YAxis.AxisDependency.LEFT);//세로스크롤, 한번에 보일 개수

            // 최신 데이터의 인덱스 계산
            int latestDataIndex = entries.size() - 1;

            // 차트를 최신 데이터가 먼저 보이도록 오른쪽으로 이동
            sugarChart.moveViewToX(latestDataIndex);

            sugarChart.getLegend().setEnabled(false); // 레전드 비활성화
            sugarChart.getDescription().setEnabled(false); //LabelDescription어쩌고 안보이게

            sugarChart.setDragEnabled(false); // 차트 드래그 비활성화

            // 그래프 갱신
            sugarChart.invalidate();

        }
    }


}