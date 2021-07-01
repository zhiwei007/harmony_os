package com.ftsafe.test.slice;

import com.ftsafe.test.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;
import ohos.bluetooth.ble.*;
import ohos.utils.SequenceUuid;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

public class BlePeripheralAbilitySlice extends AbilitySlice {
    private static final String SERVICE_UUID = "00001887-0000-1000-8000-00805f9b34fb";
    private static final String NOTIFY_CHARACTER_UUID = "00002a10-0000-1000-8000-00805f9b34fb";
    private static final String WRITE_CHARACTER_UUID = "00002a11-0000-1000-8000-00805f9b34fb";
    private BlePeripheralDevice peripheralDevice = null;
    private boolean isAdvertising = false;
    private boolean isConnected = false;
    private Text statusText;
    private TextField field;
    private Text dataText;
    private Button advertiseButton;
    private Button sendButton;

    // 实现外围设备管理回调
    private class MyBlePeripheralManagerCallback extends BlePeripheralManagerCallback {
        // 连接状态变更的回调
        @Override
        public void connectionStateChangeEvent(
                BlePeripheralDevice device, int interval, int latency, int timeout, int status) {
            if (status == BlePeripheralDevice.OPERATION_SUCC && !isConnected) {
                isConnected = true;
                peripheralDevice = device;
                updateComponent(statusText, "状态：已连接");
            }
        }

        // 远程GATT客户端已请求编写特征的回调
        @Override
        public void receiveCharacteristicWriteEvent(
                BlePeripheralDevice device,
                int transId,
                GattCharacteristic characteristic,
                boolean isPrep,
                boolean needRsp,
                int offset,
                byte[] value) {
            if (Arrays.equals("Disconnect".getBytes(), value)) {
                isConnected = false;
                peripheralDevice = null;
                updateComponent(statusText, "状态：已广播，等待连接");
                return;
            }

            // 接收中心设备写入的数据
            updateComponent(dataText, new String(value, Charset.defaultCharset()));
        }
    }

    // 获取外围设备管理回调
    private MyBlePeripheralManagerCallback peripheralManagerCallback = new MyBlePeripheralManagerCallback();

    // 获取外围设备管理对象
    private BlePeripheralManager blePeripheralManager = new BlePeripheralManager(this, peripheralManagerCallback, 1);

    // 创建具有指定UUID的GattService实例
    private GattService gattService = new GattService(UUID.fromString(SERVICE_UUID), true);

    // 创建第1个GattCharacteristic实例，用于向中心设备发送数据
    private GattCharacteristic notifyCharacteristic =
            new GattCharacteristic(
                    UUID.fromString(NOTIFY_CHARACTER_UUID),
                    1 | 16,
                    GattCharacteristic.PROPERTY_READ
                            | GattCharacteristic.PROPERTY_WRITE
                            | GattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);

    // 创建第2个GattCharacteristic实例，用于接收中心设备发送的数据
    private GattCharacteristic writeCharacteristic =
            new GattCharacteristic(
                    UUID.fromString(WRITE_CHARACTER_UUID),
                    1 | 16,
                    GattCharacteristic.PROPERTY_READ
                            | GattCharacteristic.PROPERTY_WRITE
                            | GattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);

    // 实现BLE广播回调
    private class MyBleAdvertiseCallback extends BleAdvertiseCallback {
        // 开始广播回调
        @Override
        public void startResultEvent(int result) {
            if (result == BleAdvertiseCallback.RESULT_SUCC) {
                // 为GattService添加一个或多个特征
                gattService.addCharacteristic(notifyCharacteristic);
                gattService.addCharacteristic(writeCharacteristic);
                // 删除所有服务
                blePeripheralManager.clearServices();
                // 向外围设备管理对象添加GATT服务
                blePeripheralManager.addService(gattService);
            }
        }
    }

    // 创建广播数据
    private BleAdvertiseData advertiseData = new BleAdvertiseData.Builder()
            .addServiceData(SequenceUuid.uuidFromString(SERVICE_UUID), "12".getBytes())
            .addServiceUuid(SequenceUuid.uuidFromString(SERVICE_UUID))
            .build();

    // 创建广播参数
    private BleAdvertiseSettings advertiseSettings = new BleAdvertiseSettings.Builder()
            .setConnectable(true)
            .setInterval(BleAdvertiseSettings.INTERVAL_SLOT_MIN)
            .setTxPower(BleAdvertiseSettings.TX_POWER_MAX)
            .build();

    // 获取BLE广播回调
    private MyBleAdvertiseCallback advertiseCallback = new MyBleAdvertiseCallback();

    // 获取BLE广播对象
    private BleAdvertiser advertiser = new BleAdvertiser(this, advertiseCallback);

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_ble_peripheral);
        initComponent();
        initClickedListener();
    }

    // 初始化组件
    private void initComponent() {
        if (findComponentById(ResourceTable.Id_status) instanceof Text) {
            statusText = (Text) findComponentById(ResourceTable.Id_status);
        }
        if (findComponentById(ResourceTable.Id_data) instanceof Text) {
            dataText = (Text) findComponentById(ResourceTable.Id_data);
        }
        if (findComponentById(ResourceTable.Id_input) instanceof TextField) {
            field = (TextField) findComponentById(ResourceTable.Id_input);
        }
        if (findComponentById(ResourceTable.Id_advertise) instanceof Button) {
            advertiseButton = (Button) findComponentById(ResourceTable.Id_advertise);
        }
        if (findComponentById(ResourceTable.Id_send) instanceof Button) {
            sendButton = (Button) findComponentById(ResourceTable.Id_send);
        }
    }

    // 初始化点击回调
    private void initClickedListener() {
        advertiseButton.setClickedListener(component -> {
            if (!isAdvertising) {
                advertiseButton.setText("停止广播");
                statusText.setText("状态：已广播，等待连接");
                // 开始BLE广播
                advertiser.startAdvertising(advertiseSettings, advertiseData, null);
                isAdvertising = true;
            } else {
                advertiseButton.setText("开始广播");
                statusText.setText("状态：已停止广播");
                // 停止BLE广播
                advertiser.stopAdvertising();
                isAdvertising = false;
            }
        });

        sendButton.setClickedListener(component -> {
            if (field.getText().isEmpty() || (blePeripheralManager == null) || !isConnected) {
                return;
            }

            // 向中心设备发送数据
            notifyCharacteristic.setValue(field.getText().getBytes());
            blePeripheralManager.notifyCharacteristicChanged(peripheralDevice, notifyCharacteristic, false);
        });
    }

    private void updateComponent(Text text, String content) {
        getUITaskDispatcher().syncDispatch(new Runnable() {
            @Override
            public void run() {
                text.setText(content);
            }
        });
    }
}

