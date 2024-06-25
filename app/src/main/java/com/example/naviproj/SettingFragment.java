package com.example.naviproj;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment implements CustomDatePicker2.DateSelectedListener {

    private TextInputEditText nameEditText;
    private TextInputEditText birthEditText;
    private TextInputEditText heightEditText;
    private TextInputEditText weightEditText;
    private String sexEditText;
    private String bloodEditText;



    private TextView showName, showBirth, showSex, showBlood, showBMI, spinnerTitle, spinnerTitle2; //spinnerTitle은 혈액형에서 spinnerTirle2sms 성별에서 쓰임
    private Button save;
    private boolean validate = false;

    private Spinner spinner, spinners; //spinner는 혈액형 spinners는 성별

    private String test;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_join2, container, false);


        // 이름과 생년월일 입력란과 레이아웃 찾아오기
        TextInputLayout nameTextInputLayout = view.findViewById(R.id.getName);
        nameEditText = nameTextInputLayout.findViewById(R.id.inputName);

        TextInputLayout birthTextInputLayout = view.findViewById(R.id.getBirth);
        birthEditText = birthTextInputLayout.findViewById(R.id.inputBirth);

        birthEditText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showDatePickerDialog();
            }
        });


        TextInputLayout heightTextInputLayout = view.findViewById(R.id.getHeight);
        heightEditText = heightTextInputLayout.findViewById(R.id.inputHeight);

        TextInputLayout weightTextInputLayout = view.findViewById(R.id.getWeight);
        weightEditText = weightTextInputLayout.findViewById(R.id.inputWeight);

        showName=view.findViewById(R.id.cardName);
        showBirth=view.findViewById(R.id.cardBirth);
        showSex=view.findViewById(R.id.cardSex);
        showBlood=view.findViewById(R.id.cardBlood);
        showBMI=view.findViewById(R.id.cardBlood);


        save=view.findViewById(R.id.submit);

        // submit 버튼에 클릭 이벤트 리스너 등록
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭 이벤트 발생 시 실행할 코드 작성
                ButtonSubmit();
            }
        });

        //혈액형 스피너
        spinnerTitle=view.findViewById(R.id.categoryTextView);
        spinner=view.findViewById(R.id.categorySpinner);

        spinnerTitle2=view.findViewById(R.id.categoryTextViews);
        spinners=view.findViewById(R.id.categorySpinners);


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

        return view;
    }

    private void ButtonSubmit() {

        String nameInput = nameEditText.getText().toString();
        String birthInput = "생년월일: "+birthEditText.getText().toString();
        String heightInput = heightEditText.getText().toString();
        String weightInput = weightEditText.getText().toString();
        String sexInput = "성별: "+sexEditText;
        String bloodInput = "혈액형: "+bloodEditText;
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


    private void showDatePickerDialog() {

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                //android.R.style.Theme_Holo_Light_Dialog,
                R.style.CustomDatePicker2,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 사용자가 선택한 날짜 처리
                        String selectedDate = String.format("%d년 %d월 %d일", year, monthOfYear + 1, dayOfMonth);
                        birthEditText.setText(selectedDate);
                    }
                },
                // 초기 날짜 설정 (년, 월, 일)
                2000, 0, 1
        );

        Calendar maxDate = Calendar.getInstance();
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.setTitle("생년월일");
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //datepicker의 여백을 지움
        datePickerDialog.setCancelable(false); //여백을 눌러도 꺼지지 않게 함
        datePickerDialog.show();
    }

    private void showCustomDatePicker() {
        CustomDatePicker2 customDatePicker2 = new CustomDatePicker2(requireContext(),this);
        customDatePicker2.showDialog();

    }

    public void onResume() {
        super.onResume();
        // 프래그먼트가 화면에 표시될 때마다 호출되는 메서드
        // 원하는 작업을 수행
        ButtonSubmit();
    }
    public void onDateSelected(String selectedDate) {
        // 선택한 날짜를 처리
        // 'selectedDate'에는 "YYYY-MM-DD" 형식의 선택한 날짜가 포함
        birthEditText.setText(selectedDate);
    }

}