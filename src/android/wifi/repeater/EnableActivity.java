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
	private volatile Boolean canexit = true;
	private Handler mHandler = new Handler();
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
				if (WifiUtils.setWifiApEnabled(this,true))
				{
					Toast.makeText(this,"热点启动成功",Toast.LENGTH_LONG).show();
				}
				else
				{
					Toast.makeText(this,"热点启动失败，请手动打开",Toast.LENGTH_LONG).show();
					startActivity(getHotspotSetting());
				}
				btnStart.setEnabled(true);
				break;
			case R.id.activityenablebtnstart:
				btnStart.setEnabled(false);
				canexit = false;
				final String sinterface = edtInterface.getText().toString();
				final String sconfigfile = edtConfigfile.getText().toString();
				final EnableActivity that = this;
				new Thread(new Runnable() {
						public void run() {
							String[] cmd1 = {"busybox ifconfig "+sinterface+" up",
								"cd /data/misc/wifi",
								"wpa_supplicant -B -i "+sinterface+" -c "+sconfigfile,
								};

							final ShellUtils.CommandResult cr1 = ShellUtils.execCommand(cmd1,true);
							mHandler.post(new Runnable() {
									public void run()
									{
										tvCmd.setText("wpa_supplicant:\n" + cr1.successMsg + "stderr:\n" + cr1.errorMsg + "==============================\n");
										tvCmd.setText(tvCmd.getText() + "请稍候...\n");
									}
								});
							try
							{
								Thread.sleep(10000);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
							String[] cmd2 = {
								"iwconfig "+sinterface,
								"dhcpcd "+sinterface,
								"echo '1'> /proc/sys/net/ipv4/ip_forward",
								"iptables -F",
								"iptables -P INPUT ACCEPT",
								"iptables -P FORWARD ACCEPT",
								"iptables -t nat -A POSTROUTING -o "+sinterface+" -j MASQUERADE"
							};
							final ShellUtils.CommandResult cr2 = ShellUtils.execCommand(cmd2,true);
							mHandler.post(new Runnable() {
									public void run()
									{
										tvCmd.setText(tvCmd.getText() + cr2.successMsg + "stderr:\n" + cr2.errorMsg);
										tvCmd.setText(tvCmd.getText() + "\n操作完成");
										Toast.makeText(that,"操作完成",Toast.LENGTH_LONG).show();
									}
								});
							canexit = true;
						}
					}).start();
				
				break;
				
		}

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (canexit) {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
