import { action, observable } from 'mobx';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import { DEFAULT_FILTER_VALUES } from '../../constants/Filters';
import { EMPTY_SEARCH_RESPONSE } from '../../constants/General';
import type {
  AddDelegatePayload,
  FeedSearchQuery,
  Order,
  OrderField,
  SearchResponse,
  SortDirection,
  TariffCost,
  TariffRequestMulti
} from '../../interfaces/Orders.types';
import { Source } from '../../interfaces/Orders.types';
import type { ICargoService, ICargoStore } from '../../interfaces/Cargo/Cargo.interface';
import { DEFAULT_STATUS_FILTERS, dOrderFields } from 'modules/OrderExecution/constants/Cargo/Cargo';
import type { SchedulerSearchResponse } from 'modules/OrderExecution/interfaces/SchedulerOrders.types';
import { Category, Tab } from '../../constants/Tabs';
import type { UUID } from 'utils/io-ts';
import { filterSession } from 'modules/OrderExecution/utils/FilterStorage/filterStorage';

@injectable()
export class DICargoStore implements ICargoStore {
  @inject(TYPES.ICargoService)
  private service!: ICargoService;

  @observable organizationId!: UUID;
  @observable executorGroupId: string[] = [];
  @observable isOrganization: boolean = true;
  @observable isLoadingOrg: boolean = false;
  @observable isLoadingExec: boolean = false;

  @observable pageFilters: FeedSearchQuery = DEFAULT_FILTER_VALUES;

  @observable cargoQueryFilters: any = DEFAULT_STATUS_FILTERS;

  @observable cargoOrderList: SearchResponse = EMPTY_SEARCH_RESPONSE;

  @observable cargoSchedulerList!: SchedulerSearchResponse;

  @observable cargoOrderActive: Order | undefined | null = null;

  @observable tariffsListMulti: TariffCost[] = [];

  @observable cachedTariffs: Record<UUID, TariffCost[]> = {};

  @observable cargoSortField: string | null = null;

  @observable cargoSortOrder: SortDirection = null;

  @observable activeTabKey = Tab.cargo;

  @observable activeCategory: Category = Category.all;

  @action.bound
  setCargoSortOrder = (field: string | null, direction: SortDirection): void => {
    if (this.cargoSortField === field && this.cargoSortOrder === direction) return;

    this.cargoSortField = field;
    this.cargoSortOrder = direction;
    this.pageFilters.page = 0;
  };

  @action.bound
  setExecutorGroupId(executorGroupId: string[]): void {
    this.executorGroupId = executorGroupId;
  }

  @action.bound
  setIsOrganization(isOrganization: boolean): void {
    this.isOrganization = isOrganization;
  }

  @action.bound
  getCargoOrderActive = async (id: UUID, source: Source): Promise<void> => {
    const response = await this.service.getCargoOrderActive(id, source);
    this.cargoOrderActive = response;
  };

  @action.bound
  clearOrderActive  = (): void => {
    this.cargoOrderActive = null;
  };

  @action.bound
  getCargoOrderList = async (): Promise<void> => {
    if (!this.organizationId) return;

    const sortParams = this.cargoSortField && this.cargoSortOrder
      ? {
        sortField: this.cargoSortField,
        sortDirection: this.cargoSortOrder.toUpperCase()
      }
      : {};
    try {
      const response = await this.service.getCargoOrderList(
        this.organizationId,
        {
          ...this.pageFilters,
          ...this.cargoQueryFilters,
          ...sortParams,
        }
      );
      this.cargoOrderList = response;
    } catch (error) {
      console.error('Ошибка при загрузке грузовых заказов:', error);
    }
  };

  @action.bound
  getCargoSchedulerList = async (): Promise<void> => {
    if (!this.organizationId) {
      return;
    }
    this.service
      .getCargoSchedulerList(this.organizationId, {
        ...this.pageFilters,
        ...this.cargoQueryFilters,
      })
      .then(response => {
        this.cargoSchedulerList = response;
      });
  };

  @action.bound
  setCargoPageSetting = async ({ page, size }: { page: number; size: number }): Promise<void> => {
    this.pageFilters.page = page;
    this.pageFilters.pageSize = size;

    await this.getCargoOrderListDeferredPost();
  };

  @action.bound
  setCargoSchedulerPageSetting = async ({ page, size }: { page: number; size: number }): Promise<void> => {
    this.pageFilters.page = page;
    this.pageFilters.pageSize = size;

    // Для расписаний используем старый GET метод с параметрами в URL
    await this.getCargoSchedulerList();
  };

  @action.bound
  initializeFilters = (): void => {
    // Этот метод нужно вызвать один раз при инициализации
    const storedFilters = filterSession.data;
    const initialCategory = this.activeCategory;

    // Если текущая категория не template и есть сохраненные фильтры для нее
    if (initialCategory !== Category.template && storedFilters && storedFilters[initialCategory]) {
      // Если для текущей категории есть сохраненные фильтры, загружаем их
      this.cargoQueryFilters = storedFilters[initialCategory];
    } else {
      // Иначе устанавливаем фильтры по умолчанию и сохраняем их только если категория не template
      const defaultFilters = DEFAULT_STATUS_FILTERS;
      this.cargoQueryFilters = defaultFilters;
      if (initialCategory !== Category.template) {
        filterSession.data = {
          [initialCategory]: defaultFilters,
        };
      }
    }
  };

  @action.bound
  setCargoFilterQueryProps = async (queryProps: FeedSearchQuery): Promise<void> => {
    this.cargoQueryFilters = {
      ...this.cargoQueryFilters,
      ...queryProps
    };

    this.pageFilters = { ...this.pageFilters, page: 0 };

    // Сохраняем обновленные фильтры в sessionStorage только если категория не template
    if (typeof this.activeCategory === "string" && this.activeCategory !== Category.template) {
      filterSession.data = {
        [this.activeCategory]: this.cargoQueryFilters
      };
    }
  };

  @action.bound
  resetFilters = (): void => {
    // Сбрасываем фильтры к состоянию по умолчанию для текущей категории
    const defaultFilters = DEFAULT_STATUS_FILTERS;
    this.cargoQueryFilters = defaultFilters;
    this.pageFilters = DEFAULT_FILTER_VALUES;

    // Сохраняем сброшенное состояние в sessionStorage только если категория не template
    if (this.activeCategory !== Category.template) {
      filterSession.data = {
        [this.activeCategory]: this.cargoQueryFilters
      };
    }
  };

  @action.bound
  changeOrder = async (orderId: UUID, orderObjFields: Partial<Order>): Promise<void> => {
    const orderFields: OrderField[] = Object.keys(orderObjFields).map(key => ({
      field: dOrderFields[key] || 'unknown',
      value: orderObjFields[key],
    }));
    await this.service.changeOrder(orderId, orderFields);
    if (this.cargoOrderActive) {
      this.cargoOrderActive = {
        ...this.cargoOrderActive,
        ...orderObjFields,
      };
    }
  };

  @action.bound
  changeEngineerComment = async (humanReadableId: UUID, query: string): Promise<void> => {
    try {
      const response = await this.service.changeEngineerComment(humanReadableId, query);
      if (response && Array.isArray(response)) {
        if (this.cargoOrderActive && response.length > 0) {
          this.cargoOrderActive = {
            ...this.cargoOrderActive,
            commentEng: response[0].value,
          };
        }
      }
    } catch (error) {
      console.error("Ошибка при изменении комментария инженера:", error);
      throw error;
    }
  };

  @action.bound
  addAdditionalContact = async (humanReadableId: string, contactData: AddDelegatePayload[]): Promise<void> => {
    try {
      await this.service.addAdditionalContact(humanReadableId, contactData);
    } catch (error) {
      console.error("Ошибка при добавлении дополнительного контакта:", error);
      throw error;
    }
  };

  @action.bound
  sendOrderToContractor = async (orderId: string): Promise<void> => {
    try {
      await this.service.sendOrderToContractor(orderId);
    } catch (err) {
      throw err;
    }
  }

  @action.bound
  sendRouteToContractor = async (routeId: string): Promise<void> => {
    try {
      await this.service.sendRouteToContractor(routeId);
    } catch (err) {
      throw err;
    }
  }

  @action.bound
  async calculateAllTariffsMulti(data: TariffRequestMulti | undefined): Promise<void> {
    const orderId = data?.orderId || '';
    if (this.cachedTariffs[orderId]) {
      this.tariffsListMulti = this.cachedTariffs[orderId];
      return;
    }
    const tariffs = await this.service.calculateAllTariffsMulti(data) || [];
    if (tariffs.length > 0) {
      this.tariffsListMulti = tariffs
        .sort((a, b) => a.cost - b.cost)
        .reduce((acc: TariffCost[], cur, idx) => {
          const tariff = { ...cur, idx };
          return [...acc, tariff];
        }, []);
      this.cachedTariffs[orderId] = this.tariffsListMulti;
    } else {
      this.tariffsListMulti = [];
    }
  }

  @action.bound
  clearTariffs(): void {
    this.tariffsListMulti = [];
    this.cachedTariffs = {};
  }

  @action.bound
  setOrganizationId(organizationId: UUID): void {
    this.organizationId = organizationId;
  }

  @action.bound
  setActiveTabKey(activeKey: Tab): void {
    this.activeTabKey = activeKey;
  }

  @action.bound
  setActiveCategory(category: Category): void {
    if (this.activeCategory === category) return;

    this.activeCategory = category;

    // Сбрасываем executorGroupId при переключении категории
    // Чтобы избежать использования групп исполнителей одного типа для другого
    this.executorGroupId = [];

    // Сбрасываем список заказов на пустой объект
    this.cargoOrderList = EMPTY_SEARCH_RESPONSE;

    const storedFilters = filterSession.data;

    // Проверяем, есть ли сохраненные фильтры для новой категории только если категория не template
    if (category !== Category.template && storedFilters && storedFilters[category] !== undefined) {
      this.cargoQueryFilters = storedFilters[category];
    } else {
      // Если нет, устанавливаем по умолчанию
      const defaultFilters = DEFAULT_STATUS_FILTERS;
      this.cargoQueryFilters = defaultFilters;

      // И сохраняем только если категория не template
      if (category !== Category.template) {
        filterSession.data = {
          [category]: defaultFilters,
        };
      }
    }
  }

  @action.bound
  getCargoOrderListDeferredPost = async (emptyExecutorGroup: boolean = false): Promise<void> => {
    if (
      !this.organizationId &&
      !this.executorGroupId.length &&
      !this.isOrganization &&
      !emptyExecutorGroup
    ) {
      this.cargoOrderList = EMPTY_SEARCH_RESPONSE;
      return;
    }

    const sortParams = this.cargoSortField && this.cargoSortOrder
      ? {
        sortField: this.cargoSortField,
        sortDirection: this.cargoSortOrder.toUpperCase()
      }
      : {};

    try {
      if (this.isOrganization && this.organizationId) {
        // По organization через POST
        const response = await this.service.getFeedOrderListPost(
          this.organizationId,
          {
            ...this.pageFilters,
            ...this.cargoQueryFilters,
            ...sortParams,
          },
          [], // executorGroupIds пустой для режима организации
          false // UUID ≠ empty
        );
        this.cargoOrderList = response;
      } else if (this.executorGroupId.length) {
        // По executorGroup через POST - organizationId не передаём (только executorGroupIds)
        const response = await this.service.getFeedOrderListPost(
          undefined, // orgId не передаём для режима групп
          {
            ...this.pageFilters,
            ...this.cargoQueryFilters,
            ...sortParams,
          },
          this.executorGroupId,
          false // хардкод: UUID ≠ empty
        );
        this.cargoOrderList = response;
      } else {
        // «Все группы» или «Без групп» — пустой executorGroupIds, emptyExecutorGroup из контекста
        this.cargoOrderList = await this.service.getFeedOrderListPost(
          undefined,
          {
            ...this.pageFilters,
            ...this.cargoQueryFilters,
            ...sortParams,
          },
          [], // executorGroupIds пустой
          emptyExecutorGroup // значение из контекста
        );
      }
    } catch (error) {
      console.error('Ошибка при загрузке грузовых заказов (Deferred POST):', error);
      this.cargoOrderList = EMPTY_SEARCH_RESPONSE;
    }
  };
}
