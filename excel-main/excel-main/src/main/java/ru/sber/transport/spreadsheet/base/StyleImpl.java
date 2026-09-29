package ru.sber.transport.spreadsheet.base;

import lombok.Data;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;
import ru.sber.transport.spreadsheet.style.Style;

import java.awt.Color;

/**
 * Style.
 */
@Data
public class StyleImpl implements Style {
    
    /**
     * Wrap the text on cells.
     */
    private Boolean wrapText;
    
    /**
     * Color of background.
     */
    private Color backgroundColor;

    /**
     * Color of font.
     */
    private Color fontColor;
    
    /**
     * Width of columns.
     */
    private Integer width;
    
    /**
     * Enable autosizing.
     */
    private Boolean autosize;

    /**
     * Style of borders.
     */
    private BorderStyle borderStyle;

    /**
     * Make text bold.
     */
    private Boolean bold;

    /**
     * Horizontal text alignment.
     */
    private HorizontalAlignment horizontalAlignment;

    /**
     * Vertical text alignment.
     */
    private VerticalAlignment verticalAlignment;

    @SneakyThrows(CloneNotSupportedException.class)
    @Override
    public Style clone() { // NOSONAR
        return (Style) super.clone();
    }
}
