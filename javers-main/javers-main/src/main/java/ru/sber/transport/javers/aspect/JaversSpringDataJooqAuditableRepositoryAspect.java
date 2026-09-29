package ru.sber.transport.javers.aspect;

import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.javers.core.Javers;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.javers.spring.auditable.AspectUtil;
import org.javers.spring.auditable.AuthorProvider;
import org.javers.spring.auditable.CommitPropertiesProvider;
import org.javers.spring.auditable.aspect.JaversCommitAdvice;
import org.jooq.Record;
import org.springframework.core.annotation.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.AbstractRepositoryMetadata;
import org.springframework.lang.NonNull;
import ru.sber.transport.javers.query.QueryBuilder;
import ru.sber.transport.javers.query.Reflector;

import java.util.Collection;
import java.util.Optional;

/**
 * Аспект, оборачивающий запросы к CrudRepository.
 */
@Aspect
@Order(0)
public class JaversSpringDataJooqAuditableRepositoryAspect {

    private final JaversCommitAdvice javersCommitAdvice;

    private final Javers javers;

    public JaversSpringDataJooqAuditableRepositoryAspect(Javers javers,
                                                         AuthorProvider authorProvider,
                                                         CommitPropertiesProvider commitPropertiesProvider) {
        this.javers = javers;
        this.javersCommitAdvice = new JaversCommitAdvice(javers, authorProvider, commitPropertiesProvider);
    }

    @AfterReturning("execution(public * delete(..)) && this(org.springframework.data.repository.CrudRepository)")
    public void onDeleteExecuted(JoinPoint pjp) {
        doDelete(pjp);
    }

    @AfterReturning("execution(public * deleteById(..)) && this(org.springframework.data.repository.CrudRepository)")
    public void onDeleteByIdExecuted(JoinPoint pjp) {
        doDelete(pjp);
    }

    @AfterReturning("execution(public * deleteAll(..)) && this(org.springframework.data.repository.CrudRepository)")
    public void onDeleteAllExecuted(JoinPoint pjp) {
        doDelete(pjp);
    }

    @AfterReturning(
        value = "execution(public * save(..)) && this(org.springframework.data.repository.CrudRepository)",
        returning = "responseEntity"
    )
    public void onSaveExecuted(JoinPoint pjp, Object responseEntity) {
        doSave(pjp, responseEntity);
    }

    @AfterReturning(
        value = "execution(public * saveAll(..)) && this(org.springframework.data.repository.CrudRepository)",
        returning = "responseEntity"
    )
    public void onSaveAllExecuted(JoinPoint pjp, Object responseEntity) {
        doSave(pjp, responseEntity);
    }

    private void doSave(JoinPoint pjp, Object returnedObject) {
        getRepositoryInterface(pjp).ifPresent(i -> {
            if (returnedObject instanceof Collection<?> collection) {
                collection.forEach(item -> doSave(item, pjp));
            } else {
                doSave(returnedObject, pjp);
            }
        });
    }

    private void doSave(Object returnedObject, JoinPoint pjp) {
        if (returnedObject instanceof Record dbRecord) {
            var instance = Reflector.createJaversObject(dbRecord);
            javersCommitAdvice.commitObject(pjp, instance);
        } else {
            AspectUtil.collectReturnedObjects(returnedObject).forEach(item -> javersCommitAdvice.commitObject(pjp, item));
        }
    }

    private void doDelete(JoinPoint pjp) {
        getRepositoryInterface(pjp).ifPresent( i -> {
            var metadata = AbstractRepositoryMetadata.getMetadata(i);
            for (var deletedObject : AspectUtil.collectArguments(pjp)) {
                handleDelete(pjp, metadata, deletedObject);
            }
        });
    }

    private void handleDelete(JoinPoint pjp, RepositoryMetadata metadata, Object domainObjectOrId) {
        if (domainObjectOrId instanceof Record domainRecord) {
            if (javers.findSnapshots(QueryBuilder.byInstance(domainRecord).limit(1).build()).isEmpty()) {
                return;
            }
            javersCommitAdvice.commitShallowDelete(pjp, Reflector.createJaversObject(domainRecord));
        } else {
            var domainType = Reflector.findClass(metadata.getDomainType());

            if (javers.findSnapshots(QueryBuilder.byInstanceId(domainObjectOrId, metadata.getDomainType()).limit(1).build()).isEmpty()) {
                return;
            }
            javersCommitAdvice.commitShallowDeleteById(pjp, domainObjectOrId, domainType);
        }
    }

    private Optional<Class<?>> getRepositoryInterface(JoinPoint pjp) {
        var targetClass = unproxy(pjp.getTarget().getClass());
        if (checkJaversAnnotated(targetClass)) {
            return Optional.of(targetClass);
        }
        var superClass = targetClass.getSuperclass();
        if (checkJaversAnnotated(superClass)) {
            return Optional.of(targetClass);
        }
        for (var i : targetClass.getInterfaces()) {
            if (checkJaversAnnotated(i)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private boolean checkJaversAnnotated(Class<?> clazz) {
        return clazz != null && clazz.isAnnotationPresent(JaversSpringDataAuditable.class) && CrudRepository.class.isAssignableFrom(clazz);
    }

    @SneakyThrows(ClassNotFoundException.class)
    private @NonNull Class<?> unproxy(Class<?> possibleProxy) {
        if (possibleProxy.getSimpleName().contains("$$SpringCGLIB$$")) {
            var proxyName = possibleProxy.getCanonicalName().replaceAll("\\$\\$SpringCGLIB\\$\\$\\d+", "");
            return Class.forName(proxyName);
        }
        return possibleProxy;
    }
}
