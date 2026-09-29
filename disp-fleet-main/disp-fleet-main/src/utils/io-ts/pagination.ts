import { SortDerection } from 'constants/app.constants';
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
 * Создает обертку с пагинацией для content
 * @param content - io-ts type содержимого страницы
 * @returns io-ts type пагинированного ответа
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
 * Параметры пагинации для запросов
 */
export interface PaginationParams {
  page: number;
  size: number;
}

/**
 * Параметры сортировки для запросов
 */
export interface SortParams<TField = string> {
  field: TField;
  direction?: SortDerection;
}
