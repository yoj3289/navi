//체중 전체보기(상세검색) 리사이클러뷰에서 사용되는 어댑터
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

public class ShowWeightAdapter extends RecyclerView.Adapter<ShowWeightAdapter.ViewHolder>{
    private List<ShowWeightItem> ShowWeightList;
    private Context context;

    public ShowWeightAdapter(List<ShowWeightItem> ShowWeightList) {
        this.ShowWeightList = ShowWeightList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show_weight, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShowWeightItem showWeightItem = ShowWeightList.get(position);

        holder.Date.setText(showWeightItem.getDate());
        holder.Gram.setText(String.valueOf(showWeightItem.getGram()));
        String weightDifference=showWeightItem.getUpDown();
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
        holder.UpDown.setText(showWeightItem.getUpDown());

    }


    @Override
    public int getItemCount() {
        return ShowWeightList.size();
    }

    public void updateData(List<ShowWeightItem> newShowWeightList) {
        ShowWeightList.clear();
        ShowWeightList.addAll(newShowWeightList);
        ShowWeightAdapter.this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView Date,Gram,UpDown;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Date = itemView.findViewById(R.id.Date);
            Gram = itemView.findViewById(R.id.Gram);
            UpDown = itemView.findViewById(R.id.UpDown);
        }
    }

    public void setData(List<ShowWeightItem> newData) {
        this.ShowWeightList = newData;
        notifyDataSetChanged();
    }
}