import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { createPagination, SortParams } from 'utils/io-ts/pagination';

export const ContractorDispatcherBase = t.intersection([
  t.type({
    firstName: t.string,
    lastName: t.string,
    id: tt.uuid,
  }),
  t.partial({
    patronymic: t.string,
  }),
]);

export const ContractorDispatcher = t.intersection([
  ContractorDispatcherBase,
  t.type({
    phone: t.string,
    email: t.string,
    contractorId: tt.uuid,
    humanReadableId: t.string,
  }),
  t.partial({
    autoparkId: t.string,
  }),
]);

export const ContractorDispatcherResponse = createPagination(ContractorDispatcher);

export const PaginationParams = t.type({
  page: t.number,
  size: t.number,
});

export type ContractorDispatcherResponse = t.TypeOf<typeof ContractorDispatcherResponse>;

export type ContractorDispatcherBase = t.TypeOf<typeof ContractorDispatcherBase>;

export type ContractorDispatcher = t.TypeOf<typeof ContractorDispatcher>;

export type PaginationParams = t.TypeOf<typeof PaginationParams>;

/** Автомобиль в списке доступных авто для брони */
export const TripTransport = t.intersection([
  t.type({
    id: tt.uuid,
    stateNumber: t.string,
  }),
  t.partial({
    brand: t.string,
    model: t.string,
    trips: t.array(t.type({
      start: t.string,
      end: t.string,
    })),
  }),
]);
export type TripTransport = t.TypeOf<typeof TripTransport>;

/** Пагинированный список доступных авто для брони */
export const TripTransports = createPagination(TripTransport);
export type TripTransports = t.TypeOf<typeof TripTransports>;

export interface TripTransportFilters extends PaginationParams, SortParams {
  startDate?: string;
  endDate?: string;
  timeZone?: 'Z'; // С другими таймзонами какая-то проблема на бэке
  search?: string;
  result?: string[];
}

export type DispatcherQuery = Partial<ContractorDispatcher> & { page?: number; size?: number };

export interface UseSearchDispatchersProps {
  contractorId: string;
  autoparkId?: string;
  query?: DispatcherQuery;
}
