package com.pmtest.sudo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;

public abstract class ExecuteAsRoot {
	private static final int EXIT_CODE = 0;
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
