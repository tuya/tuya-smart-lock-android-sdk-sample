package com.tuya.appsdk.sample.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.sdk.os.ThingOSDevice;
import com.thingclips.smart.interior.device.bean.DeviceRespBean;
import com.thingclips.smart.lock.bean.DeviceBeanWrapper;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.tuya.appsdk.sample.R;

import java.util.List;
import java.util.Objects;

public class LockListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DeviceBeanWrapper> lockList;
    private LockClickListener lockClickListener;
    private String category;

    private static final int TYPE_LOCK_LIST = 1;
    private static final int TYPE_GATEWAY_LIST = 2;

    public LockListAdapter(List<DeviceBeanWrapper> lockList, String category, LockClickListener lockClickListener) {
        this.lockList = lockList;
        this.category = category;
        this.lockClickListener = lockClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOCK_LIST) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lock_item, parent, false);
            return new LockViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gateway_item, parent, false);
            return new GatewayViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        if (vh instanceof LockViewHolder) {
            LockViewHolder holder = (LockViewHolder) vh;
            DeviceBeanWrapper device = lockList.get(position);
            DeviceBean lockDevice = ThingOSDevice.getDeviceBean(device.deviceRespBean.getDevId());
            holder.lockName.setText(device.deviceRespBean.getName() + getProtocal(device.deviceRespBean));
            Boolean isOnline = lockDevice.getIsOnline();

            holder.onlineStatus.setText(isOnline ? "Online" : "Offline");
            holder.connectButton.setEnabled(!lockDevice.getIsLocalOnline());
            holder.connectButton.setVisibility(lockDevice.isSingleBle() ? View.VISIBLE : View.GONE);
            holder.unlockButton.setEnabled(isOnline);

            holder.root.setOnClickListener(v -> lockClickListener.onLockClick(device));

            // 设置开锁按钮点击事件
            holder.unlockButton.setOnClickListener(v -> {
                // 执行开锁操作
                lockClickListener.onUnlockClick(device);
            });

            holder.removeButton.setOnClickListener(v -> lockClickListener.onRemoveClick(device));

            holder.connectButton.setOnClickListener(v -> lockClickListener.onConnectClick(device));
        } else if (vh instanceof GatewayViewHolder) {
            GatewayViewHolder holder = (GatewayViewHolder) vh;
            DeviceBeanWrapper device = lockList.get(position);
            holder.root.setOnClickListener(v -> lockClickListener.onGatewayClick(device));
            holder.lockName.setText(device.deviceRespBean.getName() + getProtocal(device.deviceRespBean));
            Boolean isOnline = ThingOSDevice.getDeviceBean(device.deviceRespBean.getDevId()).getIsOnline();
            holder.onlineStatus.setText(isOnline ? "Online" : "Offline");

            holder.update_name.setOnClickListener(v -> lockClickListener.onUpdateNameClick(device));
            //only zigbee gateway supported
            holder.search_sub_device.setEnabled(getProtocal(device.deviceRespBean).equals("[Zigbee]"));
            holder.search_sub_device.setOnClickListener(v -> lockClickListener.onSearchSubDeviceClick(device));

            holder.remove.setOnClickListener(v -> lockClickListener.onRemoveClick(device));
        }

    }

    private String getProtocal(DeviceRespBean deviceRespBean) {
        DeviceBean deviceBean = ThingOSDevice.getDeviceBean(deviceRespBean.getDevId());
        if (deviceBean == null) return "[Null]";

        if (deviceBean.isZigBeeSubDev() || deviceBean.isZigBeeWifi()) {
            return "[Zigbee]";
        }

        if (deviceBean.isSingleBle()) return "[BLE]";
        if (deviceBean.isSigMeshWifi()) return "[BleWifi]";

        return "[UnSupported]";
    }

    @Override
    public int getItemViewType(int position) {
        return Objects.equals(category, "lock") ? TYPE_LOCK_LIST : TYPE_GATEWAY_LIST;
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

    public static class GatewayViewHolder extends RecyclerView.ViewHolder {
        public TextView lockName;
        public TextView onlineStatus;
        public Button update_name;
        public Button search_sub_device;
        public Button remove;
        public View root;

        public GatewayViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            lockName = itemView.findViewById(R.id.lockName);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);
            update_name = itemView.findViewById(R.id.update_name);
            search_sub_device = itemView.findViewById(R.id.search_sub_device);
            remove = itemView.findViewById(R.id.remove);
        }
    }

    public interface LockClickListener {
        void onLockClick(DeviceBeanWrapper lock);

        void onGatewayClick(DeviceBeanWrapper gateway);

        void onUnlockClick(DeviceBeanWrapper lock);

        void onRemoveClick(DeviceBeanWrapper lock);

        void onConnectClick(DeviceBeanWrapper lock);

        void onUpdateNameClick(DeviceBeanWrapper lock);

        void onSearchSubDeviceClick(DeviceBeanWrapper lock);

    }
}
