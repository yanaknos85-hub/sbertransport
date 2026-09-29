package ru.sberbank.ditsib.transport.reports.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.reports.dto.taxi.ContractorResultSet;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity of contractor.
 */
@Entity
@Table(schema = "reports", name = "contractor")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
@Builder(toBuilder = true)

@SqlResultSetMapping(
        name="ContractorResultSet",
        classes= {
                @ConstructorResult(
                        targetClass = ContractorResultSet.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "name")
                        }
                )
        }
)

public class Contractor {
    
    @Id
    private UUID id;
    
    @Column(name = "name")
    private String name;
    
}
