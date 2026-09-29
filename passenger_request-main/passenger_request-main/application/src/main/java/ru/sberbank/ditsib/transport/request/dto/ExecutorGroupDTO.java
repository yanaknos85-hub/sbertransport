package ru.sberbank.ditsib.transport.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO with executor group data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorGroupDTO {

    private UUID id;

    private String name;
}
