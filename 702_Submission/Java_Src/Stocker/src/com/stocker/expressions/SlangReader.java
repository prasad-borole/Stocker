package com.stocker.expressions;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SlangReader {
    public static HashMap<String, String> slangList = new HashMap<>();

    @SuppressWarnings("deprecation")
    public static void slangReader() {
        try {
            int i = 3;
            String key = "", val = "";

            InputStream ExcelFileToRead = new FileInputStream("slangList.xlsx");
            XSSFWorkbook wb = new XSSFWorkbook(ExcelFileToRead);

            XSSFWorkbook test = new XSSFWorkbook();

            XSSFSheet sheet = wb.getSheetAt(0);
            XSSFRow row;
            XSSFCell cell;

            Iterator rows = sheet.rowIterator();

            while (rows.hasNext()) {
                row = (XSSFRow) rows.next();
                Iterator cells = row.cellIterator();
                while (cells.hasNext()) {
                    cell = (XSSFCell) cells.next();

                    if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
                        if (cell.getStringCellValue() == "")
                            continue;
                        // System.out.print(i+" "+cell.getStringCellValue()+"
                        // ");
                        if (i == 1)
                            key = cell.getStringCellValue();
                        else if (i == 3)
                            val = cell.getStringCellValue();
                    } else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
                        // if(cell.getStringCellValue()=="") continue;
                        // System.out.print(cell.getNumericCellValue()+" 2");
                    } else {
                        // U Can Handel Boolean, Formula, Errors
                    }
                }
                if (i == 3) {
                    // System.out.println();
                    slangList.put(key.trim(), val.trim());
                    i = 0;
                }
                i++;
            }
            //////////////////////////////////////////////////
            // TEST the HashMap with Any keyword
            //////////////////////////////////////////////////
            // System.out.println(slangList.get("fml"));

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }
}
