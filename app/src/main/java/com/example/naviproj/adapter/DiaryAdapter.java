package com.example.naviproj.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.naviproj.R;
import com.example.naviproj.ViewModel.DiaryDetailActivity;
import com.example.naviproj.model.Diary;

import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    private List<Diary> datas;
    private ViewPager2 viewPager2; // ViewPager2를 참조하기 위한 변수 추가
    private int selectedPosition = RecyclerView.NO_POSITION; // 클릭한 아이템의 위치 기억
    private ImageView iv_emoji;

    public DiaryAdapter(List<Diary> datas, ViewPager2 viewPager2) {
        this.datas = datas;
        this.viewPager2 = viewPager2; // ViewPager2를 받아옴
    }

    // 클릭한 아이템의 위치 설정 메서드
    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged(); // 변경 사항을 RecyclerView에 알림
    }

    // 클릭한 아이템의 위치 가져오는 메서드
    public int getSelectedPosition() {
        return selectedPosition;
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DiaryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        // 각각의 홀더의 위치(position)에 데이터(컨텐츠, 타이틀)를 넣음
        Diary data = datas.get(position);
        holder.diary_title.setText(data.getTitle()); // 제목 설정
        holder.diary_contents.setText(data.getContents()); // 내용 설정
        holder.diary_name.setText(data.getName()); // 이름 설정
        holder.diary_documentid.setText(data.getDocumentId());
        holder.diary_collectionid.setText(data.getCollectionId());
        holder.diary_timestamp.setText(data.getTimestamp());

        String emoji = data.getEmoji();
        if (emoji != null && !emoji.isEmpty()) {
            int emojiResource = getEmojiResource(emoji);
            if (emojiResource != 0) {
                holder.iv_emoji.setImageResource(emojiResource);
                holder.iv_emoji.setVisibility(View.VISIBLE);
            } else {
                holder.iv_emoji.setVisibility(View.GONE);
            }
        } else {
            holder.iv_emoji.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, DiaryDetailActivity.class);
                    intent.putExtra("diary_collectionid", datas.get(position).getCollectionId());
                    intent.putExtra("diary_documentid", datas.get(position).getDocumentId());
                    intent.putExtra("selectedPosition", position);
                    context.startActivity(intent);
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

    private OnItemClickListener mListener = null;

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class DiaryViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv_emoji;
        private TextView diary_title;
        private TextView diary_contents;
        private TextView diary_name;
        private TextView diary_documentid;
        private TextView diary_timestamp;
        private TextView diary_collectionid;

        //board view holder의 생성자
        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);

            // title, name, contents 섞임현상으로 임시로 수정
            diary_title = itemView.findViewById(R.id.item_diary_name);
            diary_name = itemView.findViewById(R.id.item_diary_contents);
            diary_contents = itemView.findViewById(R.id.item_diary_title);
            diary_documentid = itemView.findViewById(R.id.item_diary_documentid);
            diary_timestamp = itemView.findViewById(R.id.item_diary_timestamp);
            diary_collectionid = itemView.findViewById(R.id.item_diary_collectionid);
            iv_emoji = itemView.findViewById(R.id.iv_emoji);
        }
    }

}