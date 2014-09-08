package imdbextractor.main;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main_Test {

	static final Logger logger = LogManager.getLogger(Main_Test.class.getName());

	public static void main(String[] args) throws InterruptedException {
		JOptionPane.showMessageDialog(null, "started...");
		logger.info("hui");
		JOptionPane.showMessageDialog(null, "Finished...");
	}

}
