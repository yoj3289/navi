package com.example.naviproj;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.Inflater;



public class MedicationFragment extends Fragment {

    private List<MediAlarmItem> MediAlarmList;

    private DataBaseHelper dbHelper;

    private String name,period;
    private Drawable drawable1,drawable2,drawable3,drawable4;

    private String earlyTime="";//제일 빨리 울리는 알람을 찾기 위함

    private TextView titleInfo;

    private int count=0;//현재 설정중인 알람개수 표기 위함

    public View view;

    public RecyclerView mediAlarmRecyclerView;



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_medi, container, false);


        dbHelper = new DataBaseHelper(requireContext());


        titleInfo=view.findViewById(R.id.mainInfo);
        setTitle();


        ImageView alarmAdd=view.findViewById(R.id.addAlarm);
        alarmAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 팝업 액티비티를 띄우기 위한 Intent 생성
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 레벨 26 이상
                    // API 레벨 26 이상에서만 실행되는 코드
                    Intent intent = new Intent(view.getContext(), makeMediAlarm.class);
                    view.getContext().startActivity(intent);
                } else {
                    // API 레벨 26 미만에서 실행되는 코드
                    Toast.makeText(view.getContext(), "알람은 안드로이드 8.0 이상부터 사용할 수 있어요!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        ImageView chatMiniBi=view.findViewById(R.id.chatMiniBi);
        chatMiniBi.setOnClickListener(new View.OnClickListener() { //복약지도용 미니챗봇
            @Override
            public void onClick(View view) {
                // 팝업 액티비티를 띄우기 위한 Intent 생성
                Intent intent = new Intent(view.getContext(), chatMiniBi2.class);
                view.getContext().startActivity(intent);
            }
        });


        mediAlarmRecyclerView = view.findViewById(R.id.alarmListRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mediAlarmRecyclerView.setLayoutManager(layoutManager);

        MediAlarmAdapter mediAlarmAdapter=new MediAlarmAdapter(getMediAlarmData());
        mediAlarmRecyclerView.setAdapter(mediAlarmAdapter);

        PagerSnapHelper pagerSnapHelper1 = new PagerSnapHelper();
        pagerSnapHelper1.attachToRecyclerView(mediAlarmRecyclerView);

        return view;
    }

    //메인 화면에서 알람의 개수가 몇개 인지 알려주기 위함
    private  void setTitle() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Count(*) FROM MediAlarmSystem", null);
        //long currentTimeMillis = System.currentTimeMillis();
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0); // 첫 번째 열의 값을 가져옴
            cursor.close();
            if (count == 0) {
                // 중복된 코드가 없으면 바로 반환
                titleInfo.setText("현재 설정중인\n알람이 없어요!");
            }
            else{
                titleInfo.setText("현재 "+count+"개의 알람이 \n설정 돼 있어요!");
            }
        }
    }
    private List<MediAlarmItem> getMediAlarmData() {
        // 필요에 따라 더 많은 가상 데이터를 추가
        MediAlarmList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM userMediAlarm", null);
        if(cursor.moveToFirst()){
            do {
                name = cursor.getString(0);
                period = cursor.getString(1) + "~" + cursor.getString(2);

                if (!(cursor.getString(3).isEmpty())) {
                    drawable1 = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_morning, null);
                } else if (cursor.getString(3).isEmpty()) {
                    drawable1 = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_morning_black, null);
                }

                if (!(cursor.getString(4).isEmpty())) {
                    drawable2 = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_noon, null);
                } else if (cursor.getString(4).isEmpty()) {
                    drawable2 = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_noon_black, null);
                }

                if (!(cursor.getString(5).isEmpty())) {
                    drawable3 = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_evening, null);
                } else if (cursor.getString(5).isEmpty()) {
                    drawable3 = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_evening_black, null);
                }

                if (!(cursor.getString(6).isEmpty())) {
                    drawable4 = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_night, null);
                } else if (cursor.getString(6).isEmpty()) {
                    drawable4 = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_night_black, null);
                }

                MediAlarmList.add(new MediAlarmItem(name, period, drawable1, drawable2, drawable3, drawable4));
            }while(cursor.moveToNext());
        }

        return MediAlarmList;
    }

    private void findFirstAlarm(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM MediAlarmSystem", null);
        long currentTimeMillis = System.currentTimeMillis();
        if(cursor.moveToFirst()){
            do {
                earlyTime = cursor.getString(5);
                SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
                try {
                    Date time = format.parse(earlyTime);
                    long timeInMillis = time.getTime(); // long 형식으로 변환된 시간

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }while(cursor.moveToNext());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        // 여기에 반복적으로 수행하고 싶은 작업을 구현
        setTitle();
        MediAlarmAdapter mediAlarmAdapter=new MediAlarmAdapter(getMediAlarmData());
        mediAlarmRecyclerView.setAdapter(mediAlarmAdapter);
    }

}
