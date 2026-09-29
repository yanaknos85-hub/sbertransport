package ru.sberbank.ditsib.transport.reports.model.excel;

import lombok.*;
import org.hibernate.annotations.Cascade;
import ru.sberbank.ditsib.transport.reports.model.Contractor;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Реестр поездок на такси от контрагента за месяц
 */
@Entity
@Table(schema = "reports", name = "taxi_trip_registry",
       uniqueConstraints = @UniqueConstraint(columnNames = {"contractor_id", "date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxiTripRegistry {

    /**
     * ID реестра
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Валидность всего реестра. Определяется по результатам прохождения проверок каждой из строк реестра
     */
    @Column(name = "is_valid", nullable = false)
    @Builder.Default
    private Boolean valid = false;

    /**
     * Контрагент
     */
    @OneToOne
    @JoinColumn(name = "contractor_id", nullable = false)
    private Contractor contractor;

    /**
     * Дата (месяц и год) реестра
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    /**
     * Список строк реестра. Каждая строка содержит импортированные из excel данные + результаты проверок
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "registry_id")
    private List<TaxiTripRegistryString> registryStrings = new ArrayList<>();
    
    /**
     * Список ячеек с пустыми значениями
     */
    @ElementCollection
    @CollectionTable(
            schema = "reports",
            name = "blank_cell_address",
            joinColumns = @JoinColumn(name = "registry_id")
    )
    @Column(name = "blank_cell")
    @Builder.Default
    private Set<String> blankCells = new HashSet<>();

    /**
     * Список ячеек с ошибками - несоответствие типов данных таблицы и класса-шаблона, а также формулы/ошибки excel
     */
    @ElementCollection
    @CollectionTable(
            schema = "reports",
            name = "incorrect_type_cell_address",
            joinColumns = @JoinColumn(name = "registry_id")
    )
    @Column(name = "incorrect_cell")
    @Builder.Default
    private Set<String> incorrectTypeCells = new HashSet<>();
    
    /**
     * Полное имя сохраненного файла
     */
    @Column(name = "upload_file_full_name")
    private String uploadFileFullName;
    
    /**
     * Заполнить список строк реестра из списка импортированных строк
     * @param parsedStrings список импортированных строк
     */
    public void setRegistryStringsFromParsedStrings(List<TaxiTripRegistryStringTemplate> parsedStrings) {
        registryStrings = new ArrayList<>(parsedStrings.size());
        
        for (int i = 0; i < parsedStrings.size(); i++) {
            registryStrings.add(i, TaxiTripRegistryString.builder().parsedString(parsedStrings.get(i)).build());
        }
    }
}
