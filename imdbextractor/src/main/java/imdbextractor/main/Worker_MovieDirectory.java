package imdbextractor.main;

import java.awt.Toolkit;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import imdbextractor.data.DirectoryData;
import imdbextractor.data.ImdbData;
import imdbextractor.operations.ExcelOperations;
import imdbextractor.operations.FileOperations;
import imdbextractor.operations.ImdbOperations;
import imdbextractor.util.ProgressPanel;


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
		File[] listFiles = directoryWithMovies.listFiles();
		Collection<File> filteredFiles = Arrays.asList(listFiles).stream()
				.filter(f -> 
					(!f.isFile() && !f.getName().equals("New folder")) ||
						(f.getName().endsWith(".mkv") ||
						f.getName().endsWith(".avi") ||
						f.getName().endsWith(".mp4") ||
						f.getName().endsWith(".divx")))
				.collect(Collectors.toList());
		ProgressPanel panel = new ProgressPanel();
		JProgressBar progressBar = panel.getInstance(filteredFiles.size());

		for (File f : filteredFiles) {
			String dirFileName = f.getName();
			if (f.isFile()) {
				dirFileName = f.getName().substring(0, f.getName().lastIndexOf("."));
			}
			DirectoryData directoryData = FileOperations.processDirectoryName(dirFileName);
			directoryData.setFileDirectory(f);
			directoryData.setLastModified(new SimpleDateFormat("dd.MM.yyyy").format(new Date(f.lastModified())));
			
			HtmlPage page = ImdbOperations.searchMovie(directoryData, webClient);
			if (page == null) {
				failed.add(directoryData);
				logger.error("page == null");
				incrementProgressBar(progressBar, listFiles.length);
				continue;
			}
			ImdbData data = null;
			try {
				data = ImdbOperations.extractDataFromImdb(page);
			} catch (Exception e) {
				failed.add(directoryData);
				logger.error("extractDataFromImdb Exception", e);
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
