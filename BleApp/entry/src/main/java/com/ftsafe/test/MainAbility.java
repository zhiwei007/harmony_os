package com.ftsafe.test;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.AbsButton;
import ohos.agp.components.Checkbox;

import static com.ftsafe.test.MyApplication.BUNDLE_NAME;

public class MainAbility extends Ability implements AbsButton.CheckedStateChangedListener {
    private Checkbox abcCheckBox;
    private Checkbox yxycCheckBox;
    @Override
    public void onStart(Intent intent) {
        this.requestPermissionsFromUser(new String[]{"ohos.permission.LOCATION"},0);
        super.onStart(intent);
//         super.setMainRoute(ABCBleCentralAbilitySlice.class.getName());
        setUIContent(ResourceTable.Layout_ability_main);
        abcCheckBox = (Checkbox)findComponentById(ResourceTable.Id_abc_slice);
        yxycCheckBox = (Checkbox)findComponentById(ResourceTable.Id_ycyx_slice);
        abcCheckBox.setCheckedStateChangedListener(this);
        yxycCheckBox.setCheckedStateChangedListener(this);
    }

    @Override
    public void onCheckedChanged(AbsButton absButton, boolean b) {
           if(absButton == abcCheckBox && b){
               intentForAbility(ABCAbility.class.getName());
           }else{
               intentForAbility(YXYCAbility.class.getName());
           }
    }


    private  void intentForAbility(String className){
        Intent  intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withBundleName(BUNDLE_NAME)
                .withAbilityName(className).build();
        intent.setOperation(operation);
        startAbility(intent);
    }
}
