package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.evaluators.RemarkType;

@Entity
@IdClass(EvalSettingsId.class)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Table(schema = "request", name = "eval_settings")
@Setter
@Getter
public class EvalSettings {
    
    /**
     * Transport type
     */
    @Id
    @Column(name = "transport_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    /**
     * type of remark
     *
     * Тип замечания/комментария:
     *   ADVANTAGES - преимущества
     *   DRAWBACKS - недостатки
     */
    @Id
    @Column(name = "remark_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RemarkType remarkType;
    
    /**
     * code
     *
     * Код преднастроенного замечания (комментария)
     */
    @Id
    @Column(name = "code")
    private String code;
    
    /**
     * order
     *
     * Порядковый номер преднастроенного замечания (комментария)
     */
    @Column(name = "order_num")
    private Integer order;

    /**
     * title
     *
     * Надпись преднастроенного замечания (комментария)
     */
    @Column(name = "title")
    private String title;
    
    /**
     * image
     *
     * Код иконки преднастроенного замечания (комментария)
     * Может повторяться, если одинаковые иконки используются для разных code
     */
    @Column(name = "image")
    private String image;
}