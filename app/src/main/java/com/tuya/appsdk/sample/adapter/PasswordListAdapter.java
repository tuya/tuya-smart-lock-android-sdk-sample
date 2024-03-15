package com.tuya.appsdk.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.smart.lock.bean.LockLivecycle;
import com.thingclips.smart.lock.bean.PasswordBean;
import com.thingclips.smart.lock.bean.PasswordType;
import com.thingclips.smart.lock.bean.PeriodType;
import com.tuya.appsdk.sample.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class PasswordListAdapter extends RecyclerView.Adapter<PasswordListAdapter.KeyViewHolder> {

    private List<PasswordBean> keyList;
    private Context context;
    private EkeyClickListener clickListener;

    public PasswordListAdapter(Context context, List<PasswordBean> keyList, EkeyClickListener clickListener) {
        this.context = context;
        this.keyList = keyList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public KeyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ekey, parent, false);
        return new KeyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KeyViewHolder holder, int position) {
        PasswordBean key = keyList.get(position);
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onItemClick(key);
            }
        });
        holder.bind(key);
    }

    @Override
    public int getItemCount() {
        return keyList.size();
    }

    class KeyViewHolder extends RecyclerView.ViewHolder {

        public TextView keyTextView;
        public TextView accountTextView;
        public TextView statusView;
        public TextView effectiveTimeView;
        public View rootView;

        KeyViewHolder(View itemView) {
            super(itemView);
            this.rootView = itemView;
            keyTextView = itemView.findViewById(R.id.keyTextView);
            accountTextView = itemView.findViewById(R.id.account);
            statusView = itemView.findViewById(R.id.status);
            effectiveTimeView = itemView.findViewById(R.id.effectiveTime);
        }

        void bind(PasswordBean key) {
            keyTextView.setText(key.passwordName);
            accountTextView.setText(context.getResources().getString(R.string.password) + ":" + key.password);
            if (Objects.equals(key.periodType, PeriodType.EFFECT)) {
                statusView.setText(R.string.effect);
            } else if (Objects.equals(key.periodType, PeriodType.WAIT_EFFECT)) {
                statusView.setText(R.string.wait_effect);
            } else if (Objects.equals(key.periodType, PeriodType.EXPIRED)) {
                statusView.setText(R.string.expired);
            }

            switch (key.passwordType) {
                case PasswordType.TIME_LIMIT_OFFLINE:
                    effectiveTimeView.setText(R.string.limit_offline);
                    break;
                case PasswordType.TIME_LIMIT_ONLINE:
                    effectiveTimeView.setText(R.string.limit_online);
                    break;
                case PasswordType.TIME_ONCE_OFFLINE:
                    effectiveTimeView.setText(R.string.once_offline);
                    break;
                case PasswordType.TIME_PERMANENT_ONLINE:
                    effectiveTimeView.setText(R.string.permanent_online);
                    break;
            }
        }
    }

    public interface EkeyClickListener {
        void onItemClick(PasswordBean PasswordBean);
    }
}
