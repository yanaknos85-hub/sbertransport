package ru.sber.transport.contractor.database.model;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class EmailIntegrationParams {
    //Название контрагента латиницей (указывается при интеграции в имени файла)
    @Length(min = 3)
    @Column(name = "contractor_name")
    private String contractorName;
    
    //Название контрагента кириллицей (указывается при интеграции в поле ИСПОЛНИТЕЛЬ)
    @Length(min = 3)
    @Column(name = "contractor_rus_name")
    private String contractorRusName;
    
    //Интеграционный email контрагента
    @Column(name = "integration_email")
    @Pattern(regexp = "^(?<nonSpace>[\\w!#$%&’*+/=?`{|}~^-]+)(?:\\.\\k<nonSpace>)*@[.\\w-]+[A-Za-z]{2,6}$")
    private String email;
    
}
