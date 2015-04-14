package android.wifi.repeater.utils;
import android.net.wifi.*;
import android.content.*;
import java.lang.reflect.*;

public class WifiUtils
{
	private WifiUtils() {
        throw new AssertionError();
    }
	
	public static void toggleWiFi(Context context, boolean enabled) {  
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);  
    }
	
	public static boolean EnableWifiAp(Context context,String ApSSID,String ApPassword) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		// disable WiFi in any case
		toggleWiFi(context,false);
		try {
			WifiConfiguration apConfig = new WifiConfiguration();
			apConfig.SSID = ApSSID;
			apConfig.preSharedKey = ApPassword;
			//通过反射调用设置热点
			Method method = wifiManager.getClass().getMethod(
				"setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
			//返回热点打开状态
			return (Boolean) method.invoke(wifiManager, apConfig, true);
		} catch (Exception e) {
			return false;
		}
	}
}
