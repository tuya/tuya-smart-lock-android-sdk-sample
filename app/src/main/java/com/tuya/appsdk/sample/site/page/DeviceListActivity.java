package com.tuya.appsdk.sample.site.page;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.thingclips.sdk.os.ThingOSBLE;
import com.thingclips.sdk.os.ThingOSDevice;
import com.thingclips.smart.android.ble.builder.BleConnectBuilder;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.api.ILockSite;
import com.thingclips.smart.lock.bean.DeviceBeanWrapper;
import com.thingclips.smart.sdk.api.IDevListener;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.tuya.appsdk.sample.R;
import com.tuya.appsdk.sample.adapter.LockListAdapter;
import com.tuya.appsdk.sample.site.page.config.BleGatewaySetupActivity;
import com.tuya.appsdk.sample.site.page.config.BleLockDeviceActivity;
import com.tuya.appsdk.sample.site.page.config.GwConfigSubDeviceActivity;
import com.tuya.appsdk.sample.site.page.config.ZigbeeWiredGatewayConfigActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeviceListActivity extends AppCompatActivity {

    private List<DeviceBeanWrapper> lockList = new ArrayList<>();
    private LockListAdapter adapter;
    private long siteId;
    private long startId;
    private boolean loading = false;

    private IDevListener devListener = new IDevListener() {
        @Override
        public void onDpUpdate(String devId, String dpStr) {

        }

        @Override
        public void onRemoved(String devId) {

        }

        @Override
        public void onStatusChanged(String devId, boolean online) {
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onNetworkStatusChanged(String devId, boolean status) {

        }

        @Override
        public void onDevInfoUpdate(String devId) {

        }
    };

    private String category = "lock";
    private ILockSite lockSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_list_activity);

        setTitle(Objects.equals(category, "lock") ? R.string.lock_list : R.string.gateway_list);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("site_id")) {
            siteId = intent.getLongExtra("site_id", 0L);
            category = intent.getStringExtra("category");
        }

        lockSite = ThingOSLock.newSiteInstance(siteId);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new LockListAdapter(lockList, category, new LockListAdapter.LockClickListener() {
            @Override
            public void onLockClick(DeviceBeanWrapper lock) {
                Intent intent = new Intent(DeviceListActivity.this, DoorLockDetailActivity.class);
                intent.putExtra("site_id", siteId);
                intent.putExtra("device_id", lock.deviceRespBean.getDevId());
                startActivity(intent);
            }

            @Override
            public void onGatewayClick(DeviceBeanWrapper gateway) {
                Intent intent = new Intent(DeviceListActivity.this, GatewayDetailActivity.class);
                intent.putExtra("site_id", siteId);
                intent.putExtra("device_id", gateway.deviceRespBean.getDevId());
                startActivity(intent);
            }

            @Override
            public void onUnlockClick(DeviceBeanWrapper lock) {
                ThingOSLock.newLockInstance(siteId, lock.deviceRespBean.getDevId()).unlock(new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(DeviceListActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(DeviceListActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRemoveClick(DeviceBeanWrapper lock) {
                if (Objects.equals(category, "lock")) {
                    lockSite.deleteLock(lock.deviceRespBean.getDevId(), new IThingResultCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            lockList.remove(lock);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(DeviceListActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String errorCode, String errorMessage) {
                            Toast.makeText(DeviceListActivity.this, getString(R.string.failed) + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    lockSite.deleteGateway(lock.deviceRespBean.getDevId(), new IThingResultCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            lockList.remove(lock);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(DeviceListActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String errorCode, String errorMessage) {
                            Toast.makeText(DeviceListActivity.this, getString(R.string.failed) + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onConnectClick(DeviceBeanWrapper lock) {
                //connect to this lock
                connectLock(lock);
            }

            @Override
            public void onUpdateNameClick(DeviceBeanWrapper device) {
                show(DeviceListActivity.this, new InputDialogListener() {
                    @Override
                    public void onTextEntered(String text) {
                        ThingOSDevice.newDeviceInstance(device.deviceRespBean.getDevId()).renameDevice(text, new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {
                                Toast.makeText(DeviceListActivity.this, error, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess() {
                                device.deviceRespBean.setName(text);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }

            @Override
            public void onSearchSubDeviceClick(DeviceBeanWrapper lock) {
                Intent intent = new Intent(DeviceListActivity.this, GwConfigSubDeviceActivity.class);
                intent.putExtra("gw_id", lock.deviceRespBean.getDevId());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActionSheet();
            }
        });

        findViewById(R.id.loadMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMore();
            }
        });
    }

    public void connectLock(DeviceBeanWrapper lockDevice) {
        List<BleConnectBuilder> builderList = new ArrayList<>();
        BleConnectBuilder bleConnectBuilder1 = new BleConnectBuilder();
        bleConnectBuilder1.setDevId(lockDevice.deviceRespBean.getDevId());
        bleConnectBuilder1.setLevel(BleConnectBuilder.Level.FORCE);
        builderList.add(bleConnectBuilder1);
        ThingOSBLE.manager().connectBleDevice(builderList); // 开始连接

        ThingOSDevice.newDeviceInstance(lockDevice.deviceRespBean.getDevId()).registerDevListener(devListener);
    }

    protected void onResume() {
        super.onResume();
        startId = 0;
        loadMore();
    }

    private void showActionSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.action_sheet_layout, null);
        ListView listView = view.findViewById(R.id.actionSheetList);
        String[] items = {getString(R.string.add_ble_lock), getString(R.string.add_ble_gateway), getString(R.string.add_zigbee_gateway)
                , getString(R.string.add_zigbee_lock)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            switch (position) {
                case 0:
                    //ble lock
                    Intent intent = new Intent(DeviceListActivity.this, BleLockDeviceActivity.class);
                    intent.putExtra("site_id", siteId);
                    startActivity(intent);
                    break;
                case 1:
                    //ble gateway
                    Intent intent1 = new Intent(DeviceListActivity.this, BleGatewaySetupActivity.class);
                    intent1.putExtra("site_id", siteId);
                    startActivity(intent1);
                    break;
                case 2:
                    //zigbee gateway
                    Intent intent2 = new Intent(DeviceListActivity.this, ZigbeeWiredGatewayConfigActivity.class);
                    intent2.putExtra("site_id", siteId);
                    startActivity(intent2);
                    break;
                case 3:
                    //zigbee lock
                    Toast.makeText(this, R.string.add_zigbee_device_tip, Toast.LENGTH_SHORT).show();
                    break;
            }
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    public static void show(Context context, final InputDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_update_name_dialog, null);
        builder.setView(view);

        final EditText editText = view.findViewById(R.id.editText);
        Button btnOk = view.findViewById(R.id.btnOk);

        final AlertDialog dialog = builder.create();

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if (!text.isEmpty() && listener != null) {
                    listener.onTextEntered(text);
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public interface InputDialogListener {
        void onTextEntered(String text);
    }

    private void loadMore() {
        if (loading) {
            return;
        }
        loading = true;
        if (Objects.equals(category, "lock")) {
            ThingOSLock.newSiteInstance(siteId).getLockDeviceList(startId, new IThingResultCallback<ArrayList<DeviceBeanWrapper>>() {
                @Override
                public void onSuccess(ArrayList<DeviceBeanWrapper> result) {
                    loading = false;
                    if (result == null || result.size() <= 0) {
                        return;
                    }

                    if (startId == 0) {
                        lockList.clear();
                    }
                    lockList.addAll(result);
                    adapter.notifyDataSetChanged();
                    startId = result.get(result.size() - 1).gatewayIndexId;
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    loading = false;
                    Toast.makeText(DeviceListActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ThingOSLock.newSiteInstance(siteId).getGatewayDeviceList(startId, new IThingResultCallback<ArrayList<DeviceBeanWrapper>>() {
                @Override
                public void onSuccess(ArrayList<DeviceBeanWrapper> result) {
                    loading = false;
                    if (result == null || result.size() <= 0) {
                        return;
                    }

                    if (startId == 0) {
                        lockList.clear();
                    }
                    lockList.addAll(result);
                    adapter.notifyDataSetChanged();
                    startId = result.get(result.size() - 1).gatewayIndexId;
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    loading = false;
                    Toast.makeText(DeviceListActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        devListener = null;
    }
}
