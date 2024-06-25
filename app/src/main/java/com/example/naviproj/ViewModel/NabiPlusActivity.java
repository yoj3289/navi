package com.example.naviproj.ViewModel;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.naviproj.R;
import com.example.naviproj.adapter.ContentsPagerAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NabiPlusActivity extends AppCompatActivity {

    private Button btn_logout;

    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private ContentsPagerAdapter mContentPagerAdapter;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nabiplus);

        mTabLayout = findViewById(R.id.tab_layout);

        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.home_hilight));
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.profile));
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.feed));
//        mTabLayout.addTab(mTabLayout.newTab().setText("홈"));

        mViewPager = findViewById(R.id.ViewPager);

        // 로그아웃 버튼 초기화
        btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(v -> {
            // Firebase에서 로그아웃
            mAuth.signOut();

            // 로그아웃 알림 다이얼로그 표시
            new MaterialAlertDialogBuilder(NabiPlusActivity.this)
                    .setMessage("로그아웃 되었습니다!")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 다이얼로그 확인 클릭 시 액티비티 종료
                            finish();
                        }
                    })
                    .show();
        });


        // 로그인한 사용자 정보 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // 사용자가 로그인한 상태
        if(user != null){
            String email = user.getEmail(); // 사용자 이메일
        }else{
            // 사용자가 로그인하지 않은 상태일 경우 로그인 화면으로 돌아가기
            Intent intent = new Intent(NabiPlusActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        //프레그먼트 이동 구현
        ContentsPagerAdapter contentsPagerAdapter = new ContentsPagerAdapter(this);
        mViewPager.setAdapter(contentsPagerAdapter);


        //tabLayout - ViewPager 연결
        new TabLayoutMediator(mTabLayout, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                // 각 탭에 이미지 배치
                mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        mViewPager.setCurrentItem(tab.getPosition());
                        switch (tab.getPosition()) {
                            case 0 :
                                mTabLayout.getTabAt(0).setIcon(R.drawable.home_hilight);
                                mTabLayout.getTabAt(1).setIcon(R.drawable.profile);
                                mTabLayout.getTabAt(2).setIcon(R.drawable.feed);
                                break;
                            case 1:
                                mTabLayout.getTabAt(0).setIcon(R.drawable.home);
                                mTabLayout.getTabAt(1).setIcon(R.drawable.profile_hilight);
                                mTabLayout.getTabAt(2).setIcon(R.drawable.feed);
                                break;
                            case  2:
                                mTabLayout.getTabAt(0).setIcon(R.drawable.home);
                                mTabLayout.getTabAt(1).setIcon(R.drawable.profile);
                                mTabLayout.getTabAt(2).setIcon(R.drawable.feed_hilight);
                                break;
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                    }
                });
            }
        }).attach();


    }
}