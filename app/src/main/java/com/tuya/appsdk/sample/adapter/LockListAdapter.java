package com.tuya.appsdk.sample.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.sdk.os.ThingOSDevice;
import com.thingclips.smart.lock.bean.DeviceBeanWrapper;
import com.tuya.appsdk.sample.R;

import java.util.List;

public class LockListAdapter extends RecyclerView.Adapter<LockListAdapter.LockViewHolder> {

    private List<DeviceBeanWrapper> lockList;
    private LockClickListener lockClickListener;

    public LockListAdapter(List<DeviceBeanWrapper> lockList, LockClickListener lockClickListener) {
        this.lockList = lockList;
        this.lockClickListener = lockClickListener;
    }

    @NonNull
    @Override
    public LockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lock_item, parent, false);
        return new LockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LockViewHolder holder, int position) {
        DeviceBeanWrapper lock = lockList.get(position);
        holder.lockName.setText(lock.deviceRespBean.getName());
        Boolean isOnline = ThingOSDevice.getDeviceBean(lock.deviceRespBean.getDevId()).getIsOnline();

        holder.onlineStatus.setText(isOnline ? "Online" : "Offline");
        holder.connectButton.setEnabled(!isOnline);
        holder.unlockButton.setEnabled(isOnline);
        holder.removeButton.setEnabled(isOnline);

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockClickListener.onItemClick(lock);
            }
        });

        // 设置开锁按钮点击事件
        holder.unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 执行开锁操作
                lockClickListener.onUnlockClick(lock);
            }
        });

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockClickListener.onRemoveClick(lock);
            }
        });

        holder.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockClickListener.onConnectClick(lock);
            }
        });

    }

    @Override
    public int getItemCount() {
        return lockList.size();
    }

    public static class LockViewHolder extends RecyclerView.ViewHolder {
        public TextView lockName;
        public TextView effectiveTime;
        public TextView onlineStatus;
        public Button unlockButton;
        public Button removeButton;
        public Button connectButton;
        public View root;

        public LockViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            lockName = itemView.findViewById(R.id.lockName);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);
            unlockButton = itemView.findViewById(R.id.unlockButton);
            removeButton = itemView.findViewById(R.id.remove);
            connectButton = itemView.findViewById(R.id.connect);
        }
    }

    public interface LockClickListener {
        void onItemClick(DeviceBeanWrapper lock);

        void onUnlockClick(DeviceBeanWrapper lock);

        void onRemoveClick(DeviceBeanWrapper lock);

        void onConnectClick(DeviceBeanWrapper lock);
    }
}
