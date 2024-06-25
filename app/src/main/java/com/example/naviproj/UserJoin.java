//회원가입을 위한 클래스. 추후 코드 수정해야됨

package com.example.naviproj;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class UserJoin extends AppCompatActivity {

    private TextInputEditText nameEditText;
    private TextInputEditText birthEditText;
    private TextInputEditText heightEditText;
    private TextInputEditText weightEditText;
    //private TextInputEditText sexEditText;
    private String sexEditText;
    //private TextInputEditText bloodEditText;
    private String bloodEditText;


    private TextInputEditText userName, userBirth, userHeight, userWeight, userSex, userBlood;

    private TextView showName, showBirth, showSex, showBlood, showBMI, spinnerTitle, spinnerTitle2; //spinnerTitle은 혈액형에서 spinnerTirle2sms 성별에서 쓰임
    private Button save;
    private boolean validate = false;

    private Spinner spinner, spinners; //spinner는 혈액형 spinners는 성별

    private String test;

    private DataBaseHelper dbHelper;

    private String nameInput, birthInput,sexInput,bloodInput;

    private double heightInput,weightInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_join2); // XML 파일과 연결

        Toolbar toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        dbHelper=new DataBaseHelper(this);


        // 이름과 생년월일 입력란과 레이아웃 찾아오기
        TextInputLayout nameTextInputLayout = findViewById(R.id.getName);
        nameEditText = nameTextInputLayout.findViewById(R.id.inputName);

        TextInputLayout birthTextInputLayout = findViewById(R.id.getBirth);
        birthEditText = birthTextInputLayout.findViewById(R.id.inputBirth);

        birthEditText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showDatePickerDialog();
            }
        });


        TextInputLayout heightTextInputLayout = findViewById(R.id.getHeight);
        heightEditText = heightTextInputLayout.findViewById(R.id.inputHeight);

        TextInputLayout weightTextInputLayout = findViewById(R.id.getWeight);
        weightEditText = weightTextInputLayout.findViewById(R.id.inputWeight);




        showName=findViewById(R.id.cardName);
        showBirth=findViewById(R.id.cardBirth);
        showSex=findViewById(R.id.cardSex);
        showBlood=findViewById(R.id.cardBlood);
        showBMI=findViewById(R.id.cardBlood);


        save=findViewById(R.id.submit);

        // submit 버튼에 클릭 이벤트 리스너 등록
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭 이벤트 발생 시 실행할 코드 작성
                ButtonSubmit();
            }
        });

        //혈액형 스피너
        spinnerTitle=findViewById(R.id.categoryTextView);
        spinner=findViewById(R.id.categorySpinner);

        spinnerTitle2=findViewById(R.id.categoryTextViews);
        spinners=findViewById(R.id.categorySpinners);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent,View view, int position, long id ){
                //spinnerTitle.setText(""+parent.getItemAtPosition(position));
                bloodEditText=(String)parent.getItemAtPosition(position);
            }
            public void onNothingSelected(AdapterView<?> parent){}
        });

        spinners.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent,View view, int position, long id ){
                //spinnerTitle.setText(""+parent.getItemAtPosition(position));
                sexEditText=(String)parent.getItemAtPosition(position);
            }
            public void onNothingSelected(AdapterView<?> parent){}
        });

    }
    private void ButtonSubmit() {

        showName.setText(nameInput);
        showBirth.setText(birthInput);
        showSex.setText(sexInput);
        showBlood.setText(bloodInput);

        nameInput = nameEditText.getText().toString();
        birthInput=birthEditText.getText().toString();
        heightInput = Double.parseDouble(heightEditText.getText().toString());
        weightInput = Double.parseDouble(weightEditText.getText().toString());


        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("Name",nameInput);
        values.put("Birth",birthInput);
        values.put("Height",heightInput);
        values.put("Kg",weightInput);
        values.put("Sex",sexEditText);
        values.put("blood",bloodEditText);

        db.insert("userInfo", null, values);

        db.close();
        finish();
    }
    private void showDatePickerDialog() {

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                //android.R.style.Theme_Holo_Light_Dialog,
                R.style.CustomDatePicker2,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 사용자가 선택한 날짜 처리
                        //String selectedDate = String.format("%d.%d.%d", year, monthOfYear + 1, dayOfMonth);
                        String selectedDate = String.format("%02d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                        birthEditText.setText(selectedDate);
                    }
                },
                // 초기 날짜 설정 (년, 월, 일)
                2000, 0, 1
        );

        Calendar maxDate = Calendar.getInstance();
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.setTitle("생년월일");
        datePickerDialog.setCancelable(false);
        //datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //datepicker의 여백을 지움
        datePickerDialog.show();
    }

    /*private void showCustomDatePicker() {
        CustomDatePicker2 customDatePicker2 = new CustomDatePicker2(this,this);
        customDatePicker2.showDialog();
    }*/

    public void onResume() {
        super.onResume();
        // 프래그먼트가 화면에 표시될 때마다 호출되는 메서드
        // 원하는 작업을 수행
        nameInput = nameEditText.getText().toString();
        birthInput = "생년월일: "+birthEditText.getText().toString();
        sexInput = "성별: "+sexEditText;
        bloodInput = "혈액형: "+bloodEditText;
        //String sexInput = "성별: "+sexEditText.getText().toString();
        //String bloodInput = "혈액형: "+bloodEditText.getText().toString()+"형";
        if(sexEditText==null){
            sexInput="성별: ";
        }
        if(bloodEditText==null){
            bloodInput="혈액형: ";
        }

        showName.setText(nameInput);
        showBirth.setText(birthInput);
        showSex.setText(sexInput);
        showBlood.setText(bloodInput);
    }
    public void onDateSelected(String selectedDate) {
        // 여기서 선택한 날짜를 처리
        // 'selectedDate'에는 "YYYY-MM-DD" 형식의 선택한 날짜가 포함
        birthEditText.setText(selectedDate);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId ()) {
            case android.R.id.home: //툴바 뒤로가기버튼 눌렸을 때 동작
                finish ();
                return true;
            default:
                return super.onOptionsItemSelected (item);
        }
    }
}