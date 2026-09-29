package ru.sber.transport.address.database;

import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.conf.ParamType;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Bind database object to ts vector.
 */
public class TsVectorBinding implements Binding<Object, TsVector> {

    private final Converter<Object, TsVector> converter = new TsVectorConverter();

    @Override
    public @NotNull Converter<Object, TsVector> converter() {
        return converter;
    }

    @Override
    public void sql(BindingSQLContext<TsVector> ctx) throws SQLException {
        if (ctx.render().paramType() == ParamType.INLINED) {
            ctx.render().sql(String.valueOf(converter.to(ctx.value())));
        } else {
            if (ctx.value().isNewData()) {
                ctx.render().sql("to_tsvector(").sql(ctx.variable()).sql(")");
            } else {
                ctx.render().sql(ctx.variable()).sql("::tsvector");
            }
        }

    }

    @Override
    public void register(BindingRegisterContext<TsVector> ctx) throws SQLException {
        //noinspection resource
        ctx.statement().registerOutParameter(ctx.index(), Types.OTHER);
    }

    @Override
    public void set(BindingSetStatementContext<TsVector> ctx) throws SQLException {
        //noinspection resource
        ctx.statement().setObject(ctx.index(), converter.to(ctx.value()));
    }

    @Override
    public void set(BindingSetSQLOutputContext<TsVector> ctx) throws SQLException {
        ctx.output().writeObject(ctx.value(), JDBCType.OTHER);
    }

    @Override
    public void get(BindingGetResultSetContext<TsVector> ctx) throws SQLException {
        //noinspection resource
        ctx.value(converter.from(ctx.resultSet().getObject(ctx.index())));
    }

    @Override
    public void get(BindingGetStatementContext<TsVector> ctx) throws SQLException {
        //noinspection resource
        ctx.value(converter.from(ctx.statement().getObject(ctx.index())));
    }

    @Override
    public void get(BindingGetSQLInputContext<TsVector> ctx) throws SQLException {
        ctx.value(converter.from(ctx.input().readObject()));
    }
}
