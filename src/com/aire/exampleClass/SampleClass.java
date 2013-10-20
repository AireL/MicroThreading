package com.aire.exampleClass;

import com.aire.fwk.raw.sys.microthreading.interfaces.MicroThread;

/**
 * Sample class:
 * 
 * This class merely calculates the next fibonnaci sequence number, but shows how the doLogic() method works
 * @author Tom
 *
 */
public class SampleClass implements MicroThread
{
	boolean isFinished = false;
	long alpha = 1;
	long beta = 0;
	
	
	@Override
	public void doLogic()
	{
		long temp = alpha;
		alpha = alpha + beta;
		beta = temp;
	}

	@Override
	public boolean isFinished() {
		return isFinished;
	}

	@Override
	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

}
