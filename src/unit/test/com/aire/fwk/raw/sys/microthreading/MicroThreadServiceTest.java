package unit.test.com.aire.fwk.raw.sys.microthreading;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.aire.fwk.raw.sys.microthreading.MicroThreadService;
import com.aire.fwk.raw.sys.microthreading.interfaces.MicroThread;

/**
 * Test class. (Junit4) - allows simple testing of the class. Please bear in mind that for larger values (aka test size > 50,000)
 * delays will be needed to ensure that the loops are allowed to complete. At higher sizes (1,000,000+) dynamic allocation is
 * far slower than generic allocation. For objects that have very similar code, it is preferable to use generic allocation.
 * @author Tom
 *
 */
public class MicroThreadServiceTest 
{
	private MicroThreadService testServ = new MicroThreadService(10, false);
	private static final int testSize = 10000;
	
	@Test
	public void testService() throws InterruptedException
	{
		List<MicroThread> testList = new ArrayList<MicroThread>();
		for (int i = 0; i < testSize; i++)
		{
			testList.add(new TestMicroThread());
		}
		
		for(MicroThread t : testList)
		{
			testServ.addMicroThread(t);
		}
	
		
		synchronized(this)
		{
			this.wait(1000L);
		}
	
		
		int counter = 0;
		for (MicroThread t : this.testServ.getActiveThreads())
		{
			counter++;
			if (counter % 2 == 0)
			{
				this.testServ.removeThread(t);
			}
		}

		synchronized(this)
		{
			this.wait(4000L);
		}

		assertEquals(testSize/2, this.testServ.getActiveThreads().size());
		
		this.testServ.endService();
	}
	
	private class TestMicroThread implements MicroThread
	{
		private boolean finished = false;
		private int counter = 0;
		
		@Override
		public boolean isFinished() {
			return finished;
		}
		
		@Override
		public void doLogic()
		{
			counter++;
		}
		
		public int getCounter()
		{
			return counter;
		}

		@Override
		public void setFinished(boolean isFinished)
		{
			this.finished = isFinished;
		}
	}
}
