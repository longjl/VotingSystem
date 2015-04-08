package com.vs;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.vs.model.Temp;

/**
 * Created by longjianlin on 15/3/26.
 */
public class VSApplication extends Application {
    private static final String TAG = "VSApplication";
    public String macAddress;//mac 地址

    public Temp temp = new Temp();


    public static VSApplication getApplication(Context context) {
        return (VSApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getMacAddress();
    }

    /**
     * 获取Mac地址
     */
    public void getMacAddress() {
        if (macAddress == null) {
            // 获取wifi管理器
            WifiManager wifiMng = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMng.getConnectionInfo();
            macAddress = wifiInfo.getMacAddress();
            Log.i(TAG, "--- DVB Mac Address : " + macAddress);
        }
    }
}
