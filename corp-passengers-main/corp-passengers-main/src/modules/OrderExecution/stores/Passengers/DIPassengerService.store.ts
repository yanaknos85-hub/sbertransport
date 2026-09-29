import { action, observable } from 'mobx';
import { inject, injectable } from 'inversify';

import { TYPES } from 'ioc/types';
import type { UUID } from 'utils/io-ts';
import { DEFAULT_FILTER_VALUES } from '../../constants/Filters';
import type { SearchResponse, FeedSearchQuery } from '../../interfaces/Orders.types';
import type { IPassengerStore, IPassengerService } from '../../interfaces/Passengers/Passenger.interface';
import type { TTripFromCoop } from 'stores/Registry/Registry.interface';

@injectable()
export class DIPassengerStore implements IPassengerStore {
  @inject(TYPES.IPassengerService)
  private service!: IPassengerService;

  @observable pageFilters: FeedSearchQuery = DEFAULT_FILTER_VALUES;
  @observable organizationId: UUID = '' as UUID;
  @observable executorGroupId: UUID[] = [];
  @observable isOrganization = true;
  @observable personQueryFilters: FeedSearchQuery | object = {};

  @observable savePersonQueryFilters: FeedSearchQuery = {
    page: 0,
    pageSize: 0,
  };

  @observable personOrderList!: SearchResponse;

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  @observable order!: any;

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  @observable relatedOrder!: any;

  @observable sharedTrip!: TTripFromCoop | null;

  @observable isLoadingOrderList = false;

  @observable category = '';

  @action.bound
    getPersonOrderList = async (): Promise<void> => {
      if (!this.organizationId) {
        return;
      }

      this.isLoadingOrderList = true;

      const queries = this.service.createBusParams({
        ...this.pageFilters,
        ...this.personQueryFilters,
      });

      this.service
        .getPersonOrderList(this.organizationId, queries)
        .then(response => {
          this.personOrderList = response;
        })
        .finally(() => {
          this.isLoadingOrderList = false;
        });
    };

  @action.bound
    getPersonOrderListExec = async (): Promise<void> => {
      if (!this.executorGroupId.length) {
        return;
      }

      this.isLoadingOrderList = true;

      const queries = this.service.createBusParams({
        ...this.pageFilters,
        ...this.personQueryFilters,
      });

      this.service
        .getPersonOrderListExec(queries, this.executorGroupId)
        .then(response => {
          this.personOrderList = response;
        })
        .finally(() => {
          this.isLoadingOrderList = false;
        });
    };

  @action.bound
    getOrder = async (orderId: UUID, transportType: string): Promise<void> => {
      this.service.getOrder(orderId, transportType).then(order => {
        this.order = order;
      });
    };

  @action.bound
    resetOrder = (): void => {
      this.order = null;
    };

  @action.bound
    setPersonPageSetting = async ({ page, size }: { page: number; size: number }): Promise<void> => {
      this.pageFilters.page = page;
      this.pageFilters.pageSize = size;

      if (this.isOrganization) {
        this.getPersonOrderList().then();
      } else {
        this.getPersonOrderListExec().then();
      }
    };

  @action.bound
    setPersonFilterQueryProps = async (queryProps: FeedSearchQuery): Promise<void> => {
      this.personQueryFilters = queryProps;
      this.pageFilters = { ...this.pageFilters, page: 0 };
    };

  @action.bound
    setSavePersonFilterQueryProps = async (queryProps: FeedSearchQuery): Promise<void> => {
      this.savePersonQueryFilters = queryProps;
    };

  @action.bound
    setCategory = (type: string): void => {
      this.category = type;
    };

  @action.bound
    resetFilters = (): void => {
      this.pageFilters = DEFAULT_FILTER_VALUES;
    };

  @action.bound
    setOrganizationId = (organizationId: UUID): void => {
      if (!organizationId) {
        return;
      }
      this.organizationId = organizationId;
    };

  @action.bound
    setExecutorGroupId = (executorGroupId: UUID[]): void => {
      if (!executorGroupId) {
        return;
      }
      this.executorGroupId = executorGroupId;
    };

  @action.bound
    setIsOrganization = (isOrganization: boolean): void => {
      this.isOrganization = isOrganization;
    };

  @action.bound
    getRelatedOrder = async (orderId: UUID, transportType: string): Promise<void> => {
      this.service.getOrder(orderId, transportType).then(order => {
        this.relatedOrder = order;
      });
    };

  @action.bound
    getSharedTrip = async (orderId: UUID): Promise<void> => {
      this.service.getSharedTrip(orderId).then(shared => {
        this.sharedTrip = shared;
      });
    };

  @action.bound
    resetRelatedOrder = (): void => {
      this.relatedOrder = null;
    };

  @action.bound
    resetSharedTrip = (): void => {
      this.sharedTrip = null;
    };
}
