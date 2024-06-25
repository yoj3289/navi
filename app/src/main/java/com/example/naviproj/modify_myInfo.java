package com.example.naviproj;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class modify_myInfo extends AppCompatActivity {

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
        setContentView(R.layout.user_join2_2); // XML 파일과 연결

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


        loadData();

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

        nameInput = nameEditText.getText().toString();
        birthInput=birthEditText.getText().toString();
        heightInput = Double.parseDouble(heightEditText.getText().toString());
        weightInput = Double.parseDouble(weightEditText.getText().toString());


        if(sexEditText!="성별"&&bloodEditText!="혈액형"){
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String query = "UPDATE userInfo SET Birth=?,Height=?,Kg=?,Sex=?,Blood=? WHERE (Name=?)";//이렇게 변수를 직접 전달하지 말고 ?로 전달하여 SQLinjection방지
            db.execSQL(query, new Object[]{birthInput,heightInput,weightInput,sexEditText,bloodEditText,nameInput});
            db.close();
            setResult(Activity.RESULT_OK);
            finish();
            Toast.makeText(this, "정보가 성공적으로 수정됐어요!", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "성별과 혈액형을 입력 해 주세요!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadData(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo", null);
        if (cursor.moveToNext()) {
            nameEditText.setText(cursor.getString(0));
            birthEditText.setText(cursor.getString(1));

            heightEditText.setText(cursor.getString(2));
            weightEditText.setText(cursor.getString(3));

        } else {
            // 결과가 없을 경우 처리
        }
    }
    private void showDatePickerDialog() {

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
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
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //datepicker의 여백을 지움
        datePickerDialog.show();
    }


}
