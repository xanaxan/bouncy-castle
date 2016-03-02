package imdbextractor.main;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Main_ImdbExtractor {

	static final Logger logger = LogManager.getLogger(Main_ImdbExtractor.class);

	public static void main(String[] args) {
		String OPTION_MOVIEDIRECTORY = "Movie Directory";
		
		Object[] possibilities = { OPTION_MOVIEDIRECTORY, "Url List" };
		String s = (String) JOptionPane.showInputDialog(null, "", "Kind of Data Input", JOptionPane.PLAIN_MESSAGE, null, possibilities, OPTION_MOVIEDIRECTORY);

		if (s == null) {
			return;
		}
		try {
			if ((s.equals(OPTION_MOVIEDIRECTORY))) {
				Worker_MovieDirectory.work(args);
			} else {
				Worker_UrlList.work(args);
			}
			JOptionPane.showMessageDialog(null, "Finished...");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getStackTrace(), "Error", JOptionPane.ERROR_MESSAGE);
			logger.error("", e);
		}

	}
	


}
