package imdbextractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;

import imdbextractor.operations.FileOperations;

public class ExcelTest {

	@Test
	public void readExcel() throws IOException, InvalidFormatException {
		String path = FileOperations.fileChooser();
		System.out.println(path);
		InputStream inp = new FileInputStream(path);
		Workbook wb = WorkbookFactory.create(inp);
		Sheet sheet = wb.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.rowIterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			Cell cell = row.getCell(0);
			System.out.println(cell.getStringCellValue());
		}
		inp.close();
		// Write the output to a file
//		FileOutputStream fileOut = new FileOutputStream("D:\\alex\\programming\\tmp\\workbook1.xls");
//		wb.write(fileOut);
//		fileOut.close();
	}

	@Test
	public void correctCoverUrlsInExcel() throws InvalidFormatException, IOException {
		InputStream inp = new FileInputStream(new File("D:\\tmp\\Movie Collection 20150629.xlsx"));
		Workbook wb = WorkbookFactory.create(inp);
		Sheet sheet = wb.getSheetAt(0);
		Iterator<Row> it = sheet.rowIterator();

//		it.next();
		while (it.hasNext()) {
			Row row = it.next();
			Cell cell = row.getCell(1);
			if (cell != null) {
				String url = cell.getStringCellValue();

//				if (url.indexOf(",") != -1) {

				String result = url.substring(0, url.indexOf(".", url.indexOf(".com") + 4)) + ".jpg";
				cell.setCellValue(result);
				//				}
			}

		}
		FileOutputStream fos = new FileOutputStream(new File("D:\\tmp\\MC_corr.xlsx"));
		wb.write(fos);
		fos.close();
	}

	@Test
	public void stringy() {
		String url = "https://images-na.ssl-images-amazon.com/images/M/MV5BMTY3MjU0NDA0OF5BMl5BanBnXkFtZTgwNTc0MTU3OTE@._V1_UY268_CR3,0,182,268_AL_.jpg";
		String result = "wu";
		if (url.indexOf(",") != -1) {
			result = url.substring(0, url.indexOf(".", url.indexOf(".com") + 4)) + ".jpg";
			System.out.println(result);
		}
	}
}
