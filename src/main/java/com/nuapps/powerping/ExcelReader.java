package com.nuapps.powerping;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelReader {
    //List<Hosts> hostsList = new ArrayList<>();
    private final List<Hosts> hostsList = new ArrayList<>();

    public ExcelReader() throws IOException {
        File file = new File("devices.xlsx");
        FileInputStream fileInputStream = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fileInputStream);

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next(); // Skip header row

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            Hosts hosts = new Hosts(
                    getStringCellValue(cellIterator.next()),
                    getStringCellValue(cellIterator.next()),
                    getStringCellValue(cellIterator.next())
            );
            hostsList.add(hosts);
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        } else {
            return "";
        }
    }

    public List<Hosts> getHostsList() {
        return hostsList;
    }
}
