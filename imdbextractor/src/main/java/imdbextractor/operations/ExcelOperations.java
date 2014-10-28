package imdbextractor.operations;

import imdbextractor.constants.ExcelRows;
import imdbextractor.data.DirectoryData;
import imdbextractor.data.ImdbData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

//import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ExcelOperations {

	public static void makeExcel(List<ImdbData> imdbDataList, String saveLocation, String status, File directoryWithMovies) throws IOException {
		Workbook wb = new HSSFWorkbook();
//		Workbook wb = new XSSFWorkbook();
//		CreationHelper createHelper = wb.getCreationHelper();
		Sheet sheet = wb.createSheet("new sheet");
		Row headerRow = sheet.createRow((short) 0);
		for (ExcelRows er : ExcelRows.values()) {
			headerRow.createCell(er.ordinal()).setCellValue(er.name());
		}
		Row dataRow;
		for (ImdbData data : imdbDataList) {
			if (data.getDirectoryData() == null) {
				data.setDirectoryData(new DirectoryData());
			}
			dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
			dataRow.createCell(ExcelRows.Title.ordinal()).setCellValue(data.getMovieName());
			dataRow.createCell(ExcelRows.YearReleased.ordinal()).setCellValue(data.getReleaseYear());
			dataRow.createCell(ExcelRows.Rating.ordinal(), Cell.CELL_TYPE_STRING).setCellValue(data.getImdbRating().replace(".", ","));
			dataRow.createCell(ExcelRows.Genre.ordinal()).setCellValue(listToString(data.getGenres()));
			dataRow.createCell(ExcelRows.Description.ordinal()).setCellValue(data.getShortDescription());
			dataRow.createCell(ExcelRows.Imdb.ordinal()).setCellValue(data.getImdbUrl());
			dataRow.createCell(ExcelRows.Cover.ordinal()).setCellValue(data.getPosterImgLink());
			dataRow.createCell(ExcelRows.Actors.ordinal()).setCellValue(listToString(data.getActors()));
			dataRow.createCell(ExcelRows.Director.ordinal()).setCellValue(data.getDirector());
			dataRow.createCell(ExcelRows.Writers.ordinal()).setCellValue(listToString(data.getWriters()));		
			dataRow.createCell(ExcelRows.Duration.ordinal()).setCellValue(data.getDuration());
			dataRow.createCell(ExcelRows.Format.ordinal()).setCellValue(data.getDirectoryData().getResolution());
			dataRow.createCell(ExcelRows.Status.ordinal()).setCellValue(status);
			dataRow.createCell(ExcelRows.SaveLocation.ordinal()).setCellValue(saveLocation != null ? saveLocation : data.getDirectoryData().getSaveLocation());
			dataRow.createCell(ExcelRows.Metascore.ordinal()).setCellValue(data.getMetascore());
			dataRow.createCell(ExcelRows.Added.ordinal()).setCellValue(data.getDirectoryData().getLastModified() != null ? 
					data.getDirectoryData().getLastModified() : new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
			
			String searchNameDate = data.getDirectoryData().getName();
			if (data.getDirectoryData().getYear() != null) {
				searchNameDate += " (" + data.getDirectoryData().getYear() + ")";
			}
			if (searchNameDate != null) {
				dataRow.createCell(ExcelRows.SearchNameDate.ordinal()).setCellValue(searchNameDate.replace("%20", " "));
			}
		}
		String filename = ".\\workbook" + new Date().getTime();
		if (directoryWithMovies != null) {
			filename += "_" + FileOperations.fetchVolumeName(directoryWithMovies);
			String dir = directoryWithMovies.getName();
			filename += "_" + dir.substring(dir.lastIndexOf("\\") + 1, dir.length());
		} else {
			filename += "_imdbUrls";
		}
		FileOutputStream fileOut = new FileOutputStream(filename + ".xls");
		wb.write(fileOut);
		fileOut.close();
	}
	
	public static void makeExcelForFailed(List<DirectoryData> failed) throws IOException {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("failed sheet");
		Row headerRow = sheet.createRow(0);
		headerRow.createCell(0).setCellValue("Directory/Filename");
		headerRow.createCell(1).setCellValue("Year");
		headerRow.createCell(2).setCellValue("Format");
		headerRow.createCell(3).setCellValue("LastModified");
		headerRow.createCell(4).setCellValue("VolumeName");
		headerRow.createCell(5).setCellValue("imdbLink");
		
		Row row = null;
		int i = 1;
		for (DirectoryData directoryData : failed) {
			row = sheet.createRow(i);
			row.createCell(0).setCellValue(directoryData.getFileDirectory().getName());
			row.createCell(1).setCellValue(directoryData.getYear());
			row.createCell(2).setCellValue(directoryData.getResolution());
			row.createCell(3, Cell.CELL_TYPE_STRING).setCellValue(directoryData.getLastModified());
			row.createCell(4).setCellValue(FileOperations.fetchVolumeName(directoryData.getFileDirectory()));
			i++;
		}
		Iterator<Row> it = sheet.rowIterator();
		while (it.hasNext()) {
			sheet.autoSizeColumn(it.next().getRowNum());
		}
		FileOutputStream fileOut = new FileOutputStream("c:\\tmp\\Failed.xls");
		wb.write(fileOut);
		fileOut.close();
	}
	
	public static List<DirectoryData> readFailedExcel(String pathToFile) throws InvalidFormatException, IOException {
		InputStream inp = new FileInputStream(new File(pathToFile));
		Workbook wb = WorkbookFactory.create(inp);
		Sheet sheet = wb.getSheetAt(0);
		Iterator<Row> it = sheet.rowIterator();
		
		List<DirectoryData> entryList = new ArrayList<DirectoryData>();
		DirectoryData data = null;
		it.next();
		while (it.hasNext()) {
			Row row = it.next();
			data = new DirectoryData();
			if (row.getCell(2) != null) {
				data.setResolution(row.getCell(2).getStringCellValue());
			}
			if (row.getCell(3) != null) {
				data.setLastModified(row.getCell(3).toString());
			}
			if (row.getCell(4) != null) {
				data.setSaveLocation(row.getCell(4).getStringCellValue());
			}
			data.setName(row.getCell(5).getStringCellValue());
			entryList.add(data);
		}
		
		return entryList;
	}
	
	private static String listToString(List<String> list) {
		String string = "";
		for (int i = 0; i < list.size(); i++) {
			string += list.get(i);
			if (i < list.size() - 1) {
				string += ", ";
			}
		}
		return string;
	}

}
