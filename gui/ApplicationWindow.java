package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import analyzer.Constants;
import analyzer.Trace;

public class ApplicationWindow extends JFrame{

	// We had to declare a static final serialVersionUID field of type long
	// because this is a serializable class
	private static final long serialVersionUID = 1L;
	private static final String title = "Mobile Private Data Sniffer";
	private WaitingCaptureWindow waitingWindow;
	
	JPanel mainWindow = new JPanel();

	JLabel lbHead = new JLabel("What do you want to do?");
	String[] listPossibilities = { "Do a new capture", "Read an existing file" };
	JList lsTask = new JList(listPossibilities);
	JLabel lbFilename = new JLabel("Enter the trace filename:");
	JTextField tfFile = new JTextField("");
	JLabel lbExtension = new JLabel(".csv");
	JLabel lbRootPassword = new JLabel("Enter the root password:");
	JPasswordField pfRootPassword = new JPasswordField(10);
	JButton btRun = new JButton("Run");

	FlowLayout formLayout = new FlowLayout();

	public ApplicationWindow() {
		setTitle(title);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(270, 200);
		setResizable(false);
		setLocation(200, 100);
		
		setMainWindowProperties();

		add(mainWindow);
	}
	

	private void setMainWindowProperties() {
		mainWindow.setLayout(new BoxLayout(mainWindow,BoxLayout.Y_AXIS));

		this.setLayout(formLayout);

		this.add(lbHead);
		
		lsTask.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lsTask.setSelectedIndex(0);
		lsTask.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if(lsTask.getSelectedIndex()==1) {
					lbRootPassword.setVisible(false);
					pfRootPassword.setVisible(false);
					setSize(270, 170);
				} else {
					lbRootPassword.setVisible(true);
					pfRootPassword.setVisible(true);
					setSize(270, 200);
				}
			}
		});
		this.add(lsTask);

		this.add(lbFilename);
		tfFile.setPreferredSize(new Dimension(200, 25));
		this.add(tfFile);
		lbExtension.setPreferredSize(new Dimension(40, 20));
		this.add(lbExtension);
		
		this.add(lbRootPassword);
		this.add(pfRootPassword);

		btRun.setEnabled(true);
		this.add(btRun);
		btRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Constants.initializeConstantValues();
				waitingWindow = new WaitingCaptureWindow();
				waitingWindow.setVisible(true);
				Trace trace = new Trace(lsTask.getSelectedIndex()+1, tfFile.getText(), new String(pfRootPassword.getPassword()), waitingWindow);
				trace.start();
//				waitingWindow.setVisible(false);


//				waitingWindow.show();
//				java.awt.EventQueue.invokeLater(new Runnable()
//		        {
//		            public void run() 
//		           { 
//						Trace trace = new Trace(lsTask.getSelectedIndex()+1,
//												tfFile.getText(),
//												new String(pfRootPassword.getPassword()));
//						trace.analyze();
//						waitingWindow.hide();
//						trace.printInNewWindow();
//		            }
//		        });
			}
		});
	}
}