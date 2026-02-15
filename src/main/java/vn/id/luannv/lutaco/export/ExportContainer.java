package vn.id.luannv.lutaco.export;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import vn.id.luannv.lutaco.dto.PeriodWindow;

import java.time.LocalDateTime;

@UtilityClass
public class ExportContainer {

    public record ExportContext(
            String author,
            String authorId,
            PeriodWindow window,
            LocalDateTime exportedAt,
            String range
    ) {}

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ExcelStyles {
        XSSFCellStyle bold;
        XSSFCellStyle centerBold;
        XSSFCellStyle money;
    }

    public static ExcelStyles createCommonStyles(XSSFWorkbook workbook) {
        XSSFFont boldFont = workbook.createFont();
        boldFont.setBold(true);

        XSSFCellStyle bold = workbook.createCellStyle();
        bold.setFont(boldFont);

        XSSFCellStyle centerBold = workbook.createCellStyle();
        centerBold.setFont(boldFont);
        centerBold.setAlignment(HorizontalAlignment.CENTER);

        XSSFCellStyle money = workbook.createCellStyle();
        money.setDataFormat(
                workbook.createDataFormat().getFormat("#,##0")
        );
        money.setAlignment(HorizontalAlignment.CENTER);

        return new ExcelStyles(bold, centerBold, money);
    }

    public static int createMetaRow(
            XSSFSheet sheet,
            int rowIdx,
            String label,
            String value,
            XSSFCellStyle labelStyle
    ) {
        XSSFRow row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
        row.getCell(0).setCellStyle(labelStyle);
        return rowIdx + 1;
    }

    public static int createTitleRow(
            XSSFSheet sheet,
            int rowIdx,
            String title,
            int fromCol,
            int toCol,
            XSSFCellStyle style
    ) {
        XSSFRow row = sheet.createRow(rowIdx);
        row.createCell(fromCol).setCellValue(title);
        row.getCell(fromCol).setCellStyle(style);

        sheet.addMergedRegion(
                new CellRangeAddress(rowIdx, rowIdx, fromCol, toCol)
        );
        return rowIdx + 1;
    }

}