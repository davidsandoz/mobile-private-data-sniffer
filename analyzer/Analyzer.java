package analyzer;

import java.util.Scanner;


public class Analyzer {
	public static void main(String[] args) throws Exception{
		
		Constants.initializeConstantValues();
		do {
			Trace trace = new Trace();
			trace.analyze();
			trace.printInNewWindow();
		} while (!mustQuit());
	}
	
	private static boolean mustQuit() {
		String answer = "";
		Scanner in = new Scanner(System.in);
		while (true) {
			System.out.print("\nDo you want to continue? [y/n] ");
			answer = in.nextLine();
			answer = answer.toLowerCase();
			if (answer.equals("y")) {
				System.out.println("\n********************************\n");
				return false;
			} else if (answer.equals("n")){
				System.out.println("\nBye bye!");
				return true;
			}
		}
	}
}
