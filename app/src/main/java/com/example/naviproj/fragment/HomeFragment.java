package com.example.naviproj.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.naviproj.R;
import com.example.naviproj.ViewModel.HomeFragmentViewModel;
import com.example.naviproj.ViewModel.UploadActivity;
import com.example.naviproj.adapter.UploadAdapter;
import com.example.naviproj.model.Upload;
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
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private View view;

    private TextView tv_home;

    private FloatingActionButton floatingActionButton;

    private HomeFragmentViewModel mViewModel;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private RecyclerView mUploadRecyclerView;
    private UploadAdapter mAdapter;
    private List<Upload> mDatas;
    private RecyclerDecoration spaceDecoration; // RecyclerDecoration 객체를 멤버 변수로 선언

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_home, container, false);

        tv_home = (TextView) view.findViewById(R.id.tv_home);

        floatingActionButton = view.findViewById(R.id.board_write);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UploadActivity.class); //fragment라서 activity intent와는 다른 방식
                // intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
        mUploadRecyclerView = view.findViewById(R.id.home_RecyclerView);
        mDatas = new ArrayList<>();

        return view;
    }

    // 시작시 리사이클러뷰를 통해 작성한 글 나열
    @Override
    public void onStart() {
        super.onStart();

        // inent로 이미지 uri 받아오기 + 데이터 세팅하는거 수정
        mDatas = new ArrayList<>();
        mStore.collection(FirebaseID.upload)
                .orderBy(FirebaseID.timestamp, Query.Direction.DESCENDING)    // DESCENDING = 오름차순, ASCENDING = 내림차순 정렬
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            mDatas.clear();
                            for (DocumentSnapshot snap : value.getDocuments()) {
                                Map<String, Object> shot = snap.getData();
                                String documentID = String.valueOf(shot.get((FirebaseID.documentId)));
                                String name = String.valueOf(shot.get(FirebaseID.name));
                                String image = String.valueOf(shot.get(FirebaseID.image));
                                String contents = String.valueOf(shot.get(FirebaseID.contents));
                                String collectionID = String.valueOf(shot.get(FirebaseID.collectionId));
                                String time = String.valueOf(shot.get(FirebaseID.timestamp));
                                String url = String.valueOf(shot.get("url"));
                                Upload data = new Upload(documentID, contents, name, image, collectionID, time, url);
                                mDatas.add(data);
                            }
                            mAdapter = new UploadAdapter(mDatas);
                            mUploadRecyclerView.setAdapter(mAdapter);

                            updateItemDecoration(); // 아이템이 추가/제거될 때마다 간격 설정
                        }
                    }
                });
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeFragmentViewModel.class);
    }

    // 아이템이 추가/제거될 때마다 호출되는 메서드
    private void updateItemDecoration() {
        if (spaceDecoration == null) {
            spaceDecoration = new RecyclerDecoration(10); // 최초에 RecyclerDecoration 객체 생성
            mUploadRecyclerView.addItemDecoration(spaceDecoration);
        } else {
            mUploadRecyclerView.removeItemDecoration(spaceDecoration); // 기존의 간격 설정 제거
            mUploadRecyclerView.addItemDecoration(spaceDecoration); // 새로운 간격 설정 추가
        }
    }

}
