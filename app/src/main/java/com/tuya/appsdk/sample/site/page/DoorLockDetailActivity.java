package com.tuya.appsdk.sample.site.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.thingclips.sdk.os.ThingOSDevice;
import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.api.ILockDevice;
import com.thingclips.smart.lock.api.ILockStatusListener;
import com.thingclips.smart.lock.bean.LockAbility;
import com.thingclips.smart.lock.bean.LockDetailBean;
import com.thingclips.smart.lock.bean.LockLivecycle;
import com.thingclips.smart.lock.bean.LockStatus;
import com.thingclips.smart.lock.bean.ReverseLockState;
import com.thingclips.smart.lock.bean.TimeScheduleInfo;
import com.tuya.appsdk.sample.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DoorLockDetailActivity extends AppCompatActivity {

    private static final String TAG = "DoorLockDetailActivity";
    private long siteId;
    private String deviceId;

    private LockDetailBean data;
    private Button lockButton;
    private ILockDevice lockDevice;
    private TextView reverseStatusView;
    private TextView openStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_detail_activity);

        setTitle(R.string.lock_detail);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("site_id")) {
            siteId = intent.getLongExtra("site_id", 0L);
        }
        if (intent != null && intent.hasExtra("device_id")) {
            deviceId = intent.getStringExtra("device_id");
        }

        lockDevice = ThingOSLock.newLockInstance(siteId, deviceId);
        lockDevice.registerLockStatusListener(new ILockStatusListener() {
            @Override
            public void onStateChanged(LockStatus status) {
                L.i(TAG, JSON.toJSONString(status));
                lockButton.setEnabled(status.manualCloseFlag);
                reverseStatusView.setText(getReverseStatus(status.reverseLockState));
                openStatus.setText(status.closedOpenedState);
            }
        });

        TextView deviceIdView = findViewById(R.id.tvDeviceIdValue);
        TextView copyIdView = findViewById(R.id.copy_id);
        TextView textDeviceNameValue = findViewById(R.id.textDeviceNameValue);
        TextView textOnlineStatusValue = findViewById(R.id.textOnlineStatusValue);
        TextView textBatteryLevelValue = findViewById(R.id.textBatteryLevelValue);
        TextView textUserTypeValue = findViewById(R.id.textUserTypeValue);
        TextView textEffectiveTimeValue = findViewById(R.id.textEffectiveTimeValue);
        reverseStatusView = findViewById(R.id.tvReverseStatus);
        openStatus = findViewById(R.id.locked_status);

        Button buttonElectronicKey = findViewById(R.id.buttonElectronicKey);
        Button buttonPassword = findViewById(R.id.buttonPassword);
        Button buttonLog = findViewById(R.id.buttonLog);
//        Button buttonSettings = findViewById(R.id.buttonSettings);
        Button buttonUnlock = findViewById(R.id.buttonUnlock);
        Button buttonFingerprint = findViewById(R.id.buttonFingerprint);
        Button buttonCard = findViewById(R.id.buttonCard);
        Button autoLockOnButton = findViewById(R.id.btnAutoCloseOn);
        Button autoLockOffButton = findViewById(R.id.btnAutoCloseOff);
        View tvAutoClose = findViewById(R.id.tvAutoClose);
        lockButton = findViewById(R.id.lock);
        findViewById(R.id.force_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThingOSLock.newSiteInstance(siteId).forceDeleteLock(deviceId, new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        finish();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(DoorLockDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        if (lockDevice.isSupportAutoLock()) {
            autoLockOnButton.setVisibility(View.VISIBLE);
            autoLockOffButton.setVisibility(View.VISIBLE);
            tvAutoClose.setVisibility(View.VISIBLE);
            lockButton.setVisibility(View.VISIBLE);
        }

        deviceIdView.setText(deviceId);
        copyIdView.setOnClickListener(view -> {
            android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clipData = android.content.ClipData.newPlainText("DeviceId", deviceId);
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(DoorLockDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
        });

        buttonElectronicKey.setOnClickListener(view -> {
            Intent ekeyIntent = new Intent(DoorLockDetailActivity.this, EKeyListActivity.class).putExtra("site_id", siteId).putExtra("device_id", deviceId);
            startActivity(ekeyIntent);
        });

        buttonPassword.setOnClickListener(view -> {
            Intent passwordIntent = new Intent(DoorLockDetailActivity.this, PasswordListActivity.class).putExtra("site_id", siteId).putExtra("device_id", deviceId);
            startActivity(passwordIntent);
        });

        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockDevice.lock(new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        data.manualCloseFlag = false;
                        Toast.makeText(DoorLockDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(DoorLockDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        autoLockOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockDevice.setAutoLockSwitch(true, new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(DoorLockDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(DoorLockDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        autoLockOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockDevice.setAutoLockSwitch(false, new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(DoorLockDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(DoorLockDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        buttonFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent passwordIntent = new Intent(DoorLockDetailActivity.this, FingerprintListActivity.class).putExtra("site_id", siteId).putExtra("device_id", deviceId);
                startActivity(passwordIntent);
            }
        });

        buttonCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent passwordIntent = new Intent(DoorLockDetailActivity.this, CardListActivity.class).putExtra("site_id", siteId).putExtra("device_id", deviceId);
                startActivity(passwordIntent);
            }
        });

        buttonLog.setOnClickListener(view -> {
            showActionSheet();
        });

        buttonUnlock.setOnClickListener(view -> {
            lockDevice.unlock(new IThingResultCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Toast.makeText(DoorLockDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    Toast.makeText(DoorLockDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

        });

        lockDevice.getLockDetail(new IThingResultCallback<LockDetailBean>() {
            @Override
            public void onSuccess(LockDetailBean result) {
                data = result;
                textDeviceNameValue.setText(result.deviceName);
                textOnlineStatusValue.setText(ThingOSDevice.getDeviceBean(result.deviceId).getIsOnline() ? "Online" : "Offline");
                textBatteryLevelValue.setText(result.electricQuantity + "%");
                textUserTypeValue.setText(result.deviceRole);

                if (result.livecycleType != null) {
                    TimeScheduleInfo timeScheduleInfo = result.timeScheduleInfo;
                    switch (result.livecycleType) {
                        case LockLivecycle.PERMANENT:
                            textEffectiveTimeValue.setText(getString(R.string.permanent));
                            break;
                        case LockLivecycle.PERIODICITY:
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String effectiveTime = formatter.format(new Date(timeScheduleInfo.effectiveTime));
                            String invalideTime = formatter.format(new Date(timeScheduleInfo.invalidTime));
                            textEffectiveTimeValue.setText(effectiveTime + " - " + invalideTime);
                            break;
                        case LockLivecycle.ONCE:
                            textEffectiveTimeValue.setText(getString(R.string.once));
                            break;
                    }
                }

                String[] supportAbilities = result.supportAbilities;
                for (String ability : supportAbilities) {
                    if (ability.equals(LockAbility.E_KEY)) {
                        buttonElectronicKey.setVisibility(View.VISIBLE);
                    } else if (ability.equals(LockAbility.OFFLINE_PASSCODE) || ability.equals(LockAbility.TEMPORARY_PASSCODE)) {
                        buttonPassword.setVisibility(View.VISIBLE);
                    } else if (ability.equals(LockAbility.LOG)) {
                        buttonLog.setVisibility(View.VISIBLE);
                    } else if (ability.equals(LockAbility.FINGERPRINT)) {
                        buttonFingerprint.setVisibility(View.VISIBLE);
                    } else if (ability.equals(LockAbility.CARD)) {
                        buttonCard.setVisibility(View.VISIBLE);
                    }
                }

                lockButton.setEnabled(result.manualCloseFlag);
                reverseStatusView.setText(getReverseStatus(result.reverseLockState));
                openStatus.setText(result.closedOpenedState);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(DoorLockDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getReverseStatus(int status) {
        if (status == ReverseLockState.STATE_LOCKED) {
            return getString(R.string.reverse_locked);
        } else if (status == 0) {
            return getString(R.string.reverse_not_locked);
        }
        return "Unknown";
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showActionSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.action_sheet_layout, null);
        ListView listView = view.findViewById(R.id.actionSheetList);
        String[] items = {getString(R.string.operation_records), getString(R.string.unlock_records), getString(R.string.alarm_records)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            switch (position) {
                case 0:
                    Intent intent1 = new Intent(DoorLockDetailActivity.this, OperationRecordsListActivity.class).putExtra("site_id", siteId).putExtra("device_id", deviceId);
                    startActivity(intent1);
                    break;
                case 1:
                    Intent intent2 = new Intent(DoorLockDetailActivity.this, OpenRecordsListActivity.class).putExtra("site_id", siteId).putExtra("device_id", deviceId);
                    startActivity(intent2);
                    break;
                case 2:
                    Intent intent3 = new Intent(DoorLockDetailActivity.this, AlarmRecordsListActivity.class).putExtra("site_id", siteId).putExtra("device_id", deviceId);
                    startActivity(intent3);
                    break;
            }
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lockDevice.unregisterLockStatusListener();
    }
}
