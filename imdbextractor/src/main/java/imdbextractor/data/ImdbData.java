package imdbextractor.data;

import java.util.ArrayList;
import java.util.List;

public class ImdbData {
	private String imdbUrl;
	private String movieName;
	private String releaseYear;
	private String imdbRating;
	private String posterImgLink;
	private String director;
	private List<String> actors = new ArrayList<String>();
	private List<String> writers = new ArrayList<String>();
	private String shortDescription;
	private String duration;
	private String metascore;
	private List<String> genres = new ArrayList<String>();
	
	private DirectoryData directoryData;
	
	public DirectoryData getDirectoryData() {
		return directoryData;
	}
	public void setDirectoryData(DirectoryData directoryData) {
		this.directoryData = directoryData;
	}
	public String getMetascore() {
		return metascore;
	}
	public void setMetascore(String metascore) {
		this.metascore = metascore;
	}
	public String getImdbUrl() {
		return imdbUrl;
	}
	public void setImdbUrl(String imdbUrl) {
		this.imdbUrl = imdbUrl;
	}
	public String getMovieName() {
		return movieName;
	}
	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}
	public String getReleaseYear() {
		return releaseYear;
	}
	public void setReleaseYear(String releaseYear) {
		this.releaseYear = releaseYear;
	}
	public String getImdbRating() {
		return imdbRating;
	}
	public void setImdbRating(String imdbRating) {
		this.imdbRating = imdbRating;
	}
	public String getPosterImgLink() {
		return posterImgLink;
	}
	public void setPosterImgLink(String posterImgLink) {
		this.posterImgLink = posterImgLink;
	}
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = director;
	}
	public String getShortDescription() {
		return shortDescription;
	}
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public List<String> getActors() {
		return actors;
	}
	public void setActors(List<String> actors) {
		this.actors = actors;
	}
	public List<String> getGenres() {
		return genres;
	}
	public void setGenres(List<String> genres) {
		this.genres = genres;
	}
	public List<String> getWriters() {
		return writers;
	}
	public void setWriters(List<String> writers) {
		this.writers = writers;
	}

	
}
