package imdbextractor.main;

import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;

import imdbextractor.data.DirectoryData;
import imdbextractor.data.ImdbData;
import imdbextractor.operations.ExcelOperations;
import imdbextractor.operations.FileOperations;


public class Worker_MovieDirectory {
	static final Logger logger = LogManager.getLogger(Worker_MovieDirectory.class.getName());

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
//		ProgressPanel panel = new ProgressPanel();
//		JProgressBar progressBar = panel.getInstance(filteredFiles.size());

		logger.info("Found movie directories/files: " + filteredFiles.size());
		ExecutorService executor = Executors.newCachedThreadPool();
		List<ImdbData> imdbDataList = Collections.synchronizedList(new ArrayList<>());
		for (File f : filteredFiles) {
			executor.execute(new ImdbRunner(f, imdbDataList, failed));
		}
		executor.shutdown();
		executor.awaitTermination(60, TimeUnit.SECONDS);

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
//		panel.dispose();
	}
	
//	private static void incrementProgressBar(JProgressBar progressBar, int total) {
//		progressBar.setValue(++progressBarIdx);
//		progressBar.setString(progressBarIdx + " of " + total);
//	}


}

