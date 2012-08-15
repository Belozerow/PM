package com.pmtest;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pmtest.sudo.ExecuteAsRoot;

public class MainActivity extends Activity {
	private EditText installPath;
	private EditText uninstallPath;
	private TextView textResult;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        installPath = (EditText)findViewById(R.id.input_install_path);
        uninstallPath = (EditText)findViewById(R.id.input_uninstall_path);
        textResult = (TextView)findViewById(R.id.text_result);
        
    }
    private void setExecutedText(String command){
    	textResult.setText(String.format(getResources().getString(R.string.execution),command));
    }
    public void onUninstallButtonClick(View button){
    	final String text = uninstallPath.getText().toString();
    	if(text!=null){
    		ExecuteAsRoot execAsRoot = new ExecuteAsRoot() {
				@Override
				protected ArrayList<String> getCommandsToExecute() {
					ArrayList<String> commands = new ArrayList<String>();
					commands.add(String.format("pm uninstall %s",text));
					
					return commands;
				}

				@Override
				protected void onPostExecuteRootCommands() {
					textResult.setText(String.format(getResources().getString(R.string.success_uninstall),text));
				}

				@Override
				protected void onPreExecuteRootCommands() {
					setExecutedText("uninstall");
				}
			};
			execAsRoot.execute();
    	}
    }
    public void onInstallButtonClick(View button){
    	final String text = installPath.getText().toString();
    	if(text!=null){
	    	ExecuteAsRoot execAsRoot = new ExecuteAsRoot() {
				@Override
				protected ArrayList<String> getCommandsToExecute() {
					ArrayList<String> commands = new ArrayList<String>();
					commands.add(String.format("pm install %s",text));
					return commands;
				}

				@Override
				protected void onPostExecuteRootCommands() {
					textResult.setText(String.format(getResources().getString(R.string.success_install),text));
				}

				@Override
				protected void onPreExecuteRootCommands() {
					setExecutedText("install");
				}
			};
			execAsRoot.execute();
    	}
    }
}
