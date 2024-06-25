package com.example.naviproj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.naviproj.R;
import com.example.naviproj.model.DiaryPage;
import com.example.naviproj.model.PageCurlFrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DiaryPagerAdapter extends RecyclerView.Adapter<DiaryPagerAdapter.DiaryViewHolder> {
    private Context context;
    private List<DiaryPage> diaryPages;
    private ViewPager2 viewPager;

    // 생성자: Context, ViewPager2를 매개변수로 받아 어댑터 초기화
    public DiaryPagerAdapter(Context context, ViewPager2 viewPager) {
        this.context = context;
        this.diaryPages = new ArrayList<>();
        this.viewPager = viewPager;
    }

    // ViewHolder 생성: PageCurlFrameLayout과 NestedScrollView를 사용하여 뷰 생성
    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PageCurlFrameLayout pageCurlFrameLayout = new PageCurlFrameLayout(parent.getContext());
        pageCurlFrameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        NestedScrollView nestedScrollView = new NestedScrollView(parent.getContext());
        nestedScrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary_page_content, nestedScrollView, false);
        nestedScrollView.addView(itemView);
        pageCurlFrameLayout.addView(nestedScrollView);

        return new DiaryViewHolder(pageCurlFrameLayout);
    }

    // ViewHolder에 데이터 바인딩: position에 해당하는 DiaryPage 데이터를 ViewHolder에 바인딩
    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryPage diaryPage = diaryPages.get(position);
        holder.bind(diaryPage);
    }

    // 아이템 개수 반환: 일기장 페이지 개수 반환
    @Override
    public int getItemCount() {
        return diaryPages.size();
    }

    // 특정 위치의 DiaryPage 객체 반환: position에 해당하는 DiaryPage 객체 반환
    public DiaryPage getDiaryPage(int position) {
        return diaryPages.get(position);
    }

    class DiaryViewHolder extends RecyclerView.ViewHolder {
        private PageCurlFrameLayout pageCurlFrameLayout;

        // 생성자: PageCurlFrameLayout을 매개변수로 받아 ViewHolder 초기화
        public DiaryViewHolder(@NonNull PageCurlFrameLayout itemView) {
            super(itemView);
            pageCurlFrameLayout = itemView;
        }

        // 데이터 바인딩: DiaryPage 데이터를 뷰에 바인딩
        public void bind(DiaryPage diaryPage) {
            TextView itemDiaryTitle = itemView.findViewById(R.id.item_diary_title);
            ImageView itemDiaryImage = itemView.findViewById(R.id.item_diary_image);
            TextView itemDiaryTime = itemView.findViewById(R.id.diary_time);
            ImageView itemDiaryEmoji = itemView.findViewById(R.id.item_diary_emoji);

            itemDiaryTitle.setText(diaryPage.getTitle());

            String contents = diaryPage.getContents();
            // 일기 내용을 50자까지 각각의 TextView에 설정
            for (int i = 0; i < 50; i++) {
                char c;
                if (contents != null && i < contents.length()) {
                    c = contents.charAt(i);
                } else {
                    c = ' '; // contents가 null이거나 i가 contents의 길이보다 크거나 같은 경우 공백 처리
                }

                String textViewID = "text" + (i + 1); // 동적으로 TextView ID를 생성
                int resID = itemView.getContext().getResources().getIdentifier(textViewID, "id", itemView.getContext().getPackageName()); // 동적으로 리소스 ID를 얻음

                TextView textView = itemView.findViewById(resID); // 해당 ID의 TextView를 찾기
                if (textView != null) {
                    textView.setText(String.valueOf(c)); // TextView에 문자를 설정
                }
            }

            String convert = ""; // 가져온 날짜를 연/월/일만 사용
            convert = diaryPage.getTimestamp();
            itemDiaryTime.setText(convert.substring(0, 4) + "년 " + convert.substring(5, 7) + "월 " + convert.substring(8, 10) + "일");

            Glide.with(itemView)
                    .load(diaryPage.getImage())
                    .into(itemDiaryImage);

            // 이모지 표시
            String emoji = diaryPage.getEmoji();
            if (emoji != null && !emoji.isEmpty()) {
                int emojiResource = getEmojiResource(emoji);
                if (emojiResource != 0) {
                    itemDiaryEmoji.setImageResource(emojiResource);
                    itemDiaryEmoji.setVisibility(View.VISIBLE);
                } else {
                    itemDiaryEmoji.setVisibility(View.GONE);
                }
            } else {
                itemDiaryEmoji.setVisibility(View.GONE);
            }
        }
    }

    // DiaryPage 객체 추가: 일기장 페이지 추가 및 시간 순으로 정렬
    public void addDiaryPage(DiaryPage diaryPage) {
        diaryPages.add(diaryPage);
        sortDiaryPagesByTime();
        notifyDataSetChanged();
    }

    // DiaryPage 객체 전체 삭제: 일기장 페이지 전체 삭제
    public void clearDiaryPages() {
        diaryPages.clear();
        notifyDataSetChanged();
    }

    // 시간 순으로 DiaryPage 객체 정렬: 최신 일기장 페이지가 맨 앞에 오도록 정렬
    private void sortDiaryPagesByTime() {
        Collections.sort(diaryPages, new Comparator<DiaryPage>() {
            @Override
            public int compare(DiaryPage o1, DiaryPage o2) {
                if (o1.getTime() == null && o2.getTime() == null) {
                    return 0;
                } else if (o1.getTime() == null) {
                    return 1;
                } else if (o2.getTime() == null) {
                    return -1;
                } else {
                    return o1.getTime().compareTo(o2.getTime());
                }
            }
        });
    }

    private int getEmojiResource(String emojiString) {
        switch (emojiString) {
            case "emoji1":
                return R.drawable.ic_pulsar;
            case "emoji2":
                return R.drawable.ic_smile;
            case "emoji3":
                return R.drawable.ic_anger;
            case "emoji4":
                return R.drawable.ic_boring;
            case "emoji5":
                return R.drawable.ic_sad;
            case "emoji6":
                return R.drawable.ic_joy;
            case "emoji7":
                return R.drawable.ic_surprise;
            case "emoji8":
                return R.drawable.ic_sick;
            case "emoji9":
                return R.drawable.ic_funny;
            case "emoji10":
                return R.drawable.ic_mid;
            case "emoji11":
                return R.drawable.ic_happy;
            case "emoji12":
                return R.drawable.ic_burnout;
            case "emoji13":
                return R.drawable.ic_tired;
            case "emoji14":
                return R.drawable.ic_love;
            case "emoji15":
                return R.drawable.ic_awkward;
            case "emoji16":
                return R.drawable.ic_none;
            default:
                return 0;
        }
    }
}