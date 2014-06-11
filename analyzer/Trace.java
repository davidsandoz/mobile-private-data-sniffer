package analyzer;

import gui.ResultWindow;
import gui.WaitingCaptureWindow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import org.apache.commons.csv.CSVParser;

public class Trace extends Thread {
	
	private String filename;
	private int choice;
	private ArrayList<Request> content;
	private String privacyInformations;
	private WaitingCaptureWindow waitingWindow;

	static final String TRACES_DIRECTORY = "traces";
    
    enum RequestType { GET, POST, RESPONSE, UNKNOWN };
	
	public Trace() {
		content = new ArrayList<Request>();
		privacyInformations = "";
		makeChoice();
		getFile();
	}
	
	public Trace(int newChoice, String newFilename, String givenRootPassword, WaitingCaptureWindow theWaitingWindow) {
		content = new ArrayList<Request>();
		privacyInformations = "";
		choice = newChoice;
		filename = newFilename;
		savePassword(givenRootPassword);
		
		waitingWindow = theWaitingWindow;
	}
	
	public void run() {
		getFile();
		analyze();
		deletePassword();
		printInNewWindow();
		waitingWindow.setVisible(false);
	}
	
	public void savePassword(String password) {
		File psswrd = new File(Constants.PASSWORD_FILENAME);
		try {
			psswrd.createNewFile();
			FileWriter fstream = new FileWriter(Constants.PASSWORD_FILENAME);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(password + "\n");
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private boolean passwordExists() {
		File psswrd = new File(Constants.PASSWORD_FILENAME);
		return psswrd.exists();
	}
	
	private void deletePassword() {
		File psswrd = new File(Constants.PASSWORD_FILENAME);
		psswrd.delete();
	}
	
	private void makeChoice() {
		Scanner in = new Scanner(System.in);
		choice = 0;
		do {
			System.out.println("What do you want to do?");
			System.out.println("    1. Do a new capture");
			System.out.println("    2. Read an existing file");
			choice = in.nextInt();
		} while (choice != 1 && choice != 2);

		Scanner in2 = new Scanner(System.in);
		System.out.print("Enter the trace (csv file) filename (without the extension): ");
		this.filename = in2.nextLine();
	}
	
	private void getFile() {
		if (choice == 1) {
			waitingWindow.setLabel("Currently capturing for " + Constants.duration + "s and parsing packets");
			waitingWindow.startProgressBar();
			capture();
		} else if (choice == 2) {
			//Just need to read the file.
		}
		waitingWindow.setLabel("Reading and analyzing CSV file");
		readfile();
	}
	
	private void capture() {
		if (!passwordExists()) {
			Scanner in = new Scanner(System.in);
			System.out.print("Enter the root password: ");
			String password = in.nextLine();	
			savePassword(password);
		}
		
		try {
			System.out.println("Starting capture...");
			String[] command = new String[5];
			command[0] = "./capture_pcap.sh";
			command[1] = Constants.networkInterface;
			command[2] = Constants.channel;
			command[3] = Constants.duration;
			command[4] = filename;
			Process capture = Runtime.getRuntime().exec(command);
			capture.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(capture.getInputStream()));
			String line ="";
			while ((line=buf.readLine())!=null) {
				System.out.println(line);
			}
			System.out.println("Capture done.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Deleting the PCAP file.
		new File("traces/" + filename + ".pcap").delete();
	}
	
	private void readfile(){
        File file = new File(TRACES_DIRECTORY + "/" + filename + ".csv");
        BufferedReader reader = null;
        try {
            int index = -1;
            String source = "";
            String destination = "";
            String load = "";
            
            reader = new BufferedReader(new FileReader(file));
            CSVParser csvParser = new CSVParser(reader);
            String[][] allTheValues = csvParser.getAllValues();
            
            for (int line = 1; line < allTheValues.length; line++) {
            	index = Integer.parseInt(allTheValues[line][0]);
            	source = allTheValues[line][1];
            	destination = allTheValues[line][2];
            	load = allTheValues[line][3];
            	content.add(new Request(index, source, destination, load));            	
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

	public void analyze(){ 
        
        Pattern hostPattern = Pattern.compile("^.*Host: (.*).*$", Pattern.MULTILINE);
        Matcher hostMatcher;
        
        RequestType currentRequestType = null;
        boolean isFirstPacket = false;
        boolean record = false;
        boolean idFound = false;
        boolean localisationFound = false;
        
        StringBuilder outputString = new StringBuilder();
        Request request = null;
        String load = "";
        int positivePacketsCounter = 0;
        
        for (Iterator<Request> i = content.iterator();i.hasNext();) {
        	request = i.next();

        	load = request.getLoad();
        	currentRequestType = defineRequestType(load);
        	isFirstPacket = isFirstPacket(currentRequestType);
        	if (isFirstPacket) {
            	if (idFound || localisationFound) {
            		//Save the content of last "exchange".
            		privacyInformations += outputString;
            		positivePacketsCounter++;
            	}
            	
            	record = true;
            	idFound = false;
            	localisationFound = false;
            	outputString.delete(0, outputString.length());
            	outputString.append("\nFrame #" + (request.getIndex() - 1) + "\n");
        	}

        	if (currentRequestType == RequestType.RESPONSE) {
        		record = false; //Do not record if it is a response.
            	idFound = false;
            	localisationFound = false;
            	outputString.delete(0, outputString.length());
        	}

        	if (record) {
        		//Showing the Host line
        		hostMatcher = hostPattern.matcher(load);
        		if (hostMatcher.find()) {
        			outputString.append("    Destination: " + request.getDestination() + "\n");
        			outputString.append("    Host: " + hostMatcher.group(1) + "\n");
        		}
        		
        		/* Si quelquechose a été trouvé dans un paquet précédent qui fait parti de la même série de paquet,
        		 * on ne redéfini pas idFound
        		 */
        		if (idFound) {
        			idFinder(outputString, load);
        		} else { // Si par contre rien n'a encore été trouvé, on redéfini idFound au cas où on trouve qqch.
        			idFound = idFinder(outputString, load);
        		}
        		/* Idem pour location */
        		if (localisationFound) {
        			positionFinder(outputString, load);
        		} else {
        			localisationFound = positionFinder(outputString, load);
        		}
        	}
        	
        }
    	if (idFound || localisationFound) {
    		//Save the content of last "exchange".
    		privacyInformations += outputString;
    		positivePacketsCounter++;
    	}
    	outputString.delete(0, outputString.length());
        outputString.append("\n\n" + positivePacketsCounter + " packets with supposed private data found.");
        privacyInformations += outputString;
	}
	
	private RequestType defineRequestType(String load) {
		if (load.startsWith("POST")) {
			return RequestType.POST;
		} else if (load.startsWith("GET")) {
			return RequestType.GET;
		} else if (load.startsWith("HTTP")) {
			return RequestType.RESPONSE;
		} else {
			return RequestType.UNKNOWN;
		}
	}
	
	private boolean isFirstPacket(RequestType currentRequestType) {
		if (currentRequestType == RequestType.UNKNOWN) {
			return false;
		} else {
			return true;
		}
	}
	
	public void print(){
		System.out.println(privacyInformations);
	}
	
	public void printInNewWindow(){
		ResultWindow rw = new ResultWindow(privacyInformations);
		rw.setVisible(true);
	}
	
	private boolean idFinder(StringBuilder outputString, String content) {
        Pattern labelIdFinderPattern = Pattern.compile("^(.*?(?:&|\\?|\\s)|)(udid|isu|uuid|auid|id|nid)(?:=|: )([0-9a-zA-Z]{5,}?)((\"|&).*|)$", Pattern.MULTILINE);
        Matcher labelIdFinderMatcher;
        Pattern idFinderPattern = Pattern.compile("^(.*(?:&|\\?|\\s)|)(.+)(?:=|: )((?i)" + Constants.imei
        																 + "|(?i)" + Constants.androidID
        																 + "|(?i)" + Constants.androidIDmd5 + ").*$",
        																 Pattern.MULTILINE);
        Matcher idFinderMatcher;

        boolean privacyFound = false;
        String idFound = "";
        String labelFound = "";

		labelIdFinderMatcher = labelIdFinderPattern.matcher(content.toLowerCase());
		if (labelIdFinderMatcher.find()) {
			idFound = labelIdFinderMatcher.group(3);
			labelFound = labelIdFinderMatcher.group(2);
			outputString.append("    " + labelFound + ": " + idFound + "\n");
			privacyFound = true;
		}
		idFinderMatcher = idFinderPattern.matcher(content.toLowerCase());
		if(idFinderMatcher.find()) {
			if (!idFound.equals(idFinderMatcher.group(3)) || !labelFound.equals(idFinderMatcher.group(2))) {
				idFound = idFinderMatcher.group(3);
				labelFound = idFinderMatcher.group(2);
				outputString.append("    " + labelFound + ": " + idFound + "\n");
				privacyFound = true;
			}
		}
		return privacyFound;		
	}
	
	private boolean positionFinder(StringBuilder outputString, String content) {
        Pattern latLonLabelPattern = Pattern.compile("^(?:.*?(?:&|\\?)|)(lat|lon)=([^&]+)&(lon|lat)=([^(?:&|\\n)]+)(&.*|)$", Pattern.MULTILINE);
        Matcher latLonLabelMatcher;
        Pattern oneArgLabelLocalisationPattern = Pattern.compile("^(?:.*?(?:&|\\?)|)(ll|coord|GEOLOCATION)=([^(?:&|\\n)]+)(&.*|)$", Pattern.MULTILINE);
        Matcher oneArgLabelLocalisationMatcher;
        Pattern xyLocalisationPattern = Pattern.compile("^(?:.*?(?:&|\\?)|)(x|y)=([^&]+)&(y|x)=([^(?:&|\\n)]+)(&.*|)$", Pattern.MULTILINE);
        Matcher xyLocalisationMatcher;
        Pattern latLonPattern = Pattern.compile("^(.*(?:&|\\?)|)(.+)=(" + Constants.latValues + "|" + Constants.lonValues + ")"
				  							  	+ "&(.+)=(" + Constants.lonValues + "|" + Constants.latValues + ").*$", Pattern.MULTILINE);
        Matcher latLonMatcher;
        Pattern oneArgLocalisationPattern = Pattern.compile("^(.*(?:&|\\?|\\s)|)(.+)((" + Constants.latValues + "|" + Constants.lonValues + ")"
				  											+ "([^(&|\\n)]*)(" + Constants.lonValues + "|" + Constants.latValues + ")).*$", Pattern.MULTILINE);;
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

				outputString.append("    Localisation:\n");
				outputString.append("\t" + latLonLabelFound1 + ": " + latLonFound1 + "\n");
				outputString.append("\t" + latLonLabelFound2 + ": " + latLonFound2 + "\n");
				privacyFound = true;
			}
		} else if (oneArgLocalisationMatcher.find()) {

				oneArgFound = oneArgLocalisationMatcher.group(3);
				oneArgLabelFound = oneArgLocalisationMatcher.group(2);

				outputString.append("    Localisation:\n");
				outputString.append("\tlabel: " + oneArgLabelFound + "\n");
				outputString.append("\t1st coord: " + oneArgLocalisationMatcher.group(4) + "\n");
				outputString.append("\tbetween: " + oneArgLocalisationMatcher.group(5) + "\n");
				outputString.append("\t2nd coord: " + oneArgLocalisationMatcher.group(6) + "\n");
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
	
	public class BackgroundWorker extends SwingWorker<Void, Void> {
		   private WaitingCaptureWindow dialog;
		   private long sleepTime;
		    
		   public BackgroundWorker(WaitingCaptureWindow dialog, long sleepTime) {
		      this.dialog = dialog;
		      this.sleepTime = sleepTime;
		   }
		 
		   @Override
		   protected Void doInBackground() throws Exception {
		      Thread.sleep(sleepTime);
		      return null;
		   }
		    
		   @Override
		   protected void done() {
		      dialog.setVisible(false);
		   }
		    
	}
}
