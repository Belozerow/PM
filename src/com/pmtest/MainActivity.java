package com.pmtest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pmtest.sudo.RootUtils;
import com.pmtest.sudo.RootUtils.OnRootActionListener;

public class MainActivity extends Activity {
	private EditText installPath;
	private EditText uninstallPath;
	private EditText processPid;
	private TextView textResult;
	private RootUtils rootUtils;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        installPath = (EditText)findViewById(R.id.input_install_path);
        uninstallPath = (EditText)findViewById(R.id.input_uninstall_path);
        processPid = (EditText)findViewById(R.id.input_pid);
        
        textResult = (TextView)findViewById(R.id.text_result);
        
        rootUtils = new RootUtils(this);
    }
    private void setExecutedText(String command){
    	textResult.setText(String.format(getResources().getString(R.string.execution),command));
    }
    public void onUninstallButtonClick(View button){
    	final String text = uninstallPath.getText().toString();
    	rootUtils.setOnUninstall(new OnRootActionListener() {
			@Override
			public void onPreExecute() {
				setExecutedText("uninstall");
			}
			@Override
			public void onPostExecute(int result) {
				if(result == RootUtils.RESULT_SUCCESS)
					textResult.setText(String.format(getResources().getString(R.string.success_uninstall),text));
				else
					textResult.setText(String.format(getResources().getString(R.string.fail_uninstall),text));
			}
		});
    	rootUtils.uninstallPackage(text);
    }
    public void onInstallButtonClick(View button){
    	final String text = installPath.getText().toString();
    	rootUtils.setOnInstall(new OnRootActionListener() {
			@Override
			public void onPreExecute() {
				setExecutedText("install");
			}			
			@Override
			public void onPostExecute(int result) {
				if(result == RootUtils.RESULT_SUCCESS)
					textResult.setText(String.format(getResources().getString(R.string.success_install),text));
				else
					textResult.setText(String.format(getResources().getString(R.string.fail_install),text));
			}
			
		});
    	rootUtils.installPackage(text);
    }
    public void onSqlButtonClick(View button){
    	final String dbPath = "/data/data/org.xbmc.android.remote/databases/xbmc_hosts.db";
    	final String dbQuery = "update hosts set name='Notebook' where _id=1;";
    	rootUtils.setOnDBQuery(new OnRootActionListener() {
			@Override
			public void onPreExecute() {
				setExecutedText("SQL");
			}
			@Override
			public void onPostExecute(int result) {
				if(result == RootUtils.RESULT_SUCCESS)
					textResult.setText(String.format(getResources().getString(R.string.success_sql),dbQuery));
				else
					textResult.setText(String.format(getResources().getString(R.string.fail_sql),dbQuery));
			}
		});
    	rootUtils.sqlite3Query(dbPath, dbQuery);
    }
    public void onKillButtonClick(View button){
    	final int pid = Integer.parseInt(processPid.getText().toString());
    	//15 - TERM
    	//9 - KILL
    	final int signal = 15;
    	rootUtils.setOnKill(new OnRootActionListener() {
			@Override
			public void onPreExecute() {
				setExecutedText("kill");
			}
			@Override
			public void onPostExecute(int result) {
				if(result == RootUtils.RESULT_SUCCESS)
					textResult.setText(String.format(getResources().getString(R.string.kill_success),pid));
				else
					textResult.setText(String.format(getResources().getString(R.string.kill_fail),pid));
			}
		});
    	rootUtils.killProcess(pid, signal);
    }
    public void onChownButtonClick(View view){
    	final String content = "hello world";
    	final String filePath = "/data/data/com.pmtest/files/hello.txt";
    	rootUtils.setOnFileChange(new OnRootActionListener() {
			@Override
			public void onPreExecute() {
				setExecutedText("chown");
			}
			
			@Override
			public void onPostExecute(int result) {
				if(result == RootUtils.RESULT_SUCCESS)
					textResult.setText(getResources().getString(R.string.chown_success));
				else
					textResult.setText(getResources().getString(R.string.chown_fail));
			}
		});
    	rootUtils.changeFile(filePath, 10077, 10077, 777, content);
    }
}
