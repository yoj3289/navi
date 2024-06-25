package com.example.naviproj.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.naviproj.R;
import com.example.naviproj.ViewModel.DiaryDetailActivity;
import com.example.naviproj.ViewModel.PageCurlPageTransformer;
import com.example.naviproj.adapter.DiaryPagerAdapter;
import com.example.naviproj.model.DiaryPage;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiaryDetailFragment extends Fragment {

    private ViewPager2 viewPager;
    private DiaryPagerAdapter adapter;
    private List<String> diaryIds; // 일기 ID 목록
    private String currentCollectionId; // 현재 페이지의 collectionId를 저장할 변수 추가

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary_detail, container, false);
        viewPager = view.findViewById(R.id.viewpage_diary);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.viewpage_diary);
        adapter = new DiaryPagerAdapter(getContext(), viewPager);
        viewPager.setAdapter(adapter);

        // PageTransformer 설정
        viewPager.setPageTransformer(new PageCurlPageTransformer());

        String collectionId = getArguments().getString("diary_collectionid");
        String documentId = getArguments().getString("diary_documentid");
        int selectedPosition = getArguments().getInt("selectedPosition", 0);
        viewPager.setCurrentItem(selectedPosition, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("diary")
                .document("diarypage")
                .collection("pages")
                .whereEqualTo("documentId", documentId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        // Handle failure
                        return;
                    }

                    adapter.clearDiaryPages(); // 기존 데이터 초기화

                    // 역순으로 정렬된 리스트 생성 240603추가
                    List<DocumentSnapshot> reversedList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        reversedList.add(documentSnapshot);
                    }
                    Collections.reverse(reversedList);

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (documentSnapshot.exists()) {
                            DiaryPage diaryPage = documentSnapshot.toObject(DiaryPage.class);
                            String emoji = documentSnapshot.getString("emoji"); // emoji 필드 가져오기
                            diaryPage.setEmoji(emoji); // DiaryPage 객체에 emoji 설정
                            adapter.addDiaryPage(diaryPage);
                        }
                    }

                    viewPager.setAdapter(adapter); // 어댑터 업데이트
                    viewPager.setCurrentItem(selectedPosition, false);
                });

        // Back button pressed
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        });
    }

    // setCurrentPosition 메서드를 추가하여 외부에서 호출 가능
    public void setCurrentPosition(int position) {
        viewPager.setCurrentItem(position);
    }

    // getCurrentPosition 메서드를 추가하여 현재 선택된 일기장의 위치를 반환
    public int getCurrentPosition() {
        return viewPager.getCurrentItem();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
    }

    private ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            DiaryPage currentDiaryPage = adapter.getDiaryPage(position);
            currentCollectionId = currentDiaryPage.getCollectionId();
            ((DiaryDetailActivity) requireActivity()).updateCollectionId(currentCollectionId);
        }
    };

}
