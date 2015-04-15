package android.wifi.repeater;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.wifi.repeater.utils.*;
import android.view.View.*;
import android.content.*;

public class MainActivity extends Activity implements OnClickListener 
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		TextView tvifconfig = (TextView) findViewById(R.id.maintvifconfig);
		Button btnEnable = (Button) findViewById(R.id.main_btn_enable);
		btnEnable.setOnClickListener(this);
		Button btnDisable = (Button) findViewById(R.id.main_btn_disable);
		btnDisable.setOnClickListener(this);
		
		ShellUtils.CommandResult cr = ShellUtils.execCommand("busybox ifconfig -a",true);
		tvifconfig.setText(cr.successMsg + cr.errorMsg);
    }
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.main_btn_enable:
				startActivity(new Intent(this,EnableActivity.class));
				break;
			case R.id.main_btn_disable:
				String[] cmd = {
					"killall wpa_supplicant",
					"killall dhcpcd",
					"iptables -t nat -F"};

				ShellUtils.execCommand(cmd,true);
				WifiUtils.setWifiApEnabled(this,false);
				WifiUtils.toggleWiFi(this,true);
				Toast.makeText(this,"操作完成",Toast.LENGTH_LONG).show();
				finish();
				break;
			
		}

	}
}
