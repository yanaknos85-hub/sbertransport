package org.dhatim.fastexcel.reader;

import org.apache.poi.ss.util.CellRangeAddress;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Итератор по смерженным ячейкам.
 */
public class MergeRegionSpliterator implements Spliterator<CellRangeAddress> {

    private static final String MERGE_CELLS = "mergeCells";

    private static final String MERGE_CELL = "mergeCell";

    private final SimpleXmlReader reader;

    /**
     * Создать итератор.
     *
     * @param inputStream поток данных для парсинга.
     */
    public MergeRegionSpliterator(InputStream inputStream) throws XMLStreamException {
        reader = new SimpleXmlReader(DefaultXMLInputFactory.factory, inputStream);
        reader.goTo(MERGE_CELLS);
    }

    @Override
    public boolean tryAdvance(Consumer<? super CellRangeAddress> action) {
        try {
            if (hasNext()) {
                action.accept(next());
                return true;
            } else {
                return false;
            }
        } catch (XMLStreamException var3) {
            throw new ExcelReaderException(var3);
        }
    }

    @Override
    public Spliterator<CellRangeAddress> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.IMMUTABLE;
    }

    private boolean hasNext() throws XMLStreamException {
        return reader.goTo(() -> reader.isStartElement(MERGE_CELL)
                || reader.isEndElement(MERGE_CELLS))
                && MERGE_CELL.equals(reader.getLocalName());
    }

    private CellRangeAddress next() {
        if (!MERGE_CELL.equals(reader.getLocalName())) {
            throw new NoSuchElementException();
        } else {
            var ref = reader.getAttribute("ref");
            var cells = ref.split(":");
            var firstCell = new CellAddress(cells[0]);
            var secondCell = new CellAddress(cells[1]);
            return new CellRangeAddress(firstCell.getRow(), secondCell.getRow(), firstCell.getColumn(), secondCell.getColumn());
        }
    }
}
