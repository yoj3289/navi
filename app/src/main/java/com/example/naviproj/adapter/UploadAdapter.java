package com.example.naviproj.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.naviproj.R;
import com.example.naviproj.model.Upload;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.UploadViewHolder> {


    private List<Upload> datas;

    private String collectionid2;
    private Uri uri;

    public UploadAdapter(List<Upload> datas) {
        this.datas = datas;
    }

    @NonNull
    @Override
    public UploadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UploadViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upload, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull UploadViewHolder holder, int position) {
        Upload data = datas.get(position);
        holder.name.setText(data.getName());
        holder.contents.setText(data.getContents());
        holder.documentid.setText(data.getDocumentId());
        holder.collectionid.setText(data.getCollectionId());
        holder.upload_time.setText(data.getTimestamp());
        holder.item_upload_url.setText(data.getUrl());
        Glide.with(holder.itemView)
                .load(datas.get(position).getImage())
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return (datas != null ? datas.size() : 0);
    }

    class UploadViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView contents;
        private TextView documentid;
        private TextView collectionid;
        private TextView upload_time;
        private TextView item_upload_url;
        private ImageView image;
        private ImageView item_upload_star;
        private ImageView item_upload_delete;

        public UploadViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.item_upload_name);
            contents = itemView.findViewById(R.id.item_upload_contents);
            image = itemView.findViewById(R.id.item_upload_image);
            documentid = itemView.findViewById(R.id.item_upload_documentid);
            collectionid = itemView.findViewById(R.id.item_upload_collectionid);
            upload_time = itemView.findViewById(R.id.upload_time);
            item_upload_star = itemView.findViewById(R.id.item_upload_star);
            item_upload_delete = itemView.findViewById(R.id.item_upload_delete);
            item_upload_url = itemView.findViewById(R.id.item_upload_url);
            };
        }
    }
