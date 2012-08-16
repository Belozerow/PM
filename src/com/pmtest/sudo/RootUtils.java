package com.pmtest.sudo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.pmtest.R;

import android.content.Context;
import android.util.Log;

public class RootUtils {
	Context context;
	private OnRootAction onUninstall;
	private OnRootAction onInstall;
	private OnRootAction onDBQuery;
	public final static int RESULT_SUCCESS = 1;
	public final static int RESULT_FAIL = 2;
	public RootUtils(Context context){
		this.context = context;
	}
	public void setOnUninstall(OnRootAction onUninstall){
		this.onUninstall = onUninstall;
	}
	public void setOnInstall(OnRootAction onInstall){
		this.onInstall = onInstall;
	}
	public void setOnDBQuery(OnRootAction onDBQuery){
		this.onDBQuery = onDBQuery;
	}
	public interface OnRootAction{
		public void onPreExecute();
		public void onPostExecute(int result);
	};
	
	private int checkPMLogFile(String logName, final String logPath, String errorContains){
		int retVal = RESULT_SUCCESS;
		File filesDir = context.getFilesDir();
		File[] files = filesDir.listFiles();
		for(File file : files){
			if(logName.equals(file.getName())){
				try {
					//read log file
					Scanner fScanner = new Scanner(file);
					while(fScanner.hasNext()){
						if(fScanner.next().toLowerCase().contains(errorContains.toLowerCase())){
							retVal = RESULT_FAIL;
							break;
						}
					}
					fScanner.close();
					//remove log file
					ExecuteAsRoot delLog = new ExecuteAsRoot() {
						@Override
						protected void onPreExecuteRootCommands() {}									
						@Override
						protected void onPostExecuteRootCommands() {}
						@Override
						protected ArrayList<String> getCommandsToExecute() {
							ArrayList<String> commands = new ArrayList<String>();
							commands.add(String.format("rm %s",logPath));
							return commands;
						}
					};
					delLog.execute();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return retVal;
			}
		}
		return retVal;
	}
	public void uninstallPackage(final String packageName){
		if(packageName.length()>0){
			final String logName = String.format("%s.uninstall.log",packageName).replaceAll("\\s", "");
			final String logPath = String.format("/data/data/%s/files/%s",context.getPackageName(),logName);
    		ExecuteAsRoot execAsRoot = new ExecuteAsRoot(){
				@Override
				protected ArrayList<String> getCommandsToExecute() {
					ArrayList<String> commands = new ArrayList<String>();
					commands.add(String.format("pm uninstall %s -s > %s",packageName,logPath));
					//TODO native util for change file owner (chown)
					commands.add(String.format("chmod -R 775 %s", logPath));
					return commands;
				}
				@Override
				protected void onPostExecuteRootCommands() {
					int result = checkPMLogFile(logName, logPath,"Failure");
					if(onUninstall!=null)
						onUninstall.onPostExecute(result);
				}
				@Override
				protected void onPreExecuteRootCommands() {
					if(onUninstall!=null)
					onUninstall.onPreExecute();
				}
			};
			execAsRoot.execute();
    	}
	}
	public void installPackage(final String packagePath){
		if(packagePath.length()>0){
			String[] packagePathSplit = packagePath.split("/"); 
			final String packageName = packagePathSplit[packagePathSplit.length-1];
			Log.i("ROOT","PCKG name: "+packageName);
			final String logName = String.format("%s.install.log",packageName).replaceAll("\\s", "");;
			final String logPath = String.format("/data/data/%s/files/%s",context.getPackageName(),logName);
    		ExecuteAsRoot execAsRoot = new ExecuteAsRoot() {
				@Override
				protected ArrayList<String> getCommandsToExecute() {
					ArrayList<String> commands = new ArrayList<String>();
					commands.add(String.format("pm install %s -s 2> %s",packagePath,logPath));
					//TODO native util for change file owner (chown)
					commands.add(String.format("chmod -R 775 %s", logPath));
					return commands;
				}
				@Override
				protected void onPostExecuteRootCommands() {
					int result = checkPMLogFile(logName, logPath,"Failure"); 
					if(onInstall!=null)
						onInstall.onPostExecute(result);
				}
				@Override
				protected void onPreExecuteRootCommands() {
					if(onInstall!=null)
						onInstall.onPreExecute();
				}
			};
			execAsRoot.execute();
    	}
	}
	
	public void sqlite3Query(final String dbPath, final String query){
		if(query.length()>0 && dbPath.length()>0){
			String[] dbPathSplit = dbPath.split("/");
			final String dbName = dbPathSplit[dbPathSplit.length-1];
			final String logName = String.format("%s.dbquery.log",dbName).replaceAll("\\s", "");
			final String logPath = String.format("/data/data/%s/files/%s",context.getPackageName(),logName);
			ExecuteAsRoot execAsRoot = new ExecuteAsRoot() {
				@Override
				protected void onPreExecuteRootCommands() {
					if(onDBQuery!=null)
						onDBQuery.onPreExecute();
				}
				@Override
				protected void onPostExecuteRootCommands() {
					int result = checkPMLogFile(logName, logPath,"Error"); 
					if(onDBQuery!=null)
						onDBQuery.onPostExecute(result);
				}
				@Override
				protected ArrayList<String> getCommandsToExecute() {
					ArrayList<String> commands = new ArrayList<String>();
					commands.add(String.format("sqlite3 %s \"%s\" 2> %s",dbPath,query,logPath));
					//TODO native util for change file owner (chown)
					commands.add(String.format("chmod -R 775 %s", logPath));
					return commands;
				}
			};
			execAsRoot.execute();
		}
	}
}
