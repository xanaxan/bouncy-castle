package imdbextractor.operations;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHeading1;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;

import imdbextractor.data.DirectoryData;
import imdbextractor.data.ImdbData;

public class ImdbOperations {

	static final Logger logger = LogManager.getLogger(ImdbOperations.class.getName());

//	static ImdbData imdbData = new ImdbData();
	ImdbData imdbData;

	@SuppressWarnings("unchecked")
	public static HtmlPage searchMovie(DirectoryData directoryData, WebClient webClient)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		
		String url = "http://www.imdb.com/find?q=" + directoryData.getName() + "&s=tt&ttype=ft&exact=true";
		HtmlPage searchPage = webClient.getPage(url);

		List<Object> listResult = (List<Object>) searchPage.getByXPath("//td[@class='result_text']");
		if (listResult.isEmpty()) {
			String urlNotExact = "http://www.imdb.com/find?q=" + directoryData.getName() + "&s=tt&ttype=ft";
			searchPage = webClient.getPage(urlNotExact);
			listResult = (List<Object>) searchPage.getByXPath("//td[@class='result_text']");
			if (listResult.isEmpty()) {
				logger.error("Search Failed");
				return null;
			}
		}
		int i = 1;
		HtmlAnchor a = null;
		HtmlAnchor aToKeep = null;
		for (Object o : listResult) {
			HtmlTableDataCell tdResult = (HtmlTableDataCell) o;
			a = (HtmlAnchor) tdResult.getFirstElementChild();
			String yearInSearchResult = a.getNextSibling().asText();
			if (StringUtils.isNotBlank(yearInSearchResult) && yearInSearchResult.length() > 5) {
				yearInSearchResult = yearInSearchResult.substring(yearInSearchResult.length() - 5,
						yearInSearchResult.length() - 1);
			}
			// TODO WARNUNG wenn mehr als 1 film mit demselben jahr (und namen)
			// oder
			// wenn kein jahr im directory/file angegeben
			if (directoryData.getYear() != null && directoryData.getYear() != null
					&& !directoryData.getYear().equals(yearInSearchResult)) {
				Integer year1 = Integer.valueOf(directoryData.getYear());
				Integer year2 = 0;
				try {
					year2 = Integer.valueOf(yearInSearchResult);
				} catch (NumberFormatException nfe) {
					// e.g. (in development), no year
				}
				if ((year1 > year2 && year1 - year2 < 2) || (year2 > year1 && year2 - year1 < 2)) {
					aToKeep = a;
				}
				if (i++ > 5) {
					if (aToKeep != null) {
						logger.warn(directoryData.getName()
								+ ": Years don't match! Falling back to nearest possible Result. File/Directory Year: "
								+ directoryData.getYear() + " Imdb Search Result Year: " + yearInSearchResult);
						return aToKeep.click();
					} else {
						return null;
					}
				}
				continue;
			}
			return a.click();
		}
		if (aToKeep != null) {
			return aToKeep.click();
		} else {
			return null;
		}
	}

	public ImdbData extractDataFromImdb(HtmlPage moviePage, ImdbData inputImdbData) throws Exception {
		imdbData = inputImdbData;

		String url = moviePage.getUrl().toString();
		url = url.substring(0, url.lastIndexOf("/") + 1);
		imdbData.setImdbUrl(url);

		HtmlDivision divTitleBarWrapper = findFirstDivWithClass(moviePage, "title_bar_wrapper");
		if (divTitleBarWrapper == null) logger.error("do sixtas!");
		processTitleReleaseyear(divTitleBarWrapper);
		processGenres(divTitleBarWrapper);
		processRatingsBox(divTitleBarWrapper);
		processDescription(moviePage);
		HtmlDivision divPlotSummaryWrapper = findFirstDivWithClass(moviePage, "plot_summary_wrapper");
		processPersons(divPlotSummaryWrapper);
		fetchPosterImgLink(findFirstDivWithClass(moviePage, "poster"));
		processDuration(moviePage);
		processMetascore(divPlotSummaryWrapper);
		return imdbData;
	}

	private void processDuration(DomNode parentNode) {
		HtmlDivision divTitleDetails = parentNode.getFirstByXPath("//div[@id='titleDetails']");
		HtmlElement element = divTitleDetails.getFirstByXPath("//time[@itemprop='duration']");
		imdbData.setDuration(element.asText());
	}

	private void processDescription(DomNode parentNode) {
		String description = findFirstDivWithClass(parentNode, "summary_text").asText();

		if (description.contains("See full summary")) {

			Object o = parentNode.getFirstByXPath("//div[@id='titleStoryLine']/div[1]/p[1]");
			if (o != null) {
				String plot = ((HtmlParagraph) o).asText();
				if (plot.indexOf("Written by") != -1) {
					plot = plot.substring(0, plot.indexOf("Written by"));
				}
				if (plot.length() > 300) {
					plot = plot.substring(0, 300);
				}
				description = plot + " ...";
			}
		}
		imdbData.setShortDescription(description);
	}

	private void processPersons(DomNode parentNode) {
		HtmlSpan span = (HtmlSpan) parentNode.getFirstByXPath("//span[@itemprop='director']//span[@itemprop='name']");
		if (span != null) {
			imdbData.setDirector(span.asText());
		}

		for (Object o : parentNode.getByXPath(
				"//span[@itemprop='creator' and @itemtype='http://schema.org/Person']//span[@itemprop='name']")) {
			HtmlSpan spanWriter = (HtmlSpan) o;
			imdbData.getWriters().add(spanWriter.asText());
		}

		for (Object o : parentNode.getByXPath("//span[@itemprop='actors']//span[@itemprop='name']")) {
			imdbData.getActors().add(((HtmlSpan) o).asText());
		}
	}

	private void processTitleReleaseyear(HtmlDivision parentDiv) {
		HtmlHeading1 h1 = (HtmlHeading1) parentDiv.getHtmlElementsByTagName("h1").get(0);
		imdbData.setMovieName(h1.asText());

		List<HtmlElement> lista = (List<HtmlElement>) h1.getHtmlElementsByTagName("a");
		if (!lista.isEmpty()) {
			HtmlAnchor a = (HtmlAnchor) lista.get(0);
			imdbData.setReleaseYear(a.asText());
			imdbData.setMovieName(imdbData.getMovieName().substring(0,
					imdbData.getMovieName().indexOf("(" + imdbData.getReleaseYear()) - 1));
		} else {
			HtmlSpan spanYear = (HtmlSpan) h1.getFirstByXPath("//span[@class='nobr']");
			if (spanYear != null) {
				String year = spanYear.asText();
				imdbData.setReleaseYear(year.substring(1, 5));
			}
			logger.error("passiert das eigentlich irgendwann??!");
		}
	}

	private void processGenres(HtmlDivision parentDiv) {
		for (Object span : parentDiv.getByXPath("//span[@itemprop='genre']")) {
			imdbData.getGenres().add(((HtmlSpan) span).asText());
		}
	}

	private void processRatingsBox(DomNode parentNode) {
		imdbData.setImdbRating(findFirstSpanWithItemProp(parentNode, "ratingValue").asText());
	}

	private void processMetascore(DomNode parentNode) {
		HtmlDivision div1 = findFirstDivWithClass(parentNode, "titleReviewBarItem");
		if (div1 == null) return;
		Object o = div1.getFirstByXPath("//div[contains(@class,'metacriticScore')]");
		if (o != null) {
			imdbData.setMetascore(((HtmlDivision) o).asText());
		}
	}

	private void fetchPosterImgLink(HtmlDivision parentDiv) {
		List<HtmlElement> list = (List<HtmlElement>) parentDiv.getHtmlElementsByTagName("img");
		if (!list.isEmpty()) {
			HtmlImage img = (HtmlImage) list.get(0);
			String url = img.getSrcAttribute();
			if (StringUtils.isNotBlank(url)) {
				imdbData.setPosterImgLink(url.substring(0, url.indexOf(".", 25)));
			}
		}
	}

	private HtmlDivision findFirstDivWithClass(DomNode node, String className) {
		return node.getFirstByXPath("//div[@class='" + className + "']");
	}

	private HtmlSpan findFirstSpanWithItemProp(DomNode node, String itempropName) {
		return node.getFirstByXPath("//span[@itemprop='" + itempropName + "']");
	}
}
