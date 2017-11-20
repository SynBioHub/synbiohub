package org.synbiohub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main
{
	public static void main(String[] args)
	{
		ExecutorService executor = Executors.newCachedThreadPool();
		
		System.err.println("Starting main loop");
		
		while(true)
		{
			try
			{
				final Job job = ProcessBridge.awaitJob();

				if(job == null)
					break;
				
				System.err.println("Received job type " + job.getClass().getName());
				
				executor.submit(new Runnable() {
				
					public void run()
					{
						try {
							job.execute();
						} catch(Exception e) {
							job.finish(new ErrorResult(job, e));
						}
					}
					
				});			
			}
			catch (UnknownJobTypeException e)
			{
				continue;
			}
		}		

		System.err.println("Shutting down");
	}

}
