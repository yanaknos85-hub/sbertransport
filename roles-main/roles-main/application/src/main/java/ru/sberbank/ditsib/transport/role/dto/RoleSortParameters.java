package ru.sberbank.ditsib.transport.role.dto;

import ru.sberbank.ditsib.request.PageSortFilterParameters;

/**
 * Parameters of roles controller sorting.
 */
public class RoleSortParameters extends PageSortFilterParameters<RoleSearchParameters> {

    /**
     * Создать новый объект.
     */
    protected RoleSortParameters() {
        super(RoleSearchParameters.NAME);
    }

}
