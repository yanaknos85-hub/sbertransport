package ru.sber.transport.spreadsheet.style;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.awt.*;

public interface Style extends Cloneable {

    /**
     * Wrap the text on cells.
     */
    Boolean getWrapText();

    /**
     * Color of background.
     */
    Color getBackgroundColor();

    /**
     * Color of font.
     */
    Color getFontColor();

    /**
     * Width of columns.
     */
    Integer getWidth();

    /**
     * Enable autosizing.
     */
    Boolean getAutosize();

    /**
     * Style of borders.
     */
    BorderStyle getBorderStyle();

    /**
     * Make text bold.
     */
    Boolean getBold();

    /**
     * Vertical text alignment.
     */
    VerticalAlignment getVerticalAlignment();


    /**
     * Horizontal text alignment.
     */
    HorizontalAlignment getHorizontalAlignment();

    /**
     * Wrap the text on cells.
     */
    void setWrapText(Boolean wrapText);

    /**
     * Color of background.
     */
    void setBackgroundColor(Color indexedColors);

    /**
     * Color of font.
     */
    void setFontColor(Color indexedColors);

    /**
     * Width of columns.
     */
    void setWidth(Integer width);

    /**
     * Enable autosizing.
     */
    void setAutosize(Boolean autosize);

    /**
     * Style of borders.
     */
    void setBorderStyle(BorderStyle border);

    /**
     * Make text bold.
     */
    void setBold(Boolean bold);

    /**
     * Vertical text alignment.
     */
    void setVerticalAlignment(VerticalAlignment verticalAlignment);

    /**
     * Horizontal text alignment.
     */
    void setHorizontalAlignment(HorizontalAlignment horizontalAlignment);

    default Style merge(Style source) {
        if (source == null) {
            return this;
        }
        var cloned = this.clone();
        cloned.setWidth(source.getWidth() != null ? source.getWidth() : this.getWidth());
        cloned.setWrapText(source.getWrapText() != null ? source.getWrapText() : this.getWrapText());
        cloned.setBackgroundColor(source.getBackgroundColor() != null ? source.getBackgroundColor() : this.getBackgroundColor());
        cloned.setAutosize(source.getAutosize() != null ? source.getAutosize() : this.getAutosize());
        cloned.setFontColor(source.getFontColor() != null ? source.getFontColor() : this.getFontColor());
        cloned.setBorderStyle(source.getBorderStyle() != null ? source.getBorderStyle() : this.getBorderStyle());
        cloned.setBold(source.getBold() != null ? source.getBold() : this.getBold());
        cloned.setVerticalAlignment(source.getVerticalAlignment() != null ? source.getVerticalAlignment() : this.getVerticalAlignment());
        cloned.setHorizontalAlignment(source.getHorizontalAlignment() != null ? source.getHorizontalAlignment() : this.getHorizontalAlignment());
        return cloned;
    }

    Style clone(); // NOSONAR

}
