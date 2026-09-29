package ru.sberbank.ditsib.transport.request.messaging.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Сотрудники, имеюшие право согласовывать и редактировать заявки подразделения
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentTripRequestApproversMessage implements Message<UUID> {
    /**
     * Подразделение, заявки которого могут согласовывать согласующие
     */
    private UUID departmentId;

    @Builder.Default
    private Collection<Approver> approvers = new ArrayList<>();
    
    @JsonIgnore
    @Override
    public UUID getId() {
        return departmentId;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class Approver {
    
        private UUID employeeId;
    
        /**
         * Согласующий может согласовывать заявки только данного типа транспорта.
         * Null если ограничений нет.
         */
        private String transportType;
    
    }
    
}
