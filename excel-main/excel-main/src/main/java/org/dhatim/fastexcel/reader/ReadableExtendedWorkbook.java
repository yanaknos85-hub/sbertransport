package org.dhatim.fastexcel.reader;

import lombok.SneakyThrows;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.xml.stream.XMLStreamException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Расширенная рабочая книга для чтения слитых ячеек.
 */
public class ReadableExtendedWorkbook extends ReadableWorkbook {

    private OPCPackage pkg;

    /**
     * Создать расширенную рабочую книгу.
     *
     * @param inputStream поток данных для парсинга.
     */
    public ReadableExtendedWorkbook(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    @SneakyThrows
    public void getPkg() {
        var field = ReadableWorkbook.class.getDeclaredField("pkg");
        field.trySetAccessible(); // NOSONAR
        pkg = (OPCPackage) field.get(this);
    }

    /**
     * Создать поток слитых ячеек.
     *
     * @param sheet страница.
     * @return поток слитых ячеек.
     */
    public Stream<CellRangeAddress> openMergeStream(Sheet sheet) throws IOException {
        try {
            var inputStream = this.pkg.getSheetContent(sheet);
            var stream = StreamSupport.stream(new MergeRegionSpliterator(inputStream), false);
            return stream.onClose(asUncheckedRunnable(inputStream));
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private static Runnable asUncheckedRunnable(Closeable c) {
        return () -> {
            try {
                c.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
