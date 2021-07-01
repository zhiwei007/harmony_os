package com.ftsafe.test;

import com.ftsafe.test.slice.ble.ABCBleCentralAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class ABCAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(ABCBleCentralAbilitySlice.class.getName());
    }
}
