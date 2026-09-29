package ru.sberbank.ditsib.transport.request.human_readable_id.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.humanreadableid.model.BaseCompanySQ;

import java.io.Serial;
import java.io.Serializable;

/**
 * Entity of sequence
 */

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@Table(schema = "request", name = "company_sq")
@Getter
@Setter
public class CompanySQRequest extends BaseCompanySQ implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
}