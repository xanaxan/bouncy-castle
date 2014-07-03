package imdbextractor.main;

import imdbextractor.data.DirectoryData;
import imdbextractor.data.ImdbData;
import imdbextractor.operations.ExcelOperations;
import imdbextractor.operations.FileOperations;
import imdbextractor.operations.ImdbOperations;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class Main_MovieDirectory {
	static java.util.logging.Logger logger = Logger.getLogger(Main_MovieDirectory.class.getName());
	
	static List<ImdbData> imdbDataList = new ArrayList<ImdbData>();

	public static void main(String[] args) throws Exception {		
		String directoryWithMoviesString = FileOperations.directoryChooser();
		File directoryWithMovies = new File(directoryWithMoviesString);
		
		String saveLocation= JOptionPane.showInputDialog("Please input SaveLocation: ", FileOperations.fetchVolumeName(directoryWithMovies));
		String status= JOptionPane.showInputDialog("Please input Status (Watch/Watched): ", "Watch");
		
		logger.info("Starting...");
		WebClient webClient = new WebClient();
		webClient.setCssErrorHandler(new SilentCssErrorHandler());
		webClient.getOptions().setJavaScriptEnabled(false);
		List<DirectoryData> failed = new ArrayList<DirectoryData>();
		for (File f : directoryWithMovies.listFiles()) {
			String dirFileName = f.getName();
			if (f.isFile()) {
				if (!(f.getName().endsWith(".mkv") || f.getName().endsWith(".avi") || f.getName().endsWith(".mp4"))) {
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
				continue;
			}
			ImdbData data = null;
			try {
				data = ImdbOperations.extractDataFromImdb(page);
			} catch (Exception e) {
				failed.add(directoryData);
				e.printStackTrace();
				continue;
			}
			if (data != null) {
				data.setDirectoryData(directoryData);
				imdbDataList.add(data);
			}
		}
		ExcelOperations.makeExcel(imdbDataList, saveLocation, status, directoryWithMovies);
		ExcelOperations.makeExcelForFailed(failed);
		for (DirectoryData dirData : failed) {
			System.out.println("FAILED: " + dirData.getFileDirectory().getName());
		}
		logger.info("Finished!");
	}
	
	@Test
	public void test() throws Exception {
		String dir = "Skeleton Lake (2012) dvdRip [Xvid] {1337x}-X";
		dir = dir.replace("(", " ").replace(")", "")
				.replace("[", " ").replace("]", " ")
				.replace(".", " ");
		dir = dir.replace("  ", " ");
//		boolean firstBlank = false;
//		for (int i = 0; i < dir.length(); i++) {
//			if (dir.charAt(0) == ' ' && !firstBlank) {
//				firstBlank = true;
//			} else if (dir.charAt(i) == ' ' && firstBlank) {
//				dir.r
//			}
//		}
		System.out.println(dir);
		DirectoryData directoryData = FileOperations.processDirectoryName(dir);
		System.out.println(directoryData.getResolution());
	}

}