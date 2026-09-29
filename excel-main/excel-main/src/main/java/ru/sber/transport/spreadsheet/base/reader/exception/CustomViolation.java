package ru.sber.transport.spreadsheet.base.reader.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;

import java.util.Iterator;
import java.util.List;

interface CustomViolation extends ConstraintViolation<Object>  {

    @Override
    default Object getRootBean() {
        return null;
    }

    @Override
    default Class<Object> getRootBeanClass() {
        return null;
    }

    @Override
    default Object getLeafBean() {
        return null;
    }

    @Override
    default Object[] getExecutableParameters() {
        return new Object[0];
    }

    @Override
    default Object getExecutableReturnValue() {
        return null;
    }

    @Override
    default  <U> U unwrap(Class<U> type) {
        return null;
    }

    record FieldPath(String fieldName) implements Path {

        @Override
        public Iterator<Node> iterator() {
            Node node = new FieldNode(fieldName);
            return List.of(node).iterator();
        }

        record FieldNode(String fieldName) implements Node {

            @Override
            public String getName() {
                return fieldName;
            }

            @Override
            public boolean isInIterable() {
                return false;
            }

            @Override
            public Integer getIndex() {
                return null;
            }

            @Override
            public Object getKey() {
                return null;
            }

            @Override
            public ElementKind getKind() {
                return null;
            }

            @Override
            public <T extends Node> T as(Class<T> nodeType) {
                return null;
            }
        }
    }
}
