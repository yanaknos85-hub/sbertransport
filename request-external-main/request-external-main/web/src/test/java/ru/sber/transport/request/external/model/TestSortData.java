package ru.sber.transport.request.external.model;

public record TestSortData(String field, boolean asc) implements SortData {
}