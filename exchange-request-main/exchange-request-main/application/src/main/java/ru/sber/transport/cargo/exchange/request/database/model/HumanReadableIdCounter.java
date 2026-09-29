package ru.sber.transport.cargo.exchange.request.database.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "humanreadable_id_counter", schema = "exchange_request")
@IdClass(HumanReadableIdCounterId.class)
@Getter
@Setter
public class HumanReadableIdCounter {

    @Id
    @Column(name = "year_month", length = 6, nullable = false)
    private String yearMonth;

    @Id
    @Column(name = "prefix", length = 10, nullable = false)
    private String prefix;

    @Column(name = "next_val", nullable = false)
    private Long nextVal = 1L;
}