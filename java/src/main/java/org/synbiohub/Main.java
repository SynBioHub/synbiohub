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
							System.err.println("Executing job");
							job.execute();
							System.err.println("Executed!");
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
