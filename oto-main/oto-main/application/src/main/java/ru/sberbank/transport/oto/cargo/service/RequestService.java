package ru.sberbank.transport.oto.cargo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import ru.sberbank.transport.oto.cargo.database.model.Request;
import ru.sberbank.transport.oto.cargo.enums.SortDirection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RequestService {
  
  /**
   * Поиск заявок по фильтрам
   *
   * @param spec - спецификация
   *
   * @return - список заявок
   */
  List<Request> findAllBySpec(Specification<Request> spec);
  
  /**
   * Поиск заявок по фильтрам
   *
   * @param spec - спецификация
   *
   * @return - список заявок
   */
  Page<Request> findAllBySpec(
          Specification<Request> spec, Integer size, Integer page,
          SortDirection direction, String field
                             );
  
  /**
   * Поиск заявок по id
   *
   * @param id - id заявки
   *
   * @return заявка
   */
  Optional<Request> findById(UUID id);
  
  Request save(Request request);
  
  Page<Request> findAllForCargo(
          Specification<Request> spec, Integer size, Integer page,
          SortDirection direction, String field
                               );
}
