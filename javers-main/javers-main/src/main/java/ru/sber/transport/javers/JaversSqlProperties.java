package ru.sber.transport.javers;

import lombok.Getter;
import lombok.Setter;
import org.javers.spring.JaversSpringProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.sber.transport.javers.jooq.integration.JooqAccessHook;

@Getter
@Setter
@ConfigurationProperties(prefix = "javers")
public class JaversSqlProperties extends JaversSpringProperties {
    private static final String DEFAULT_OBJECT_ACCESS_HOOK = JooqAccessHook.class.getName();

    private boolean sqlSchemaManagementEnabled = true;

    private boolean sqlGlobalIdCacheDisabled;

    private String sqlSchema;

    private String sqlGlobalIdTableName;

    private String sqlCommitTableName;

    private String sqlSnapshotTableName;

    private String sqlCommitPropertyTableName;

    protected String defaultObjectAccessHook(){
        return DEFAULT_OBJECT_ACCESS_HOOK;
    }
}
