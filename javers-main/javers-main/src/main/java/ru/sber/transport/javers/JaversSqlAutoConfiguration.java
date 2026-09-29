package ru.sber.transport.javers;

import lombok.extern.slf4j.Slf4j;
import org.javers.core.Javers;
import org.javers.core.JaversBuilderPlugin;
import org.javers.repository.sql.ConnectionProvider;
import org.javers.repository.sql.DialectName;
import org.javers.repository.sql.JaversSqlRepository;
import org.javers.repository.sql.SqlRepositoryBuilder;
import org.javers.spring.RegisterJsonTypeAdaptersPlugin;
import org.javers.spring.auditable.*;
import org.javers.spring.auditable.aspect.JaversAuditableAspect;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import ru.sber.transport.javers.aspect.JaversSpringDataJooqAuditableRepositoryAspect;
import ru.sber.transport.javers.service.UserExtractor;
import ru.sber.transport.javers.transactional.TransactionalJaversBuilder;

import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
@AutoConfigureAfter(DSLContext.class)
@EnableConfigurationProperties(value = {JaversSqlProperties.class})
@Import({RegisterJsonTypeAdaptersPlugin.class})
class JaversSqlAutoConfiguration {

    @Bean
    DialectName javersSqlDialectName(DSLContext context) {
        return new DialectMapper().map(context.configuration());
    }

    @Bean(name = "JaversSqlRepositoryFromStarter")
    @ConditionalOnMissingBean
    JaversSqlRepository javersSqlRepository(ConnectionProvider connectionProvider,
                                            DialectName dialectName,
                                            JaversSqlProperties javersSqlProperties,
                                            @Value("${spring.application.name}") String appName) {
        return SqlRepositoryBuilder
            .sqlRepository()
            .withSchema(Optional.ofNullable(javersSqlProperties.getSqlSchema()).orElse(appName))
            .withConnectionProvider(connectionProvider)
            .withDialect(dialectName)
            .withSchemaManagementEnabled(javersSqlProperties.isSqlSchemaManagementEnabled())
            .withGlobalIdCacheDisabled(javersSqlProperties.isSqlGlobalIdCacheDisabled())
            .withGlobalIdTableName(javersSqlProperties.getSqlGlobalIdTableName())
            .withCommitTableName(javersSqlProperties.getSqlCommitTableName())
            .withSnapshotTableName(javersSqlProperties.getSqlSnapshotTableName())
            .withCommitPropertyTableName(javersSqlProperties.getSqlCommitPropertyTableName())
            .build();
    }

    @Bean(name = "JaversFromStarter")
    @ConditionalOnMissingBean
    Javers javers(JaversSqlRepository sqlRepository,
                  PlatformTransactionManager transactionManager,
                  JaversSqlProperties javersSqlProperties,
                  List<JaversBuilderPlugin> plugins) {
        var javersBuilder = TransactionalJaversBuilder
            .javers()
            .withTxManager(transactionManager)
            .registerJaversRepository(sqlRepository)
            .withObjectAccessHook(javersSqlProperties.createObjectAccessHookInstance())
            .withProperties(javersSqlProperties);

        plugins.forEach(plugin -> plugin.beforeAssemble(javersBuilder));

        return javersBuilder.build();
    }

    @Bean(name = "SpringSecurityAuthorProvider")
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = {"org.springframework.security.core.context.SecurityContextHolder"})
    AuthorProvider springSecurityAuthorProvider() {
        return new SpringSecurityAuthorProvider();
    }

    @Bean(name = "MockAuthorProvider")
    @ConditionalOnMissingBean
    @ConditionalOnMissingClass({"org.springframework.security.core.context.SecurityContextHolder"})
    AuthorProvider unknownAuthorProvider() {
        return new MockAuthorProvider();
    }

    @Bean(name = "JpaHibernateConnectionProvider")
    @ConditionalOnMissingBean
    ConnectionProvider jpaConnectionProvider(DSLContext context) {
        return context::diagnosticsConnection;
    }

    @Bean
    @ConditionalOnProperty(name = "javers.auditableAspectEnabled", havingValue = "true", matchIfMissing = true)
    JaversAuditableAspect javersAuditableAspect(Javers javers,
                                                AuthorProvider authorProvider,
                                                CommitPropertiesProvider commitPropertiesProvider) {
        return new JaversAuditableAspect(javers, authorProvider, commitPropertiesProvider);
    }

    @Bean
    @ConditionalOnProperty(name = "javers.springDataAuditableRepositoryAspectEnabled",
        havingValue = "true",
        matchIfMissing = true)
    JaversSpringDataJooqAuditableRepositoryAspect javersSpringDataAuditableAspect(
        Javers javers,
        AuthorProvider authorProvider,
        CommitPropertiesProvider commitPropertiesProvider
    ) {
        return new JaversSpringDataJooqAuditableRepositoryAspect(javers,
            authorProvider,
            commitPropertiesProvider);
    }

    @Bean
    CommitPropertiesProvider commitPropertiesProvider() {
        return new EmptyPropertiesProvider();
    }

    @Bean
    AuthorProvider authorProvider(List<UserExtractor> extractors) {
        return () -> {
            var security = SecurityContextHolder.getContext();
            if (security != null) {
                var auth = security.getAuthentication();
                if (auth != null) {
                    return extractors.parallelStream().filter(bean -> bean.support(auth.getClass()))
                        .findFirst()
                        .map(extractor -> extractor.extract(auth)).orElse("anonymous");
                }
            }
            return "anonymous";
        };
    }
}
