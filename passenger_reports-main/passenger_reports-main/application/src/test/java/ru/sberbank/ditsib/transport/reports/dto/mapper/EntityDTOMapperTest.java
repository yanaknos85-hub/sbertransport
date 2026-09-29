package ru.sberbank.ditsib.transport.reports.dto.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.reports.dto.*;
import ru.sberbank.ditsib.transport.reports.mappers.*;
import ru.sberbank.ditsib.transport.reports.model.Contractor;
import ru.sberbank.ditsib.transport.reports.model.excel.CalculatedTripStatus;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistry;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistryString;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistryStringTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тест маппера")
class EntityDTOMapperTest {
    
    private final PositionMapper positionMapper = new PositionMapperImpl();
    
    private final DepartmentMapper departmentMapper = new DepartmentMapperImpl();
    
    private final EntityDTOMapper mapper = new EntityDTOMapperImpl(positionMapper, departmentMapper);
    
    @Test
    void contractorToDto() {
        Contractor contractor = Contractor.builder().id(UUID.randomUUID()).name("Contractor").build();
        ContractorDTO actual = mapper.contractorToDto(contractor);
        
        assertThat(actual.getId()).isEqualTo(contractor.getId());
        assertThat(actual.getName()).isEqualTo(contractor.getName());
    }
    
    @Test
    void dtoToContractor() {
        ContractorDTO dto = ContractorDTO.builder().id(UUID.randomUUID()).name("Contractor").build();
        Contractor actual = mapper.dtoToContractor(dto);
    
        assertThat(actual.getId()).isEqualTo(dto.getId());
        assertThat(actual.getName()).isEqualTo(dto.getName());
    }
    
    @Test
    void newDtoToTaxiRegistry() {
        NewTaxiTripRegistryDTO dto = NewTaxiTripRegistryDTO.builder()
                                                           .contractorId(UUID.randomUUID())
                                                           .date(LocalDate.of(2021, 2, 15))
                                                           .build();
        TaxiTripRegistry actual = mapper.newDtoToTaxiRegistry(dto);
        
        //дата конвертируется с первым числом месяца
        LocalDate convertedDate = actual.getDate();
        assertThat(convertedDate).isNotEqualTo(dto.getDate());
        assertThat(convertedDate.getMonth()).isEqualTo(dto.getDate().getMonth());
        assertThat(convertedDate.getYear()).isEqualTo(dto.getDate().getYear());
        assertThat(convertedDate.getDayOfMonth()).isEqualTo(1);
        assertThat(actual.getContractor().getId()).isEqualTo(dto.getContractorId());
    }
    
    @Test
    void taxiRegistryStringToDto() {
        TaxiTripRegistryString entity = TaxiTripRegistryString.builder()
                                                              .id(UUID.randomUUID())
                                                              .calculatedTripStatus(CalculatedTripStatus.DONE)
                                                              .calculatedTripType(TripType.COOP)
                                                              .parsedString(new TaxiTripRegistryStringTemplate())
                                                              .validTaxiId0(true)
                                                              .validWaitTime8(true)
                                                              .build();
        
        TaxiTripRegistryStringDTO actual = mapper.taxiRegistryStringToDto(entity);
        
        assertThat(actual.getId()).isEqualTo(entity.getId());
        assertThat(actual.getCalculatedTripStatus().name()).isEqualTo(entity.getCalculatedTripStatus().name());
        assertThat(actual.getCalculatedTripType().name()).isEqualTo(entity.getCalculatedTripType().name());
        assertThat(actual.getValidTaxiId0()).isEqualTo(true);
        assertThat(actual.getValidTripStatus1()).isEqualTo(false);
        assertThat(actual.getValidCalcDistance4()).isEqualTo(false);
        assertThat(actual.getValidFactCost7()).isEqualTo(false);
        assertThat(actual.getValidWaitTime8()).isEqualTo(true);
    }
    
    @Test
    void taxiRegistryStringsToDtoList() {
        int size = 10;
        List<TaxiTripRegistryString> strings = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            strings.add(i, TaxiTripRegistryString.builder()
                                                 .id(UUID.randomUUID())
                                                 .calculatedTripStatus(CalculatedTripStatus.CANCELED)
                                                 .calculatedTripType(TripType.SINGLE)
                                                 .parsedString(new TaxiTripRegistryStringTemplate())
                                                 .validTaxiId0(true)
                                                 .validWaitTime8(true)
                                                 .build());
        }
        List<TaxiTripRegistryStringDTO> actual = mapper.taxiRegistryStringsToDtoList(strings);
        
        assertThat(actual.size()).isEqualTo(size);
        TaxiTripRegistryStringDTO actual1 = actual.get(0);
        assertThat(actual1.getId()).isEqualTo(strings.get(0).getId());
        assertThat(actual1.getCalculatedTripStatus().name()).isEqualTo(CalculatedTripStatus.CANCELED.name());
        assertThat(actual1.getCalculatedTripType().name()).isEqualTo(TripType.SINGLE.name());
        assertThat(actual1.getValidTaxiId0()).isEqualTo(true);
        assertThat(actual1.getValidTripStatus1()).isEqualTo(false);
        assertThat(actual1.getValidCalcDistance4()).isEqualTo(false);
        assertThat(actual1.getValidFactCost7()).isEqualTo(false);
        assertThat(actual1.getValidWaitTime8()).isEqualTo(true);
    }
    
    @Test
    void TaxiRegistryToShortDtoWithoutContractorList() {Contractor contractor = Contractor.builder().id(UUID.randomUUID()).name("Contractor").build();
        Contractor contractor2 = Contractor.builder().id(UUID.randomUUID()).name("Contractor2").build();
    
        TaxiTripRegistry reg1 = TaxiTripRegistry.builder()
                                                .id(UUID.randomUUID()).contractor(contractor)
                                                .date(LocalDate.of(2021, 1, 15))
                                                .registryStrings(createTaxiRegistryStrings(10))
                                                .blankCells(Set.of("A1", "B2")).incorrectTypeCells(Set.of("D4", "Z15"))
                                                .build();
    
        TaxiTripRegistry reg2 = TaxiTripRegistry.builder().id(UUID.randomUUID()).contractor(contractor)
                                                .date(LocalDate.of(2021, 2, 10))
                                                .registryStrings(createTaxiRegistryStrings(15))
                                                .blankCells(Set.of("V5", "X5")).incorrectTypeCells(Set.of("F12"))
                                                .build();
    
        TaxiTripRegistry reg3 = TaxiTripRegistry.builder()
                                                .id(UUID.randomUUID()).contractor(contractor2)
                                                .date(LocalDate.of(2021, 1, 5))
                                                .registryStrings(createTaxiRegistryStrings(5))
                                                .blankCells(Set.of("C3")).incorrectTypeCells(Set.of("G4", "H15"))
                                                .build();
    
        List<TaxiTripRegistry> registries = List.of(reg1, reg2, reg3);
    
        List<TaxiTripRegistryShortWithoutContractorDTO> actualList =
                mapper.taxiRegistryToShortDtoListWithoutContractor(registries);
        assertThat(actualList.size()).isEqualTo(registries.size());
        checkRegistryShortDto(actualList.get(0), reg1);
        checkRegistryShortDto(actualList.get(1), reg2);
        checkRegistryShortDto(actualList.get(2), reg3);
    }
    
    @Test
    void taxiRegistryToDtoAndToShortDto() {
        TaxiTripRegistryStringTemplate str1 = TaxiTripRegistryStringTemplate.builder().ordinal(0).build();
        TaxiTripRegistryStringTemplate str2 = TaxiTripRegistryStringTemplate.builder().ordinal(1).build();
        TaxiTripRegistryStringTemplate str3 = TaxiTripRegistryStringTemplate.builder().ordinal(2).build();
        List<TaxiTripRegistryStringTemplate> importedStrings = List.of(str1, str2, str3);
        
        Contractor contractor = Contractor.builder().id(UUID.randomUUID()).name("Contractor").build();
        
        TaxiTripRegistry registry = TaxiTripRegistry.builder()
                                                    .valid(true)
                                                    .id(UUID.randomUUID())
                                                    .contractor(contractor)
                                                    .date(LocalDate.now())
                                                    .blankCells(Set.of("A1", "B2"))
                                                    .incorrectTypeCells(Set.of("C3", "D4"))
                                                    .build();
        
        registry.setRegistryStringsFromParsedStrings(importedStrings);
        assertThat(registry.getRegistryStrings().size()).isEqualTo(3);
    
        TaxiTripRegistryDTO actual = mapper.taxiRegistryToDto(registry);
        checkRegistryDto(actual, registry);
    
        TaxiTripRegistryShortDTO actual2 = mapper.taxiRegistryToShortDto(registry);
    
        assertThat(actual2.getId()).isEqualTo(registry.getId());
        assertThat(actual2.getValid()).isEqualTo(registry.getValid());
        assertThat(actual2.getContractor().getId()).isEqualTo(registry.getContractor().getId());
        assertThat(actual2.getContractor().getName()).isEqualTo(registry.getContractor().getName());
        assertThat(actual2.getDate()).isEqualTo(registry.getDate());
        assertThat(actual2.getStringsQnt()).isEqualTo(registry.getRegistryStrings().size());
    }
    
    @Test
    void taxiRegistryToDtoListAndToShortDtoList() {
        Contractor contractor = Contractor.builder().id(UUID.randomUUID()).name("Contractor").build();
        Contractor contractor2 = Contractor.builder().id(UUID.randomUUID()).name("Contractor2").build();
    
        TaxiTripRegistry reg1 = TaxiTripRegistry.builder()
                                                .id(UUID.randomUUID()).contractor(contractor)
                                                .date(LocalDate.of(2021, 1, 15))
                                                .registryStrings(createTaxiRegistryStrings(10))
                                                .blankCells(Set.of("A1", "B2")).incorrectTypeCells(Set.of("D4", "Z15"))
                                                .build();
        
        TaxiTripRegistry reg2 = TaxiTripRegistry.builder().id(UUID.randomUUID()).contractor(contractor)
                                                .date(LocalDate.of(2021, 2, 10))
                                                .registryStrings(createTaxiRegistryStrings(15))
                                                .blankCells(Set.of("V5", "X5")).incorrectTypeCells(Set.of("F12"))
                                                .build();
    
        TaxiTripRegistry reg3 = TaxiTripRegistry.builder()
                                                .id(UUID.randomUUID()).contractor(contractor2)
                                                .date(LocalDate.of(2021, 1, 5))
                                                .registryStrings(createTaxiRegistryStrings(5))
                                                .blankCells(Set.of("C3")).incorrectTypeCells(Set.of("G4", "H15"))
                                                .build();
        
        List<TaxiTripRegistry> registries = List.of(reg1, reg2, reg3);
    
        List<TaxiTripRegistryDTO> actualList = mapper.taxiRegistryToDtoList(registries);
        assertThat(actualList.size()).isEqualTo(registries.size());
        checkRegistryDto(actualList.get(0), reg1);
        checkRegistryDto(actualList.get(1), reg2);
        checkRegistryDto(actualList.get(2), reg3);
    
        List<TaxiTripRegistryShortDTO> shortList = mapper.taxiRegistryToShortDtoList(registries);
        assertThat(shortList.size()).isEqualTo(registries.size());
        checkRegistryShortDto(shortList.get(0), reg1);
        checkRegistryShortDto(shortList.get(1), reg2);
        checkRegistryShortDto(shortList.get(2), reg3);
    }
    
    /**
     * Вернуть список строк Реестра (в строке - только id)
     * @param stringsQnt количество строк
     * @return список строк Реестра
     */
    private List<TaxiTripRegistryString> createTaxiRegistryStrings(int stringsQnt) {
        List<TaxiTripRegistryString> strings = new ArrayList<>();
        for (int i = 0; i < stringsQnt; i++) {
            strings.add(TaxiTripRegistryString.builder().id(UUID.randomUUID()).build());
        }
        return strings;
    }
    
    /**
     * Проверка DTO по реестру
     * @param actual
     * @param registry
     */
    private void checkRegistryDto(TaxiTripRegistryDTO actual, TaxiTripRegistry registry) {
        assertThat(actual.getStringsQnt()).isEqualTo(registry.getRegistryStrings().size());
        assertThat(actual.getBlankCells().size()).isEqualTo(registry.getBlankCells().size());
        assertThat(actual.getBlankCells().containsAll(registry.getBlankCells())).isTrue();
        assertThat(actual.getIncorrectTypeCells().size()).isEqualTo(registry.getIncorrectTypeCells().size());
        assertThat(actual.getIncorrectTypeCells().containsAll(registry.getIncorrectTypeCells())).isTrue();
        assertThat(actual.getValid()).isEqualTo(registry.getValid());
        assertThat(actual.getId()).isEqualTo(registry.getId());
        assertThat(actual.getContractor().getId()).isEqualTo(registry.getContractor().getId());
        assertThat(actual.getDate().getMonth()).isEqualTo(registry.getDate().getMonth());
        assertThat(actual.getDate().getYear()).isEqualTo(registry.getDate().getYear());
    }
    
    /**
     * Проверка shortDTO по реестру
     * @param actual
     * @param registry
     */
    private void checkRegistryShortDto(TaxiTripRegistryShortDTO actual, TaxiTripRegistry registry) {
        assertThat(actual.getStringsQnt()).isEqualTo(registry.getRegistryStrings().size());
        assertThat(actual.getValid()).isEqualTo(registry.getValid());
        assertThat(actual.getContractor().getName()).isEqualTo(registry.getContractor().getName());
        assertThat(actual.getContractor().getId()).isEqualTo(registry.getContractor().getId());
        assertThat(actual.getId()).isEqualTo(registry.getId());
        assertThat(actual.getDate().getMonth()).isEqualTo(registry.getDate().getMonth());
        assertThat(actual.getDate().getYear()).isEqualTo(registry.getDate().getYear());
    }
    
    /**
     * Проверка shortDTOWithoutContractor по реестру
     * @param actual
     * @param registry
     */
    private void checkRegistryShortDto(TaxiTripRegistryShortWithoutContractorDTO actual, TaxiTripRegistry registry) {
        assertThat(actual.getStringsQnt()).isEqualTo(registry.getRegistryStrings().size());
        assertThat(actual.getValid()).isEqualTo(registry.getValid());
        assertThat(actual.getId()).isEqualTo(registry.getId());
        assertThat(actual.getDate().getMonth()).isEqualTo(registry.getDate().getMonth());
        assertThat(actual.getDate().getYear()).isEqualTo(registry.getDate().getYear());
    }
}