
package org.synbiohub;

import java.io.InputStreamReader;
import java.util.Scanner;

import com.google.gson.Gson;

public class ProcessBridge
{
	private static final Gson gson = new Gson();

	private static final String EOT = "\04";
	
	public static Job awaitJob() throws UnknownJobTypeException
	{
		InputStreamReader inputStreamReader = new InputStreamReader(System.in);

		Scanner scanner = new Scanner(inputStreamReader);
		scanner.useDelimiter(EOT);
		
		while (scanner.hasNext()) {
			
		    String json = scanner.next();
		    
		    return Job.fromJSON(json);		    
		    
		}
		
		scanner.close();
		
		return null;
	}
	
	public static void sendResult(Result result)
	{
		System.err.println("Sending result for job ID " + result.jobId);
		
		String resultSerialized = gson.toJson(result);
		
		synchronized(System.out)
		{
			System.out.print(resultSerialized);
			System.out.print(EOT);
		}
		
	}
}
