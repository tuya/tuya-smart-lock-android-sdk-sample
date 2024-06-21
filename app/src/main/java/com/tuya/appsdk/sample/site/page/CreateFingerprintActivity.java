package com.tuya.appsdk.sample.site.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.api.IThingFingerprintInputCallback;
import com.thingclips.smart.lock.bean.CreateFingerprintResp;
import com.thingclips.smart.lock.bean.LockLivecycle;
import com.tuya.appsdk.sample.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class CreateFingerprintActivity extends AppCompatActivity {

    private long siteId;
    private String deviceId;
    private View effectiveDateLayout;
    private EditText effectiveEditText;
    private View invalidDateLayout;
    private EditText invalidEditText;
    private String liveCycle = LockLivecycle.PERIODICITY;
    private EditText userNameEditText;
    private int totalInputCount;
    private TextView stepView;
    private View cancelCreateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_fingerprint_activity);

        setTitle(R.string.create_fingerprint);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("site_id")) {
            siteId = intent.getLongExtra("site_id", 0L);
        }
        if (intent != null && intent.hasExtra("device_id")) {
            deviceId = intent.getStringExtra("device_id");
        }

        userNameEditText = findViewById(R.id.et_user_name);

        effectiveDateLayout = findViewById(R.id.startDateLayout);
        effectiveEditText = effectiveDateLayout.findViewById(R.id.etEffectiveTime);

        invalidDateLayout = findViewById(R.id.endDateLayout);
        invalidEditText = invalidDateLayout.findViewById(R.id.etInvalidTime);
        cancelCreateView = findViewById(R.id.buttonCancel);

        stepView = findViewById(R.id.tvStep);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        View saveButton = findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(v -> createCard());

        cancelCreateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCreate();
            }
        });
    }

    private void cancelCreate() {
        ThingOSLock.getFingerprintManager().cancelFingerprintCreate(siteId, deviceId, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(CreateFingerprintActivity.this, R.string.cancel_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(CreateFingerprintActivity.this, R.string.cancel_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createCard() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String cardName = userNameEditText.getText().toString().trim();
        if (Objects.equals(liveCycle, LockLivecycle.PERMANENT)) {
            ThingOSLock.getFingerprintManager().createPermanentFingerprint(siteId, deviceId, cardName, new IThingFingerprintInputCallback() {
                        @Override
                        public void totalInputCount(int count) {
                            totalInputCount = count;
                            stepView.setText("0/" + count);

                            cancelCreateView.setEnabled(true);
                        }

                        @Override
                        public void currentInputCount(int count) {
                            stepView.setText(count + "/" + totalInputCount);
                        }
                    },
                    new IThingResultCallback<CreateFingerprintResp>() {
                        @Override
                        public void onSuccess(CreateFingerprintResp result) {
                            Toast.makeText(CreateFingerprintActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                            finish();

                            cancelCreateView.setEnabled(true);
                        }

                        @Override
                        public void onError(String errorCode, String errorMessage) {
                            Toast.makeText(CreateFingerprintActivity.this, errorMessage, Toast.LENGTH_SHORT).show();

                            cancelCreateView.setEnabled(true);
                        }
                    });
        } else if (Objects.equals(liveCycle, LockLivecycle.PERIODICITY)) {
            long effectiveTime;
            long invalidTime;
            try {
                effectiveTime = dateFormat.parse(effectiveEditText.getText().toString()).getTime();
                invalidTime = dateFormat.parse(invalidEditText.getText().toString()).getTime();
            } catch (ParseException e) {
                Toast.makeText(this, R.string.effective_time_or_invalid_time_error, Toast.LENGTH_SHORT).show();
                return;
            }
            ThingOSLock.getFingerprintManager().createLimitFingerprint(siteId, deviceId, cardName, effectiveTime, invalidTime, new IThingFingerprintInputCallback() {
                        @Override
                        public void totalInputCount(int count) {
                            totalInputCount = count;
                            stepView.setText("0/" + count);
                        }

                        @Override
                        public void currentInputCount(int count) {
                            stepView.setText(count + "/" + totalInputCount);
                        }
                    },
                    new IThingResultCallback<CreateFingerprintResp>() {
                        @Override
                        public void onSuccess(CreateFingerprintResp result) {
                            Toast.makeText(CreateFingerprintActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String errorCode, String errorMessage) {
                            Toast.makeText(CreateFingerprintActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.nav_temporary:
                        liveCycle = LockLivecycle.PERIODICITY;
                        effectiveDateLayout.setVisibility(View.VISIBLE);
                        invalidDateLayout.setVisibility(View.VISIBLE);
                        return true;
                    case R.id.nav_permanent:
                        liveCycle = LockLivecycle.PERMANENT;
                        effectiveDateLayout.setVisibility(View.GONE);
                        invalidDateLayout.setVisibility(View.GONE);
                        return true;
                }
                return false;
            };

}
