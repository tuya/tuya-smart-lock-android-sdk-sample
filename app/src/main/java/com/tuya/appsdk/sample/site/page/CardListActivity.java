package com.tuya.appsdk.sample.site.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.bean.CardBean;
import com.thingclips.smart.lock.bean.CardListResp;
import com.tuya.appsdk.sample.R;
import com.tuya.appsdk.sample.adapter.CardAdapter;

import java.util.ArrayList;
import java.util.List;

public class CardListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private long siteId;
    private String deviceId;
    private List<CardBean> cardBeanList = new ArrayList<>();
    private int currentPageNo = 1;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_list_activity);
        setTitle(R.string.card_list);

        Intent intent = getIntent();
        siteId = intent.getLongExtra("site_id", 0);
        deviceId = intent.getStringExtra("device_id");

        recyclerView = findViewById(R.id.recyclerView);
        View clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThingOSLock.getCardManager().clearAllCard(siteId, deviceId, new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(CardListActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                        cardBeanList.clear();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(CardListActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CardAdapter(this, cardBeanList, new CardAdapter.ItemClickListener() {
            @Override
            public void onItemClick(CardBean CardBean) {
                Intent intent = new Intent(CardListActivity.this, CardDetailActivity.class);
                intent.putExtra("site_id", siteId);
                intent.putExtra("device_id", deviceId);
                intent.putExtra("card_id", CardBean.cardId);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        findViewById(R.id.loadMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMore();
            }
        });

        initCreateButton();
    }

    private void loadMore() {
        if (loading) {
            return;
        }
        loading = true;

        ThingOSLock.getCardManager().getCardList(siteId, deviceId, currentPageNo, new IThingResultCallback<CardListResp>() {
            @Override
            public void onSuccess(CardListResp result) {
                if (currentPageNo == 1) {
                    cardBeanList.clear();
                }
                cardBeanList.addAll(result.data);
                adapter.notifyDataSetChanged();
                loading = false;
                currentPageNo++;
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                loading = false;
                Toast.makeText(CardListActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initCreateButton() {
        Button button = findViewById(R.id.createButton);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(CardListActivity.this, CreateCardActivity.class);
            intent.putExtra("site_id", siteId);
            intent.putExtra("device_id", deviceId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentPageNo = 1;
        loadMore();
    }
}
