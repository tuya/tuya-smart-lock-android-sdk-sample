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
;
import com.thingclips.smart.lock.api.ILockFingerprint;
import com.thingclips.smart.lock.bean.FingerprintBean;
import com.thingclips.smart.lock.bean.LockLivecycle;
import com.tuya.appsdk.sample.R;

import java.text.SimpleDateFormat;

public class FingerprintDetailActivity extends AppCompatActivity {

    private long siteId;
    private String deviceId;
    private String fingerprintId;
    private View effectiveDateLayout;
    private TextView effectiveEditText;
    private View invalidDateLayout;
    private TextView invalidEditText;
    private EditText userNameEditText;
    private FingerprintBean fingerprintBean;
    private TextView fingerprintType;
    private ILockFingerprint lockFingerprint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_detail_activity);

        setTitle(R.string.fingerprint_detail);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("site_id")) {
            siteId = intent.getLongExtra("site_id", 0L);
        }
        if (intent != null && intent.hasExtra("device_id")) {
            deviceId = intent.getStringExtra("device_id");
        }
        if (intent != null && intent.hasExtra("fingerprint_id")) {
            fingerprintId = intent.getStringExtra("fingerprint_id");
        }

        lockFingerprint = ThingOSLock.newFingerprintInstance(siteId, deviceId, fingerprintId);

        userNameEditText = findViewById(R.id.et_user_name);

        effectiveDateLayout = findViewById(R.id.startDateLayout);
        effectiveEditText = effectiveDateLayout.findViewById(R.id.etEffectiveTime);

        fingerprintType = findViewById(R.id.et_type);

        invalidDateLayout = findViewById(R.id.endDateLayout);
        invalidEditText = invalidDateLayout.findViewById(R.id.etInvalidTime);

        View saveButton = findViewById(R.id.remove);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove();
            }
        });
        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateName();
            }
        });

        lockFingerprint.getFingerprintDetail(new IThingResultCallback<FingerprintBean>() {
            @Override
            public void onSuccess(FingerprintBean result) {
                fingerprintBean = result;
                render(result);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(FingerprintDetailActivity.this, R.string.failed + ":" + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateName() {
        String name = userNameEditText.getText().toString().trim();

        ThingOSLock.newFingerprintInstance(siteId, deviceId, fingerprintId).updateName(name, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(FingerprintDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(FingerprintDetailActivity.this, getString(R.string.failed) + ":" + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void render(FingerprintBean result) {
        String livecycleType = result.liveCycleType;
        if (livecycleType.equals(LockLivecycle.PERMANENT)) {
            effectiveDateLayout.setVisibility(View.GONE);
            invalidDateLayout.setVisibility(View.GONE);
            fingerprintType.setText(R.string.permanent);
        } else if (livecycleType.equals(LockLivecycle.PERIODICITY)) {
            effectiveDateLayout.setVisibility(View.VISIBLE);
            invalidDateLayout.setVisibility(View.VISIBLE);
            fingerprintType.setText(R.string.periodicity);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        userNameEditText.setText(result.fingerprintName);
        effectiveEditText.setText(dateFormat.format(result.effectiveTime));
        invalidEditText.setText(dateFormat.format(result.invalidTime));
    }

    private void remove() {
        if (fingerprintBean == null) return;

        ThingOSLock.newFingerprintInstance(siteId, deviceId, fingerprintId).remove(fingerprintBean.lockId, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(FingerprintDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(FingerprintDetailActivity.this, getString(R.string.failed) + ":" + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
