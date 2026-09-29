package ru.sberbank.ditsib.transport.srm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.srm.config.SrmSettingNames;

@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Table(schema = "srm", name = "srm_setting")
@Data
public class SrmSetting {
    
    /**
     * Name
     */
    @Id
    @Column(name = "name", nullable = false)
    @Enumerated(EnumType.STRING)
    private SrmSettingNames name;
    
    /**
     * Value
     */
    @Column(name = "value", nullable = false)
    private String value;
}
