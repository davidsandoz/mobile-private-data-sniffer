package analyzer;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Constants {
	
	static final String CONFIG_FILENAME = "config.xml";

	public static String duration;
	static String networkInterface;
	static String channel;

	static String imei;
	static String androidID;
	static String androidIDmd5;

	static String latitude;
	static String latitudeMin;
	static String latitudeMax;
	static String latValues;

	static String longitude;
	static String longitudeMin;
	static String longitudeMax;
	static String lonValues;
	
	public static final String PASSWORD_FILENAME = ".mypass";
	
	public static void initializeConstantValues() {
		try {
			File file = new File(CONFIG_FILENAME);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			// System.out.println("Root element " +
			// doc.getDocumentElement().getNodeName());
			NodeList nodeLst = doc.getElementsByTagName("config");
			System.out.println("* Config informations *");

			char latBounds = '[';
			char lonBounds = '[';
			

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);

				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;

					duration = getContent(fstElmnt, "duration");
					networkInterface = getContent(fstElmnt, "interface");
					channel = getContent(fstElmnt, "channel");

					imei = getContent(fstElmnt, "IMEI");
					androidID = getContent(fstElmnt, "androidID");
					androidIDmd5 = AeSimpleMD5.MD5(androidID);

					latitude = getContent(fstElmnt, "latitude");
					latitudeMin = getBelowValue(latitude);
					latitudeMax = getAboveValue(latitude);
					if (!latitude.startsWith("-")) {
						latValues = Constants.latitudeMin + "[0-9]*|"
				 		  		  + Constants.latitude + "[0-9]*"; //We do not put the max in purpose
					} else {
						latValues = Constants.latitude + "[0-9]*|"
		 		  		  		  + Constants.latitudeMax + "[0-9]*"; //We do not put the min in purpose
						latBounds = ']';
					}

					longitude = getContent(fstElmnt, "longitude");
					longitudeMin = getBelowValue(longitude);
					longitudeMax = getAboveValue(longitude);
					if (!longitude.startsWith("-")) {
						lonValues = Constants.longitudeMin + "[0-9]*|"
				 		  		  + Constants.longitude + "[0-9]*"; //We do not put the max in purpose
					} else {
						lonValues = Constants.longitude + "[0-9]*|"
				 		  		  + Constants.longitudeMax + "[0-9]*"; //We do not put the min in purpose
						lonBounds = ']';
					}

					System.out.println("Capture duration: " + duration + "s");
					System.out.println("Capture interface: " + networkInterface);
					System.out.println("Capture channel: " + channel);
					System.out.println("IMEI: " + imei);
					System.out.println("Android ID: " + androidID);
					System.out.println("md5(Android ID): " + androidIDmd5);
					System.out.println("Latitude: " + latBounds + latitudeMin + ";" + latitudeMax + latBounds);
					System.out.println("Longitude: " + lonBounds + longitudeMin + ";" + longitudeMax + lonBounds);
					System.out.println("The surface covered is about 4.95km2 where the middle is the point at the given coordinates.");
					System.out.println("\n---");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String getContent(Element fstElmnt, String label){
	      NodeList elmntLst = fstElmnt.getElementsByTagName(label);
	      Element elmnt = (Element) elmntLst.item(0);
	      NodeList ndLst = elmnt.getChildNodes();
	      return ((Node) ndLst.item(0)).getNodeValue();	
	}
	
	private static String getBelowValue(String value) {
		char[] charValue = value.toCharArray();
		int dotPos = value.indexOf('.');
		if (charValue[0] == '-') {
			increment(charValue, dotPos);
		} else {
			decrement(charValue, dotPos);
		}
		String newValue = new String(charValue);
		return newValue;
	}
	
	private static String getAboveValue(String value) {
		char[] charValue = value.toCharArray();
		int dotPos = value.indexOf('.');
		if (charValue[0] == '-') {
			decrement(charValue, dotPos);
		} else {
			increment(charValue, dotPos);
		}
		String newValue = new String(charValue);
		return newValue;
	}
	
	private static void decrement(char[] charValue, int dotPos) {
		if(charValue[dotPos + 2] == '0') {
			charValue[dotPos + 2] = '9';
			charValue[dotPos + 1] -= 1;
		} else {
			charValue[dotPos + 2] -= 1;
		}
	}
	
	private static void increment(char[] charValue, int dotPos) {
		if(charValue[dotPos + 2] == '9') {
			charValue[dotPos + 2] = '0';
			charValue[dotPos + 1] += 1;
		} else {
			charValue[dotPos + 2] += 1;
		}
	}
}
