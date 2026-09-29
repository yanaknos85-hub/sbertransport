package ru.sber.transport.javers.query;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.javers.core.metamodel.annotation.Id;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.tools.StringUtils;
import org.jooq.tools.reflect.Reflect;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс для осуществления рефлективных операций.
 */
@UtilityClass
public class Reflector {

    private final Map<Class<? extends Record>, Class<?>> cache = new ConcurrentHashMap<>();

    /**
     * Создать класс, принимаемый Javers по {@link Record}.
     *
     * @param dbRecord исходный объект с данными.
     * @return созданный класс.
     */
    public Class<?> createClass(Record dbRecord) {
        var recordClass = dbRecord.getClass();
        if (Reflector.cache.containsKey(recordClass)) {
            return Reflector.cache.get(recordClass);
        }
        var canonicalName = recordClass.getCanonicalName() + "Generated";
        var content = getContent(canonicalName, dbRecord);
        var compiled = Reflect.compile(canonicalName, content).type();
        cache.put(recordClass, compiled);
        return compiled;
    }

    /**
     * Найти класс для работы с Javers.
     *
     * @param aClass исходный класс.
     * @return класс Javers или исходный, если исходный класс не является классом Record.
     */
    public static Class<?> findClass(Class<?> aClass) {
        if (Record.class.isAssignableFrom(aClass)) {
            if (Reflector.cache.containsKey(aClass)) {
                return Reflector.cache.get(aClass);
            }
            var canonicalName = aClass.getCanonicalName() + "Generated";
            var content = getContent(canonicalName, (Record) createInstance(aClass));
            var compiled = Reflect.compile(canonicalName, content).type();
            //noinspection unchecked
            cache.put((Class<? extends Record>) aClass, compiled);
            return compiled;
        }
        return null;
    }

    /**
     * Создать объект Javers.
     *
     * @param dbRecord исходный объект.
     * @return Javers-объект.
     */
    @SneakyThrows({NoSuchFieldException.class, IllegalAccessException.class})
    public static Object createJaversObject(Record dbRecord) {
        var compiled = Reflector.createClass(dbRecord);
        var instance = createInstance(compiled);
        for (var field : dbRecord.fields()) {
            var declaredField = instance.getClass().getDeclaredField(StringUtils.toCamelCaseLC(field.getName()));
            declaredField.set(instance, dbRecord.getValue(field)); // NOSONAR
        }
        return instance;
    }

    /**
     * Получение иденктифкатора(-ов) объекта, если таковые присутствуют.
     *
     * @param entity объект.
     * @param <T> тип объекта.
     * @return идентификатор(-ы).
     */
    @SneakyThrows({IllegalArgumentException.class, IllegalAccessException.class})
    public static <T> Object getId(T entity) {
        var ids = new LinkedList<>();
        for (var field : entity.getClass().getDeclaredFields()) {
            if (field.getAnnotation(Id.class) != null) {
                ids.add(field.get(entity));
            }
        }
        if (ids.size() == 1) {
            return ids.poll();
        }
        return ids;
    }

    @SneakyThrows({InstantiationException.class, IllegalAccessException.class, IllegalArgumentException.class,
        InvocationTargetException.class})
    private static Object createInstance(@NonNull Class<?> compiled) {
        for (var constructor : compiled.getDeclaredConstructors()) {
            if (constructor.getParameters().length == 0) {
                return constructor.newInstance();
            }
        }
        throw new IllegalArgumentException("No-args constructor not found");
    }

    private String getContent(String canonicalName, Record baseRecord) {
        var nameParts = canonicalName.split("\\.");
        var className = nameParts[nameParts.length - 1];
        var pack = canonicalName.replace("." + className, "");
        return """
            package %1$s;
                   
            @org.javers.core.metamodel.annotation.Entity
            public class %2$s {
                %3$s
            }
            """.formatted(pack, className, getFields(baseRecord));
    }

    private String getFields(Record fields) {
        var builder = new StringBuilder();
        for (var field : fields.fields()) {
            var id = false;
            if (field instanceof TableField<?,?> tableField) {
                var table = tableField.getTable();
                if (table != null) {
                    var primaryKey = table.getPrimaryKey();
                    if (primaryKey != null) {
                        id = primaryKey.getFields().contains(tableField);
                    }
                }
            }
            var fieldName = field.getName();
            var content = """
                %4$s
                public %1$s %2$s;
                """.formatted(
                field.getDataType().getType().getCanonicalName(),
                StringUtils.toCamelCaseLC(fieldName),
                StringUtils.toCamelCase(fieldName),
                id ? "@org.javers.core.metamodel.annotation.Id" : "");
            builder.append(content);
        }
        return builder.toString();
    }

    /**
     * Получение оригинального объекта.
     *
     * @param it исходный объект.
     * @return целевой объект.
     */
    public Object createOriginal(Object it) {
        return cache.entrySet().stream().filter(cl -> Objects.equals(cl.getValue(), it.getClass()))
            .findFirst()
            .map(cl -> toRecord(cl.getKey(), it)).orElse(it);
    }

    @SneakyThrows({NoSuchFieldException.class, IllegalArgumentException.class, IllegalAccessException.class})
    private Object toRecord(Class<?> recordClass, Object it) {
        if (createInstance(recordClass) instanceof Record recordInstance) {
            var itClass = it.getClass();
            for (var field : recordInstance.fields()) {
                var itField = itClass.getField(StringUtils.toCamelCaseLC(field.getName()));
                //noinspection unchecked
                recordInstance.set((Field<Object>) field, itField.get(it));
            }
            return recordInstance;
        }
        return it;
    }
}
