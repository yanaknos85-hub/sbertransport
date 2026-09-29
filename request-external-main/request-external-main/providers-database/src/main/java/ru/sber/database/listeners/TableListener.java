package ru.sber.database.listeners;

import org.jooq.DeleteQuery;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.InsertQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;

/**
 * Слушатель изменений таблицы.
 */
public interface TableListener extends ExecuteListener {

    @Override
    default void start(ExecuteContext ctx) {
        start(ctx, getTableName(ctx));
        ExecuteListener.super.start(ctx);
    }

    @Override
    default void end(ExecuteContext ctx) {
        end(ctx, getTableName(ctx));
        ExecuteListener.super.end(ctx);
    }

    @Override
    default void renderStart(ExecuteContext ctx) {
        renderStart(ctx, getTableName(ctx));
        ExecuteListener.super.renderStart(ctx);
    }

    @Override
    default void renderEnd(ExecuteContext ctx) {
        renderEnd(ctx, getTableName(ctx));
        ExecuteListener.super.renderEnd(ctx);
    }

    @Override
    default void recordStart(ExecuteContext ctx) {
        recordStart(ctx, getTableName(ctx));
        ExecuteListener.super.recordStart(ctx);
    }

    @Override
    default void recordEnd(ExecuteContext ctx) {
        recordEnd(ctx, getTableName(ctx));
        ExecuteListener.super.recordEnd(ctx);
    }

    @Override
    default void prepareStart(ExecuteContext ctx) {
        prepareStart(ctx, getTableName(ctx));
        ExecuteListener.super.prepareStart(ctx);
    }

    @Override
    default void prepareEnd(ExecuteContext ctx) {
        prepareEnd(ctx, getTableName(ctx));
        ExecuteListener.super.prepareEnd(ctx);
    }

    @Override
    default void bindStart(ExecuteContext ctx) {
        bindStart(ctx, getTableName(ctx));
        ExecuteListener.super.bindStart(ctx);
    }

    @Override
    default void bindEnd(ExecuteContext ctx) {
        bindEnd(ctx, getTableName(ctx));
        ExecuteListener.super.bindEnd(ctx);
    }

    @Override
    default void executeStart(ExecuteContext ctx) {
        executeStart(ctx, getTableName(ctx));
        ExecuteListener.super.executeStart(ctx);
    }

    @Override
    default void executeEnd(ExecuteContext ctx) {
        executeEnd(ctx, getTableName(ctx));
        ExecuteListener.super.executeEnd(ctx);
    }

    @Override
    default void fetchStart(ExecuteContext ctx) {
        fetchStart(ctx, getTableName(ctx));
        ExecuteListener.super.fetchStart(ctx);
    }

    @Override
    default void fetchEnd(ExecuteContext ctx) {
        fetchEnd(ctx, getTableName(ctx));
        ExecuteListener.super.fetchEnd(ctx);
    }

    @Override
    default void resultStart(ExecuteContext ctx) {
        resultStart(ctx, getTableName(ctx));
        ExecuteListener.super.resultStart(ctx);
    }

    @Override
    default void resultEnd(ExecuteContext ctx) {
        resultEnd(ctx, getTableName(ctx));
        ExecuteListener.super.resultEnd(ctx);
    }

    @Override
    default void outStart(ExecuteContext ctx) {
        outStart(ctx, getTableName(ctx));
        ExecuteListener.super.outStart(ctx);
    }

    @Override
    default void outEnd(ExecuteContext ctx) {
        outEnd(ctx, getTableName(ctx));
        ExecuteListener.super.outEnd(ctx);
    }

    /**
     * Вызывается перед выполнением запроса.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void start(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается после выполнения запроса.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void end(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается перед рендером запроса.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void renderStart(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается после рендеринга запроса.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void renderEnd(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается перед обработкой записи.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void recordStart(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается после обработки записи.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void recordEnd(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается перед подготовкой запроса.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void prepareStart(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается после подготовки запроса.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void prepareEnd(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается перед привязкой параметров.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void bindStart(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается после привязки параметров.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void bindEnd(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается перед выполнением запроса.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void executeStart(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается после выполнения запроса.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void executeEnd(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается перед получением результата.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void fetchStart(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается после получения результата.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void fetchEnd(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается перед получением результата.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void resultStart(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается после получения результата.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void resultEnd(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается перед получением результата.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void outStart(ExecuteContext ctx, Table<?> table) {
    }

    /**
     * Вызывается после получения результата.
     *
     * @param ctx   - контекст выполнения
     * @param table - таблица, над которой выполняется запрос
     */
    default void outEnd(ExecuteContext ctx, Table<?> table) {
    }

    private Table<?> getTableName(ExecuteContext ctx) {
        final var query = ctx.query();
        assert query != null;
        final var splitted = query.getSQL().split(" ");
        final int tableNameIndex;
        if (query instanceof InsertQuery<?> || query instanceof DeleteQuery<?>) {
            tableNameIndex = 2;
        } else {
            tableNameIndex = 1;
        }
        final var tableNameParts = splitted[tableNameIndex].split("\\.");
        if (tableNameParts.length == 2) {
            return DSL.table(DSL.name(tableNameParts[0].replace("\"", "")).append(tableNameParts[1].replace("\"", "")));
        }
        return null;
    }

}
