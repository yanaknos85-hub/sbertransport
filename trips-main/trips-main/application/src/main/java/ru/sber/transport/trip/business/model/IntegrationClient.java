package ru.sber.transport.trip.business.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IntegrationClient {
        UUID id;
        /**
         * ID контрагента
         */
        UUID contractorId;
        private boolean active;
}
