//이상감지 리사이클러뷰
package com.example.naviproj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    private List<AlertItem> alertList;
    private Context context;

    public AlertAdapter(List<AlertItem> alertList) {
        this.alertList = alertList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert_system, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlertItem alertItem = alertList.get(position);

        holder.alertTitle.setText(alertItem.getTitle());

        holder.alertIcon.setImageDrawable(alertItem.getAlertIcon());
        holder.alertDetail.setText(alertItem.getDetail());

    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    public void updateData(List<AlertItem> newAlertList) {
        alertList.clear();
        alertList.addAll(newAlertList);
        AlertAdapter.this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView alertTitle,alertDetail;
        ImageView alertIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            alertTitle = itemView.findViewById(R.id.alertTitle);
            alertIcon=itemView.findViewById(R.id.alertIcon);
            alertDetail = itemView.findViewById(R.id.alertDetail);
        }
    }
    public void setData(List<AlertItem> newData) {
        this.alertList = newData;
        notifyDataSetChanged();
    }
}
