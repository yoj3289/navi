package com.example.naviproj.ViewModel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.naviproj.R;
import com.example.naviproj.utility.FirebaseID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_register_id, et_register_pw, et_register_name, et_register_age;
    private Button btn_register;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // xml파일에서 선언한 아이디 연결
        et_register_id = findViewById(R.id.et_login_id);
        et_register_pw = findViewById(R.id.et_login_pw);
        et_register_name = findViewById(R.id.et_register_name);
        et_register_age = findViewById(R.id.et_register_age);

        btn_register = findViewById(R.id.btn_register);

        findViewById(R.id.btn_register).setOnClickListener(this);

    }


    private boolean checkInputFields(String input, String fieldName, EditText editText) {
        if (TextUtils.isEmpty(input) || input == null) {
            Toast.makeText(RegisterActivity.this, fieldName + "을(를) 입력하세요", Toast.LENGTH_SHORT).show();
            editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        final String email = et_register_id.getText().toString().trim();
        final String password = et_register_pw.getText().toString().trim();
        final String name = et_register_name.getText().toString().trim();
        final String age = et_register_age.getText().toString().trim();

        try {
            boolean isEmailValid = checkInputFields(email, "이메일", et_register_id);
            boolean isPasswordValid = checkInputFields(password, "비밀번호", et_register_pw);
            boolean isNameValid = checkInputFields(name, "이름", et_register_name);
            boolean isAgeValid = checkInputFields(age, "나이", et_register_age);

            if (isEmailValid && isPasswordValid && isNameValid && isAgeValid) {
                // 이메일, 비밀번호, 이름, 나이가 모두 입력된 경우 Firebase에 회원가입 요청을 보냄
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // 회원가입 성공시
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put(FirebaseID.documentId, user.getUid());
                                    userMap.put(FirebaseID.email, email);
                                    userMap.put(FirebaseID.password, password);
                                    userMap.put(FirebaseID.name, name);
                                    userMap.put(FirebaseID.age, age);
                                    mStore.collection(FirebaseID.user).document(user.getUid()).set(userMap, SetOptions.merge());
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // 회원가입 실패시
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthUserCollisionException e) {
                                        Toast.makeText(RegisterActivity.this, "이미 가입한 이메일입니다", Toast.LENGTH_SHORT).show();
                                    } catch (FirebaseAuthInvalidCredentialsException e) {
                                        Toast.makeText(RegisterActivity.this, "이메일 형식이 잘못되었습니다", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Toast.makeText(RegisterActivity.this, "회원가입 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        } catch (Exception e) {
            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}