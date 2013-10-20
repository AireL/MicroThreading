package com.aire.fwk.raw.sys.microthreading.interfaces;

import java.util.UUID;

/**
 * Micro Threading interface.
 * 
 * The interface implements two methods, which should be completed for each microthread to run properly.
 * 
 * @author Aire
 */
public interface MicroThread 
{
	
	/**
	 * Worker thread. This thread implements the logic behind the implementing class. This thread should not loop, as
	 * until the isFinished method returns true, this method will be called repeatedly.
	 */
	public void doLogic();
	
	/**
	 * Returns true when the worker thread has finished operation. Else returns false
	 * @return	The state of the worker thread
	 */
	public boolean isFinished();
	
	/**
	 * Sets the state of the worker thread
	 * @return	The state of the worker thread
	 */
	public void setFinished(boolean isFinished);
	
}	// End interface MicroThread
