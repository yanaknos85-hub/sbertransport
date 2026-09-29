package ru.sberbank.ditsib.transport.reports.migration;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.springframework.stereotype.Component;

/**
 * Миграция для сброса сдвига получателя подразделений текущего модуля.
 *
 * unused: используется в файлах миграции.
 */

@SuppressWarnings("unused")
@Component
public class DropDepartmentsOffset implements CustomTaskChange {
    @Override
    public void execute(Database database) {
    }
    
    @Override
    public String getConfirmationMessage() {
        return "";
    }
    
    @Override
    public void setUp() throws SetupException {
    
    }
    
    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    
    }
    
    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }
}