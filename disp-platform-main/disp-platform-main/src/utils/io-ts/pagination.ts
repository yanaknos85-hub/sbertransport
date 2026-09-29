import { SortDirection } from 'constants/app.constants';
import * as t from 'io-ts';

const Sort = t.partial({
  sorted: t.boolean,
  unsorted: t.boolean,
  empty: t.boolean,
});

const Pageable = t.type({
  sort: Sort,
  offset: t.number,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
});

/**
 * Создает io-ts тип для пагинации с указанным типом контента
 * @param content - io-ts тип для элементов массива content
 * @returns io-ts тип для пагинированного ответа
 * @example
 * const UserPagination = createPagination(t.type({ id: t.number, name: t.string }));
 * // decode({ content: [...], totalElements: 10, ... }) -> Pagination<User>
 */
export const createPagination = <T extends t.Mixed>(content: T) => t.type({
  content: t.array(content),
  empty: t.boolean,
  first: t.boolean,
  last: t.boolean,
  number: t.number,
  numberOfElements: t.number,
  pageable: Pageable,
  size: t.number,
  sort: Sort,
  totalElements: t.number,
  totalPages: t.number,
});

/**
 * Параметры для запроса пагинации
 */
export interface PaginationParams {
  page: number;
  size: number;
}

/**
 * Параметры сортировки
 * @template TField - Тип поля для сортировки (по умолчанию string)
 */
export interface SortParams<TField = string> {
  field?: TField;
  direction?: SortDirection;
}
