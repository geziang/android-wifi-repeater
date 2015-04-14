package android.wifi.repeater;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.wifi.repeater.utils.*;
import android.view.View.*;
import android.content.*;
import android.provider.*;

public class EnableActivity extends Activity implements OnClickListener 
{
	private TextView tvCmd;
	private Button btnConnectWifi,btnEnableHotspot,btnStart;
	private EditText edtInterface,edtConfigfile;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable);
		
		tvCmd = (TextView) findViewById(R.id.activityenabletvcmd);
		edtInterface = (EditText) findViewById(R.id.activityenable_edt_interface);
		edtConfigfile = (EditText) findViewById(R.id.activityenable_edt_configfile);
		btnConnectWifi = (Button) findViewById(R.id.activityenablebtnconnectwifi);
		btnConnectWifi.setOnClickListener(this);
		btnEnableHotspot = (Button) findViewById(R.id.activityenablebtnenablehotspot);
		btnEnableHotspot.setOnClickListener(this);
		btnStart = (Button) findViewById(R.id.activityenablebtnstart);
		btnStart.setOnClickListener(this);
		
    }
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.activityenablebtnconnectwifi:
				WifiUtils.toggleWiFi(this,false);
				WifiUtils.toggleWiFi(this,true);
				startActivity(getWifiSetting());
				btnEnableHotspot.setEnabled(true);
				break;
			case R.id.activityenablebtnenablehotspot:
				startActivity(getHotspotSetting());
				btnStart.setEnabled(true);
				break;
			case R.id.activityenablebtnstart:
				btnStart.setEnabled(false);
				String sinterface = edtInterface.getText().toString();
				String sconfigfile = edtConfigfile.getText().toString();
				String[] cmd = {"busybox ifconfig "+sinterface+" up",
								"cd /data/misc/wifi",
					"wpa_supplicant -B -i "+sinterface+" -c "+sconfigfile,
					"sleep 10",
					"iwconfig "+sinterface,
					"dhcpcd "+sinterface,
					"echo '1'> /proc/sys/net/ipv4/ip_forward",
					"iptables -F",
					"iptables -P INPUT ACCEPT",
					"iptables -P FORWARD ACCEPT",
					"iptables -t nat -A POSTROUTING -o "+sinterface+" -j MASQUERADE"};
				
				ShellUtils.CommandResult cr = ShellUtils.execCommand(cmd,true);
				tvCmd.setText(cr.successMsg + "error:\n" + cr.errorMsg);
				Toast.makeText(this,"操作完成",Toast.LENGTH_LONG).show();
				break;
				
		}

	}
	
	private static Intent getHotspotSetting()
	{
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        ComponentName com = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setComponent(com);
        return intent;
    }
	private static Intent getWifiSetting()
	{
		Intent intent = new Intent();
		intent.setAction(Settings.ACTION_WIFI_SETTINGS);
		return intent;
	}
	
}
