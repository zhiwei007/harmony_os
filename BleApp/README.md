#harmony_os
 
##鸿蒙 BLE基本操作
作为中心设备，实现扫描，连接，并给蓝牙Key发送APDU的功能

###收不到蓝牙Key返回数据：
*   数据较大，未拆分为每20字节一包下发<br>
*   拆分为多包下发，多次setValue 但是只有一次写特征(peripheralDevice.writeCharacteristic)<br>  
*   多包下发时，之间必须间隔一定时间(Thread.sleep(7))<br>  
*   农行蓝牙Key(justwork加密方式) 必须配对才能下发数据<br>  