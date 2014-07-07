package imdbextractor.data;

import java.io.File;

public class DirectoryData {
	private String name;
	private String year;
	private String resolution;
	private String lastModified;
	private String saveLocation;
	
	private File fileDirectory;
	
	
	public String getSaveLocation() {
		return saveLocation;
	}
	public void setSaveLocation(String saveLocation) {
		this.saveLocation = saveLocation;
	}
	public File getFileDirectory() {
		return fileDirectory;
	}
	public void setFileDirectory(File fileDirectory) {
		this.fileDirectory = fileDirectory;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

}
