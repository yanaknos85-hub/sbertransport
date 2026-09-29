package ru.sber.transport.contractor.dto.files;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * New data about contractor.
 */
@Getter
@Setter
public class ContractorFile {
    private String name;
    private String msrn;
    private String tin;
    private String contactPersonInfo;
    private String contactPersonPhone;
    private Integer rating;
    private String img;
    private List<UUID> regionIds;
    private String integrationType;
    private String contractorName;
    private String contractorRusName;
    private String integrationEmail;
    private String url;
    private String login;
    private String password;
}
