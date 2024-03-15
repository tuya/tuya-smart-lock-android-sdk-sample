package com.tuya.appsdk.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.smart.lock.bean.OpenRecordDetail;
import com.tuya.appsdk.sample.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OpenRecordsAdapter extends RecyclerView.Adapter<OpenRecordsAdapter.KeyViewHolder> {

    private List<OpenRecordDetail> keyList;
    private Context context;

    public OpenRecordsAdapter(Context context, List<OpenRecordDetail> keyList) {
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
        OpenRecordDetail key = keyList.get(position);
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

        void bind(OpenRecordDetail key) {
            text1.setText(context.getString(R.string.operation_user) + key.openDoorUser);
            text2.setText(context.getString(R.string.operation_type) + key.openDoorType);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            text3.setText(context.getString(R.string.operation_time) + dateFormat.format(new Date(key.openDoorTime)));
        }
    }

    public interface EkeyClickListener {
        void onItemClick(OpenRecordDetail OpenRecordDetail);
    }
}
