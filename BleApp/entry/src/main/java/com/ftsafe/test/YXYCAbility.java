package com.ftsafe.test;

import com.ftsafe.test.slice.ble.YXYCBleCentralAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class YXYCAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(YXYCBleCentralAbilitySlice.class.getName());
    }
}
