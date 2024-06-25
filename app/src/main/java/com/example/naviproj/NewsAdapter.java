package com.example.naviproj;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsItem> newsList;

    private static OnItemClickListener listener; // listener 변수 선언

    public NewsAdapter(List<NewsItem> newsList) {
        this.newsList = newsList;
    }

    // 클릭 리스너를 설정하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // 아이템 클릭 이벤트를 처리하는 인터페이스
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NewsItem newsItem = newsList.get(position);
        holder.newsTitle.setText(newsItem.getTitle());
        //holder.newsContent.setText(newsItem.getContent());

        Glide.with(holder.newsContent.getContext())
                .load(newsItem.getContent())
                .error(R.drawable.ic_launcher_foreground) //오류 시 표기할 기본 이미지
                .into(holder.newsContent); //240303추가

        Glide.with(holder.newsImage.getContext())
                .load(newsItem.getImg())
                .error(R.drawable.ic_launcher_foreground) //오류 시 표기할 기본 이미지
                .into(holder.newsImage); //240303추가
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView newsTitle;
        //TextView newsContent;

        ImageView newsContent;

        ImageView newsImage; //240303추가

        public ViewHolder(View itemView) {
            super(itemView);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsContent = itemView.findViewById(R.id.newsContent);
            newsImage=itemView.findViewById(R.id.newsImage); //240303추가

            // 아이템 뷰에 클릭 리스너를 설정
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 클릭된 아이템의 위치를 가져옴
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        // 클릭 이벤트를 처리하는 메서드를 호출
                        listener.onItemClick(position);
                    }
                }
            });
        }
        }
    }