package ru.sber.transport.javers.transactional;

import org.javers.common.exception.JaversException;
import org.javers.common.exception.JaversExceptionCode;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.repository.sql.JaversSqlRepository;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Декоратор транзакции.
 */
public final class TransactionalJaversBuilder extends JaversBuilder {
    private PlatformTransactionManager txManager;

    private TransactionalJaversBuilder() {
    }

    public static TransactionalJaversBuilder javers() {
        return new TransactionalJaversBuilder();
    }

    public TransactionalJaversBuilder withTxManager(PlatformTransactionManager txManager) {
        this.txManager = txManager;
        return this;
    }

    @Override
    public Javers build() {
        if (this.txManager == null) {
            throw new JaversException(JaversExceptionCode.TRANSACTION_MANAGER_NOT_SET);
        } else {
            var javersCore = super.assembleJaversInstance();
            return new JaversTransactionalJpaDecorator(javersCore, this.getContainerComponent(JaversSqlRepository.class), this.txManager);
        }
    }
}
