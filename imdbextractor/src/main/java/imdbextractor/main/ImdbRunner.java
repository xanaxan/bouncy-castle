package imdbextractor.main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JProgressBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import imdbextractor.data.DirectoryData;
import imdbextractor.data.ImdbData;
import imdbextractor.operations.FileOperations;
import imdbextractor.operations._ImdbOperations;
import imdbextractor.operations.ImdbOperations;

public class ImdbRunner implements Runnable {

	transient final Logger logger = LogManager.getLogger(ImdbRunner.class);

	File f;
	List<ImdbData> imdbDataList;
	List<DirectoryData> failed;
	JProgressBar progressBar;

	public ImdbRunner(File f, List<ImdbData> list, List<DirectoryData> failed) {
		this.f = f;
		this.imdbDataList = list;
		this.failed = failed;
	}

	@Override
	public void run() {
		try {
			WebClient webClient = new WebClient();
			webClient.setCssErrorHandler(new SilentCssErrorHandler());
			webClient.getOptions().setJavaScriptEnabled(false);
			
			String dirFileName = f.getName();
			if (f.isFile()) {
				dirFileName = f.getName().substring(0, f.getName().lastIndexOf("."));
			}
			DirectoryData directoryData = FileOperations.processDirectoryName(dirFileName);
			directoryData.setFileDirectory(f);
			directoryData.setLastModified(new SimpleDateFormat("dd.MM.yyyy").format(new Date(f.lastModified())));

			ImdbData imdbData = new ImdbData();
			HtmlPage page = _ImdbOperations.searchMovie(directoryData, webClient);
			if (page == null) {
				failed.add(directoryData);
				logger.error("page == null");
				return;
			}
			ImdbData data = null;
			ImdbOperations imdbOperations = new ImdbOperations();
			try {
				data = imdbOperations.extractDataFromImdb(page, imdbData);
			} catch (Exception e) {
				failed.add(directoryData);
				logger.error("extractDataFromImdb Exception", e);
				// incrementProgressBar(progressBar, listFiles.length);
				return;
			}
			if (data != null) {
				data.setDirectoryData(directoryData);
				imdbDataList.add(data);
			}

			logger.info("I'm finished!");
			webClient.closeAllWindows();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

}
