package android.wifi.repeater;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.wifi.repeater.utils.*;
import android.view.View.*;
import android.content.*;
import android.provider.*;
import java.io.*;

public class EnableActivity extends Activity implements OnClickListener 
{
	private String WPACONF = "wpa_supplicant_repeater.conf";
	private volatile Boolean canexit = true;
	private Handler mHandler = new Handler();
	private TextView tvCmd;
	private TextView tvSSID;
	private Button btnConnectWifi,btnEnableHotspot,btnStart;
	private EditText edtInterface,edtConfigfile;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable);
		
		tvCmd = (TextView) findViewById(R.id.activityenabletvcmd);
		tvSSID = (TextView) findViewById(R.id.activityenable_tv_ssid);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
        tvSSID.setText(WifiUtils.getWiFiSSID(this));
    }
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.activityenablebtnconnectwifi:
				WifiUtils.toggleWiFi(this,true);
				startActivityForResult(getWifiSetting(),0);
				btnEnableHotspot.setEnabled(true);
				break;
			case R.id.activityenablebtnenablehotspot:
				if (WifiUtils.setWifiApEnabled(this,true))
				{
					Toast.makeText(this,getString(R.string.tipshotspotenabled),Toast.LENGTH_LONG).show();
					tvCmd.setText(getString(R.string.tips_pleasewait));
				}
				else
				{
					Toast.makeText(this,getString(R.string.tipshotspotenableerr),Toast.LENGTH_LONG).show();
					startActivity(getHotspotSetting());
				}
				Runnable runnable=new Runnable(){
					@Override
					public void run() {
						btnStart.setEnabled(true);
						tvCmd.setText("");
					} 
				};
				mHandler.postDelayed(runnable, 10000);
				break;
			case R.id.activityenablebtnstart:
				btnStart.setEnabled(false);
				canexit = false;
				final String SSID = tvSSID.getText().toString();
				final String sinterface = edtInterface.getText().toString();
				final String sconfigfile = edtConfigfile.getText().toString();
				final EnableActivity that = this;
				final String errinvfile = getString(R.string.tips_err_invalidfile);
				final String errcannotfindnet = getString(R.string.tips_err_cannotfindnet);
				final String pleasewait = getString(R.string.tips_pleasewait);
				final String errlink = getString(R.string.tips_err_linkerror);
				final String completed = getString(R.string.tips_completed);
				new Thread(new Runnable() {
						public void run() {
							String conf = getConf(sconfigfile,SSID);
							if (conf.equals("fileerr"))
							{
								mHandler.post(new Runnable() {
										public void run()
										{
											tvCmd.setText(errinvfile+sconfigfile);
											btnStart.setEnabled(true);
										}
									});
							}
							else if (conf.equals("nosuchwifi"))
							{
								mHandler.post(new Runnable() {
										public void run()
										{
											tvCmd.setText(errcannotfindnet+SSID);
											btnStart.setEnabled(true);
										}
									});
							}
							else
							{
								StoreConf(conf);
								String[] cmd1 = {"busybox ifconfig "+sinterface+" up",
									"cd /data/misc/wifi",
									"busybox cp /data/data/"+getPackageName()+"/files/"+WPACONF+" ./",
									"busybox chmod 664 "+WPACONF,
									"wpa_supplicant -B -i "+sinterface+" -c "+WPACONF,
								};

								final ShellUtils.CommandResult cr1 = ShellUtils.execCommand(cmd1,true);
								mHandler.post(new Runnable() {
										public void run()
										{
											tvCmd.setText("wpa_supplicant:\n" + cr1.successMsg + "stderr:\n" + cr1.errorMsg + "\n");
											tvCmd.setText(tvCmd.getText() + pleasewait + "\n");
										}
									});
								Boolean linked = false;
								ShellUtils.CommandResult cr_iwconfig = ShellUtils.execCommand("iwconfig "+sinterface,true);
								if (cr_iwconfig.result != 0)
								{
									mHandler.post(new Runnable() {
											public void run()
											{
												tvCmd.setText(tvCmd.getText() + "iwconfig error\n");
											}
										});
									linked = true;
									try
									{
										Thread.sleep(10000);
									}
									catch (Exception e)
									{
										e.printStackTrace();
									}
								}
								else
								{
									for (int i=0;i<10;i++)
									{
										cr_iwconfig = ShellUtils.execCommand("iwconfig "+sinterface,true);
										if (cr_iwconfig.successMsg.contains(SSID))
										{
											linked = true;
											break;
										}
										try
										{
											Thread.sleep(1000);
										}
										catch (Exception e)
										{
											e.printStackTrace();
										}
									}
								}
								if (linked)
								{
									mHandler.post(new Runnable() {
											public void run()
											{
												tvCmd.setText(tvCmd.getText() + "link success!\n" + "==============================\n" + pleasewait + "\n");
											}
										});
									String[] cmd2 = {
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
												tvCmd.setText(tvCmd.getText() + "\n" + completed);
												Toast.makeText(that,completed,Toast.LENGTH_LONG).show();
											}
										});
								}
								else
								{
									ShellUtils.execCommand("killall wpa_supplicant",true);
									mHandler.post(new Runnable() {
											public void run()
											{
												tvCmd.setText(tvCmd.getText() + "\n" + errlink);
												Toast.makeText(that,errlink,Toast.LENGTH_LONG).show();
												btnStart.setEnabled(true);
											}
										});
								}
								
							}
							
							canexit = true;
						}
					}).start();
				
				break;
				
		}

	}
	
	private String getConf(String conffile,String sSSID)
	{
		String realSSID = sSSID;
		if ((sSSID.charAt(0)=='"')&&(sSSID.charAt(sSSID.length()-1)=='"'))
			realSSID = sSSID.substring(1,sSSID.length()-1);
		String encodedSSID = realSSID;
		try
		{
			encodedSSID = bytesToHexString(realSSID.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		ShellUtils.CommandResult cr = ShellUtils.execCommand("cat "+conffile,true);
		if (cr.result != 0)
		{
			return "fileerr";
		}
		StringBuilder r = new StringBuilder();
		String[] ss = cr.successMsg.split("\n");
		int j = 0;
		for (int i=0;i<ss.length;i++)
		{
			r.append(ss[i] + "\n");
			if (ss[i].trim().isEmpty())
			{
				j = i;
				break;
			}
		}
		int a = 0,b = 0;
		for (int i=j;i<ss.length;i++)
		{
			if ((ss[i].contains(sSSID))||(ss[i].contains(encodedSSID)))
			{
				for (int k=i;k>=j;k--)
				{
					if (ss[k].startsWith("network="))
					{
						a = k;
						break;
					}
				}
				for (int k=i;k<ss.length;k++)
				{
					if (ss[k].endsWith("}"))
					{
						b = k;
						break;
					}
				}

				break;
			}
		}
		if ((a==0)&&(b==0))
		{
			return "nosuchwifi";
		}
		for (int i=a;i<=b;i++)
		{
			r.append(ss[i] + "\n");
		}
		
		return r.toString();
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
	
	private void StoreConf(final String conf) {
		try {
			String tag = WPACONF;
			FileOutputStream fos = this.openFileOutput(tag, Context.MODE_PRIVATE);
			fos.write(conf.getBytes());
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();      
		} catch (IOException e) {
			e.printStackTrace();

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
	
	private static String bytesToHexString(byte[] src){
		StringBuilder stringBuilder =new StringBuilder("");
		if(src==null||src.length<=0){
			return null;
		}
		for(int i =0; i < src.length; i++){
			int v = src[i]&0xFF;
			String hv =Integer.toHexString(v);
			if(hv.length()<2){
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
	
}
