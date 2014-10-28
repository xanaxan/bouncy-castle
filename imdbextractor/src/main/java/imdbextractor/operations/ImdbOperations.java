package imdbextractor.operations;

import imdbextractor.data.DirectoryData;
import imdbextractor.data.ImdbData;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHeading1;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;

public class ImdbOperations {
	
	static final Logger logger = LogManager.getLogger(ImdbOperations.class.getName());

	static ImdbData imdbData = new ImdbData();

	@SuppressWarnings("unchecked")
	public static HtmlPage searchMovie(DirectoryData directoryData, WebClient webClient) throws FailingHttpStatusCodeException, 
																					MalformedURLException, IOException {
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
				yearInSearchResult = yearInSearchResult.substring(yearInSearchResult.length() - 5, yearInSearchResult.length() - 1);
			}
			//TODO WARNUNG wenn mehr als 1 film mit demselben jahr (und namen) oder
//			 wenn kein jahr im directory/file angegeben
			if (directoryData.getYear() != null && directoryData.getYear() != null && !directoryData.getYear().equals(yearInSearchResult)) {
				Integer year1 = Integer.valueOf(directoryData.getYear());
				Integer year2 = 0;
				try {
					year2 = Integer.valueOf(yearInSearchResult);
				} catch (NumberFormatException nfe) {
					// e.g. (in development), no year
				}
				if ((year1 > year2 && year1 - year2 < 2) ||
						(year2 > year1 && year2 - year1 < 2)) {
					aToKeep = a;
				}
				if (i++ > 5) {
					if (aToKeep != null) {
						logger.warn(directoryData.getName() + ": Years don't match! Falling back to nearest possible Result. File/Directory Year: "
								+ directoryData.getYear() +
								" Imdb Search Result Year: " + yearInSearchResult);
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
	
	public static ImdbData extractDataFromImdb(HtmlPage moviePage) throws Exception {
		imdbData = new ImdbData();
		
		String url = moviePage.getUrl().toString();
		url = url.substring(0, url.lastIndexOf("/") + 1);
		imdbData.setImdbUrl(url);
		
		HtmlTableDataCell tdOverviewTop = moviePage.getHtmlElementById("overview-top");
		processHeaderData(tdOverviewTop);
		processInfoBar(tdOverviewTop);
		processRatingsBox(tdOverviewTop);
		processDescriptionAndPersons(tdOverviewTop, moviePage);
		fetchPosterImgLink(moviePage);
		
		return imdbData;
	}

	private static void processDescriptionAndPersons(HtmlTableDataCell tdOverviewTop, HtmlPage page) {
		HtmlParagraph p = (HtmlParagraph) tdOverviewTop.getByXPath("//p[@itemprop='description']").get(0);
		String description = p.asText();
		if (description.contains("See full summary")) {
			Object o = page.getFirstByXPath("//div[@id='titleStoryLine']/div[1]/p[1]");
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


		HtmlSpan span = (HtmlSpan) tdOverviewTop.getFirstByXPath("//div[@itemprop='director']//span[@itemprop='name']");
		if (span != null) {
			imdbData.setDirector(span.asText());
		}
		
		for (Object o : tdOverviewTop.getByXPath("//div[@itemprop='creator']//span[@itemprop='name']")) {
			HtmlSpan spanWriter = (HtmlSpan) o;
			String writer = spanWriter.asText() + " "
					+ spanWriter.getParentNode().getNextSibling().asText();
			if (writer.contains(",")) {
				writer = writer.substring(0, writer.length() - 1);
			}
			imdbData.getWriters().add(writer.trim());
		}

		for (Object o : tdOverviewTop.getByXPath("//div[@itemprop='actors']//span[@itemprop='name']")) {
			imdbData.getActors().add(((HtmlSpan) o).asText());
		}
	}

	private static void processHeaderData(HtmlTableDataCell tdOverviewTop) {
		HtmlHeading1 h1 = (HtmlHeading1) tdOverviewTop.getHtmlElementsByTagName("h1").get(0);

		HtmlSpan span = (HtmlSpan) h1.getByXPath("//span[@itemprop='name']").get(0);
		imdbData.setMovieName(span.asText());
			
		List<HtmlElement> lista = (List<HtmlElement>) h1.getHtmlElementsByTagName("a");
		if (!lista.isEmpty()) {
			HtmlAnchor a = (HtmlAnchor) lista.get(0);
			imdbData.setReleaseYear(a.asText());
		} else {
			HtmlSpan spanYear = (HtmlSpan) h1.getFirstByXPath("//span[@class='nobr']");
			if (spanYear != null) {
				String year = spanYear.asText();
				imdbData.setReleaseYear(year.substring(1, 5));
			}
		}
	}

	private static void processInfoBar(HtmlTableDataCell overviewTop) {
		HtmlDivision divInfobar = (HtmlDivision) overviewTop.getByXPath("//div[@class='infobar']").get(0);

		DomNodeList<HtmlElement> list = divInfobar.getElementsByTagName("time");
		if (!list.isEmpty()) {
			String duration = list.get(0).asText();
			imdbData.setDuration(duration.substring(0, duration.length() - 4));
		}
		for (Object span : divInfobar.getByXPath("//span[@itemprop='genre']")) {
			imdbData.getGenres().add(((HtmlSpan) span).asText());
		}
	}

	private static void processRatingsBox(HtmlTableDataCell tdOverviewTop) {
		List<HtmlElement> noRatingElementList = tdOverviewTop.getElementsByAttribute("div", "class", "rating-ineligible");
		if (!noRatingElementList.isEmpty() && noRatingElementList.get(0).asText().equals("Not yet released")) {
			imdbData.setImdbRating("NYR");
			return;
		}

		HtmlSpan span = (HtmlSpan) tdOverviewTop.getByXPath("//span[@itemprop='ratingValue']").get(0);
		imdbData.setImdbRating(span.asText());
		
		Object o = tdOverviewTop.getFirstByXPath("//div[@class='star-box-details']/a[2]");
		if (o != null) {
			String s = ((HtmlAnchor) o).asText();
			if (!s.contains("user") && s.contains("/100")) {
				String metascore = ((HtmlAnchor)o).asText();
				metascore = metascore.substring(0, metascore.length() - 4);
				imdbData.setMetascore(metascore);
			}
		}
	}

	private static void fetchPosterImgLink(HtmlPage moviePage) {
		HtmlTableDataCell td = moviePage.getHtmlElementById("img_primary");
		List<HtmlElement> list = (List<HtmlElement>) td.getHtmlElementsByTagName("img");
		if (!list.isEmpty()) {
			HtmlImage img = (HtmlImage) list.get(0);
			imdbData.setPosterImgLink(img.getSrcAttribute());
		}		
	}
}
