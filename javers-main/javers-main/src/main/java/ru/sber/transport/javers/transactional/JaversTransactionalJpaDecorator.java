package ru.sber.transport.javers.transactional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.javers.common.validation.Validate;
import org.javers.core.Javers;
import org.javers.core.commit.Commit;
import org.javers.repository.sql.JaversSqlRepository;
import org.javers.spring.transactions.JaversTransactionalDecorator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;

import java.util.Map;

@Slf4j
class JaversTransactionalJpaDecorator extends JaversTransactionalDecorator {

    private final JaversSqlRepository javersSqlRepository;

    private final PlatformTransactionManager txManager;

    JaversTransactionalJpaDecorator(Javers delegate, JaversSqlRepository javersSqlRepository,
                                    PlatformTransactionManager txManager) {
        super(delegate);
        Validate.argumentsAreNotNull(javersSqlRepository, txManager);
        this.javersSqlRepository = javersSqlRepository;
        this.txManager = txManager;
        ensureSchema();
    }

    @Override
    @Transactional
    public Commit commit(String author, Object currentVersion) {
        this.registerRollbackListener();
        return super.commit(author, currentVersion);
    }

    @Override
    @Transactional
    public Commit commit(String author, Object currentVersion, Map<String, String> commitProperties) {
        this.registerRollbackListener();
        return super.commit(author, currentVersion, commitProperties);
    }

    private void ensureSchema() {
        if (this.javersSqlRepository.getConfiguration().isSchemaManagementEnabled()) {
            var tmpl = new TransactionTemplate(this.txManager);
            tmpl.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                    JaversTransactionalJpaDecorator.this.javersSqlRepository.ensureSchema();
                }
            });
        }

    }

    private void registerRollbackListener() {
        if (!this.javersSqlRepository.getConfiguration().isGlobalIdCacheDisabled()
            && TransactionSynchronizationManager.isSynchronizationActive()
            && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCompletion(int status) {
                    if (1 == status) {
                        log.info("evicting javersSqlRepository local cache due to transaction rollback");
                        JaversTransactionalJpaDecorator.this.javersSqlRepository.evictCache();
                    }

                }
            });
        }

    }
}
