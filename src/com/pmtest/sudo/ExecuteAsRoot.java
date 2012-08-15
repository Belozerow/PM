package com.pmtest.sudo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;

public abstract class ExecuteAsRoot {
	private static final int EXIT_CODE = 0;
	public static boolean canRunRootCommands(){
		boolean retVal = false;
		Process suProcess;
		
		try {
			suProcess = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
			DataInputStream osRes = new DataInputStream(suProcess.getInputStream());
			
			if(os != null && osRes != null){
				// Getting the id of the current user to check if this is root
				
				os.writeBytes("id\n");
				os.flush();
				
				String currUid = osRes.readLine();
				boolean exitSu = false;
				
				if(currUid == null){
					retVal = false;
					exitSu = false;
					Log.d("ROOT","Can't get root access or denied by user");
				}
				else if(currUid.contains("uid=0")){
					retVal = true;
					exitSu = true;
					Log.d("ROOT", "Root access granted");
				}
				else{
					retVal = false;
					exitSu = true;
					Log.d("ROOT","Root access rejected: " + currUid);
				}
				
				if(exitSu){
					os.writeBytes("exit\n");
					os.flush();
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			retVal = false;
			Log.d("ROOT", "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
		}
		
		
		
		return retVal;
	}
	public void execute(){
		ExecTask exTask = new ExecTask();
		exTask.execute();
	}
	private boolean _execute(){
		boolean retVal = false;
		try {
			ArrayList<String> commands = getCommandsToExecute();
			if(commands != null && commands.size() > 0){
				Process suProcess;
				suProcess = Runtime.getRuntime().exec("pmtestsu");
								
				DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
				
				// Execute commands that require root access
				for(String currCommand : commands){
					os.writeBytes(currCommand + "\n");
					Log.i("ROOT","Command : "+currCommand);
					os.flush();
				}
				
				os.writeBytes("exit\n");
				os.flush();
					
				try {
					int suProcessRetVal = suProcess.waitFor();
					
					if(suProcessRetVal == EXIT_CODE){
						//root access granted
						retVal = true;
					}
					else{
						//root access denied
						retVal = false;
					}
				} catch (InterruptedException e) {
					Log.e("Error executing root action", e.toString());
				}
			}
		} 
		catch (IOException ex){
			Log.w("ROOT", "Can't get root access", ex);
	    }
	    catch (SecurityException ex){
	    	Log.w("ROOT", "Can't get root access", ex);
	    }
	    catch (Exception ex){
	      Log.w("ROOT", "Error executing internal operation", ex);
	    }
		return retVal;
	}
	private class ExecTask extends AsyncTask<ArrayList<String>, Boolean, Boolean>{

		@Override
		protected Boolean doInBackground(ArrayList<String>... params) {
			boolean retVal = _execute(); 
			return retVal;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if(result){
				onPostExecuteRootCommands();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			onPreExecuteRootCommands();
		}
		
	}
	protected abstract ArrayList<String> getCommandsToExecute();
	protected abstract void onPostExecuteRootCommands();
	protected abstract void onPreExecuteRootCommands();
}
