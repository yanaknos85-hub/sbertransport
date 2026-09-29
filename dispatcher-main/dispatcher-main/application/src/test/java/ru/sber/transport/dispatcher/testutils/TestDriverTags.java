package ru.sber.transport.dispatcher.testutils;

import ru.sber.transport.dispatcher.dto.NewAttributeDTO;
import ru.sber.transport.dispatcher.database.model.Attribute;
import ru.sber.transport.dispatcher.database.model.Contractor;

/**
 * Класс создан для того, чтобы вынести создание сущностей в одно место,
 * что то вроде фабрики. Таким образом, при изменении модели, править создание и связи
 * надо будет только здесь
 */
public final class TestDriverTags {

    public static Attribute createTestDriverTag(Contractor contractor){
        return Attribute.builder()
                .name("Tag Driver")
                .contractor(contractor)
                .build();
    }

    public static NewAttributeDTO createTestDriverTagDto(){
        return NewAttributeDTO.builder()
                .name("Tag Driver")
                .build();
    }
    public static Attribute incrementDriverTag(Attribute t, int i)
    {
         return Attribute.builder()
                 .name(t.getName()+i)
                 .contractor(t.getContractor())
                 .build();
    }

    public static NewAttributeDTO incrementDriverTagDto(NewAttributeDTO t, int i)
    {
        return NewAttributeDTO.builder()
                .name(t.getName()+i)
                .build();
    }

}
