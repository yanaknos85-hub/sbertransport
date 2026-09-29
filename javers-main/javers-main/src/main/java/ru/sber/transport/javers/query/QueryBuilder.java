package ru.sber.transport.javers.query;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jooq.Record;

import java.util.Arrays;

/**
 * Объект для построения запросов. Использовать вместо {@link org.javers.repository.jql.QueryBuilder}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryBuilder {

    /**
     * Query for selecting changes (or snapshots) made on any object.
     * <br/><br/>
     *
     * For example, last changes committed on any object can be fetched with:
     * <pre>
     * javers.findChanges( QueryBuilder.anyDomainObject().build() );
     * </pre>
     * @since 2.0
     */
    public static org.javers.repository.jql.QueryBuilder anyDomainObject(){
        return org.javers.repository.jql.QueryBuilder.anyDomainObject();
    }

    /**
     * Query for selecting changes (or snapshots) made on
     * any object (Entity or ValueObject) of given classes.
     * <br/><br/>
     *
     * For example, last changes on any object of MyClass.class:
     * <pre>
     * javers.findChanges( QueryBuilder.byClass(MyClass.class).build() );
     * </pre>
     */
    public static org.javers.repository.jql.QueryBuilder byClass(Class... requiredClasses) {
        return org.javers.repository.jql.QueryBuilder.byClass(Arrays.stream(requiredClasses).map(Reflector::findClass).toArray(Class[]::new));
    }

    /**
     * Query for selecting Changes, Snapshots or Shadows for a given Entity instance.
     * <br/><br/>
     *
     * For example, last Changes on "bob" Person:
     * <pre>
     * javers.findChanges( QueryBuilder.byInstanceId("bob", Person.class).build() );
     * </pre>
     *
     * @param localId Value of an Id-property. When an Entity has Composite-Id (more than one Id-property) &mdash;
     *                <code>localId</code> should be <code>Map&lt;String, Object&gt;</code> with
     *                Id-property name to value pairs.
     * @see <a href="https://github.com/javers/javers/blob/master/javers-core/src/test/groovy/org/javers/core/examples/CompositeIdExample.groovy">CompositeIdExample.groovy</a>
     */
    public static org.javers.repository.jql.QueryBuilder byInstanceId(Object localId, Class<?> entityClass){
        return org.javers.repository.jql.QueryBuilder.byInstanceId(localId, Reflector.findClass(entityClass));
    }


    /**
     * Query for selecting Changes, Snapshots or Shadows for a given Entity instance, identified by its type name.
     * <br/><br/>
     *
     * For example, last Changes on "bob" Person:
     * <pre>
     * javers.findChanges( QueryBuilder.byInstanceId("bob", "Person").build() );
     * </pre>
     *
     * @param localId Value of an Id-property. When an Entity has Composite-Id (more than one Id-property) &mdash;
     *                <code>localId</code> should be <code>Map&lt;String, Object&gt;</code> with
     *                Id-property name to value pairs.
     * @see <a href="https://github.com/javers/javers/blob/master/javers-core/src/test/groovy/org/javers/core/examples/CompositeIdExample.groovy">CompositeIdExample.groovy</a>
     */
    public static org.javers.repository.jql.QueryBuilder byInstanceId(Object localId, String typeName) {
        return org.javers.repository.jql.QueryBuilder.byInstanceId(localId, typeName + "Generated");
    }

    /**
     * Query for selecting changes (or snapshots) made on a concrete Entity instance.
     * <br/><br/>
     *
     * For example, last changes on "bob" Person:
     * <pre>
     * javers.findChanges( QueryBuilder.byInstanceId(new Person("bob")).build() );
     * </pre>
     * @since 2.8.0
     */
    public static org.javers.repository.jql.QueryBuilder byInstance(Object instance) {
        if (instance instanceof Record rec) {
            return org.javers.repository.jql.QueryBuilder.byInstance(Reflector.createJaversObject(rec));
        }
        return org.javers.repository.jql.QueryBuilder.byInstance(instance);
    }

    /**
     * Query for selecting changes (or snapshots)
     * made on all ValueObjects at given path, owned by any instance of given Entity.
     * <br/><br/>
     *
     * See <b>path</b> parameter hints in {@link #byValueObjectId(Object, Class, String)}.
     */
    public static org.javers.repository.jql.QueryBuilder byValueObject(Class<?> ownerEntityClass, String path){
        return org.javers.repository.jql.QueryBuilder.byValueObject(Reflector.findClass(ownerEntityClass), path);
    }

    /**
     * Query for selecting changes (or snapshots) made on a concrete ValueObject
     * (so a ValueObject owned by a concrete Entity instance).
     * <br/><br/>
     *
     * <b>Path parameter</b> is a relative path from owning Entity instance to ValueObject that you are looking for.
     * <br/><br/>
     *
     * When ValueObject is just <b>a property</b>, use propertyName. For example:
     * <pre>
     * class Employee {
     *     &#64;Id String name;
     *     Address primaryAddress;
     * }
     * ...
     * javers.findChanges( QueryBuilder.byValueObjectId("bob", Employee.class, "primaryAddress").build() );
     * </pre>
     *
     * When ValueObject is stored in <b>a List</b>, use propertyName and list index separated by "/", for example:
     * <pre>
     * class Employee {
     *     &#64;Id String name;
     *     List&lt;Address&gt; addresses;
     * }
     * ...
     * javers.findChanges( QueryBuilder.byValueObjectId("bob", Employee.class, "addresses/0").build() );
     * </pre>
     *
     * When ValueObject is stored as <b>a Map value</b>, use propertyName and map key separated by "/", for example:
     * <pre>
     * class Employee {
     *     &#64;Id String name;
     *     Map&lt;String,Address&gt; addressMap;
     * }
     * ...
     * javers.findChanges( QueryBuilder.byValueObjectId("bob", Employee.class, "addressMap/HOME").build() );
     * </pre>
     */
    public static org.javers.repository.jql.QueryBuilder byValueObjectId(Object ownerLocalId, Class<?> ownerEntityClass, String path){
        return org.javers.repository.jql.QueryBuilder.byValueObjectId(ownerLocalId, Reflector.findClass(ownerEntityClass), path);
    }

}
