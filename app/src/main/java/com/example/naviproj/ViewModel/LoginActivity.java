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
 import com.google.android.gms.tasks.OnCompleteListener;
 import com.google.android.gms.tasks.Task;
 import com.google.firebase.auth.AuthResult;
 import com.google.firebase.auth.FirebaseAuth;
 import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
 import com.google.firebase.auth.FirebaseAuthInvalidUserException;

 public class LoginActivity extends AppCompatActivity {

     private EditText et_login_id, et_login_pw;
     private Button btn_Register;
     private Button btn_Login;

     private FirebaseAuth mAuth = FirebaseAuth.getInstance();

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);

         et_login_id = findViewById(R.id.et_login_id);
         et_login_pw = findViewById(R.id.et_login_pw);
         btn_Login = findViewById(R.id.btn_login);
         btn_Register = findViewById(R.id.btn_register);

         //회원가입 버튼 클릭시 화면 전환
         btn_Register.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                 startActivity(intent);
                 finish();
             }
         });

         //로그인 버튼 클릭시 화면 전환
         btn_Login.setOnClickListener(new View.OnClickListener() {
             private boolean checkInputFields(String input, String fieldName, EditText editText) {
                 if (TextUtils.isEmpty(input) || input == null) {
                     Toast.makeText(LoginActivity.this, fieldName + "을(를) 입력하세요", Toast.LENGTH_SHORT).show();
                     editText.requestFocus();
                     InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                     imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                     return false;
                 }
                 return true;
             }

             @Override
             public void onClick(View v) {
                 String email = et_login_id.getText().toString().trim();
                 String password = et_login_pw.getText().toString().trim();

                 try {
                     boolean isEmailValid = checkInputFields(email, "이메일", et_login_id);
                     boolean isPasswordValid = checkInputFields(password, "비밀번호", et_login_pw);
                     if (isEmailValid && isPasswordValid) {
                         mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                             @Override
                             public void onComplete(@NonNull Task<AuthResult> task) {
                                 if (task.isSuccessful()) {
                                     Intent intent = new Intent(LoginActivity.this, NabiPlusActivity.class);
                                     startActivity(intent);
                                     finish();
                                 } else {
                                     try {
                                         throw task.getException();
                                     } catch (FirebaseAuthInvalidUserException invalidEmail) {
                                         Toast.makeText(LoginActivity.this, "해당하는 이메일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                                     } catch (FirebaseAuthInvalidCredentialsException wrongPassword) {
                                         Toast.makeText(LoginActivity.this, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                                     } catch (Exception e) {
                                         Toast.makeText(LoginActivity.this, "로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                     }
                                 }
                             }
                         });
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         });
     }
 }