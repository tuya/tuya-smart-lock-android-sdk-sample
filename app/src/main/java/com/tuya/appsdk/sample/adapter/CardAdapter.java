package com.tuya.appsdk.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.smart.lock.bean.CardBean;
import com.thingclips.smart.lock.bean.LockLivecycle;
import com.thingclips.smart.lock.bean.PeriodType;
import com.tuya.appsdk.sample.R;

import java.util.List;
import java.util.Objects;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<CardBean> cardList;
    private Context context;
    private ItemClickListener clickListener;

    public CardAdapter(Context context, List<CardBean> cardList, ItemClickListener clickListener) {
        this.context = context;
        this.cardList = cardList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardBean key = cardList.get(position);
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
        return cardList.size();
    }

    class CardViewHolder extends RecyclerView.ViewHolder {

        public TextView keyTextView;
        public TextView accountTextView;
        public TextView statusView;
        public TextView effectiveTimeView;
        public View rootView;

        CardViewHolder(View itemView) {
            super(itemView);
            this.rootView = itemView;
            keyTextView = itemView.findViewById(R.id.keyTextView);
            statusView = itemView.findViewById(R.id.status);
            effectiveTimeView = itemView.findViewById(R.id.effectiveTime);
        }

        void bind(CardBean key) {
            keyTextView.setText(key.cardName);

            if (Objects.equals(key.periodType, PeriodType.EFFECT)) {
                statusView.setText(R.string.effect);
            } else if (Objects.equals(key.periodType, PeriodType.WAIT_EFFECT)) {
                statusView.setText(R.string.wait_effect);
            } else if (Objects.equals(key.periodType, PeriodType.EXPIRED)) {
                statusView.setText(R.string.expired);
            }

            switch (key.livecycleType) {
                case LockLivecycle.PERMANENT:
                    effectiveTimeView.setText(R.string.permanent);
                    break;
                case LockLivecycle.PERIODICITY:
                    effectiveTimeView.setText(R.string.periodicity);
                    break;
                case LockLivecycle.ONCE:
                    effectiveTimeView.setText(R.string.once);
                    break;
            }
        }
    }

    public interface ItemClickListener {
        void onItemClick(CardBean CardBean);
    }
}
