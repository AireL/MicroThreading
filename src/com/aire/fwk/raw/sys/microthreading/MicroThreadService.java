package com.aire.fwk.raw.sys.microthreading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.aire.fwk.raw.sys.microthreading.interfaces.MicroThread;

/**
 * Micro-Threading service. This service implements an instance of the micro-threading service.<br><br>
 * 
 * The service can be set up to use a thread pool of a set size, or a single thread. The service
 * will then run every attached micro-thread in the order they were added, repeatedly. When
 * a micro-thread returns false on its 'isFinished' method, the thread will be removed.<br><br>
 * 
 * Conceptually, the idea is to allow a simpler version of threading that can split a load of multiple
 * objects that require use of a thread over a single thread, to enable better load balancing.<br><br>
 * 
 * Note, that a standard thread pool will run in a more optimised fashion for normal heavy thread use.<br>
 * 
 * @author Tom
 */
public class MicroThreadService 
{
	private final List<MicroWorkerThread> poolQueue;
	private static final int KEEP_ALIVE_TICKS = 100000;
	private final boolean dynamicAllocation;
	
	private int threadMax = 1;
	private boolean shutDown = false;
	
	/**
	 * Standard initialise of the micro-threading service. Does not pool threads
	 */
	public MicroThreadService()
	{
		poolQueue = new ArrayList<MicroWorkerThread>(1);
		this.dynamicAllocation = false;
	}
	
	/**
	 * Instantiates a thread pooling version of the micro-threading system.
	 * 
	 * @param poolSize				Base pool size.
	 * @param dynamicAllocation		Whether to allocate the resources dynamically
	 */
	public MicroThreadService(int poolSize, boolean dynamicAllocation)
	{
		this.dynamicAllocation = dynamicAllocation;
		this.threadMax = poolSize;
		poolQueue = new ArrayList<MicroWorkerThread>(poolSize);
	}
	
	/**
	 * Adds a thread to the micro-threading service
	 * @param thread
	 */
	public void addMicroThread(MicroThread thread)
	{
		if (this.shutDown)
		{
			throw new IllegalStateException("Error: Micro-Threading service shutting down. Cannot add new threads");
		}
		if(this.poolQueue.size() < threadMax)
		{
			MicroWorkerThread workerThread = new MicroWorkerThread(this);
			poolQueue.add(workerThread);
			workerThread.addThread(thread);
			workerThread.start();
		}
		else
		{
			if (dynamicAllocation)
			{
				long opTime = (this.poolQueue.get(0).runTime + 
						((this.poolQueue.get(0).runTime * this.poolQueue.get(0).addThread.size()) / 
								(this.poolQueue.get(0).threadList.size() + 1)));
				MicroWorkerThread t = this.poolQueue.get(0);
				for (int i = 1; i < this.poolQueue.size(); i++)
				{
					long runTime = this.poolQueue.get(i).runTime;
					if (opTime > (runTime + ((runTime * this.poolQueue.get(i).addThread.size()) / 
							(this.poolQueue.get(i).threadList.size() + 1))))
					{
						t = this.poolQueue.get(i);
						opTime = (runTime + ((runTime * this.poolQueue.get(i).addThread.size()) /
								(this.poolQueue.get(i).threadList.size() + 1)));
					}
				}
				t.addThread(thread);
			}
			else
			{
				MicroWorkerThread t = this.poolQueue.get(0);
				for (int i = 1; i < this.poolQueue.size(); i++)
				{
					if ((t.threadList.size() + t.addThread.size()) > 
						(this.poolQueue.get(i).threadList.size() + this.poolQueue.get(i).addThread.size()))
					{
						t = this.poolQueue.get(i);
					}
				}
				t.addThread(thread);
			}
		}
	}
	
	/**
	 * Removes a thread from the micro-thread service
	 * @param remove	The thread to remove.
	 */
	public void removeThread(MicroThread remove)
	{
		remove.setFinished(true);
	}
	
	/**
	 * Returns a list of currently active micro-threads in this micro-thread service
	 * @return
	 */
	public List<MicroThread> getActiveThreads()
	{
		List<MicroThread> returnVal = new ArrayList<MicroThread>();
		for (MicroWorkerThread t : this.poolQueue)
		{
			returnVal.addAll(t.threadList);
		}
		return returnVal;
	}
	
	
	/**
	 * Attempts to shut down all of the current threads by setting the workers to end.
	 * Note this will not clean the class, but will stop any future micro-threads
	 * from being added, throwing an Illegal State Exception at any attempts to add.
	 */
	public void endService()
	{
		this.shutDown = true;
		for (MicroWorkerThread t: this.poolQueue)
		{
			t.endWorker();
		}
	}
	
	/**
	 * Worker thread to be managed by the Micro-Threading service.
	 * @author Tom
	 */
	private class MicroWorkerThread extends Thread
	{
		private boolean cancelThread = false;
		
		private final List<MicroThread> threadList = new LinkedList<MicroThread>();
		private volatile List<MicroThread> addThread = Collections.synchronizedList(new LinkedList<MicroThread>());
		
		private MicroThreadService parent;
		private int keepAliveTicks = 0;
		private long runTime;
		
		public MicroWorkerThread(MicroThreadService parentThread)
		{
			this.parent = parentThread;
		}
		
		/**
		 * Runs every micro-thread attached to this worker thread. Checks if the micro-thread
		 * has been completed, then ends it (if necessary);
		 */
		@Override
		public void run() 
		{
			while(!cancelThread)
			{
				// For use with dynamic allocation if set to true
				if (parent.dynamicAllocation)
				{
					runTime = System.currentTimeMillis();
				}
				// Runs the business logic
				for (int i = 0; i < this.threadList.size(); i++)
				{
					MicroThread t = this.threadList.get(i);
					if (!t.isFinished())
					{
						t.doLogic();
					}
					else
					{
						this.threadList.remove(i);
						i--;
					}
				}
				
				// For use with dynamic allocation if set to true
				if (parent.dynamicAllocation)
				{
					runTime = (System.currentTimeMillis() - runTime);
				}
				
				// HouseKeeping:				
				
				// Adds new threads to the list
				for(int count = 0; count < addThread.size(); count++)
				{
					threadList.add(addThread.remove(0));
				}

				
				// Manages empty threads
				if (this.threadList.size() < 1)
				{
					keepAliveTicks++;
					if (keepAliveTicks == MicroThreadService.KEEP_ALIVE_TICKS)
					{
						this.cancelThread = true;
					}
				}
				else
				{
					if(this.keepAliveTicks > 0)
					{
						this.keepAliveTicks = 0;
					}
				}
			}
			this.parent.poolQueue.remove(this);
		}
		
		/**
		 * @param add	The micro-thread to add to this worker thread
		 */
		public void addThread(MicroThread add)
		{
			addThread.add(add);	
		}
		
		/**
		 * Ends the thread
		 */
		public void endWorker()
		{
			this.cancelThread = true;
		}
	}	// End MicroWorkerThread class
}
