package StaticReachabilityAnalysis;
/*
 * NetworkDataDump.java
 *
 */
import java.util.*;
import java.io.*;
class NetworkDataDump {
	/** Creates a new instance of NetworkDataDump */
	NetworkDataDump(NetworkConfig network, File outputDir) {
		try {
			DeleteFiles (outputDir);
			String outputFileName = "Network Data.txt";
			File outputFile = new File (outputDir, outputFileName);
			FileWriter outFile = new FileWriter (outputFile);
			// Data to be written to file
			outFile.write("Network ID : " + network.networkName + "\r\n\r\n");
			// test output to screen
			System.out.println("Network ID : " + network.networkName);
			// Write Router Data
			outFile.write("Router List : \r\n");
			System.out.println("Router List : "); // test output to screen
			int routerCounter=0;
			RouterConfig currentRouter;
			Enumeration routerList = network.tableOfRouters.elements();
			while ( routerList.hasMoreElements() ) {
				currentRouter = (RouterConfig) routerList.nextElement();
				routerCounter++;
				outFile.write("(" + routerCounter + ") " +
						currentRouter.hostName + "\r\n");
				System.out.println("(" + routerCounter + ") " +
						currentRouter.hostName); // test output to screen
				RouterConfig.Debug(currentRouter, outputDir);
				//currentRouter.Debug(currentRouter, outputDir);
			}
			//Close the file written to
			outFile.close();
		} catch (Exception e) { System.out.println ("Error - " + e); }
	}
	
	
	boolean DeleteFiles(File directory) {
		boolean success = false;
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (int i=0; i<files.length; i++) {
				if (files[i].isDirectory()) {
					success = DeleteFiles(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		return success;
	}
}