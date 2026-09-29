package ru.sber.transport.address.database;

import org.jooq.impl.AbstractConverter;

/**
 * Converter for TsVector objects of Postgres.
 */
public class TsVectorConverter extends AbstractConverter<Object, TsVector> {

    /**
     * Create a new converter.
     */
    public TsVectorConverter() {
        super(Object.class, TsVector.class);
    }

    @Override
    public TsVector from(Object databaseObject) {
        return new TsVector(databaseObject);
    }

    @Override
    public Object to(TsVector userObject) {
        return "'%s'".formatted(userObject.getStringToVector());
    }
}
