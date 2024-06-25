package com.example.naviproj;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    HomeFragment homeFragment;
    MedicationFragment medicationFragment;
    InfoFragment infoFragment;

    PedoMeterFragment pedoMeterFragment;
    SettingFragment settingFragment;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private DataBaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper=new DataBaseHelper(this);

        homeFragment=new HomeFragment();
        infoFragment=new InfoFragment();
        settingFragment=new SettingFragment();
        medicationFragment=new MedicationFragment();

        pedoMeterFragment=new PedoMeterFragment();

        checkAndRequestPermissions();

        getSupportFragmentManager().beginTransaction().replace(R.id.containers, homeFragment).commit();

        // Info.db에서 userInfo 테이블이 없으면 UserJoin 화면으로 이동
        if (isUserInfoTableEmpty(dbHelper)) {
            Intent intent = new Intent(this, UserJoin.class);
            startActivity(intent);
            //finish(); // 현재 액티비티를 종료하여 뒤로가기 시 Main으로 돌아가지 않도록 함
            //return;
        }


        NavigationBarView navigationBarView=findViewById(R.id.bottom_navigationview);
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.containers, homeFragment).commit();
                        return true;
                    case R.id.info:
                        getSupportFragmentManager().beginTransaction().replace(R.id.containers, infoFragment).commit();
                        return true;
                    case R.id.medi:
                        getSupportFragmentManager().beginTransaction().replace(R.id.containers, medicationFragment).commit();
                        return true;

                    case R.id.run:
                        getSupportFragmentManager().beginTransaction().replace(R.id.containers, pedoMeterFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }
    private boolean isUserInfoTableEmpty(DataBaseHelper dbHelper) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM userInfo", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count == 0;
        }

    private void checkAndRequestPermissions() {
        // 필요한 권한 목록을 동적으로 구성
        String[] permissions = {
                Manifest.permission.VIBRATE,
                Manifest.permission.SCHEDULE_EXACT_ALARM,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.USE_FULL_SCREEN_INTENT,
                Manifest.permission.ACTIVITY_RECOGNITION
        };

        // 요청이 필요한 권한을 확인하여 목록에 추가
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        // 요청이 필요한 권한이 있다면 요청을 진행
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    // 권한이 거부됨. 필요한 경우 사용자에게 알림
                }
            }
        }
    }
    }
