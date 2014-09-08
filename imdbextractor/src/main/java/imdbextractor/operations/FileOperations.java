package imdbextractor.operations;

import imdbextractor.data.DirectoryData;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileOperations {
	
	static final Logger logger = LogManager.getLogger(FileOperations.class.getName());
	
	public static DirectoryData processDirectoryName(String directoryName) throws Exception {
		logger.info("");
		logger.info("Processing Directory/File: " + directoryName);
		DirectoryData directoryData = new DirectoryData();
		
		directoryName = directoryName.replace("(", " ").replace(")", "")
				.replace("[", " ").replace("]", " ")
				.replace(".", " ").replace("  ", " ");
	
		String dirSplit[] = directoryName.split("\\ ");
		String name = "";
		int i = 1;
		for (String s : Arrays.asList(dirSplit)) {
			if (i++ == 1) {
				name = s;
				continue;
			}
			if (s.matches("[1][9][4-9]\\d") || s.matches("[2][0][01]\\d")) {
				directoryData.setYear(s);
				break;
			}
			if (checkWord(s)) {
				name = name + "%20" + s;
			}
		}
		directoryData.setName(name);
//		Comedown 720p x264-1.mp4
		if (directoryName.toLowerCase().contains("720p")) {
			directoryData.setResolution("720p");
		} else if (directoryName.toLowerCase().contains("1080p")) {
			directoryData.setResolution("1080p");
		} else if (directoryName.toLowerCase().contains("dvdrip") || 
				directoryName.toLowerCase().contains("dvd rip")) {
			directoryData.setResolution("DvdRip");
		}
		
		if (directoryData.getYear() == null) {
			logger.warn("NO DATE FOUND FOR: " + directoryName);
			if (directoryData.getResolution() != null) {
				directoryData.setName(directoryName.substring(0, directoryName.toLowerCase().indexOf(directoryData.getResolution().toLowerCase()) - 1));
			}
		}
		
		return directoryData;
	}
	
	public static boolean checkWord(String wordToCheck) {
		List<String> list = new ArrayList<String>();
		list.add("unrated");
		list.add("dc");
		list.add("director");
		list.add("directors");
		list.add("director's");
		list.add("cut");
		list.add("extended");
		list.add("extd");
		
		for (String listString : list) {
			if (listString.toLowerCase().equals(wordToCheck.toLowerCase())) {
				return false;
			}
		}
		return true;
	}
	
	public static String fileChooser() {
        JFileChooser fChooser = new JFileChooser("D:\\alex\\programming");
        fChooser.setDialogTitle("select folder");
        fChooser.setAcceptAllFileFilterUsed(false);
        fChooser.showSaveDialog(null);

		logger.info("Selected File: " + fChooser.getSelectedFile().toString());
  
        return fChooser.getSelectedFile().toString();
    }   
	
	public static String directoryChooser() {
        JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
        fChooser.setDialogTitle("select folder");
        fChooser.setAcceptAllFileFilterUsed(false);
        fChooser.showSaveDialog(null);

		logger.info("Selected Directory: " + fChooser.getSelectedFile().toString());
  
        return fChooser.getSelectedFile().toString();
    }
	
	public static String fetchVolumeName(File path) {
		FileSystemView view = FileSystemView.getFileSystemView();
		File dir = new File(path.getPath().substring(0, 3));
		String name = view.getSystemDisplayName(dir);
		int index = name.lastIndexOf(" (");
		if (index > 0) {
			name = name.substring(0, index);
		}
		return name;
	}
}
