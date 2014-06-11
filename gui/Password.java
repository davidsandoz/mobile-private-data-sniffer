package gui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import analyzer.Constants;

/* PasswordDemo.java requires no other files. */

public class Password extends JPanel
implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static String OK = "ok";
	
	public static boolean passwordEntered = false;

	//private JFrame controllingFrame; //needed for dialogs
	private JPasswordField passwordField;
	
	private static JFrame frame;

	public Password(JFrame f) {
		
		//Create everything.
		passwordField = new JPasswordField(10);
		passwordField.setActionCommand(OK);
		passwordField.addActionListener(this);

		JLabel label = new JLabel("Enter the root password: ");
		label.setLabelFor(passwordField);

		JComponent buttonPane = createButtonPanel();

		//Lay out everything.
		JPanel textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		textPane.add(label);
		textPane.add(passwordField);

		add(textPane);
		add(buttonPane);
	}

	protected JComponent createButtonPanel() {
		JPanel p = new JPanel(new GridLayout());
		JButton okButton = new JButton("OK");

		okButton.setActionCommand(OK);
		okButton.addActionListener(this);
		p.add(okButton);

		return p;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (OK.equals(cmd)) { //Process the password.
			char[] input = passwordField.getPassword();
			
			
			
			// DO WHAT YOU NEED TO DO WITH PASSWORD HERE
			String password = new String(input);
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
			frame.setVisible(false);
			passwordEntered = true;
			
			
			// if (isPasswordCorrect(input)) {
			//   JOptionPane.showMessageDialog(controllingFrame,
			//    "Success! You typed the right password.");


			//Zero out the possible password, for security.
			Arrays.fill(input, '0');

			passwordField.selectAll();
			resetFocus();
		}
	}

	//Must be called from the event dispatch thread.
	protected void resetFocus() {
		passwordField.requestFocusInWindow();
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event dispatch thread.
	 */
	public static void createAndShowGUI() {
		//Create and set up the window.
		frame = new JFrame("Enter Password");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		final Password newContentPane = new Password(frame);
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);

		//Make sure the focus goes to the right component
		//whenever the frame is initially given the focus.
		frame.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				newContentPane.resetFocus();
			}
		});

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}


}