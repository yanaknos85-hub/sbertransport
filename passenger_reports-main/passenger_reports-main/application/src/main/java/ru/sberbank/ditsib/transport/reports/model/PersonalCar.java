package ru.sberbank.ditsib.transport.reports.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.domain.Persistable;
import ru.sberbank.ditsib.transport.constants.PersonalCarOwnerInfo;
import ru.sberbank.ditsib.transport.reports.dto.personalTransport.PersonalCarDTO;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Entity of personal car
 */
@Entity
@Table(schema = "reports", name = "personal_auto")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
@SqlResultSetMapping(
        name="PersonalCarDTO",
        classes={
                @ConstructorResult(
                        targetClass = PersonalCarDTO.class,
                        columns= {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "brand_name"),
                                @ColumnResult(name = "model"),
                                @ColumnResult(name = "reg_number"),
                                @ColumnResult(name = "reg_cert"),
                                @ColumnResult(name = "engine_volume"),
                                @ColumnResult(name = "insurance_number"),
                                @ColumnResult(name = "corporate_user_id", type = UUID.class),
                                @ColumnResult(name = "owner_info")
                        }
                )
        }
)
public class PersonalCar implements Persistable<UUID> {
    
    @Id
    private UUID id;
    
    @Column(name = "brand_name")
    private String brandName;
    
    @Column
    private String model;
    
    @Column(name = "reg_number", unique = true)
    private String registrationNumber;
    
    @Column(name = "reg_cert", unique = true)
    private String registrationCertificate;
    
    @Column(name = "engine_volume")
    private int engineVolume;
    
    @Column(name = "insurance_number", unique = true)
    private String insuranceNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporate_user_id")
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_info")
    private PersonalCarOwnerInfo ownerInfo;
    
    @Transient
    private boolean isNew;
    
    public void setEmployee(@NotNull Employee employee) {
        this.employee = employee;
    }

    public PersonalCar(UUID id) {
        this.id = id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonalCar)) {
            return false;
        }
        PersonalCar other = (PersonalCar) o;
        return id != null &&
               id.equals(other.getId());
    }
    
    /**
     * Данная реализация скопирована из модуля corp-client
     * @return
     */
    @Override
    public int hashCode() {
        return 31;
    }
}
