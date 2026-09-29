package ru.sber.transport.telemechanic.database.human_readable_id.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.humanreadableid.model.BaseCompanySQ;

import java.io.Serializable;

/**
 * Entity of sequence
 */

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "company_sq")
@Getter
@Setter
public class CompanySQ extends BaseCompanySQ implements Serializable {
    private static final long serialVersionUID = 1L;

}