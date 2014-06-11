package analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OldTrace {
	
	private String filename;
	private ArrayList<String> content;
	private String privacyInformations;
	
	private static final String HTTP_BEGINNING_TOKEN = "Hypertext Transfer Protocol";
	private static final String FRAME_BEGINNING_TOKEN = "Frame ";

	public OldTrace(String filename) {
		this.filename = filename;
		this.content = new ArrayList<String>();
		this.privacyInformations = "";
	}
	
	public void readfile(){
        File file = new File(filename);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            // repeat until all lines is read
            while ((text = reader.readLine()) != null) {
                content.add(text);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	
	/**
	 * Keep only the source and destination IP and the HTTP content.
	 * @param content
	 */
	public void keepInterestingLines() {
		boolean httpContent = false;
		
        for (Iterator<String> i = content.iterator();i.hasNext();) {
        	String line = i.next();
        	
        	if (line.startsWith(HTTP_BEGINNING_TOKEN)) {
        		httpContent = true;
        	}
        	if (line.startsWith(FRAME_BEGINNING_TOKEN)) {
        		httpContent = false;
        	}
        	
        	if (!httpContent && !line.matches("^ {4}(Source|Destination): [0-9\\.() ]*$")) {
        		i.remove();
        	}
        }
		
	}

	public void analyze(){
        
        
        Pattern postGetPattern = Pattern.compile("^ {4}((POST|GET) (.*))$");
        Matcher postGetMatcher;
        
        Pattern hostPattern = Pattern.compile("^.*Host: (.*)$");
        Matcher hostMatcher;
        
        boolean privacyFound = false;
        
        int packageNo = 0;
        
        StringBuilder outputString = new StringBuilder();
        
        for (Iterator<String> i = content.iterator();i.hasNext();) {
        	String line = i.next();

            // Analyzing the line that begins with POST or GET
        	postGetMatcher = postGetPattern.matcher(line);
        	if (postGetMatcher.find()) {
        		if (privacyFound) {
        			privacyInformations += outputString;
        		}
            	privacyFound = false;
            	outputString.delete(0, outputString.length());
            	packageNo++;
            	outputString.append("\nPackage #" + packageNo + "\n");
//        		outputString += postGetMatcher.group(1) + "\n";

            	String postGetContent = postGetMatcher.group(3);
            	privacyFound = idFinder(outputString, postGetContent);
            	privacyFound = positionFinder(outputString, postGetContent);
        		
        	}
        	
        	//Showing the Host line
        	hostMatcher = hostPattern.matcher(line);
        	if (hostMatcher.find() && privacyFound) {
        		outputString.append("Host: " + hostMatcher.group(1) + "\n");
        	}
        	
        	
//        	outputString += line + "\n";
        }
		
	}
	
	public void print(){
		System.out.println(privacyInformations);
	}
	
	private boolean idFinder(StringBuilder outputString, String content) {
        Pattern labelIdFinderPattern = Pattern.compile("^.*?(\\?|&)(udid|isu|u|d|uuid|user|auid|vid|hid|id)=([^&]{5,}?)(&.*|)$");
        Matcher labelIdFinderMatcher;
        Pattern idFinderPattern = Pattern.compile("^.*(\\?|&)(.+)=(" + Constants.imei + "|"
        															  + Constants.androidID + "|"
        															  + Constants.androidIDmd5 + ").*$");
        Matcher idFinderMatcher;

        boolean privacyFound = false;
        String idFound = "";
        String labelFound = "";

		labelIdFinderMatcher = labelIdFinderPattern.matcher(content);
		if (labelIdFinderMatcher.find()) {
			idFound = labelIdFinderMatcher.group(3);
			labelFound = labelIdFinderMatcher.group(2);
			outputString.append(labelFound + ": " + idFound + "\n");
			privacyFound = true;
		}
		idFinderMatcher = idFinderPattern.matcher(content);
		if(idFinderMatcher.find()) {
			if (!idFound.equals(idFinderMatcher.group(3)) || !labelFound.equals(idFinderMatcher.group(2))) {
				idFound = idFinderMatcher.group(3);
				labelFound = idFinderMatcher.group(2);
				outputString.append(labelFound + ": " + idFound + "\n");
				privacyFound = true;
			}
		}
		return privacyFound;		
	}
	
	private boolean positionFinder(StringBuilder outputString, String content) {
        Pattern latLonLabelPattern = Pattern.compile("^.*?(lat|lon)=([^&]+)&(lon|lat)=([^&]+)(&.*|)$");
        Matcher latLonLabelMatcher;
        Pattern oneArgLabelLocalisationPattern = Pattern.compile("^.*?(ll|coord|GEOLOCATION)=([^&]+)(&.*|)$");
        Matcher oneArgLabelLocalisationMatcher;
        Pattern xyLocalisationPattern = Pattern.compile("^.*?(x|y)=([^&]+)&(y|x)=([^&]+)(&.*|)$");
        Matcher xyLocalisationMatcher;
        Pattern latLonPattern = Pattern.compile("^.*(\\?|&)(.+)=(" + Constants.latValues + "|" + Constants.lonValues + ")"
				  							  	+ "&(.+)=(" + Constants.lonValues + "|" + Constants.latValues + ").*$");
        Matcher latLonMatcher;
        Pattern oneArgLocalisationPattern = Pattern.compile("^.*(\\?|&)(.+)=((" + Constants.latValues + "|" + Constants.lonValues + ")"
				  											+ "([^&]*)(" + Constants.lonValues + "|" + Constants.latValues + ")).*$");;
        Matcher oneArgLocalisationMatcher;

        boolean privacyFound = false;
        String latLonFound1 = "";
        String latLonFound2 = "";
        String latLonLabelFound1 = "";
        String latLonLabelFound2 = "";
        String oneArgFound = "";
        String oneArgLabelFound = "";

		latLonMatcher = latLonPattern.matcher(content);
		oneArgLocalisationMatcher = oneArgLocalisationPattern.matcher(content);
		latLonLabelMatcher = latLonLabelPattern.matcher(content);
		xyLocalisationMatcher = xyLocalisationPattern.matcher(content);
		oneArgLabelLocalisationMatcher = oneArgLabelLocalisationPattern.matcher(content);
		
		if (latLonMatcher.find()) {

			if (!(latLonFound1.equals(latLonMatcher.group(3)) || latLonFound1.equals(latLonMatcher.group(5)))
			 || !(latLonFound2.equals(latLonMatcher.group(3)) || latLonFound2.equals(latLonMatcher.group(5)))
			 || !(latLonLabelFound1.equals(latLonMatcher.group(2)) || latLonLabelFound1.equals(latLonMatcher.group(4)))
			 || !(latLonLabelFound2.equals(latLonMatcher.group(2)) || latLonLabelFound2.equals(latLonMatcher.group(4)))) {

		        latLonFound1 = latLonMatcher.group(3);
		        latLonFound2 = latLonMatcher.group(5);
		        latLonLabelFound1 = latLonMatcher.group(2);
		        latLonLabelFound2 = latLonMatcher.group(4);

				outputString.append("Localisation:\n");
				outputString.append("    " + latLonLabelFound1 + ": " + latLonFound1 + "\n");
				outputString.append("    " + latLonLabelFound2 + ": " + latLonFound2 + "\n");
				privacyFound = true;
			}
		} else if (oneArgLocalisationMatcher.find()) {

				oneArgFound = oneArgLocalisationMatcher.group(3);
				oneArgLabelFound = oneArgLocalisationMatcher.group(2);

				outputString.append("Localisation:\n");
				outputString.append("    label: " + oneArgLabelFound + "\n");
				outputString.append("    1st coord: " + oneArgLocalisationMatcher.group(4) + "\n");
				outputString.append("    between: " + oneArgLocalisationMatcher.group(5) + "\n");
				outputString.append("    2nd coord: " + oneArgLocalisationMatcher.group(6) + "\n");
				privacyFound = true;	
		}
		if (latLonLabelMatcher.find()) {

			if (!(latLonFound1.equals(latLonLabelMatcher.group(2)) || latLonFound1.equals(latLonLabelMatcher.group(4)))
			 || !(latLonFound2.equals(latLonLabelMatcher.group(2)) || latLonFound2.equals(latLonLabelMatcher.group(4)))
			 || !(latLonLabelFound1.equals(latLonLabelMatcher.group(1)) || latLonLabelFound1.equals(latLonLabelMatcher.group(3)))
			 || !(latLonLabelFound2.equals(latLonLabelMatcher.group(1)) || latLonLabelFound2.equals(latLonLabelMatcher.group(3)))) {

				latLonFound1 = latLonLabelMatcher.group(2);
				latLonFound2 = latLonLabelMatcher.group(4);
				latLonLabelFound1 = latLonLabelMatcher.group(1);
				latLonLabelFound2 = latLonLabelMatcher.group(3);

				outputString.append("Localisation:\n");
				outputString.append("    " + latLonLabelFound1 + ": " + latLonFound1 + "\n");
				outputString.append("    " + latLonLabelFound2 + ": " + latLonFound2 + "\n");
				privacyFound = true;
			}
		} else if (xyLocalisationMatcher.find()) {

			if (!(latLonFound1.equals(xyLocalisationMatcher.group(2)) || latLonFound1.equals(xyLocalisationMatcher.group(4)))
			 || !(latLonFound2.equals(xyLocalisationMatcher.group(2)) || latLonFound2.equals(xyLocalisationMatcher.group(4)))
			 || !(latLonLabelFound1.equals(xyLocalisationMatcher.group(1)) || latLonLabelFound1.equals(xyLocalisationMatcher.group(3)))
			 || !(latLonLabelFound2.equals(xyLocalisationMatcher.group(1)) || latLonLabelFound2.equals(xyLocalisationMatcher.group(3)))) {

				latLonFound1 = xyLocalisationMatcher.group(2);
				latLonFound2 = xyLocalisationMatcher.group(4);
				latLonLabelFound1 = xyLocalisationMatcher.group(1);
				latLonLabelFound2 = xyLocalisationMatcher.group(3);

				outputString.append("Localisation:\n");
				outputString.append("    " + latLonLabelFound1 + ": " + latLonFound1 + "\n");
				outputString.append("    " + latLonLabelFound2 + ": " + latLonFound2 + "\n");
				privacyFound = true;
			}
		} else if (oneArgLabelLocalisationMatcher.find()) {

			if (!oneArgFound.equals(oneArgLabelLocalisationMatcher.group(2)) || !oneArgLabelFound.equals(oneArgLabelLocalisationMatcher.group(1))) {

				oneArgFound = oneArgLabelLocalisationMatcher.group(2);
				oneArgLabelFound = oneArgLabelLocalisationMatcher.group(1);

				outputString.append(oneArgLabelFound + ": " + oneArgFound
						+ "\n");
				privacyFound = true;
			}
		}
		return privacyFound;		
	}
}
