package com.morefun.mpos.reader.sampleDemo.bleawake;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;

/**
 * 作者：yeqianyun on 2020/9/10 11:52
 * 邮箱：1612706976@qq.com
 */
public class WakeAndLock {

    Context context;
    PowerManager pm;
    PowerManager.WakeLock wakeLock;

    @SuppressLint("InvalidWakeLockTag")
    public WakeAndLock(Context context) {
        this.context = context;
        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_DIM_WAKE_LOCK, "WakeAndLock");
    }

    public boolean isScreenOn() {
        return pm.isInteractive();
    }

    /**
     * 唤醒屏幕
     */
    public void screenOn() {
        wakeLock.acquire(1000);
        wakeLock.release();
        android.util.Log.i("cxq", "screenOn");
    }

    /**
     * 熄灭屏幕
     */
    public void screenOff() {
        android.util.Log.i("cxq", "screenOff");
    }
}
