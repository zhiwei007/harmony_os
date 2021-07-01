package com.ftsafe.test.slice.ble;

import com.ftsafe.test.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;
import ohos.bluetooth.ProfileBase;
import ohos.bluetooth.ble.*;
import ohos.hiviewdfx.HiLog;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static com.ftsafe.test.MyApplication.hilabel;

public class YXYCBleCentralAbilitySlice extends AbilitySlice {
    private static final String   SERVICE_UUID =          "46540001-0001-00E2-0010-465453414645";
    private static final String   WRITE_CHARACTER_UUID =  "46540002-0001-00E2-0010-465453414645";
    private static final String   NOTIFY_CHARACTER_UUID = "46540003-0001-00E2-0010-465453414645";
    private static final String   DISCIBTOR_CHARACTER_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    private BlePeripheralDevice peripheralDevice = null;
    private GattCharacteristic writeCharacteristic;
    private boolean isConnected = false;
    private boolean isScanning = false;
    private Text deviceText;
    private Text statusText;
    private TextField field;
    private TextField dataText;
    private Button scanButton;
    private Button connectButton;
    private Button sendButton;

    private final String filsters = "FT_02";
    // 实现外围设备操作回调
    private class MyBlePeripheralCallback extends BlePeripheralCallback {

        // 连接状态变更的回调
        @Override
        public void connectionStateChangeEvent(int connectionState) {
            super.connectionStateChangeEvent(connectionState);
            if (connectionState == ProfileBase.STATE_CONNECTED && !isConnected) {
                HiLog.error(hilabel, "===============isConnected========>" );
                centralManager.stopScan();/*连接成功则停止扫描*/
                isConnected = true;
                peripheralDevice.discoverServices();
                updateComponent(statusText, "状态：已连接");
            }else{
                isConnected = false;
//                peripheralDevice.disconnect();
                HiLog.error(hilabel, "================disconnected========>" );
                updateComponent(statusText, "状态：未连接");
                getUITaskDispatcher().asyncDispatch(()->connectButton.setText("连接设备"));
                getUITaskDispatcher().asyncDispatch(()->scanButton.setText("开始扫描"));
            }
        }
        // 在外围设备上发现服务的回调
        @Override
        public void servicesDiscoveredEvent(int status) {
            super.servicesDiscoveredEvent(status);
            if (status == BlePeripheralDevice.OPERATION_SUCC) {
                HiLog.error(hilabel,"==========servicesDiscoveredEvent   OK....");
                for (GattService service : peripheralDevice.getServices()) {
                    checkGattCharacteristic(service);
                }
            }
        }

        private void checkGattCharacteristic(GattService service) {
            HiLog.error(hilabel, "checkGattCharacteristic  search service========>" );
            for (GattCharacteristic tmpChara : service.getCharacteristics()) {
                if (tmpChara.getUuid().equals(UUID.fromString(NOTIFY_CHARACTER_UUID))) {
                    // 启用特征通知
                    boolean  hasNotify = peripheralDevice.setNotifyCharacteristic(tmpChara, true);
                    if(hasNotify){
                        updateComponent(dataText,"setNotifyCharacteristic OK!");
                        HiLog.error(hilabel,"！！！！！notify ok!！！！！！！");
                    }else{
                        updateComponent(dataText,"setNotifyCharacteristic failed!");
                        HiLog.error(hilabel,"！！！！！！！！！notify failed!！！！！！");
                    }

                }

                if (tmpChara.getUuid().equals(UUID.fromString(WRITE_CHARACTER_UUID))) {
                    // 获取GattCharacteristic
                    HiLog.error(hilabel,"find  writeCharacteristic!!!");
                    writeCharacteristic = tmpChara;
                }
            }
        }
        @Override
        public void descriptorReadEvent(GattDescriptor descriptor, int ret) {
            // 向外围设备读描述值数据成功后的操作
            HiLog.error(hilabel, "descriptorReadEvent========>" );
        }

        @Override
        public void descriptorWriteEvent(GattDescriptor descriptor, int ret) {
            // 向外围设备写描述值数据成功后的操作
            HiLog.error(hilabel, "descriptorWriteEvent========>" );
        }


        @Override
        public void readRemoteRssiEvent(int rssi, int ret) {
            if (ret == BlePeripheralDevice.OPERATION_SUCC){
                // 读取外围设备RSSI值成功后的操作，对端RSSI值为rssi
                HiLog.error(hilabel, "readRemoteRssiEvent========>"+ rssi);
            }
        }




        // 特征变更的回调(接收数据)
        @Override
        public void characteristicChangedEvent(GattCharacteristic characteristic) {
            HiLog.error(hilabel,"===================characteristicChangedEvent OK=======>");
            super.characteristicChangedEvent(characteristic);
            String recvValue = Convection.Bytes2HexString(characteristic.getValue());
            HiLog.error(hilabel,"recvValue=======>"+recvValue);
            // 接收外围设备发送的数据
            stringBuffer.append(recvValue);
            HiLog.error(hilabel,"stringBuffer=======>"+stringBuffer.toString());
            updateComponent(dataText, stringBuffer.toString());
        }

        @Override
        public void characteristicWriteEvent(GattCharacteristic charecteristic, int ret) {
            if (ret == BlePeripheralDevice.OPERATION_SUCC){
                // 向外围设备写特征值数据成功后的操作
                HiLog.error(hilabel,"characteristicWriteEvent OK=======>"+charecteristic.getProperties());
//                peripheralDevice.readCharacteristic(charecteristic);
            }
        }

        @Override
        public void characteristicReadEvent(GattCharacteristic charecteristic, int ret) {
            if (ret == BlePeripheralDevice.OPERATION_SUCC){
                // 向外围设备写特征值数据成功后的操作
                HiLog.error(hilabel, "characteristicReadEvent OK========>" +charecteristic.getProperties());
            }
        }
    }

    // 获取外围设备操作回调
    private MyBlePeripheralCallback blePeripheralCallback = new MyBlePeripheralCallback();

    // 实现中心设备管理回调
    private  StringBuffer stringBuffer = new StringBuffer();
    private class MyBleCentralManagerCallback implements BleCentralManagerCallback {
        // 扫描结果的回调

        @Override
        public void scanResultEvent(BleScanResult bleScanResult) {
            String deviceName = bleScanResult.getPeripheralDevice().getDeviceName().get();
            HiLog.error(hilabel,"deviceName:"+ deviceName);
           if(deviceName.contains(filsters)){
//          if(deviceName.contains("FT_0200000000027")){
//          if(deviceName.contains("FT_kft")){
               peripheralDevice = bleScanResult.getPeripheralDevice();
               updateComponent(deviceText, peripheralDevice.getDeviceName().get());
                stringBuffer.append(deviceName).append("======>")
                        .append(bleScanResult.getRssi());
               stringBuffer.append('\n');
                updateComponent(dataText, stringBuffer.toString());
            }

        }

        // 扫描失败回调
        @Override
        public void scanFailedEvent(int i) {
            updateComponent(deviceText, "设备：扫描失败，请重新扫描！");
        }

        // 组扫描成功回调
        @Override
        public void groupScanResultsEvent(List list) {
            // 使用组扫描时在此对扫描结果进行处理
        }
    }

    // 获取中心设备管理回调
    private MyBleCentralManagerCallback centralManagerCallback = new MyBleCentralManagerCallback();

    // 获取中心设备管理对象
    private BleCentralManager centralManager = new BleCentralManager(this, centralManagerCallback);

    // 创建扫描过滤器
    private List filters = new ArrayList<>();

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_ble_central);
        initComponent();
        initClickedListener();
    }

    // 初始化组件
    private void initComponent() {
        if (findComponentById(ResourceTable.Id_device_info) instanceof Text) {
            deviceText = (Text) findComponentById(ResourceTable.Id_device_info);
        }
        if (findComponentById(ResourceTable.Id_status) instanceof Text) {
            statusText = (Text) findComponentById(ResourceTable.Id_status);
        }
        if (findComponentById(ResourceTable.Id_data) instanceof Text) {
            dataText = (TextField) findComponentById(ResourceTable.Id_data);
        }
        if (findComponentById(ResourceTable.Id_input) instanceof TextField) {
            field = (TextField) findComponentById(ResourceTable.Id_input);
        }
        if (findComponentById(ResourceTable.Id_scan) instanceof Button) {
            scanButton = (Button) findComponentById(ResourceTable.Id_scan);
        }
        if (findComponentById(ResourceTable.Id_connect) instanceof Button) {
            connectButton = (Button) findComponentById(ResourceTable.Id_connect);
        }
        if (findComponentById(ResourceTable.Id_send) instanceof Button) {
            sendButton = (Button) findComponentById(ResourceTable.Id_send);
        }
    }

    private void updateComponent(Text text, String content) {
        getUITaskDispatcher().asyncDispatch(() -> text.setText(content));
    }

    // 初始化点击回调
    private void initClickedListener() {
        scanButton.setClickedListener(component -> {
            if (!isScanning && !isConnected) {
                isScanning = true;
                scanButton.setText("停止扫描");
                deviceText.setText("设备：正在扫描...");
                // 开始扫描带有过滤器的指定BLE设备
                centralManager.startScan(filters);
            } else {
                isScanning = false;
                scanButton.setText("开始扫描");
                deviceText.setText("设备：暂无设备");
                // 停止扫描
                centralManager.stopScan();
            }
        });

        connectButton.setClickedListener(component -> {
            if (peripheralDevice == null) {
                statusText.setText("状态：请先扫描获取设备信息");
                return;
            }

            if (!isConnected) {
                peripheralDevice.disconnect();
                connectButton.setText("断开连接");
                statusText.setText("状态：连接中...");
                // 连接到BLE外围设备
                peripheralDevice.connect(false, blePeripheralCallback);
            } else {
                isConnected = false;
                connectButton.setText("连接设备");
                statusText.setText("状态：未连接");
                deviceText.setText("设备：暂无设备");

                peripheralDevice.disconnect();
                peripheralDevice = null;
            }
        });

        sendButton.setClickedListener(component -> {
            stringBuffer = new StringBuffer();
            stringBuffer.delete(0,stringBuffer.length());
            dataText.setText("");
            if (field.getText().isEmpty() || (peripheralDevice == null) || !isConnected) {
                return;
            }
            // 向外围设备发送数据
            if(writeCharacteristic == null){
                HiLog.error(hilabel,"writeCharacteristic == null  ");
                return ;
            }
            HiLog.error(hilabel," (send data) writeCharacteristic UUID =============>"+writeCharacteristic.getUuid().toString());

            boolean iswriteCharacteristic;

            byte[] sendata = Convection.hexString2Bytes("611A00000000000000008006000000000000810F");
            boolean isSetValueOk = writeCharacteristic.setValue(sendata);
            if(isSetValueOk){
                HiLog.error(hilabel,"writeCharacteristic setValue 1  ok!");
            }else{
                HiLog.error(hilabel, "writeCharacteristic setValue  failed!");
            }

           iswriteCharacteristic = peripheralDevice.writeCharacteristic(writeCharacteristic);
            HiLog.error(hilabel,iswriteCharacteristic?"writeCharacteristic1  OK!":"iswriteCharacteristic1 Failed!");

          try {
                Thread.sleep(7);//延时7个毫秒发送下一包
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (isSetValueOk) {
                sendata = Convection.hexString2Bytes("4F50504F5F46494E4458322D50524F23");
                isSetValueOk = writeCharacteristic.setValue(sendata);
                if(isSetValueOk){
                    HiLog.error(hilabel,"writeCharacteristic setValue 2  ok!");
                }else{
                    HiLog.error(hilabel, "writeCharacteristic setValue 2  failed!");
                }
            }

            iswriteCharacteristic = peripheralDevice.writeCharacteristic(writeCharacteristic);
            HiLog.error(hilabel,iswriteCharacteristic?"writeCharacteristic2  OK!":"iswriteCharacteristic2 Failed!");

        });
    }
}
