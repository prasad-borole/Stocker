package com.stocker.expressions;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class EmojiReader {
    public static HashMap<String, String> emojiList = new HashMap<>();

    public static void emojiReader() {
        // TODO Auto-generated method stub
        try {
            int i = 1;
            String key = "", val = "";

            // InputStream ExcelFileToRead = new
            // FileInputStream("emojiList.xlsx");
            InputStream ExcelFileToRead = new FileInputStream("newEmojiList.xlsx");
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
                        else if (i == 2)
                            val = cell.getStringCellValue();
                    } else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
                        // if(cell.getStringCellValue()=="") continue;
                        // System.out.print(cell.getNumericCellValue()+" 2");
                    } else {
                        // U Can Handel Boolean, Formula, Errors
                    }

                    if (i == 2) {
                        // System.out.println(key+" "+val);
                        emojiList.put(key.trim(), val.trim());
                        i = 0;
                    }
                    i++;
                }
            }
            //////////////////////////////////////////////////
            // .........DONOT FORGET ESCAPE CHARACTER............
            //////////////////////////////////////////////////
            // System.out.println(emojiList.get("\\u1F633"));

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

    }

}
