package ru.sberbank.ditsib.transport.reports.model.cargo;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = "reports", name = "cargo_detail_stats")
@Data
@SuperBuilder
@NoArgsConstructor
public class CargoDetail {
    /**
     * Идентификатор N-го груза
     */
    @Id
    @Column(name = "id")
    private UUID id;
    
    /**
     * Порядковый номер
     */
    @Column(name = "position")
    private Integer position;
    
    /**
     * Наименование груза.
     */
    @Column(name = "cargo_name")
    private String cargoName;
    
    /**
     * Вид груза.
     */
    @Column(name = "cargo_type")
    private String cargoType;
    
    /**
     * Категория груза.
     */
    @Column(name = "cargo_category")
    private String cargoCategory;
    
    /**
     * Связанная заявка, в которой используется груз
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stats_id")
    private RequestForCargo request;
    
    /**
     * Характер груза (хрупкий груз)
     */
    @Column(name = "fragile")
    @Builder.Default
    private boolean fragile = false;
    
    /**
     * Требуется упаковка
     */
    @Column(name = "need_package")
    @Builder.Default
    private boolean needPackage = false;
    
    /**
     * Идентификатор типа упаковки
     */
    @Column(name = "package_id")
    private UUID packageId;
    
    /**
     * Колличество упаковок
     */
    @Column(name = "package_count")
    private Integer packageCount = 0;
    
    /**
     * Информация для печатно формы ТТН
     * @return
     */
    public String getInfo() {
        return getPosition() +") "+ (getCargoCategory() == null ? "" : (getCargoCategory()))+
               (getCargoType() == null ? "" : (" " + getCargoType())) +
               (getCargoName() == null ? "" : (" " + getCargoName()))+";";
    }
}

