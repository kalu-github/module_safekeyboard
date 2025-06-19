# 检测并移除没有用到的类，变量，方法和属性；
-dontshrink
# 优化代码，非入口节点类会加上private/static/final, 没有用到的参数会被删除，一些方法可能会变成内联代码
-dontoptimize

# 指定外部模糊字典
-obfuscationdictionary proguard-rules-dict-mini.txt
# 指定class模糊字典
-classobfuscationdictionary proguard-rules-dict-mini.txt
# 指定package模糊字典
-packageobfuscationdictionary proguard-rules-dict-mini.txt


# 保护泛型
-keepattributes Signature
# 保护注解
-keepattributes *Annotation*,InnerClasses,EnclosingMethod
#-keep @interface * {
#    *;
#}

# dontwarn
-dontwarn lib.kalu.keyboard

# leanback
-keep class lib.kalu.keyboard.EditText {
    protected <methods>;
    public <methods>;
}
-keep class lib.kalu.keyboard.LogUtil {
    protected <methods>;
    public <methods>;
}