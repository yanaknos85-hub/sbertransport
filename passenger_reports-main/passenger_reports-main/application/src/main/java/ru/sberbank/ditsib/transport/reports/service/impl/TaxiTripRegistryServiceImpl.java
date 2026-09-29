package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.spreadsheet.excel.Excel;
import ru.sber.transport.spreadsheet.excel.WorkbookType;
import ru.sberbank.ditsib.transport.reports.dao.TaxiTripRegistryRepository;
import ru.sberbank.ditsib.transport.reports.dto.NewTaxiTripRegistryDTO;
import ru.sberbank.ditsib.transport.reports.dto.RegistryPerContractorDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryShortDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryShortWithoutContractorDTO;
import ru.sberbank.ditsib.transport.reports.exception.FileActionsFailsException;
import ru.sberbank.ditsib.transport.reports.exception.FolderOrFileNotFoundException;
import ru.sberbank.ditsib.transport.reports.mappers.EntityDTOMapper;
import ru.sberbank.ditsib.transport.reports.model.Contractor;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistry;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistryStringTemplate;
import ru.sberbank.ditsib.transport.reports.pojo.FileUploader;
import ru.sberbank.ditsib.transport.reports.pojo.TaxiTripRegistryChecker;
import ru.sberbank.ditsib.transport.reports.service.ContractorService;
import ru.sberbank.ditsib.transport.reports.service.ExcelParser;
import ru.sberbank.ditsib.transport.reports.service.TaxiTripRegistryService;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaxiTripRegistryServiceImpl implements TaxiTripRegistryService {
    
    private final static String FILE_SEPARATOR = File.separator;
    private final static String DOUBLE_FILE_SEPARATOR = FILE_SEPARATOR + FILE_SEPARATOR;
    private final TaxiTripRegistryChecker registryChecker;
    private final EntityDTOMapper mapper;
    private final TaxiTripRegistryRepository registryRepository;
    private final ContractorService contractorService;
    private final FileUploader fileUploader;
    // Количество строк в шапке таблицы
    @Value("${taxi-trip-registry.header-size}")
    private int headerStringsQnt;
    // Количество строк в итоговой части таблицы внизу
    @Value("${taxi-trip-registry.footer-size}")
    private int footerStringsQnt;
    // Количество пустых столбцов слева
    @Value("${taxi-trip-registry.left-indent}")
    private int leftEmptyRowsQnt;
    // Номер строки, содержащей заголовки столбцов
    @Value("${taxi-trip-registry.column-names-row}")
    private int columnNamesRowNum;
    
    private ExcelParser<TaxiTripRegistryStringTemplate> excelParser = new ExcelParserImpl<>(TaxiTripRegistryStringTemplate.class);
    
    @Override
    public Resource downloadFileAsResource(UUID contractorId, Integer year, Integer month) {
        
        TaxiTripRegistry registryFromDb = getRegistryFromDb
                (contractorId, LocalDate.of(year, month, 1));
        String uploadFileString = registryFromDb.getUploadFileFullName();
        Path fileAbsolutePath = Paths.get(uploadFileString).toAbsolutePath().normalize();
        checkFileExistence(fileAbsolutePath);
        return getResourceFromPath(fileAbsolutePath);
    }
    
    @Override
    public TaxiTripRegistryDTO findRegistry(UUID contractorId, LocalDate date) {
        LocalDate convertedDate = convertDateWithFirstDayOfMonth(date);
        TaxiTripRegistry registryFromDb = getRegistryFromDb(contractorId, convertedDate);
        return mapper.taxiRegistryToDto(registryFromDb);
    }
    
    @Override
    public List<TaxiTripRegistryShortDTO> findAllRegistries(UUID contractorId) {
        contractorService.findById(contractorId).orElseThrow(() -> new EntityNotFoundException(
                Contractor.class, contractorId));
        List<TaxiTripRegistry> registries = registryRepository.findByContractorIdOrderByDateAsc(contractorId);
        return mapper.taxiRegistryToShortDtoList(registries);
    }
    
    
    @Override
    public Set<RegistryPerContractorDTO> findAllRegistries() {
        List<TaxiTripRegistry> registries = registryRepository.findAll();
        List<Contractor> contractors = contractorService.findAll();
        Set<RegistryPerContractorDTO> registryPerContractorDTOs = new HashSet<>();
        contractors.forEach(contractor -> {
            List<TaxiTripRegistryShortWithoutContractorDTO> taxiTripRegisters = new LinkedList<>();
            registries.stream()
                      .filter(registry -> registry.getContractor().getId().equals(contractor.getId()))
                      .sorted(Comparator.comparing(TaxiTripRegistry::getDate))
                      .forEach(registry -> taxiTripRegisters.add(mapper.taxiRegistryToShortDtoWithoutContractor(registry)));
            if (!taxiTripRegisters.isEmpty()) {
                registryPerContractorDTOs.add(new RegistryPerContractorDTO(mapper.contractorToDto(contractor), taxiTripRegisters));
            }
        });
        return registryPerContractorDTOs;
    }
    
    @Override
    public TaxiTripRegistryDTO importRegistry(NewTaxiTripRegistryDTO registryDTO, MultipartFile file) {
        TaxiTripRegistry registry = mapper.newDtoToTaxiRegistry(registryDTO);
        checkRegistryIsUniqueByContractorAndDate(registry);
        Contractor contractorFromDb = getContractorFromDb(registry.getContractor().getId());
        registry.setContractor(contractorFromDb);
        //перед чтением файла поместить его в volume
        Path filePath = fileUploader.uploadFileAndGetServerPath(file);
        //если что-то пойдет не так, удалить папку и пробросить ошибку дальше
        try {
            importExcelFileToRegistry(filePath.toString(), registry);
            registryChecker.checkStrings(registry);
            // сохраняем путь
            registry.setUploadFileFullName(filePath.toString());
            registry = registryRepository.save(registry);
            return mapper.taxiRegistryToDto(registry);
        } catch (Throwable ex) {
            fileUploader.deleteFolderWithFiles(filePath.getParent());
            throw ex;
        }
    }
    
    @Override
    public TaxiTripRegistryDTO replaceRegistry(UUID registryId, MultipartFile file) {
        TaxiTripRegistry registryFromDb = getRegistryFromDb(registryId);
        // ссылка на старый файл
        String uploadFileFullName = registryFromDb.getUploadFileFullName();
        Path filePathOld = Paths.get(uploadFileFullName).toAbsolutePath().normalize();
        
        //перед чтением файла поместить его в volume
        Path filePath = fileUploader.uploadFileAndGetServerPath(file);
        //если что-то пойдет не так, удалить папку и пробросить ошибку дальше
        try {
            importExcelFileToRegistry(filePath.toString(), registryFromDb);
            registryChecker.checkStrings(registryFromDb);
            // сохраняем путь
            registryFromDb.setUploadFileFullName(filePath.toString());
            registryFromDb = registryRepository.save(registryFromDb);
            // удаояем старый файл с папкой
            fileUploader.deleteFolderWithFiles(filePathOld.getParent());
            return mapper.taxiRegistryToDto(registryFromDb);
        } catch (Throwable ex) {
            fileUploader.deleteFolderWithFiles(filePath.getParent());
            throw ex;
        }
    }
    
    @Override
    public List<TaxiTripRegistry> findAllBySpec(Specification<TaxiTripRegistry> spec) {
        return registryRepository.findAll(spec);
    }
    
    /**
     * Вернуть копию даты с первым днем месяца. Необходимо для поиска, так как все даты реестров сохраняются за первое число месяца
     *
     * @param date дата
     *
     * @return сконвертированная дата
     */
    private LocalDate convertDateWithFirstDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }
    
    /**
     * Найти Реестр по ID контрагента и дате
     *
     * @param contractorId ID контрагента
     * @param date дата
     *
     * @return Реестр
     */
    private TaxiTripRegistry getRegistryFromDb(UUID contractorId, LocalDate date) {
        return registryRepository.findByContractorIdAndDate(contractorId, date).orElseThrow(() -> new EntityNotFoundException(TaxiTripRegistry.class,
                                                                                                                              date));
    }
    
    /**
     * Бросить исключение, если найден Реестр в БД по ID контрагента и дате
     *
     * @param registry Реестр из DTO
     */
    private void checkRegistryIsUniqueByContractorAndDate(TaxiTripRegistry registry) {
        var optionalRegistry = registryRepository.findByContractorIdAndDate(registry.getContractor().getId(), registry.getDate());
        if (optionalRegistry.isPresent()) {
            var registryFromDb = optionalRegistry.get();
            throw new DuplicateDataException(TaxiTripRegistry.class, "contractorId - date",
                    String.format("%s - %s", registryFromDb.getContractor().getId(), registryFromDb.getDate()));
        }
    }
    
    /**
     * Получение контрагента по его ID (или исключения)
     *
     * @param contractorId ID контрагента
     *
     * @return контрагент
     */
    private Contractor getContractorFromDb(UUID contractorId) {
        return contractorService.findById(contractorId).orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));
    }
    
    /**
     * Получение реестра по его ID (или исключения)
     *
     * @param registryId ID реестра
     *
     * @return реестр
     */
    private TaxiTripRegistry getRegistryFromDb(UUID registryId) {
        return registryRepository.findById(registryId).orElseThrow(() -> {
            throw new EntityNotFoundException(TaxiTripRegistry.class, registryId);
        });
    }
    
    /**
     * Импортировать файл с проверкой шапки и записать данные / ошибки в реестр
     *
     * @param fileName полное имя файла
     * @param registry реестр
     */
    private void importExcelFileToRegistry(String fileName, TaxiTripRegistry registry) {
        List<String> headerTemplate = List.of(
                "№ п/п", "ID заявки", "Подразделение заказчика (код МВЗ)*", "Желаемое время подачи автомобиля",
                "Дата подачи ТС", "Вид тарифа", "Адрес подачи ТС", "Промежуточные адреса- точки маршрута",
                "Адрес конечного пункта", "Время подачи ТС", "Время начала исполнения заказа",
                "Время окончания исполнения заказа", "Время ожидания при подаче (простой), мин.",
                "Время ожидания в промежуточных точках, мин", "Протяженность маршрута, км.",
                "Стоимость минимальной поездки, руб. (без НДС)", "Тариф за время ожидания, руб./мин. (без НДС)",
                "Тариф, руб./км. (без НДС)", "Итого (без НДС), руб.", "Итого НДС, руб.", "Итого c НДС, руб. ");
        
        final var excel = new Excel(WorkbookType.XLSX);
        
        List<TaxiTripRegistryStringTemplate> parsedStrings = excelParser.parsingExcelTableWithHeaderCheck(
                fileName,
                headerStringsQnt, footerStringsQnt, leftEmptyRowsQnt,
                columnNamesRowNum, headerTemplate);
        
        registry.setRegistryStringsFromParsedStrings(parsedStrings);
        registry.setBlankCells(excelParser.getBlankCells());
        registry.setIncorrectTypeCells(excelParser.getIncorrectTypeCells());
    }
    
    /**
     * Создать Resource из Path к файлу
     *
     * @param fileAbsolutePath Path
     *
     * @return Resource
     */
    private Resource getResourceFromPath(Path fileAbsolutePath) {
        Resource resource;
        try {
            resource = new UrlResource(fileAbsolutePath.toUri());
        } catch (MalformedURLException e) {
            throw new FileActionsFailsException(
                    FileActionsFailsException.FILE_DOWNLOAD_FORMAT,
                    getFolderNameFromFolderPathOrFileNameFromFilePath(fileAbsolutePath.toString()),
                    getFolderNameFromFilePath(fileAbsolutePath.toString())
            );
        }
        return resource;
    }
    
    /**
     * Проверить наличие файла по указанному пути
     *
     * @param absolutePath путь к файлу
     */
    private void checkFileExistence(Path absolutePath) {
        File potentialFile = absolutePath.toFile();
        if (!potentialFile.isFile()) {
            throw new FolderOrFileNotFoundException(
                    FolderOrFileNotFoundException.FILE_FORMAT,
                    getFolderNameFromFolderPathOrFileNameFromFilePath(absolutePath.toString()),
                    getFolderNameFromFilePath(absolutePath.toString())
            );
        }
    }
    
    /**
     * Выделить название папки / файла из пути к папке / файлу соответственно
     *
     * @param folderOrFilePathString String
     *
     * @return название папки / файла
     */
    private String getFolderNameFromFolderPathOrFileNameFromFilePath(String folderOrFilePathString) {
        String[] split = getSeparatedPath(folderOrFilePathString);
        return split[split.length - 1];
    }
    
    /**
     * Выделить название папки из пути к файлу
     *
     * @param pathString String
     *
     * @return название папки
     */
    private String getFolderNameFromFilePath(String pathString) {
        String[] split = getSeparatedPath(pathString);
        return split[split.length - 2];
    }
    
    /**
     * Вернуть путь, разделенный по программно вычисленному разделителю пути, в зависимости от ОС
     *
     * @param pathString String
     *
     * @return путь, разделенный по программно вычисленному разделителю пути
     */
    private String[] getSeparatedPath(String pathString) {
        String[] split;
        if (FILE_SEPARATOR.equals("/")) {
            split = pathString.split(FILE_SEPARATOR);
        } else {
            split = pathString.split(DOUBLE_FILE_SEPARATOR);
        }
        return split;
    }
}
