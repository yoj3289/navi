package com.example.naviproj;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class deleteMediAlarm extends AppCompatActivity {

    public String mediName; //사용자가 지정한 알람의 이름

    private String saveTime[]=new String[4];//아침, 점심, 저녁, 밤 알람 시간을 저장하기 위함

    private String saveStartDate="";//복용 시작 시간 저장하기 위함
    private String saveEndDate="";//복용 완료 기간을 저장하기 위함

    private int year, month, dayOfMonth, year3, month3, dayOfMonth3, year5, month5, dayOfMonth5, year7, month7, dayOfMonth7; //각각 오늘/3일 후/5일 후/7일 후 날짜를 가져오기 위한 연,월,일 값
    private TextView day3, day5, day7, dayEvery, daySet; //복용 기간을 선택 시 배경 바꾸기 위해 선언

    private TextView cardview, cardtime, cardview2, cardtime2, cardview3, cardtime3, cardview4, cardtime4; //복용 기간을 선택 시 배경 바꾸기 위해 선언

    private TextView showDate; //사용자가 복용기간 선택 후 보여 줄 화면

    private TextView save;//알람 저장

    private String startDate, endDate, endDate3, endDate5, endDate7; //복용기간의 시작과 끝을 지정할 변수, 복용 끝은 3 5 7일로 나눔

    private CardView morning, noon, evening, night; //시간을 선택하는 카드뷰 값을 담기 위한 변수

    private String selectedTime; //선택된 시간을 보여주기 위함

    private boolean isCardViewOn1, isCardViewOn2, isCardViewOn3, isCardViewOn4 = false; // 상태를 저장할 변수

    private DataBaseHelper dbHelper;

    private TextInputEditText editNameInput;

    Calendar calendar = Calendar.getInstance();


    Button submit;
    EditText edit;

    boolean[] isCardOn = {isCardViewOn1, isCardViewOn2, isCardViewOn3, isCardViewOn4};// 각 TextView의 상태를 저장할 배열

    boolean admitSave=false; //저장 허가 여부 결정

    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.delete_medialarm);

        Toolbar toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뒤로가기 버튼 클릭 시 수행할 동작 추가
                finish();
            }
        });

        // Intent로부터 데이터를 받음
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("mediName")) {

            mediName = intent.getStringExtra("mediName"); //화면 설정 위함
        }

        dbHelper = new DataBaseHelper(this);

        loadData(); //저장된 데이터를 가지고 옴


        save = findViewById(R.id.btnDelete);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //날짜가 되면 알람을 삭제해주는 부분을 이용하여 사용자가 삭제 버튼을 누르면 알람이 울리지 않고 삭제됨
                AReceiver aReceiver=new AReceiver();
                aReceiver.deleteAlarm(dbHelper,deleteMediAlarm.this,mediName);

                finish();

            }
        });



        takeValue(); //findViewById로 값들 정의하기(코드가 길어져서 함수로 정의하고 한번에 정의함)
        TextView[] textViews = {day3, day5, day7, dayEvery, daySet};
        CardView[] cardViews = {morning, noon, evening, night};
        TextView[] cardNameViews = {cardview, cardview2, cardview3, cardview4};
        TextView[] cardTimeViews = {cardtime, cardtime2, cardtime3, cardtime4};

        startDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);


        editNameInput.setText(mediName);
        showDate.setText("복용기간: "+saveStartDate+"~"+saveEndDate);


        for(int i=0;i<4;i++){
            if(!(saveTime[i].equals(""))){
                cardViews[i].setBackground(ContextCompat.getDrawable(deleteMediAlarm.this, R.drawable.border_background_select_date));
                cardNameViews[i].setTextColor(ContextCompat.getColor(deleteMediAlarm.this, R.color.white));
                cardTimeViews[i].setTextColor(ContextCompat.getColor(deleteMediAlarm.this, R.color.white));
                cardTimeViews[i].setText(saveTime[i]);
            }
        }
    }

    private void takeValue() { //xml 요소들을 정의
        TextInputLayout nameInputLayout = findViewById(R.id.inputName);
        editNameInput = nameInputLayout.findViewById(R.id.editNameInput);
        editNameInput.setEnabled(false);

        //takeMedi = findViewById(R.id.takeMedi); 240519 추가주석

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


        showDate = findViewById(R.id.showDate);
        //여기까지 기간설정에 쓰이는 변수 정의-----------------------------------------


        //여기부터------------------------------------------
        morning = findViewById(R.id.card_morning);
        morning.setTag("cardView1");

        noon = findViewById(R.id.card_noon);
        noon.setTag("cardView2");

        evening = findViewById(R.id.card_evening);
        evening.setTag("cardView3");

        night = findViewById(R.id.card_night);
        night.setTag("cardView4");

        //cardview는 아침, 점심, 저녁, 취침 전의 text들
        cardview = findViewById(R.id.cardName);
        cardview.setTag("cardNameView1");

        cardview2 = findViewById(R.id.cardName2);
        cardview2.setTag("cardNameView2");

        cardview3 = findViewById(R.id.cardName3);
        cardview3.setTag("cardNameView3");

        cardview4 = findViewById(R.id.cardName4);
        cardview4.setTag("cardNameView4");

        //cardtime은 카드뷰의 오른쪽에 표기되는 시간
        cardtime = findViewById(R.id.cardTime);
        cardtime.setTag("cardTimeView1");

        cardtime2 = findViewById(R.id.cardTime2);
        cardtime2.setTag("cardTimeView2");

        cardtime3 = findViewById(R.id.cardTime3);
        cardtime3.setTag("cardNameView3");

        cardtime4 = findViewById(R.id.cardTime4);
        cardtime4.setTag("cardNameView4");

        //여기까지 시간 설정에 쓰이는 변수 정의------------------------------------------


    }

    private void loadData(){
        dbHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT morningTime,noonTime,eveningTime,nightTime FROM MediAlarmSystem WHERE alarmName=?", new String[]{mediName});
        if(cursor.moveToFirst()) {
            for (int j = 0; j < 4; j++) {
                saveTime[j] = cursor.getString(j); // alarmEnd 값을 가져옴
            }
        }
        cursor.close();

        Cursor cursor2 = db.rawQuery("SELECT alarmStart, alarmEnd FROM userMediAlarm WHERE alarmName=?", new String[]{mediName});
        if(cursor2.moveToFirst()){
            saveStartDate=cursor2.getString(0);
            saveEndDate=cursor2.getString(1);
        }

        cursor2.close();
    }


}
