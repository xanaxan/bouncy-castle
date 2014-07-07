package imdbextractor.main;

import imdbextractor.data.DirectoryData;
import imdbextractor.data.ImdbData;
import imdbextractor.operations.ExcelOperations;
import imdbextractor.operations.FileOperations;
import imdbextractor.operations.ImdbOperations;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class Main_UrlList {
	
	static Logger logger = Logger.getLogger(Main_UrlList.class.getName());
	
	static List<ImdbData> imdbDataList = new ArrayList<ImdbData>();

	public static void main(String[] args) throws Exception {
		String pathToFile = FileOperations.fileChooser();
		WebClient webClient = new WebClient();
		webClient.getOptions().setJavaScriptEnabled(false);
		if (pathToFile.endsWith(".xls")) {		
			List<DirectoryData> failedList = ExcelOperations.readFailedExcel(pathToFile);
			
			for (DirectoryData dirData : failedList) {
				HtmlPage page = webClient.getPage(dirData.getName());
				ImdbData imdbData = ImdbOperations.extractDataFromImdb(page);
				if (imdbData == null) {
					System.out.println("FAILURE: " + dirData.getName());
					continue;
				}
				imdbData.setDirectoryData(dirData);
				imdbDataList.add(imdbData);
			}
			
		} else if (pathToFile.endsWith(".txt")) {
			FileInputStream fis = new FileInputStream(pathToFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		 
			String line = null;

			while ((line = br.readLine()) != null) {
				System.out.println(line);
				HtmlPage page = webClient.getPage(line);
				ImdbData imdbData = ImdbOperations.extractDataFromImdb(page);
				if (imdbData == null) {
					System.out.println("FAILURE: " + line);
					continue;
				}
				imdbDataList.add(imdbData);
			}	 
			br.close();
		}
		webClient.closeAllWindows();
		ExcelOperations.makeExcel(imdbDataList, null, null, null);
		System.out.println("Finished!");
	}	
}
