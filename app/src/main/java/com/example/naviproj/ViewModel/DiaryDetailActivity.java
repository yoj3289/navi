package com.example.naviproj.ViewModel;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.naviproj.R;
import com.example.naviproj.adapter.DiaryPagerAdapter;
import com.example.naviproj.fragment.DiaryDetailFragment;
import com.example.naviproj.model.DiaryPage;
import com.google.firebase.firestore.FirebaseFirestore;

public class DiaryDetailActivity extends AppCompatActivity {
    private ImageView btn_back;
    private String collectionId;
    private ImageView ic_kebab;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_detail);

        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> onBackPressed());

        collectionId = getIntent().getStringExtra("diary_collectionid");
        String documentId = getIntent().getStringExtra("diary_documentid");
        int selectedPosition = getIntent().getIntExtra("selectedPosition", 0);

        DiaryDetailFragment fragment = new DiaryDetailFragment();
        Bundle args = new Bundle();
        args.putString("diary_collectionid", collectionId);
        args.putString("diary_documentid", documentId);
        args.putInt("selectedPosition", selectedPosition);
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

        ic_kebab = findViewById(R.id.ic_kebab);
        ic_kebab.setOnClickListener(this::showMenu);
    }

    private void showMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.diary_detail_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popupMenu.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diary_detail_menu, menu);
        return true;
    }

    // DiaryDetailFragment 인스턴스를 찾아 setCurrentPosition 메서드를 호출하여 수정된 일기장의 위치를 전달
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            int updatedPosition = data.getIntExtra("updated_position", 0);
            DiaryDetailFragment fragment = (DiaryDetailFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment != null) {
                fragment.setCurrentPosition(updatedPosition);
            }
        }

    }


    // DiaryDetailFragment 인스턴스를 찾아 getCurrentPosition 메서드를 호출하여 현재 선택된 일기장의 위치를 가져온다.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit:
                Intent intent = new Intent(this, DiaryUploadActivity.class);
                intent.putExtra("diary_collectionid", collectionId);
                DiaryDetailFragment fragment = (DiaryDetailFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (fragment != null) {
                    intent.putExtra("selectedPosition", fragment.getCurrentPosition());
                }
                startActivityForResult(intent, 1);
                return true;
            case R.id.menu_delete:
                showDeleteConfirmationDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("일기 삭제")
                .setMessage("해당 일기를 삭제하시겠습니까?")
                .setPositiveButton("확인", (dialog, which) -> deleteDiary())
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteDiary() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("diary")
                .document("diarypage")
                .collection("pages")
                .document(collectionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // 삭제 성공 시 동작
                    finish(); // 현재 액티비티 종료
                })
                .addOnFailureListener(e -> {
                    // 삭제 실패 시 동작
                    showErrorMessage("일기 삭제에 실패했습니다.");
                });
    }

    private void showErrorMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("에러")
                .setMessage(message)
                .setPositiveButton("확인", null)
                .show();
    }
}