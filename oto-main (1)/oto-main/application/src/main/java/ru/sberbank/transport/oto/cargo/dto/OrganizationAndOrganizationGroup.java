package ru.sberbank.transport.oto.cargo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class OrganizationAndOrganizationGroup {

    @Schema(description = "Id организаций")
    private UUID organizationId;

    @Schema(description = "Id группа организаций")
    private UUID  organizationGroupId;
}