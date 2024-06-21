package com.tuya.appsdk.sample.adapter;// DeviceAdapter.java

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.smart.lock.bean.SubDeviceBean;
import com.tuya.appsdk.sample.R;

import java.util.List;

public class BindedDeviceAdapter extends RecyclerView.Adapter<BindedDeviceAdapter.DeviceViewHolder> {

    private List<SubDeviceBean> deviceList;
    private BindDeviceClickListener listener;

    private boolean isBleWifi;

    //o for bind , 1 for unbind
    private int type = 0;

    public BindedDeviceAdapter(List<SubDeviceBean> deviceList, int type, BindDeviceClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
        this.type = type;
    }

    public void setBleWifi(boolean isBleWifi) {
        this.isBleWifi = isBleWifi;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_binded_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        SubDeviceBean deviceName = deviceList.get(position);
        holder.bind(deviceName);
        holder.unbindView.setText(type == 0 ? R.string.bind : R.string.unbind);
        holder.unbindView.setVisibility(isBleWifi ? View.VISIBLE : View.GONE);
        holder.unbindView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 0) {
                    listener.bindClick(deviceName);
                } else {
                    listener.unbindClick(deviceName);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public interface BindDeviceClickListener {
        void unbindClick(SubDeviceBean item);

        void bindClick(SubDeviceBean item);
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder {

        public TextView deviceNameTextView;
        public TextView unbindView;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameTextView = itemView.findViewById(R.id.name);
            unbindView = itemView.findViewById(R.id.unbind);
        }

        public void bind(SubDeviceBean item) {
            deviceNameTextView.setText(item.deviceName);
        }
    }
}
