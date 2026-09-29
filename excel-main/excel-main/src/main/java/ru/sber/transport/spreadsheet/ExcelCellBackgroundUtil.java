package ru.sber.transport.spreadsheet;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.awt.*;

public class ExcelCellBackgroundUtil {

    private ExcelCellBackgroundUtil(){
        
    }

    public static void setCellBackground(CellStyle style, Color color) {
        if (style instanceof XSSFCellStyle xssf) {
            setXssfCellBackground(xssf, color);
        } else if (style instanceof HSSFCellStyle hssf) {
            setHssfCellBackground(hssf, color);
        }
    }

    public static void setXssfCellBackground(XSSFCellStyle style, Color color) {
        var colorBytes = new byte[]{(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()};
        style.setFillForegroundColor(new XSSFColor(colorBytes));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    public static void setHssfCellBackground(HSSFCellStyle style, Color color) {
        style.setFillForegroundColor(new HSSFColor(0x41, -2, color));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
}
