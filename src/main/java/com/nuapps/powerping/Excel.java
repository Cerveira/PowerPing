package com.nuapps.powerping;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.IteratorUtils.toList;

public class Excel {
    List<Host> hostList = new ArrayList<>();

    public Excel() throws IOException {
        File excel = new File(System.getenv("USERPROFILE") + "/powerping/devices.xlsx");
        FileInputStream file = new FileInputStream(excel);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        List<Row> rows = toList(sheet.iterator());
        rows.remove(0);

        rows.forEach (row -> {
            List<Cell> cells = toList(row.cellIterator());
            Host host = new Host();
            host.setHostname(cells.get(0).getStringCellValue());
            host.setIp(cells.get(1).getStringCellValue());
            host.setLocation(cells.get(2).getStringCellValue());
            hostList.add(host);
        });
    }
}
