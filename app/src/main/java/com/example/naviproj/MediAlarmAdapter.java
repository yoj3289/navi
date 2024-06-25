package com.example.naviproj;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MediAlarmAdapter extends RecyclerView.Adapter<MediAlarmAdapter.ViewHolder>{
    private List<MediAlarmItem> mediAlarmList;
    private Context context;

    public MediAlarmAdapter(List<MediAlarmItem> mediAlarmList) {
        this.mediAlarmList = mediAlarmList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medi_alarm_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediAlarmItem mediAlarmItem = mediAlarmList.get(position);

        holder.alarmTitle.setText(mediAlarmItem.getTitle());

        holder.alarmPeriod.setText(mediAlarmItem.getPeriod());

        holder.alarmTime1.setImageDrawable(mediAlarmItem.getIcon1());

        holder.alarmTime2.setImageDrawable(mediAlarmItem.getIcon2());

        holder.alarmTime3.setImageDrawable(mediAlarmItem.getIcon3());

        holder.alarmTime4.setImageDrawable(mediAlarmItem.getIcon4());

        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 클릭 이벤트 처리 로직
                Intent intent = new Intent(view.getContext(), deleteMediAlarm.class);
                intent.putExtra("mediName", mediAlarmItem.getTitle());
                view.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mediAlarmList.size();
    }

    public void updateData(List<MediAlarmItem> newMediAlarmList) {
        mediAlarmList.clear();
        mediAlarmList.addAll(newMediAlarmList);
        MediAlarmAdapter.this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //TextView alertTitle,/*alertCriteriaDate*/alertIcon,alertDetail;
        public RelativeLayout rootLayout;
        TextView alarmTitle,alarmPeriod;
        ImageView alarmTime1,alarmTime2,alarmTime3,alarmTime4;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.rootLayout);
            alarmTitle = itemView.findViewById(R.id.alarmTitle);
            alarmPeriod=itemView.findViewById(R.id.alarmPeriod);
            alarmTime1=itemView.findViewById(R.id.morning);
            alarmTime2=itemView.findViewById(R.id.noon);
            alarmTime3=itemView.findViewById(R.id.evening);
            alarmTime4=itemView.findViewById(R.id.night);

        }
    }
    public void setData(List<MediAlarmItem> newData) {
        this.mediAlarmList = newData;
        notifyDataSetChanged();
    }
}

