package imdbextractor.main;

import imdbextractor.data.DirectoryData;
import imdbextractor.data.ImdbData;
import imdbextractor.operations.ExcelOperations;
import imdbextractor.operations.FileOperations;
import imdbextractor.operations.ImdbOperations;
import imdbextractor.util.ProgressPanel;

import java.awt.Toolkit;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class Worker_MovieDirectory {
	static final Logger logger = LogManager.getLogger(Worker_MovieDirectory.class.getName());
	
	static List<ImdbData> imdbDataList = new ArrayList<ImdbData>();
	static int progressBarIdx = 0;

	public static void work(String[] args) throws Exception {
		String directoryWithMoviesString = FileOperations.directoryChooser();
		File directoryWithMovies = new File(directoryWithMoviesString);
		
		String saveLocation= JOptionPane.showInputDialog("Please input SaveLocation: ", FileOperations.fetchVolumeName(directoryWithMovies));
		String status= JOptionPane.showInputDialog("Please input Status (Watch/Watched): ", "Watch");
		
		logger.info("Starting...");
		WebClient webClient = new WebClient();
		webClient.setCssErrorHandler(new SilentCssErrorHandler());
		webClient.getOptions().setJavaScriptEnabled(false);
		List<DirectoryData> failed = new ArrayList<DirectoryData>();
		File listFiles[] = directoryWithMovies.listFiles();
		ProgressPanel panel = new ProgressPanel();
		JProgressBar progressBar = panel.getInstance(listFiles.length);

		for (File f : listFiles) {

			String dirFileName = f.getName();
			if (f.isFile()) {
				// TODO filter out wrong files before (for total in progressbar
				if (!(f.getName().endsWith(".mkv") || f.getName().endsWith(".avi") || f.getName().endsWith(".mp4") || f.getName().endsWith(".divx"))) {
					incrementProgressBar(progressBar, listFiles.length);
					continue;
				}
				dirFileName = f.getName().substring(0, f.getName().lastIndexOf("."));
			}
			DirectoryData directoryData = FileOperations.processDirectoryName(dirFileName);
			directoryData.setFileDirectory(f);
			directoryData.setLastModified(new SimpleDateFormat("dd.MM.yyyy").format(new Date(f.lastModified())));
			
			HtmlPage page = ImdbOperations.searchMovie(directoryData, webClient);
			if (page == null) {
				failed.add(directoryData);
				incrementProgressBar(progressBar, listFiles.length);
				continue;
			}
			ImdbData data = null;
			try {
				data = ImdbOperations.extractDataFromImdb(page);
			} catch (Exception e) {
				failed.add(directoryData);
				e.printStackTrace();
				incrementProgressBar(progressBar, listFiles.length);
				continue;
			}
			if (data != null) {
				data.setDirectoryData(directoryData);
				imdbDataList.add(data);
			}
			incrementProgressBar(progressBar, listFiles.length);
		}

		ExcelOperations.makeExcel(imdbDataList, saveLocation, status, directoryWithMovies);
		if (!failed.isEmpty()) {
			ExcelOperations.makeExcelForFailed(failed, saveLocation, status);
		}
		for (DirectoryData dirData : failed) {
			logger.warn("FAILED: " + dirData.getFileDirectory().getName());
		}
		logger.info("Finished!");
		Toolkit.getDefaultToolkit().beep();
		Toolkit.getDefaultToolkit().beep();
		panel.dispose();
	}
	
	private static void incrementProgressBar(JProgressBar progressBar, int total) {
		progressBar.setValue(++progressBarIdx);
		progressBar.setString(progressBarIdx + " of " + total);
	}


}
