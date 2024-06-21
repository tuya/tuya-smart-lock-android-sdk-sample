package com.tuya.appsdk.sample.site.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.api.ILockCard;
import com.thingclips.smart.lock.bean.CardBean;
import com.thingclips.smart.lock.bean.LockLivecycle;
import com.tuya.appsdk.sample.R;

import java.text.SimpleDateFormat;

public class CardDetailActivity extends AppCompatActivity {

    private long siteId;
    private String deviceId;
    private String cardId;
    private View effectiveDateLayout;
    private TextView effectiveEditText;
    private View invalidDateLayout;
    private TextView invalidEditText;
    private EditText userNameEditText;
    private CardBean cardBean;
    private TextView cardTypeTextVew;
    private ILockCard lockCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_detail_activity);

        setTitle(R.string.card_detail);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("site_id")) {
            siteId = intent.getLongExtra("site_id", 0L);
        }
        if (intent != null && intent.hasExtra("device_id")) {
            deviceId = intent.getStringExtra("device_id");
        }
        if (intent != null && intent.hasExtra("card_id")) {
            cardId = intent.getStringExtra("card_id");
        }

        lockCard = ThingOSLock.newCardInstance(siteId, deviceId, cardId);

        userNameEditText = findViewById(R.id.et_user_name);

        effectiveDateLayout = findViewById(R.id.startDateLayout);
        effectiveEditText = effectiveDateLayout.findViewById(R.id.etEffectiveTime);

        cardTypeTextVew = findViewById(R.id.et_type);

        invalidDateLayout = findViewById(R.id.endDateLayout);
        invalidEditText = invalidDateLayout.findViewById(R.id.etInvalidTime);

        View saveButton = findViewById(R.id.remove);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCard();
            }
        });
        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCard();
            }
        });

        lockCard.getCardDetail(new IThingResultCallback<CardBean>() {
            @Override
            public void onSuccess(CardBean result) {
                cardBean = result;
                render(result);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(CardDetailActivity.this, R.string.failed + ":" + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCard() {
        String cardName = userNameEditText.getText().toString().trim();

        ThingOSLock.newCardInstance(siteId, deviceId, cardId).updateName(cardName, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(CardDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(CardDetailActivity.this, getString(R.string.failed) + ":" + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void render(CardBean result) {
        String livecycleType = result.livecycleType;
        if (livecycleType.equals(LockLivecycle.PERMANENT)) {
            effectiveDateLayout.setVisibility(View.GONE);
            invalidDateLayout.setVisibility(View.GONE);
            cardTypeTextVew.setText(R.string.permanent);
        } else if (livecycleType.equals(LockLivecycle.PERIODICITY)) {
            effectiveDateLayout.setVisibility(View.VISIBLE);
            invalidDateLayout.setVisibility(View.VISIBLE);
            cardTypeTextVew.setText(R.string.periodicity);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        userNameEditText.setText(result.cardName);
        effectiveEditText.setText(dateFormat.format(result.effectiveTime));
        invalidEditText.setText(dateFormat.format(result.invalidTime));
    }

    private void removeCard() {
        ThingOSLock.newCardInstance(siteId, deviceId, cardId).removeCard(cardBean.lockId, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(CardDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(CardDetailActivity.this, getString(R.string.failed) + ":" + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
