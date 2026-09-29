package ru.sber.transport.contractor.service;

import ru.sber.transport.contractor.database.model.Contractor;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * Экспортирует данные об контрагенте в exel
 */
public interface ContractorExporter {
    
    /**
     * Создание xlsx из коллекции contractor
     *
     * @param collectionContractor коллекция заявок
     *
     * @return стрим xlsx файла
     *
     * @throws IOException
     */
    ByteArrayOutputStream exportToExel(Collection<? extends Contractor> collectionContractor, HttpServletResponse response)
            throws IOException;
}
