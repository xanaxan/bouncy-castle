package imdbextractor;

import imdbextractor.constants.ExcelRows;
import imdbextractor.operations.FileOperations;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.filechooser.FileSystemView;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;

public class ImdbTest {

	@Test
	public void testProcessDescriptionAndPersons() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient webClient = new WebClient();
		webClient.getOptions().setJavaScriptEnabled(false);
		HtmlPage page = webClient.getPage("http://www.imdb.com/title/tt0085271/");

		HtmlParagraph p = (HtmlParagraph) page.getByXPath("//p[@itemprop='description']").get(0);
		String description = p.asText();
		if (description.contains("See full summary")) {
			System.out.println("hui");
		}

		// *[@id="titleStoryLine"]/div[1]/p
		Object o = page.getFirstByXPath("//div[@id='titleStoryLine']/div[1]/p[1]");
		if (o != null) {
			// Object oArray[] = (Object[]) o;
			String plot = ((HtmlParagraph) o).asText();
			System.out.println(plot);
			System.out.println(plot.substring(0, plot.indexOf("Written by")));
		}

	}

	@Test
	public void testProcessRatingsBox() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient webClient = new WebClient();
		webClient.getOptions().setJavaScriptEnabled(false);
		HtmlPage page = webClient.getPage("http://www.imdb.com/title/tt1142978/");
		HtmlTableDataCell tdOverviewTop = page.getHtmlElementById("overview-top");

		HtmlSpan span = (HtmlSpan) tdOverviewTop.getByXPath("//span[@itemprop='ratingValue']").get(0);
		System.out.println(span.asText());

		Object o = tdOverviewTop.getFirstByXPath("//div[@class='star-box-details']/a[2]");
		if (o != null) {
			String s = ((HtmlAnchor) o).asText();
			if (!s.contains("user") && s.contains("/100")) {
				s = s.substring(0, s.length() - 4);
				System.out.println(s);
			}
		}
		// System.out.println(span.asText());

	}

	@Test
	public void testEnum() {
		for (ExcelRows er : ExcelRows.values()) {
			System.out.println(er.name() + " : #" + er.ordinal());
		}
	}

	public static void main(String args[]) {
		String path = FileOperations.directoryChooser();
		FileSystemView view = FileSystemView.getFileSystemView();
		File dir = new File(path.substring(0, 3));
		String name = view.getSystemDisplayName(dir);
		int index = name.lastIndexOf(" (");
		if (index > 0) {
			name = name.substring(0, index);
		}
		System.out.println(name);
	}
}
