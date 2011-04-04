package org.andwellness.config.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.andwellness.config.xml.CampaignValidator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * CampaignValidator tester.
 * 
 * @author selsky
 */
public class TestConfigValidator {

	/**
	 * For a given directory (args[0]), attemps to validate all of the XML files present using the CampaignValidator and the 
	 * schema file provided as the second argument.
	 */
	public static void main(String[] args) throws IOException, ValidityException, ParsingException, SAXException {
		int totalFailures = 0;
		int totalSuccesses = 0;
		int totalFiles = 0;
		File directory = new File(args[0]);
		String[] files = directory.list();
		if(null == files) {
			throw new IllegalArgumentException("provided name does not represent a directory name: " + args[0]);
		} else {
			FilenameFilter filter = new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.endsWith(".xml");
			    }
			};
			files = directory.list(filter);
			totalFiles = files.length;
			CampaignValidator validator = new CampaignValidator();
			String currentFile = null;
			for (String fileName : files) {
				
				try { 
					currentFile = directory + "/" + fileName; 
					
					try {
						
						validator.run(currentFile, args[1]);
						
					} catch(SAXParseException saxe) {
						
						System.out.println("Parsing failed at line number " + saxe.getLineNumber() + " column number " + saxe.getColumnNumber());
						throw saxe;
						
					}
					
					System.out.println("successful test from file: " + currentFile);
					System.out.println(); 
					System.out.println();
					totalSuccesses++;
				}
				catch(Throwable t) { // lazy exception handling here that is almost always a bad idea
					                 // catching Throwable is particularly bad because the JVM can throw Errors that 
					                 // should not be swallowed. Here Throwable is caught because the JavaCC code 
					                 // throws Errors (when it should being throwing Exceptions)
					
					totalFailures++;
					System.out.println(currentFile + " failed test: " + t.getMessage());
					// lazy way to truncate the stack trace
					System.out.println(t.getStackTrace()[0]);
					System.out.println(t.getStackTrace()[1]);
					System.out.println(t.getStackTrace()[2]);
					System.out.println();
					System.out.println();
					
				}
			}
		}
		System.out.println("totalFiles: " + totalFiles);
		System.out.println("totalFailures: " + totalFailures);
		System.out.println("totalSuccesses: " + totalSuccesses);
	}
}
