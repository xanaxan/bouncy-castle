package imdbextractor.main;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class Main_Test {

	public static void main(String[] args) throws InterruptedException {

		JFrame frame = new JFrame("ProgressBarDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		JComponent newContentPane = new JPanel(new BorderLayout());

		JProgressBar progressBar;
		progressBar = new JProgressBar(0, 3);
		progressBar.setStringPainted(true);

		JPanel panel = new JPanel();
		panel.add(progressBar);

		newContentPane.add(panel, BorderLayout.PAGE_START);

		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);

		for (int i = 0; i < 4; i++) {
			progressBar.setValue(i);
			progressBar.setString(i + " of 3");
			Thread.sleep(2000);
		}
		frame.dispose();
	}

}
