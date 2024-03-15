package com.tuya.appsdk.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.smart.lock.bean.OperateRecordDetail;
import com.thingclips.smart.lock.bean.LockLivecycle;
import com.thingclips.smart.lock.bean.PeriodType;
import com.tuya.appsdk.sample.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class OperationRecordsAdapter extends RecyclerView.Adapter<OperationRecordsAdapter.KeyViewHolder> {

    private List<OperateRecordDetail> keyList;
    private Context context;

    public OperationRecordsAdapter(Context context, List<OperateRecordDetail> keyList) {
        this.context = context;
        this.keyList = keyList;
    }

    @NonNull
    @Override
    public KeyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_records, parent, false);
        return new KeyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KeyViewHolder holder, int position) {
        OperateRecordDetail key = keyList.get(position);
        holder.bind(key);
    }

    @Override
    public int getItemCount() {
        return keyList.size();
    }

    class KeyViewHolder extends RecyclerView.ViewHolder {

        public TextView text1;
        public TextView text2;
        public TextView text3;
        public View rootView;

        KeyViewHolder(View itemView) {
            super(itemView);
            this.rootView = itemView;
            text1 = itemView.findViewById(R.id.text1);
            text2 = itemView.findViewById(R.id.text2);
            text3 = itemView.findViewById(R.id.text3);
        }

        void bind(OperateRecordDetail key) {
            text1.setText(context.getString(R.string.operation_user) + key.operateUser);
            text2.setText(context.getString(R.string.operation_type) + key.operateType);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            text3.setText(context.getString(R.string.operation_time) + dateFormat.format(new Date(key.operateTime)));
        }
    }

    public interface EkeyClickListener {
        void onItemClick(OperateRecordDetail OperateRecordDetail);
    }
}
