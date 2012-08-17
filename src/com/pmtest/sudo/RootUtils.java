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
	private OnRootActionListener onUninstall;
	private OnRootActionListener onInstall;
	private OnRootActionListener onDBQuery;
	private OnRootActionListener onKill;
	private OnRootActionListener onFileChange;
	public final static int RESULT_SUCCESS = 1;
	public final static int RESULT_FAIL = 2;
	public RootUtils(Context context){
		this.context = context;
	}
	public void setOnUninstall(OnRootActionListener onUninstall){
		this.onUninstall = onUninstall;
	}
	public void setOnInstall(OnRootActionListener onInstall){
		this.onInstall = onInstall;
	}
	public void setOnDBQuery(OnRootActionListener onDBQuery){
		this.onDBQuery = onDBQuery;
	}
	public void setOnKill(OnRootActionListener onKill){
		this.onKill = onKill;
	}
	public void setOnFileChange(OnRootActionListener onKill){
		this.onKill = onKill;
	}
	public interface OnRootActionListener{
		public void onPreExecute();
		public void onPostExecute(int result);
	};
	private String getDirectory(){
		return String.format("/data/data/%s/files", context.getPackageName());
	}
	private String getLogName(String val, String action){
		String[] pathSplit = val.split("/"); 
		final String name = pathSplit[pathSplit.length-1];
		return String.format("%s.%s.log",name,action).replaceAll("\\s", "");
	}
	private String getLogPath(String logName){
		return String.format("%s/%s",getDirectory(),logName);
	}
	private int checkPMLogFile(final String logName, final String logPath){
		return checkPMLogFile(logName, logPath, null);
	}
	/**
	 * All comands output their errors to logName file. 
	 * This function checks log file. If it's empty than command was successfully executed.
	 * But command pm has no error output, it always print "Failure" or "Success" to standard output.
	 * So if containsError != null then log file checks for containsError.  
	 * @param logName
	 * @param logPath
	 * @param containsError
	 * @return
	 */
	private int checkPMLogFile(String logName, final String logPath, final String containsError){
		int retVal = RESULT_SUCCESS;
		File filesDir = context.getFilesDir();
		File[] files = filesDir.listFiles();
		for(File file : files){
			if(logName.equals(file.getName())){
				try {
					//read log file
					Scanner fScanner = new Scanner(file);
					while(fScanner.hasNext()){
						//pm has no error output. it writes Failure or Success to standard output.
						if(containsError != null){
							if(fScanner.next().contains(containsError)){
								retVal = RESULT_FAIL;
								Log.w("ROOT",logName + " : " + containsError);
							}
						}
						else{
							retVal = RESULT_FAIL;
							Log.w("ROOT", logName + " : " + fScanner.next());
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
			final String logName = getLogName(packageName,"uninstall");
			final String logPath = getLogPath(logName);
    		ExecuteAsRoot execAsRoot = new ExecuteAsRoot(){
				@Override
				protected ArrayList<String> getCommandsToExecute() {
					ArrayList<String> commands = new ArrayList<String>();
					commands.add(String.format("pm uninstall %s > %s",packageName,logPath));
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
			final String logName = getLogName(packagePath, "install");
			final String logPath = getLogPath(logName);
    		ExecuteAsRoot execAsRoot = new ExecuteAsRoot() {
				@Override
				protected ArrayList<String> getCommandsToExecute() {
					ArrayList<String> commands = new ArrayList<String>();
					commands.add(String.format("pm install %s 2> %s",packagePath,logPath));
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
			
			final String logName = getLogName(dbPath, "dbquery");
			final String logPath = getLogPath(logName);
			ExecuteAsRoot execAsRoot = new ExecuteAsRoot() {
				@Override
				protected void onPreExecuteRootCommands() {
					if(onDBQuery!=null)
						onDBQuery.onPreExecute();
				}
				@Override
				protected void onPostExecuteRootCommands() {
					int result = checkPMLogFile(logName, logPath); 
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
	public void killProcess(final int pid, final int signal){
		final String logName = getLogName(Integer.toString(pid), "kill");
		final String logPath = getLogPath(logName);
		ExecuteAsRoot execAsRoot = new ExecuteAsRoot() {
			@Override
			protected void onPreExecuteRootCommands() {
				if(onKill!=null)
					onKill.onPreExecute();
			}
			@Override
			protected void onPostExecuteRootCommands() {
				int result = checkPMLogFile(logName, logPath); 
				if(onKill!=null)
					onKill.onPostExecute(result);
			}
			@Override
			protected ArrayList<String> getCommandsToExecute() {
				ArrayList<String> commands = new ArrayList<String>();
				commands.add(String.format("kill -%d %d 2> %s",signal,pid,logPath));
				//TODO native util for change file owner (chown)
				commands.add(String.format("chmod -R 775 %s", logPath));
				return commands;
			}
		};
		execAsRoot.execute();
	}
	public void changeFile(final String filePath, final Integer uid, final Integer gid, final String chmod, final String content){
//		final String logName = getLogName(filePath,"change");
//		final String logPath = getLogPath(logName);
//		ExecuteAsRoot execAsRoot = new ExecuteAsRoot() {
//			@Override
//			protected void onPreExecuteRootCommands() {
//			}
//			
//			@Override
//			protected void onPostExecuteRootCommands() {
//			}
//			
//			@Override
//			protected ArrayList<String> getCommandsToExecute() {
//				return null;
//			}
//		};
	}
	public void changeFile(String filePath, String content){
		
	}
}
