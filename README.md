#
#### 安全键盘
![image](https://github.com/153437803/safekeyboard/blob/master/device1.gif )

#
#### 支持
- [x] 加密混淆
- [x] 数字键盘, 字符顺序随机排列
- [x] 字母键盘, 字符顺序随机排列

#
#### 示例
```
// 显示
Bundle bundle = new Bundle();
bundle.putBoolean(SafeKeyboardDialog.BUNDLE_RANDOM_NUMBER, false);// 数字键盘随机
bundle.putBoolean(SafeKeyboardDialog.BUNDLE_RANDOM_LETTER, false); // 字母键盘随机
bundle.putBoolean(SafeKeyboardDialog.BUNDLE_OUTSIDE_CANCLE, false); // 点击外部是否关闭安全键盘
bundle.putLong(SafeKeyboardDialog.BUNDLE_DELAY_TIME, 60); // 延迟ms显示安全键盘
bundle.putString(SafeKeyboardDialog.BUNDLE_CALLBACK_EXTRA, "jello"); // 回调携带额外的String
SafeKeyboardDialog dialog = new SafeKeyboardDialog();
dialog.setArguments(bundle);
dialog.show(getSupportFragmentManager(), SafeKeyboardDialog.TAG);

// 回调
@Override
public void onActivityReenter(int resultCode, Intent data) {
    super.onActivityReenter(resultCode, data);
    if (resultCode == SafeKeyboardDialog.INTENT_CALLBACK_CODE) {
        String type = data.getStringExtra(SafeKeyboardDialog.INTENT_CALLBACK_TYPE);
        String value = data.getStringExtra(SafeKeyboardDialog.INTENT_CALLBACK_VALUE);
    }
}
```