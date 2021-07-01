package com.ftsafe.test;

import ohos.aafwk.ability.AbilityPackage;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class MyApplication extends AbilityPackage {
    public static HiLogLabel hilabel = new HiLogLabel(HiLog.LOG_APP, 0X0, "FT");
    public static final String BUNDLE_NAME = "com.ftsafe.test";
    @Override
    public void onInitialize() {
        super.onInitialize();
    }
}
