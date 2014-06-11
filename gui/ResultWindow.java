package gui;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ResultWindow extends JFrame {

	// We had to declare a static final serialVersionUID field of type long
	// because this is a serializable class
	private static final long serialVersionUID = 1L;
	private static final String title = "Private data found";
	
	private String privateData = "";
	private JTextArea memoDisplay;
	public static final int LINES = 22;
	public static final int CHAR_PER_LINE = 50;
	
	public ResultWindow(String content) {
		setTitle(title);
		setSize(400, 500);
		setLocation(500, 100);
		setResizable(true);
		
		privateData = content;
		
		memoDisplay = new JTextArea(LINES, CHAR_PER_LINE);
		memoDisplay.setBackground(Color.WHITE);
		memoDisplay.setEditable(false);
		memoDisplay.append(privateData);

		JScrollPane scrolledText = new JScrollPane(memoDisplay);
		scrolledText.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrolledText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		add(scrolledText);
	}
	
	

}
