package com.poc.recoveryconfig;

import com.poc.recoveryconfig.util.IniStreamReader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @SuppressLint({ "SdCardPath", "SdCardPath", "SdCardPath" })
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button zipverifbtn = (Button) findViewById(R.id.button1);
        Button zipnandbtn = (Button) findViewById(R.id.button2);
        Button goowipebtn = (Button) findViewById(R.id.button3);
        Button goobootbtn = (Button) findViewById(R.id.button4);
        Button choosenandlocbtn = (Button) findViewById(R.id.button5);
        zipverifbtn.setOnClickListener(zipListener);
        zipnandbtn.setOnClickListener(zipFlashListener);
        goowipebtn.setOnClickListener(gooWipeListener);
        goobootbtn.setOnClickListener(gooBootListener);
        choosenandlocbtn.setOnClickListener(chooseNandListener);
        UpdateLabel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private OnClickListener zipListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
			ab.setMessage("Verify ZIP Integrity?").setPositiveButton("Yes", zipVerifListener)
			  .setNegativeButton("No", zipVerifListener);
			ab.show();
		}
    	
    };
    
    private OnClickListener zipFlashListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
			ab.setMessage("Prompt for Nandroid Backup?").setPositiveButton("Yes", zipNandListener)
			  .setNegativeButton("No", zipNandListener);
			ab.show();
		}
    	
    };
    
    private OnClickListener gooWipeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
			ab.setMessage("Prompt on GooManager wipes?").setPositiveButton("Yes", gooWipeListenerDlg)
			  .setNegativeButton("No", gooWipeListenerDlg);
			ab.show();
		}
    	
    };
    
    private OnClickListener gooBootListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
			ab.setMessage("Force reboot when flashing with GooManager?").setPositiveButton("Yes", gooBootListenerDlg)
			  .setNegativeButton("No", gooBootListenerDlg);
			ab.show();
		}
    	
    };
    
    private OnClickListener chooseNandListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
			final EditText input = new EditText(MainActivity.this);
			alert.setMessage("Enter backup location:");
			alert.setView(input);
			alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String fileName = ".userdefinedbackups";
					File sdDir = new File(Environment.getExternalStorageDirectory().getPath());
					if (sdDir.exists() && sdDir.canWrite()) {
						File file = new File(sdDir.getAbsolutePath() + "/cotrecovery/" + fileName);
						try {
							file.delete();
							file.createNewFile();
						} catch (IOException e) {
							Log.e("COT", "error creating file", e);
						}
						
						if (file.exists() && file.canWrite()) {
							FileOutputStream fos = null;
							try {
								fos = new FileOutputStream(file);
								fos.write(input.getText().toString().trim().getBytes());
								UpdateLabel();
							} catch (FileNotFoundException e) {
								Log.e("COT", "ERROR", e);
							} catch (IOException e) {
								Log.e("COT", "ERROR", e);
							} finally {
								if (fos != null) {
									try {
										fos.flush();
										fos.close();
									} catch (IOException e) {
									}
								}
							}
						} else {
							Log.e("COT", "error writing to file");
						}
					} else {
						Log.e("COT", "ERROR, /sdcard path not available");
					}
					
				}
			});
			alert.show();
			
		}
    	
    };
    
    DialogInterface.OnClickListener gooBootListenerDlg = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				String[] commands = {"sed -i 's/ORSReboot = 0/ORSReboot = 1/' /sdcard/cotrecovery/settings.ini"};
				RunAsRoot(commands);
				Toast.makeText(MainActivity.this, "GooManager Forced Reboot enabled!", Toast.LENGTH_SHORT).show();
				break;
			
			case DialogInterface.BUTTON_NEGATIVE:
				String[] commands1 = {"sed -i 's/ORSReboot = 1/ORSReboot = 0/' /sdcard/cotrecovery/settings.ini"};
				RunAsRoot(commands1);
				Toast.makeText(MainActivity.this, "GooManager Forced Reboot disabled!", Toast.LENGTH_SHORT).show();
				break;
			}
		}
    	
    };
    
    DialogInterface.OnClickListener gooWipeListenerDlg = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				String[] commands = {"sed -i 's/ORSWipePrompt = 0/ORSWipePrompt = 1/' /sdcard/cotrecovery/settings.ini"};
				RunAsRoot(commands);
				Toast.makeText(MainActivity.this, "GooManager Wipe Prompt enabled!", Toast.LENGTH_SHORT).show();
				break;
			
			case DialogInterface.BUTTON_NEGATIVE:
				String[] commands1 = {"sed -i 's/ORSWipePrompt = 1/ORSWipePrompt = 0/' /sdcard/cotrecovery/settings.ini"};
				RunAsRoot(commands1);
				Toast.makeText(MainActivity.this, "GooManager Wipe Prompt disabled!", Toast.LENGTH_SHORT).show();
				break;
			}
		}
    	
    };
    
    DialogInterface.OnClickListener zipNandListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				String[] commands = {"sed -i 's/BackupPrompt = 0/BackupPrompt = 1/' /sdcard/cotrecovery/settings.ini"};
				RunAsRoot(commands);
				Toast.makeText(MainActivity.this, "ZIP Flash Nandroid Prompt enabled!", Toast.LENGTH_SHORT).show();
				break;
			
			case DialogInterface.BUTTON_NEGATIVE:
				String[] commands1 = {"sed -i 's/BackupPrompt = 1/BackupPrompt = 0/' /sdcard/cotrecovery/settings.ini"};
				RunAsRoot(commands1);
				Toast.makeText(MainActivity.this, "ZIP Flash Nandroid Prompt disabled!", Toast.LENGTH_SHORT).show();
				break;
			}
		}
    	
    };
    
    DialogInterface.OnClickListener zipVerifListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				String[] commands = {"sed -i 's/SignatureCheckEnabled = 0/SignatureCheckEnabled = 1/' /sdcard/cotrecovery/settings.ini"};
				RunAsRoot(commands);
				Toast.makeText(MainActivity.this, "ZIP Verification enabled!", Toast.LENGTH_SHORT).show();
				break;
			
			case DialogInterface.BUTTON_NEGATIVE:
				String[] commands1 = {"sed -i 's/SignatureCheckEnabled = 1/SignatureCheckEnabled = 0/' /sdcard/cotrecovery/settings.ini"};
				RunAsRoot(commands1);
				Toast.makeText(MainActivity.this, "ZIP Verification disabled!", Toast.LENGTH_SHORT).show();
				break;
			}
		}
    	
    };
    
    public void RunAsRoot(String[] cmds) {
    	try {
			Process p = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			for (String tmpCmd : cmds) {
				os.writeBytes(tmpCmd+"\n");
			}
			os.writeBytes("exit\n");
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void UpdateLabel() {
    	String fileName = ".userdefinedbackups";
        File sdDir = new File(Environment.getExternalStorageDirectory().getPath());
        File readFile = new File(sdDir.getPath() + "/cotrecovery/" + fileName);
    	String orsreboot = null;
    	String orswipeprompt = null;
    	String backupprompt = null;
    	String sigcheck = null;
    	String iniFile = "settings.ini";
        InputStream readIniFile;
		try {
			readIniFile = new FileInputStream(sdDir.getPath() + "/cotrecovery/" + iniFile);
			IniStreamReader ini = new IniStreamReader(new InputStreamReader(readIniFile));
			while(ini.readNext()) {
				final String key = ini.getKey();
				final String value = ini.getValue();
				final String section = ini.getSection();
				if (section.startsWith("Settings")) {
					if (key.equalsIgnoreCase("ORSReboot")) {
						orsreboot = value.toString().trim();
						orsreboot = orsreboot.replace(";", "");
						orsreboot = orsreboot.trim();
					} else if (key.equalsIgnoreCase("ORSWipePrompt")) {
						orswipeprompt = value.toString().trim();
						orswipeprompt = orswipeprompt.replace(";", "");
						orswipeprompt = orswipeprompt.trim();
					} else if (key.equalsIgnoreCase("BackupPrompt")) {
						backupprompt = value.toString().trim();
						backupprompt = backupprompt.replace(";", "");
						backupprompt = backupprompt.trim();
					} else if (key.equalsIgnoreCase("SignatureCheckEnabled")) {
						sigcheck = value.toString().trim();
						sigcheck = sigcheck.replace(";", "");
						sigcheck = sigcheck.trim();
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	TextView t = new TextView(this);
		t = (TextView)findViewById(R.id.textView2);
        if (readFile.exists() && readFile.canRead()) {
        	FileInputStream fis = null;
        	try {
        		fis = new FileInputStream(readFile);
        		byte[] reader = new byte[fis.available()];
        		while (fis.read(reader) != -1) {
				}
				t.setText("Backup Path: /sdcard/" + new String(reader) + "\n");
        	} catch (IOException e) {
        		Log.e("COT", e.getMessage(), e);
        	} finally {
        		if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
					}
				}
        	}
        } else {
    		t.setText("Backup Path: /sdcard/cotrecovery\n");
        }
        t.append("GooManager Forced Reboots: " + orsreboot + "\n");
		t.append("GooManager Partition Wipe Prompt: " + orswipeprompt + "\n");
		t.append("ZIP Flash Nandroid Prompt: " + backupprompt + "\n");
		t.append("ZIP Integrity Checking: " + sigcheck + "\n");
    	
    }
    
}
