package com.tuya.appsdk.sample.adapter;// SiteAdapter.java

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.smart.lock.bean.SiteDetail;

import java.util.List;

public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.SiteViewHolder> {

    private List<SiteDetail> siteNames;

    private SiteClickListener clickListener;

    public SiteAdapter(List<SiteDetail> siteNames, SiteClickListener clickListener) {
        this.siteNames = siteNames;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new SiteViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SiteViewHolder holder, int position) {
        holder.bind(siteNames.get(position));
    }

    @Override
    public int getItemCount() {
        return siteNames.size();
    }

    public static class SiteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView textView;
        private SiteClickListener clickListener;
        private SiteDetail siteDetail;

        public SiteViewHolder(@NonNull View itemView, SiteClickListener clickListener) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
            this.clickListener = clickListener;
            itemView.setOnClickListener(this);
        }

        public void bind(SiteDetail siteDetail) {
            this.siteDetail = siteDetail;
            textView.setText(siteDetail.name);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onSiteClick(siteDetail);
            }
        }
    }

    // 接口用于回调点击事件
    public interface SiteClickListener {
        void onSiteClick(SiteDetail siteDetail);
    }
}
