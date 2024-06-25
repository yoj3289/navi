package com.example.naviproj;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class delete_weight {

    private Context context;
    private Dialog dialog;
    //private EditText editText;

    private DialogCloseListener listener;
    private DataBaseHelper dbHelper;

    private String userName;
    private String userKg;
    private String userInput, userDate;

    private String delMode=""; //삭제 모드를 지정
    private TextInputEditText inputKg, inputDate;

    private TextView noti;//삭제 안내 메시지, mode(체중, 혈압, 혈당)에 따라 내용이 조금씩 달라짐

    public delete_weight(Context context, DialogCloseListener listener) {
        this.context = context;
        this.listener= listener;
    }


    public void showDialog(String mode) {
        // 다이얼로그를 생성하고 배경을 투명하게 설정

        delMode=mode;

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.del_weight);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);//바깥쪽 눌러도 창이 사라지지 않게 함


        // 다이얼로그 안의 위젯들을 참조
        TextInputLayout kgInputLayout = dialog.findViewById(R.id.inputKg);//체중참조
        inputKg = kgInputLayout.findViewById(R.id.editKgInput);

        noti=dialog.findViewById(R.id.howTo);

        changeMessage(delMode); //설명란과 입력란 hint값 바꾸기

        Button btnOk = dialog.findViewById(R.id.btnOk);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        // "OK" 버튼을 눌렀을 때의 동작을 정의
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput = inputKg.getText().toString();

                if(mode=="weight"){
                    if (userInput.equals("모든 체중 데이터를 삭제하겠습니다.")) {
                        Toast.makeText(context, "모든 체중데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        deleteAllTable();
                    }
                    else{
                        Toast.makeText(context, "삭제 문구를 정확히 입력하세요.", Toast.LENGTH_SHORT).show();
                    }
                }

                else if(mode=="press"){
                    if (userInput.equals("모든 혈압 데이터를 삭제하겠습니다.")) {
                        Toast.makeText(context, "모든 혈압데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        deleteAllTable();
                    }
                    else{
                        Toast.makeText(context, "삭제 문구를 정확히 입력하세요.", Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    if (userInput.equals("모든 혈당 데이터를 삭제하겠습니다.")) {
                        Toast.makeText(context, "모든 혈당데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        deleteAllTable();
                    }
                    else{
                        Toast.makeText(context, "삭제 문구를 정확히 입력하세요.", Toast.LENGTH_SHORT).show();
                    }
                }

                if (listener != null) {
                    listener.onDialogClose(); // 클래스 A에게 신호를 보냄
                }
                // 다이얼로그를 닫기
                dialog.dismiss();


            }
        });

        // "취소" 버튼을 눌렀을 때의 동작을 정의
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // 다이얼로그를 표시
        dialog.show();
    }

    private void findData() { //데이터를 추가하기 위해 쿼리문을 설정하는 알고리즘
        dbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM userInfo", null);
        if (cursor.moveToFirst()) {
            userName = cursor.getString(0);
        }
        cursor.close();

        dbHelper.close();
    } //테이블에 요소를 추가하기 위한 함수

    private void deleteAllTable() { //데이터를 추가하기 위한 알고리즘
        dbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(delMode=="weight"){
            db.execSQL("DELETE FROM userWeight");
        }
        else if(delMode=="press"){
            db.execSQL("DELETE FROM userPress");
        }
        else{
            db.execSQL("DELETE FROM userSugar");
        }
        db.close();
    } //테이블에 요소를 추가하기 위한 함수

    //삭제 전 확인받는 textInput의 hint와 상단에 삭제 관련 설명 내용을 mode에 따라 바꿔줌
    private void changeMessage(String mode){
        if(mode=="weight"){
            inputKg.setHint("모든 체중 데이터를 삭제하겠습니다.");
            noti.setText("1. 모든 체중데이터가 삭제돼요.\n2. 삭제한 후에는 복구할 수 없어요.\n3. 문구를 따라 쓰고 버튼을 누르세요.");
        }
        else if(mode=="press"){
            inputKg.setHint("모든 혈압 데이터를 삭제하겠습니다.");
            noti.setText("1. 모든 혈압데이터가 삭제돼요.\n2. 삭제한 후에는 복구할 수 없어요.\n3. 문구를 따라 쓰고 버튼을 누르세요.");
        }
        else{
            inputKg.setHint("모든 혈당 데이터를 삭제하겠습니다.");
            noti.setText("1. 모든 혈당데이터가 삭제돼요.\n2. 삭제한 후에는 복구할 수 없어요.\n3. 문구를 따라 쓰고 버튼을 누르세요.");
        }
    }

}