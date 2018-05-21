# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/dev/adt-bundle-mac-x86_64-20131030/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn com.flurry.**

#这里com.xiaomi.mipushdemo.DemoMessageRreceiver改成app中定义的完整类名
-keep class com.newsdog.push.xiaomi.MiMessageReceiver {*;}
-keep class com.xiaomi.push.service.receivers.** {*;}
-keep class com.xiaomi.mipush.sdk.MiPushClient {public *;}
-keep class com.xiaomi.mipush.sdk.ErrorCode {public *;}
-keep class com.xiaomi.mipush.sdk.MiPushCommandMessage {public *;}
-keep class com.xiaomi.mipush.sdk.MiPushMessage {public *;}
-keep class com.xiaomi.mipush.sdk.PushMessageReceiver {public *;}

-keep class com.xiaomi.push.service.XMJobService {public *;}
-keep class com.xiaomi.push.service.XMPushService {public *;}
-keep class com.xiaomi.mipush.sdk.PushMessageHandler {public *;}
-keep class com.xiaomi.mipush.sdk.MessageHandleService {public *;}


#可以防止一个误报的 warning 导致无法成功编译，如果编译使用的 Android 版本是 23。
-dontwarn com.xiaomi.push.**


#flurry

# 第三方库的Library

-keep class com.flurry.android.FlurryAgent { public * ;}
-keep interface com.flurry.android.FlurryAgentListener { public * ;}