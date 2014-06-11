package gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class WaitingCaptureWindow extends JFrame{

	private static final long serialVersionUID = 1L;
	
	JLabel waitingMessage;
	JProgressBar waitingBar;

	public WaitingCaptureWindow() {
		setTitle("Capture");
		setSize(350, 75);
		setLocation(300, 150);
		setResizable(false);
		
		JPanel panel = new JPanel();
		
		waitingMessage = new JLabel("Please wait...");
		waitingMessage.setSize(new Dimension(320, 40));
		panel.add(waitingMessage);
		
		waitingBar = new JProgressBar();
		waitingBar.setIndeterminate(true);
		waitingBar.setSize(320, 40);
		panel.add(waitingBar);
		
		add(panel);
	}
	
	public void startProgressBar() {
//		int totalTime = 0;
//		boolean showProgressingBar = true;
//		try {
//			totalTime = Integer.parseInt(Constants.duration);
//		} catch(NumberFormatException nfe) {
//			System.out.println("Bad time entered");
//			showProgressingBar = false;
//		}
//		
//		if (showProgressingBar) {
//			waitingBar.setIndeterminate(false);
//			waitingBar.setMinimum(0);
//			waitingBar.setMaximum(totalTime);
//			for(int time=1; time<=totalTime; time++) {
//				waitingBar.setValue(time);
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			waitingBar.setIndeterminate(true);
//		}
	}
	
	public void setLabel(String newLabel) {
		waitingMessage.setText(newLabel);
	}
}
