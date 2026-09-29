package ru.sber.transport.request.external.model;

public record TestPageData(int number, int size, boolean last, boolean first, int total,
                           int count) implements PageData {
}