package com.example.naviproj.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.naviproj.R;
import com.example.naviproj.ViewModel.BoardFragmentViewModel;
import com.example.naviproj.ViewModel.UploadActivity;
import com.example.naviproj.adapter.boardAdapter;
import com.example.naviproj.model.board;
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

public class BoardFragment extends Fragment implements View.OnClickListener {

    private View view;
    private BoardFragmentViewModel mViewModel;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();

    private RecyclerView mBoardRecyclerView;
    private boardAdapter mAdapter;
    private List<board> mDatas;

    private FloatingActionButton floatingActionButton;

    private RecyclerDecoration spaceDecoration; // RecyclerDecoration 객체를 멤버 변수로 선언

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_board, container, false);

        mBoardRecyclerView = view.findViewById(R.id.board_RecyclerView);

        floatingActionButton = view.findViewById(R.id.board_write);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UploadActivity.class); //fragment라서 activity intent와는 다른 방식
                // intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        return view;
    }

    public void onActivityCreated (@Nullable Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(BoardFragmentViewModel.class);

    }


    // 시작시 리사이클러뷰를 통해 작성한 글 나열
    @Override
    public void onStart() {
        super.onStart();
        mDatas = new ArrayList<>();
        mStore.collection(FirebaseID.post)
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
                                String title = String.valueOf(shot.get(FirebaseID.title));
                                String contents = String.valueOf(shot.get(FirebaseID.contents));
                                String collectionID = String.valueOf(shot.get(FirebaseID.collectionId));
                                String timestamp = String.valueOf(shot.get(FirebaseID.timestamp));
                                board data = new board(documentID, name, title, contents, collectionID, timestamp);
                                mDatas.add(data);
                            }
                            mAdapter = new boardAdapter(mDatas);
                            mBoardRecyclerView.setAdapter(mAdapter);

                            updateItemDecoration(); // 아이템이 추가/제거될 때마다 간격 설정
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {

    }

    // 아이템이 추가/제거될 때마다 호출되는 메서드
    private void updateItemDecoration() {
        if (spaceDecoration == null) {
            spaceDecoration = new RecyclerDecoration(10); // 최초에 RecyclerDecoration 객체 생성
            mBoardRecyclerView.addItemDecoration(spaceDecoration);
        } else {
            mBoardRecyclerView.removeItemDecoration(spaceDecoration); // 기존의 간격 설정 제거
            mBoardRecyclerView.addItemDecoration(spaceDecoration); // 새로운 간격 설정 추가
        }
    }
}