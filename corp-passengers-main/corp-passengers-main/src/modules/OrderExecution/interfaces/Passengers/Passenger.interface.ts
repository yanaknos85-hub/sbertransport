import { UUID } from 'utils/io-ts';
import { FeedSearchQuery, SearchResponse } from '../Orders.types';
import { TTripFromCoop, TripResponse } from 'stores/Registry/Registry.interface';

export interface IPassengerStore {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  personOrderList: any;
  organizationId: UUID;
  executorGroupId?: UUID[];
  isOrganization: boolean;
  order: TripResponse;
  sharedTrip: TTripFromCoop | null;
  relatedOrder: TripResponse;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  pageFilters: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  personQueryFilters: any;
  savePersonQueryFilters: FeedSearchQuery;
  isLoadingOrderList: boolean;
  category: string;
  getPersonOrderList(): void;
  getPersonOrderListExec(): void;
  setPersonPageSetting(pagination: { page: number; size: number }): void;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  setPersonFilterQueryProps(queryProps: any): void;
  setSavePersonFilterQueryProps(queryProps: FeedSearchQuery): void;
  setOrganizationId(organizationId: UUID): void;
  setExecutorGroupId(executorGroupId: UUID[]): void;
  setIsOrganization(isOrganization: boolean): void;
  getOrder(orderId: UUID, transportType: string): void;
  getSharedTrip(orderId: UUID): void;
  getRelatedOrder(orderId: UUID, transportType: string): void;
  resetFilters(): void;
  resetRelatedOrder(): void;
  resetOrder(): void;
  resetSharedTrip(): void;
  setCategory(type: string): void;
}

export interface IPassengerService {
  getPersonOrderList: (orgId: UUID, query: FeedSearchQuery) => Promise<SearchResponse>;
  getPersonOrderListExec: (query: FeedSearchQuery, execIds?: UUID[]) => Promise<SearchResponse>;
  getOrder: (orderId: UUID, transportType: string) => Promise<TripResponse>;
  getSharedTrip: (orderId: UUID) => Promise<TTripFromCoop>;
  createBusParams: (params: FeedSearchQuery) => FeedSearchQuery;
}
