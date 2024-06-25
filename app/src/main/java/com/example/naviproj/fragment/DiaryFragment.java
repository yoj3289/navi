package com.example.naviproj.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.naviproj.R;
import com.example.naviproj.ViewModel.DiaryFragmentViewModel;
import com.example.naviproj.ViewModel.DiaryUploadActivity;
import com.example.naviproj.adapter.DiaryAdapter;
import com.example.naviproj.model.Diary;
import com.example.naviproj.utility.FirebaseID;
import com.example.naviproj.utility.RecyclerDecoration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiaryFragment extends Fragment implements View.OnClickListener {

    private View view;

    private DiaryFragmentViewModel mViewModel;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();

    private RecyclerView mDiaryRecyclerView;
    private DiaryAdapter mAdapter;
    private List<Diary> mDatas;
    private ViewPager2 viewPager2;

    private FloatingActionButton floatingActionButton;

    private RecyclerDecoration spaceDecoration; // RecyclerDecoration 객체를 멤버 변수로 선언


    @Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_diary, container, false);

        mDiaryRecyclerView = view.findViewById(R.id.diary_RecyclerView);
        viewPager2 = view.findViewById(R.id.diary_view2);


        floatingActionButton = view.findViewById(R.id.diary_write);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DiaryUploadActivity.class); //fragment라서 activity intent와는 다른 방식
                // intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        return view;
    }

    public void onActivityCreated (@Nullable Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DiaryFragmentViewModel.class);
        // TODO: Use the ViewModel
    }

    // 시작시 리사이클러뷰를 통해 작성한 글 나열
    @Override
    public void onStart() {
        super.onStart();
        mDatas = new ArrayList<>();
        if (mAuth.getCurrentUser() != null) {
            mStore.collection("diary")
                    .document("diarypage")
                    .collection("pages")
                    .whereEqualTo(FirebaseID.documentId, mAuth.getCurrentUser().getUid())
                    .orderBy(FirebaseID.time, Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.e("Firestore Error", "Listen failed", error);
                                return;
                            }

                            if (value != null) {
                                mDatas.clear();
                                for (DocumentSnapshot snap : value.getDocuments()) {
                                    Map<String, Object> shot = snap.getData();
                                    String documentID = String.valueOf(shot.get((FirebaseID.documentId)));
                                    String name = String.valueOf(shot.get(FirebaseID.name));
                                    String title = String.valueOf(shot.get(FirebaseID.title));
                                    String contents = String.valueOf(shot.get(FirebaseID.contents));
                                    String collectionID = String.valueOf(shot.get(FirebaseID.collectionId));
                                    String timestamp = String.valueOf(shot.get(FirebaseID.timestamp));
                                    String emoji = String.valueOf(shot.get("emoji")); // 이모지 값 가져오기
                                    Diary data = new Diary(documentID, name, title, contents, collectionID, timestamp, emoji);
                                    mDatas.add(data);
                                }
                                mAdapter = new DiaryAdapter(mDatas, viewPager2);
                                mDiaryRecyclerView.setAdapter(mAdapter);

                                updateItemDecoration();

                                // 데이터를 설정한 후 자동으로 맨 아래로 스크롤
                                LinearLayoutManager layoutManager = (LinearLayoutManager) mDiaryRecyclerView.getLayoutManager();
                                if (layoutManager != null) {
                                    layoutManager.scrollToPositionWithOffset(mDatas.size() - 1, 0);
                                } //240603추가
                            }
                        }
                    });
        }
    }

    @Override
    public void onClick(View v) {

    }

    // 아이템이 추가/제거될 때마다 호출되는 메서드
    private void updateItemDecoration() {
        if (spaceDecoration == null) {
            spaceDecoration = new RecyclerDecoration(10); // 최초에 RecyclerDecoration 객체 생성
            mDiaryRecyclerView.addItemDecoration(spaceDecoration);
        } else {
            mDiaryRecyclerView.removeItemDecoration(spaceDecoration); // 기존의 간격 설정 제거
            mDiaryRecyclerView.addItemDecoration(spaceDecoration); // 새로운 간격 설정 추가
        }
    }

}
