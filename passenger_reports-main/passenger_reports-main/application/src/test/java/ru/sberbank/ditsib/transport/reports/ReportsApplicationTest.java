package ru.sberbank.ditsib.transport.reports;

import liquibase.change.custom.CustomChange;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.postgres.EmbeddedPostgres;

@EmbeddedPostgres
@DisplayName("Проверка запуска")
class ReportsApplicationTest implements CustomChange {
    
    @Override
    public String getConfirmationMessage() {
        return "";
    }
    
    @Override
    public void setUp() {
    }
    
    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    }
    
    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }
}