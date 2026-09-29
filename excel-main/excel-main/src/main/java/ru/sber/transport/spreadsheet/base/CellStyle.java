package ru.sber.transport.spreadsheet.base;

import lombok.*;
import org.apache.poi.ss.usermodel.*;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.style.Style;

import java.awt.Color;

/**
 * Style.
 */
@Getter
@Builder(builderMethodName = "newBuilder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CellStyle implements Style {

    /**
     * Создать новый builder стилей.
     *
     * @param column столбец со стилем.
     * @return builder стилей.
     */
    public static CellStyleBuilder builder(Column<?, ?> column) {
        return CellStyle.newBuilder().column(column);
    }

    private Column<?, ?> column;

    /**
     * Wrap the text on cells.
     */
    @Setter
    private Boolean wrapText;

    /**
     * Color of background.
     */
    @Setter
    private Color backgroundColor;

    /**
     * Color of font.
     */
    @Setter
    private Color fontColor;

    /**
     * Width of columns.
     */
    @Setter
    private Integer width;

    /**
     * Enable autosizing.
     */
    @Setter
    private Boolean autosize;

    /**
     * Style of borders.
     */
    @Setter
    private BorderStyle borderStyle;

    /**
     * Make text bold.
     */
    @Setter
    private Boolean bold;

    /**
     * Horizontal text alignment.
     */
    @Setter
    private HorizontalAlignment horizontalAlignment;

    /**
     * Vertical text alignment.
     */
    @Setter
    private VerticalAlignment verticalAlignment;

    @SneakyThrows(CloneNotSupportedException.class)
    @Override
    public Style clone() { // NOSONAR
        return (Style) super.clone();
    }
    
}
