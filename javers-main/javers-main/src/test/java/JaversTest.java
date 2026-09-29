import fake.database.TestRepository;
import fake.database.impl.ClassRepositoryImpl;
import fake.database.impl.SubclassRepositoryImpl;
import fake.database.impl.TestRepositoryImpl;
import io.qameta.allure.Feature;
import org.javers.core.Javers;
import org.javers.core.diff.changetype.*;
import org.javers.core.metamodel.object.SnapshotType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.AutoConfigureJooq;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfigTest;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.javers.JaversSqlAutoConfigurationTest;
import ru.sber.transport.javers.JaversSqlProperties;
import ru.sber.transport.javers.query.QueryBuilder;
import ru.sber.transport.javers.service.impl.EnableJwtInfoExtractor;
import ru.sber.transport.javers.service.impl.EnableUsernameExtractor;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.test.database.test.tables.records.TestRecord;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@IsolatedTest
@Feature("lib_javers")
@ExtendWith(SpringExtension.class)
@AutoConfigureJooq
@EmbeddedPostgres
@Transactional
@DisplayName("Проверка Javers")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@JaversSqlAutoConfigurationTest
@JooqDatabaseConfigTest
@TestPropertySource(properties = {
    "spring.liquibase.change-log=db/changelog-master.yml",
    "spring.datasource.username=postgres",
})
@EnableJwtInfoExtractor
@EnableUsernameExtractor
@Import({
    TestRepositoryImpl.class,
    ClassRepositoryImpl.class,
    SubclassRepositoryImpl.class
})
class JaversTest {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    @Qualifier("classRepository")
    private ClassRepositoryImpl classRepositoryImpl;

    @Autowired
    private SubclassRepositoryImpl subclassRepositoryImpl;

    @Autowired
    private Javers javers;

    @DisplayName("Проверка")
    @Test
    void test() { // NOSONAR
        SecurityContextHolder.getContext().setAuthentication(null);

        var testData = new TestRecord();
        testData.setId(UUID.randomUUID());
        testData.setName("Name");

        testRepository.save(testData);

        var snapshots = javers.findSnapshots(QueryBuilder.byInstance(testData).build());
        assertThat(snapshots).hasSize(1);
        assertThat(snapshots.get(0).getChanged()).hasSameElementsAs(List.of("id", "name"));
        assertThat(snapshots.get(0).getVersion()).isEqualTo(1);
        assertThat(snapshots.get(0).getCommitId().getMajorId()).isEqualTo(1L);
        assertThat(snapshots.get(0).getGlobalId().value()).isEqualTo("ru.sber.transport.test.database.test.tables.records.TestRecordGenerated/" + testData.getId().toString());
        assertThat(snapshots.get(0).getCommitMetadata().getAuthor()).isEqualTo("anonymous");
        assertThat(snapshots.get(0).getState().getPropertyNames()).hasSameElementsAs(List.of("id", "name"));
        assertThat(snapshots.get(0).getState().getPropertyValue("id")).isEqualTo(testData.getId());
        assertThat(snapshots.get(0).getState().getPropertyValue("name")).isEqualTo(testData.getName());
        assertThat(snapshots.get(0).getType()).isEqualTo(SnapshotType.INITIAL);

        var changes = javers.findChanges(QueryBuilder.anyDomainObject().build());
        assertThat(changes).hasSize(3);
        assertThat(changes.get(0)).isInstanceOf(NewObject.class);
        assertThat(changes.get(1)).isInstanceOf(InitialValueChange.class);
        assertThat(((InitialValueChange) changes.get(1)).getChangeType()).isEqualTo(PropertyChangeType.PROPERTY_VALUE_CHANGED);
        assertThat(((InitialValueChange) changes.get(1)).getPropertyName()).isEqualTo("id");
        assertThat(((InitialValueChange) changes.get(1)).getLeft()).isNull();
        assertThat(((InitialValueChange) changes.get(1)).getRight()).isEqualTo(testData.getId());
        assertThat(changes.get(2)).isInstanceOf(InitialValueChange.class);
        assertThat(changes.get(2)).isInstanceOf(InitialValueChange.class);
        assertThat(((InitialValueChange) changes.get(2)).getChangeType()).isEqualTo(PropertyChangeType.PROPERTY_VALUE_CHANGED);
        assertThat(((InitialValueChange) changes.get(2)).getPropertyName()).isEqualTo("name");
        assertThat(((InitialValueChange) changes.get(2)).getLeft()).isNull();
        assertThat(((InitialValueChange) changes.get(2)).getRight()).isEqualTo(testData.getName());

        var shadows = javers.findShadows(QueryBuilder.anyDomainObject().build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());

        shadows = javers.findShadows(QueryBuilder.byClass(TestRecord.class).build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());

        shadows = javers.findShadows(QueryBuilder.byInstance(testData).build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());

        shadows = javers.findShadows(QueryBuilder.byInstanceId(testData.getId(), TestRecord.class).build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());

        shadows = javers.findShadows(QueryBuilder.byInstanceId(testData.getId(), TestRecord.class.getCanonicalName()).build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());
    }

    @DisplayName("Проверка класса")
    @Test
    void test_class() { // NOSONAR
        SecurityContextHolder.getContext().setAuthentication(null);

        var testData = new TestRecord();
        testData.setId(UUID.randomUUID());
        testData.setName("Name");

        classRepositoryImpl.save(testData);

        var snapshots = javers.findSnapshots(QueryBuilder.byInstance(testData).build());
        assertThat(snapshots).hasSize(1);
        assertThat(snapshots.get(0).getChanged()).hasSameElementsAs(List.of("id", "name"));
        assertThat(snapshots.get(0).getVersion()).isEqualTo(1);
        assertThat(snapshots.get(0).getCommitId().getMajorId()).isEqualTo(1L);
        assertThat(snapshots.get(0).getGlobalId().value()).isEqualTo("ru.sber.transport.test.database.test.tables.records.TestRecordGenerated/" + testData.getId().toString());
        assertThat(snapshots.get(0).getCommitMetadata().getAuthor()).isEqualTo("anonymous");
        assertThat(snapshots.get(0).getState().getPropertyNames()).hasSameElementsAs(List.of("id", "name"));
        assertThat(snapshots.get(0).getState().getPropertyValue("id")).isEqualTo(testData.getId());
        assertThat(snapshots.get(0).getState().getPropertyValue("name")).isEqualTo(testData.getName());
        assertThat(snapshots.get(0).getType()).isEqualTo(SnapshotType.INITIAL);

        var changes = javers.findChanges(QueryBuilder.anyDomainObject().build());
        assertThat(changes).hasSize(3);
        assertThat(changes.get(0)).isInstanceOf(NewObject.class);
        assertThat(changes.get(1)).isInstanceOf(InitialValueChange.class);
        assertThat(((InitialValueChange) changes.get(1)).getChangeType()).isEqualTo(PropertyChangeType.PROPERTY_VALUE_CHANGED);
        assertThat(((InitialValueChange) changes.get(1)).getPropertyName()).isEqualTo("id");
        assertThat(((InitialValueChange) changes.get(1)).getLeft()).isNull();
        assertThat(((InitialValueChange) changes.get(1)).getRight()).isEqualTo(testData.getId());
        assertThat(changes.get(2)).isInstanceOf(InitialValueChange.class);
        assertThat(changes.get(2)).isInstanceOf(InitialValueChange.class);
        assertThat(((InitialValueChange) changes.get(2)).getChangeType()).isEqualTo(PropertyChangeType.PROPERTY_VALUE_CHANGED);
        assertThat(((InitialValueChange) changes.get(2)).getPropertyName()).isEqualTo("name");
        assertThat(((InitialValueChange) changes.get(2)).getLeft()).isNull();
        assertThat(((InitialValueChange) changes.get(2)).getRight()).isEqualTo(testData.getName());

        var shadows = javers.findShadows(QueryBuilder.anyDomainObject().build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());

        shadows = javers.findShadows(QueryBuilder.byClass(TestRecord.class).build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());

        shadows = javers.findShadows(QueryBuilder.byInstance(testData).build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());

        shadows = javers.findShadows(QueryBuilder.byInstanceId(testData.getId(), TestRecord.class).build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());

        shadows = javers.findShadows(QueryBuilder.byInstanceId(testData.getId(), TestRecord.class.getCanonicalName()).build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());
    }

    @DisplayName("Проверка подкласса")
    @Test
    void test_subclass() { // NOSONAR
        SecurityContextHolder.getContext().setAuthentication(null);

        var testData = new TestRecord();
        testData.setId(UUID.randomUUID());
        testData.setName("Name");

        subclassRepositoryImpl.save(testData);

        var snapshots = javers.findSnapshots(QueryBuilder.byInstance(testData).build());
        assertThat(snapshots).hasSize(1);
        assertThat(snapshots.get(0).getChanged()).hasSameElementsAs(List.of("id", "name"));
        assertThat(snapshots.get(0).getVersion()).isEqualTo(1);
        assertThat(snapshots.get(0).getCommitId().getMajorId()).isEqualTo(1L);
        assertThat(snapshots.get(0).getGlobalId().value()).isEqualTo("ru.sber.transport.test.database.test.tables.records.TestRecordGenerated/" + testData.getId().toString());
        assertThat(snapshots.get(0).getCommitMetadata().getAuthor()).isEqualTo("anonymous");
        assertThat(snapshots.get(0).getState().getPropertyNames()).hasSameElementsAs(List.of("id", "name"));
        assertThat(snapshots.get(0).getState().getPropertyValue("id")).isEqualTo(testData.getId());
        assertThat(snapshots.get(0).getState().getPropertyValue("name")).isEqualTo(testData.getName());
        assertThat(snapshots.get(0).getType()).isEqualTo(SnapshotType.INITIAL);

        var changes = javers.findChanges(QueryBuilder.anyDomainObject().build());
        assertThat(changes).hasSize(3);
        assertThat(changes.get(0)).isInstanceOf(NewObject.class);
        assertThat(changes.get(1)).isInstanceOf(InitialValueChange.class);
        assertThat(((InitialValueChange) changes.get(1)).getChangeType()).isEqualTo(PropertyChangeType.PROPERTY_VALUE_CHANGED);
        assertThat(((InitialValueChange) changes.get(1)).getPropertyName()).isEqualTo("id");
        assertThat(((InitialValueChange) changes.get(1)).getLeft()).isNull();
        assertThat(((InitialValueChange) changes.get(1)).getRight()).isEqualTo(testData.getId());
        assertThat(changes.get(2)).isInstanceOf(InitialValueChange.class);
        assertThat(changes.get(2)).isInstanceOf(InitialValueChange.class);
        assertThat(((InitialValueChange) changes.get(2)).getChangeType()).isEqualTo(PropertyChangeType.PROPERTY_VALUE_CHANGED);
        assertThat(((InitialValueChange) changes.get(2)).getPropertyName()).isEqualTo("name");
        assertThat(((InitialValueChange) changes.get(2)).getLeft()).isNull();
        assertThat(((InitialValueChange) changes.get(2)).getRight()).isEqualTo(testData.getName());

        var shadows = javers.findShadows(QueryBuilder.anyDomainObject().build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());

        shadows = javers.findShadows(QueryBuilder.byClass(TestRecord.class).build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());

        shadows = javers.findShadows(QueryBuilder.byInstance(testData).build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());

        shadows = javers.findShadows(QueryBuilder.byInstanceId(testData.getId(), TestRecord.class).build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());

        shadows = javers.findShadows(QueryBuilder.byInstanceId(testData.getId(), TestRecord.class.getCanonicalName()).build());
        assertThat(shadows).hasSize(1);
        assertThat(shadows.get(0).get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) shadows.get(0).get()).getId()).isEqualTo(testData.getId());
        assertThat(((TestRecord) shadows.get(0).get()).getName()).isEqualTo(testData.getName());
    }

    @DisplayName("Проверка контекста безопасности")
    @Test
    void test_security() { // NOSONAR
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "password"));

        var testData = new TestRecord();
        testData.setId(UUID.randomUUID());
        testData.setName("Name");

        testRepository.save(testData);

        var snapshots = javers.findSnapshots(QueryBuilder.byInstance(testData).build());
        assertThat(snapshots).hasSize(1);
        assertThat(snapshots.get(0).getChanged()).hasSameElementsAs(List.of("id", "name"));
        assertThat(snapshots.get(0).getVersion()).isEqualTo(1);
        assertThat(snapshots.get(0).getCommitId().getMajorId()).isEqualTo(1L);
        assertThat(snapshots.get(0).getCommitId().getMinorId()).isZero();
        assertThat(snapshots.get(0).getGlobalId().value()).isEqualTo("ru.sber.transport.test.database.test.tables.records.TestRecordGenerated/" + testData.getId().toString());
        assertThat(snapshots.get(0).getCommitMetadata().getAuthor()).isEqualTo("user");
        assertThat(snapshots.get(0).getState().getPropertyNames()).hasSameElementsAs(List.of("id", "name"));
        assertThat(snapshots.get(0).getState().getPropertyValue("id")).isEqualTo(testData.getId());
        assertThat(snapshots.get(0).getState().getPropertyValue("name")).isEqualTo(testData.getName());
        assertThat(snapshots.get(0).getType()).isEqualTo(SnapshotType.INITIAL);
    }

    @DisplayName("Проверка контекста безопасности JWT")
    @Test
    void test_security_jwt() { // NOSONAR
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").jti("JWT user").header("alg", "none").build()));

        var testData = new TestRecord();
        testData.setId(UUID.randomUUID());
        testData.setName("Name");

        testRepository.save(testData);

        var snapshots = javers.findSnapshots(QueryBuilder.byInstance(testData).build());
        assertThat(snapshots).hasSize(1);
        assertThat(snapshots.get(0).getChanged()).hasSameElementsAs(List.of("id", "name"));
        assertThat(snapshots.get(0).getVersion()).isEqualTo(1);
        assertThat(snapshots.get(0).getCommitId().getMajorId()).isEqualTo(1L);
        assertThat(snapshots.get(0).getGlobalId().value()).isEqualTo("ru.sber.transport.test.database.test.tables.records.TestRecordGenerated/" + testData.getId().toString());
        assertThat(snapshots.get(0).getCommitMetadata().getAuthor()).isEqualTo("JWT user");
        assertThat(snapshots.get(0).getState().getPropertyNames()).hasSameElementsAs(List.of("id", "name"));
        assertThat(snapshots.get(0).getState().getPropertyValue("id")).isEqualTo(testData.getId());
        assertThat(snapshots.get(0).getState().getPropertyValue("name")).isEqualTo(testData.getName());
        assertThat(snapshots.get(0).getType()).isEqualTo(SnapshotType.INITIAL);
    }

    @Test
    @DisplayName("Проверка сохранения всех")
    void test_saveAll() { // NOSONAR
        var saved = new LinkedList<TestRecord>();
        for (var i = 0; i < 100; i++) {
            var testData = new TestRecord();
            testData.setId(UUID.randomUUID());
            testData.setName("Name");
            saved.add(testData);
        }
        testRepository.saveAll(saved);

        var changes = javers.findChanges(QueryBuilder.anyDomainObject().build());
        var shadows = javers.findShadows(QueryBuilder.anyDomainObject().build());
        var snapshots = javers.findSnapshots(QueryBuilder.anyDomainObject().build());

        assertThat(changes).hasSize(100 * 3);
        assertThat(shadows).hasSize(100);
        assertThat(snapshots).hasSize(100);

        Collections.reverse(saved);

        for (var i = 0; i < 100; i++) {
            var expected = saved.get(i);
            var snapshot = snapshots.get(i);
            var shadow = shadows.get(i);

            assertThat(snapshot.getChanged()).hasSameElementsAs(List.of("id", "name"));
            assertThat(snapshot.getVersion()).isEqualTo(1);
            assertThat(snapshot.getGlobalId().value()).isEqualTo("ru.sber.transport.test.database.test.tables.records.TestRecordGenerated/" + expected.getId().toString());
            assertThat(snapshot.getCommitMetadata().getAuthor()).isEqualTo("anonymous");
            assertThat(snapshot.getState().getPropertyNames()).hasSameElementsAs(List.of("id", "name"));
            assertThat(snapshot.getState().getPropertyValue("id")).isEqualTo(expected.getId());
            assertThat(snapshot.getState().getPropertyValue("name")).isEqualTo(expected.getName());
            assertThat(snapshot.getType()).isEqualTo(SnapshotType.INITIAL);

            assertThat(changes.get(i * 3)).isInstanceOf(NewObject.class);
            assertThat(changes.get(i * 3 + 1)).isInstanceOf(InitialValueChange.class);
            assertThat(((InitialValueChange) changes.get(i * 3 + 1)).getChangeType()).isEqualTo(PropertyChangeType.PROPERTY_VALUE_CHANGED);
            assertThat(((InitialValueChange) changes.get(i * 3 + 1)).getPropertyName()).isEqualTo("id");
            assertThat(((InitialValueChange) changes.get(i * 3 + 1)).getLeft()).isNull();
            assertThat(((InitialValueChange) changes.get(i * 3 + 1)).getRight()).isEqualTo(expected.getId());
            assertThat(changes.get(i * 3 + 2)).isInstanceOf(InitialValueChange.class);
            assertThat(changes.get(i * 3 + 2)).isInstanceOf(InitialValueChange.class);
            assertThat(((InitialValueChange) changes.get(i * 3 + 2)).getChangeType()).isEqualTo(PropertyChangeType.PROPERTY_VALUE_CHANGED);
            assertThat(((InitialValueChange) changes.get(i * 3 + 2)).getPropertyName()).isEqualTo("name");
            assertThat(((InitialValueChange) changes.get(i * 3 + 2)).getLeft()).isNull();
            assertThat(((InitialValueChange) changes.get(i * 3 + 2)).getRight()).isEqualTo(expected.getName());

            assertThat(shadow.get()).isInstanceOf(TestRecord.class);
            assertThat(((TestRecord) shadow.get()).getId()).isEqualTo(expected.getId());
            assertThat(((TestRecord) shadow.get()).getName()).isEqualTo(expected.getName());
        }

        var delete = saved.pollFirst();
        assert delete != null;
        testRepository.delete(delete);

        var deletedOneChanges = javers.findChanges(QueryBuilder.byInstance(delete).build());
        var deletedOneShadows = javers.findShadows(QueryBuilder.byInstance(delete).build());
        var deletedOneSnapshots = javers.findSnapshots(QueryBuilder.byInstance(delete).build());

        assertThat(deletedOneChanges.get(0)).isInstanceOf(ObjectRemoved.class);
        assertThat(deletedOneChanges.get(1)).isInstanceOf(TerminalValueChange.class);
        assertThat(((TerminalValueChange) deletedOneChanges.get(1)).getChangeType()).isEqualTo(PropertyChangeType.PROPERTY_VALUE_CHANGED);
        assertThat(((TerminalValueChange) deletedOneChanges.get(1)).getPropertyName()).isEqualTo("id");
        assertThat(((TerminalValueChange) deletedOneChanges.get(1)).getRight()).isNull();
        assertThat(((TerminalValueChange) deletedOneChanges.get(1)).getLeft()).isEqualTo(delete.getId());
        assertThat(deletedOneChanges.get(2)).isInstanceOf(TerminalValueChange.class);
        assertThat(deletedOneChanges.get(2)).isInstanceOf(TerminalValueChange.class);
        assertThat(((TerminalValueChange) deletedOneChanges.get(2)).getChangeType()).isEqualTo(PropertyChangeType.PROPERTY_VALUE_CHANGED);
        assertThat(((TerminalValueChange) deletedOneChanges.get(2)).getPropertyName()).isEqualTo("name");
        assertThat(((TerminalValueChange) deletedOneChanges.get(2)).getRight()).isNull();
        assertThat(((TerminalValueChange) deletedOneChanges.get(2)).getLeft()).isEqualTo(delete.getName());

        var deleteOneSnapshot = deletedOneSnapshots.get(0);
        assertThat(deleteOneSnapshot.getChanged()).hasSameElementsAs(List.of());
        assertThat(deleteOneSnapshot.getVersion()).isEqualTo(2);
        assertThat(deleteOneSnapshot.getGlobalId().value()).isEqualTo("ru.sber.transport.test.database.test.tables.records.TestRecordGenerated/" + delete.getId().toString());
        assertThat(deleteOneSnapshot.getCommitMetadata().getAuthor()).isEqualTo("anonymous");
        assertThat(deleteOneSnapshot.getState().getPropertyNames()).hasSameElementsAs(List.of());
        assertThat(deleteOneSnapshot.getState().getPropertyValue("id")).isNull();
        assertThat(deleteOneSnapshot.getState().getPropertyValue("name")).isNull();
        assertThat(deleteOneSnapshot.getType()).isEqualTo(SnapshotType.TERMINAL);

        var deleteOneShadow = deletedOneShadows.get(0);
        assertThat(deleteOneShadow.get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) deleteOneShadow.get()).getId()).isNull();
        assertThat(((TestRecord) deleteOneShadow.get()).getName()).isNull();

        var deleteById = saved.pollFirst();
        assert deleteById != null;
        testRepository.deleteById(deleteById.getId());

        var deletedOneByIdChanges = javers.findChanges(QueryBuilder.byInstance(deleteById).build());
        var deletedOneByIdShadows = javers.findShadows(QueryBuilder.byInstance(deleteById).build());
        var deletedOneByIdSnapshots = javers.findSnapshots(QueryBuilder.byInstance(deleteById).build());

        assertThat(deletedOneByIdChanges.get(0)).isInstanceOf(ObjectRemoved.class);
        assertThat(deletedOneByIdChanges.get(1)).isInstanceOf(TerminalValueChange.class);
        assertThat(((TerminalValueChange) deletedOneByIdChanges.get(1)).getChangeType()).isEqualTo(PropertyChangeType.PROPERTY_VALUE_CHANGED);
        assertThat(((TerminalValueChange) deletedOneByIdChanges.get(1)).getPropertyName()).isEqualTo("id");
        assertThat(((TerminalValueChange) deletedOneByIdChanges.get(1)).getRight()).isNull();
        assertThat(((TerminalValueChange) deletedOneByIdChanges.get(1)).getLeft()).isEqualTo(deleteById.getId());
        assertThat(deletedOneByIdChanges.get(2)).isInstanceOf(TerminalValueChange.class);
        assertThat(deletedOneByIdChanges.get(2)).isInstanceOf(TerminalValueChange.class);
        assertThat(((TerminalValueChange) deletedOneByIdChanges.get(2)).getChangeType()).isEqualTo(PropertyChangeType.PROPERTY_VALUE_CHANGED);
        assertThat(((TerminalValueChange) deletedOneByIdChanges.get(2)).getPropertyName()).isEqualTo("name");
        assertThat(((TerminalValueChange) deletedOneByIdChanges.get(2)).getRight()).isNull();
        assertThat(((TerminalValueChange) deletedOneByIdChanges.get(2)).getLeft()).isEqualTo(deleteById.getName());

        var deleteOneByIdSnapshot = deletedOneByIdSnapshots.get(0);
        assertThat(deleteOneByIdSnapshot.getChanged()).hasSameElementsAs(List.of());
        assertThat(deleteOneByIdSnapshot.getVersion()).isEqualTo(2);
        assertThat(deleteOneByIdSnapshot.getGlobalId().value()).isEqualTo("ru.sber.transport.test.database.test.tables.records.TestRecordGenerated/" + deleteById.getId().toString());
        assertThat(deleteOneByIdSnapshot.getCommitMetadata().getAuthor()).isEqualTo("anonymous");
        assertThat(deleteOneByIdSnapshot.getState().getPropertyNames()).hasSameElementsAs(List.of());
        assertThat(deleteOneByIdSnapshot.getState().getPropertyValue("id")).isNull();
        assertThat(deleteOneByIdSnapshot.getState().getPropertyValue("name")).isNull();
        assertThat(deleteOneByIdSnapshot.getType()).isEqualTo(SnapshotType.TERMINAL);

        var deleteOneByIdShadow = deletedOneByIdShadows.get(0);
        assertThat(deleteOneByIdShadow.get()).isInstanceOf(TestRecord.class);
        assertThat(((TestRecord) deleteOneByIdShadow.get()).getId()).isNull();
        assertThat(((TestRecord) deleteOneByIdShadow.get()).getName()).isNull();

        testRepository.deleteAll(saved);

        var deletedSnapshots = javers.findSnapshots(QueryBuilder.anyDomainObject().build());
        var deletedShadows = javers.findShadows(QueryBuilder.anyDomainObject().build());
        var deletedChanges = javers.findChanges(QueryBuilder.anyDomainObject().build());

        Collections.reverse(saved);

        for (var i = 0; i < 98; i++) {
            var expected = saved.get(i);
            var snapshot = deletedSnapshots.get(i);
            var shadow = deletedShadows.get(i);

            assertThat(snapshot.getChanged()).hasSameElementsAs(List.of());
            assertThat(snapshot.getVersion()).isEqualTo(2);
            assertThat(snapshot.getGlobalId().value()).isEqualTo("ru.sber.transport.test.database.test.tables.records.TestRecordGenerated/" + expected.getId().toString());
            assertThat(snapshot.getCommitMetadata().getAuthor()).isEqualTo("anonymous");
            assertThat(snapshot.getState().getPropertyNames()).hasSameElementsAs(List.of());
            assertThat(snapshot.getState().getPropertyValue("id")).isNull();
            assertThat(snapshot.getState().getPropertyValue("name")).isNull();
            assertThat(snapshot.getType()).isEqualTo(SnapshotType.TERMINAL);

            assertThat(deletedChanges.get(i)).isInstanceOf(ObjectRemoved.class);
            assertThat(deletedChanges.get(i).getAffectedLocalId()).isEqualTo(expected.getId());

            assertThat(shadow.get()).isInstanceOf(TestRecord.class);
            assertThat(((TestRecord) shadow.get()).getId()).isNull();
            assertThat(((TestRecord) shadow.get()).getName()).isNull();
        }
    }

    @TestConfiguration
    static class TestJaversConfig {

        @Bean
        @Primary
        JaversSqlProperties javersSqlProperties() {
            var repository = new JaversSqlProperties();
            repository.setSqlSchema("test");

            return repository;
        }

    }

}
