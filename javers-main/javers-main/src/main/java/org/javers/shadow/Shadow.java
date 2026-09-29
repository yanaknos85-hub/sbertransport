package org.javers.shadow;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.api.JaversRepository;
import ru.sber.transport.javers.query.Reflector;

/**
 * Shadow is a historical version of a domain object restored
 * from a snapshot loaded from {@link JaversRepository}.
 * <br/><br/>
 *
 * Shadows use the same types as domain objects.
 * For example, a Shadow of a Person object is an instance of Person.class.
 * <br/><br/>
 *
 * Shadows class is a thin wrapper for a Shadow object and {@link CommitMetadata}
 *
 * @param <T> type of a domain object
 * @author bartosz.walacik
 */
@ToString
@RequiredArgsConstructor
public class Shadow<T> {

    @NonNull
    @Getter
    private final CommitMetadata commitMetadata;

    @NonNull
    @Getter
    private final CdoSnapshot cdoSnapshot;

    @NonNull
    private final Object it;

    /**
     * Shadow object per se
     */
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) Reflector.createOriginal(it);
    }
}
