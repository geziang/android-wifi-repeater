package android.wifi.repeater.utils;
import android.net.wifi.*;
import android.content.*;
import java.lang.reflect.*;

public class WifiUtils
{
	private WifiUtils() {
        throw new AssertionError();
    }
	
	public static String getWiFiSSID(Context context) {  
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConnectionInfo().getSSID(); 
    }
	
	public static void toggleWiFi(Context context, boolean enabled) {  
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);  
    }
	
	public static boolean setWifiApEnabled(Context context,Boolean enabled) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		// disable WiFi in any case
		if (enabled)
		{
			toggleWiFi(context,false);
		}
		try {
			//通过反射调用设置热点
			Method method = wifiManager.getClass().getMethod(
				"setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
			//返回热点打开状态
			return (Boolean) method.invoke(wifiManager, null, enabled);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
