package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.request.dto.constant.TransportClassDTO;

@Mapper
public interface ConstantMapper {
    
    @Mapping(target = "value", expression = "java(taxiClass.name())")
    TransportClassDTO toTaxi(TaxiClass taxiClass);
    
    @Mapping(target = "value", expression = "java(groupTransferClass.name())")
    TransportClassDTO toTransfer(GroupTransferClass groupTransferClass);
    
}
