adb push jsdroid_cmd.apk /data/local/tmp/
adb shell app_process32 -Djava.class.path=/data/local/tmp/jsdroid_cmd.apk /data/local/tmp/ com.jsdroid.core.JsMain