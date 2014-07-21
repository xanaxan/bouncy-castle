package imdbextractor.util;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressPanel {

	public JProgressBar getInstance(int total) {
		JFrame frame = new JFrame("ProgressBarDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		JComponent newContentPane = new JPanel(new BorderLayout());

		JProgressBar progressBar;
		progressBar = new JProgressBar(0, total);
		progressBar.setStringPainted(true);
		progressBar.setString(0 + " of " + total);

		JPanel panel = new JPanel();
		panel.add(progressBar);

		newContentPane.add(panel, BorderLayout.PAGE_START);

		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
		return progressBar;
	}
}
