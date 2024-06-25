//혈압 전체보기(상세검색) 리사이클러뷰에서 사용되는 어댑터
package com.example.naviproj;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShowPressAdapter extends RecyclerView.Adapter<ShowPressAdapter.ViewHolder>{
    private List<ShowPressItem> ShowPressList;
    private Context context;

    public ShowPressAdapter(List<ShowPressItem> ShowPressList) {
        this.ShowPressList = ShowPressList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show_press, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShowPressItem showPressItem = ShowPressList.get(position);

        holder.Date.setText(showPressItem.getDate());
        holder.Press.setText(String.valueOf(showPressItem.getPress()));
        String weightDifference=showPressItem.getUpDown();
        if (weightDifference.contains("▲")) {
            // 양수인 경우 빨간색으로 설정
            holder.UpDown.setTextColor(Color.RED);
        } else if (weightDifference.contains("▼")) {
            // 음수인 경우 파란색으로 설정
            holder.UpDown.setTextColor(Color.BLUE);
        } else {
            // 0인 경우 기본 색상 (검정)으로 설정
            holder.UpDown.setTextColor(Color.BLACK);
        }
        holder.UpDown.setText(showPressItem.getUpDown());

    }


    @Override
    public int getItemCount() {
        return ShowPressList.size();
    }

    public void updateData(List<ShowPressItem> newShowPressList) {
        ShowPressList.clear();
        ShowPressList.addAll(newShowPressList);
        ShowPressAdapter.this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView Date,Press,UpDown;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Date = itemView.findViewById(R.id.Date);
            Press = itemView.findViewById(R.id.Press);
            UpDown = itemView.findViewById(R.id.UpDown);
        }
    }

    public void setData(List<ShowPressItem> newData) {
        this.ShowPressList = newData;
        notifyDataSetChanged();
    }
}
