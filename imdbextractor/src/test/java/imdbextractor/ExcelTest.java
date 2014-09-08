package imdbextractor;

import imdbextractor.operations.FileOperations;

import java.io.FileInputStream;
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
}
