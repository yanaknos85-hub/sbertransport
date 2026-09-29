package ru.sberbank.ditsib.transport.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Scopes of role definition.
 */
@Schema(title = "Область действия роли")
public enum Scope {

    /**
     * Employee.
     */
    EMPLOYEE,

    /**
     * Dispatcher.
     */
    DISPATCHER,

    /**
     * Driver.
     */
    DRIVER,

    /**
     * Contractor.
     */
    CONTRACTOR,

    /**
     * Autoservice.
     */
    AUTOSERVICE,

    /**
     * Autoservice Contractor
     */
    AUTOSERVICE_TA

}
