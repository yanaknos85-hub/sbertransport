package ru.sber.transport.cargo.exchange.request.database.model;

import java.io.Serializable;

public class HumanReadableIdCounterId implements Serializable {
    private String yearMonth;
    private String prefix;

    public HumanReadableIdCounterId() {}

    public HumanReadableIdCounterId(String yearMonth, String prefix) {
        this.yearMonth = yearMonth;
        this.prefix = prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HumanReadableIdCounterId)) return false;
        HumanReadableIdCounterId that = (HumanReadableIdCounterId) o;
        return yearMonth != null ? yearMonth.equals(that.yearMonth) : that.yearMonth == null &&
                prefix != null ? prefix.equals(that.prefix) : that.prefix == null;
    }

    @Override
    public int hashCode() {
        int result = yearMonth != null ? yearMonth.hashCode() : 0;
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        return result;
    }
}