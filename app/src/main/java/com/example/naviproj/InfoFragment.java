package com.example.naviproj;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.naviproj.ViewModel.LoginActivity;
import com.example.naviproj.ViewModel.NabiPlusActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoFragment extends Fragment {

    private DataBaseHelper dbHelper;

    private TextInputEditText userName, userBirth, userHeight, userWeight, userSex, userBlood;

    private TextView showName;
    private TextView showBirth;
    private TextView showSex;
    private TextView showBlood;
    private TextView showBMI;

    private ImageView showImg;

    private int age;

    private boolean validate = false;

    private RelativeLayout menu1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_page_with_info, container, false);
        dbHelper = new DataBaseHelper(requireContext());

        showName=view.findViewById(R.id.cardName); //이름을 보여주기 위함
        showBirth=view.findViewById(R.id.cardBirth); //생년월일을 보여주기 위함
        showSex=view.findViewById(R.id.cardSex); //성별을 보여주기 위함
        showBlood=view.findViewById(R.id.cardBlood); //혈액형을 보여주기 위함
        showBMI=view.findViewById(R.id.cardBmi); //체질량을 보여주기 위함
        showImg=view.findViewById(R.id.item_image);

        ActivityResultLauncher<Intent> startActivityResultLauncher = registerForActivityResult( //메인으로 돌아오면 그래프 새로고치기 위함
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Intent data = result.getData(); // 결과 데이터가 필요한 경우 사용
                        loadCard();
                    }
                });
        
        loadCard(); //건강카드 내용을 적용

        menu1 = view.findViewById(R.id.content_menu1);
        menu1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 팝업 액티비티를 띄우기 위한 Intent 생성
                Intent intent = new Intent(view.getContext(), modify_myInfo.class);
                startActivityResultLauncher.launch(intent);//홈화면 새로고침, 근데 나머지가 오류
            }
        });

        RelativeLayout content1=view.findViewById(R.id.NabiPluslativeLayout);
        content1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 로그인창 띄우기 위한 Intent 생성하기
                // 로그인한 사용자가 있는지 확인
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    // 이미 로그인한 사용자가 있다면, 바로 NabiPlusActivity로 이동
                    Intent intent = new Intent(getActivity(), NabiPlusActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getActivity(), LoginActivity.class); //fragment라서 activity intent와는 다른 방식
                    startActivity(intent);
                }
            }
        });

        RelativeLayout content2=view.findViewById(R.id.manageWeight);
        content2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ManagementWeight.class);
                view.getContext().startActivity(intent);
            }
        });

        RelativeLayout content3=view.findViewById(R.id.managePress);
        content3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ManagementPress.class);
                view.getContext().startActivity(intent);
            }
        });

        RelativeLayout content4=view.findViewById(R.id.manageSugar);
        content4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ManagementSugar.class);
                view.getContext().startActivity(intent);
            }
        });

        return view;
    }

    private void loadCard(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo", null);
        if (cursor.moveToNext()) {
            showName.setText(cursor.getString(0));
            showBirth.setText("생년월일: "+cursor.getString(1));
            showSex.setText("성별: "+cursor.getString(4));
            showBlood.setText("혈액형: "+cursor.getString(5));


            Calendar current = Calendar.getInstance();

            int currentYear  = current.get(Calendar.YEAR);
            int currentMonth = current.get(Calendar.MONTH) + 1;
            int currentDay   = current.get(Calendar.DAY_OF_MONTH);

            //substring은 (시작인덱스,끝인덱스+1) substring의 두번째 인자-1번까지의 값을 가져오기 때문
            int ageYear=Integer.parseInt(cursor.getString(1).substring(0,4));
            int ageMonth=Integer.parseInt(cursor.getString(1).substring(5,7));
            int ageDay=Integer.parseInt(cursor.getString(1).substring(8,10));
            age = currentYear - ageYear;
            // 만약 생일이 지나지 않았으면 -1
            if (ageMonth * 100 + ageDay > currentMonth * 100 + currentDay)
                age--;

            //25세까지 소년소녀, 65세까지 남성여성 이후는 어르신으로
            if(age<=25){
                if(cursor.getString(4).equals("남성")){
                    showImg.setImageResource(R.drawable.boy_test);
                }
                else{
                    showImg.setImageResource(R.drawable.ic_girl);
                }
            }
            else if(age>25&&age<=65){
                if(cursor.getString(4).equals("남성")){
                    showImg.setImageResource(R.drawable.ic_man);
                }
                else{
                    showImg.setImageResource(R.drawable.ic_woman);
                }
            }

            else if(age>65){
                if(cursor.getString(4).equals("남성")){
                    showImg.setImageResource(R.drawable.ic_grandpa);
                }
                else{
                    showImg.setImageResource(R.drawable.ic_grandma);
                }
            }



            String height=cursor.getString(2);
            String Kg=cursor.getString(3);

            double changeHeight=Double.parseDouble(height)/100;
            double res=Double.parseDouble(Kg)/Math.pow(changeHeight,2);
            res=(int)(res*100)/100.0;

            BigDecimal halfRound = BigDecimal.valueOf(res).setScale(3, RoundingMode.HALF_UP);
            res= halfRound.doubleValue();

            if(res<18.5){
                showBMI.setText("BMI: "+res+"(저체중)");
            }
            else if(res>=18.5&&res<=22.99){
                showBMI.setText("BMI: "+res+"(정상체중)");
            }
            else if(res>=23&&res<=24.99){
                showBMI.setText("BMI: "+res+"(비만 전 단계)");
            }
            else if(res>=25&&res<=29.99){
                showBMI.setText("BMI: "+res+"(1단계 비만)");
            }
            else if(res>=30&&res<=34.99){
                showBMI.setText("BMI: "+res+"(2단계 비만)");
            }
            else if(res>=35){
                showBMI.setText("BMI: "+res+"(3단계 비만)");
            }

        } else {
            // 결과가 없을 경우 처리
        }
    }

}