import type { ResponseService, IHttpService, ILogger } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import type { UUID } from 'utils/io-ts';
import type { AxiosError } from 'axios';

import { TYPES } from 'ioc/types';
import { stringify } from 'qs';

import {
  ADD_ADDITIONAL_CONTACT,
  CREATE_CARGO_ENGINEER_COMMENT,
  GET_TARIFF_TRANSPORT_TYPE_ALL_MULTI,
  GET_TEMPLATE_BY_ORG_ID,
  MOCKED_API_PREFIX,
  REQUESTS_CARGO_ACTIVE_BY_HR_ID,
  REQUESTS_OTO_FEED_CARGO_BY_ORG_ID,
  REQUESTS_OTO_FEED_CARGO_POST,
  SEND_ORDER_TO_CONTRACTOR,
  SEND_ROUTE_TO_CONTRACTOR,
  UPDATE_ORDER,
  GET_DEPARTMENT_LIST,
  GET_EMPLOYEE_LIST,
  GET_EMPLOYEE_ORGANIZATION,
  GET_ORGANIZATION_LIST,
  GET_STATE_NUMBER_LIST
} from 'constants/constants.api';

import {
  ICargoService,
  IDepartmentListResponse,
  IOrganization,
  ISearchEmployeeResponse } from '../../interfaces/Cargo/Cargo.interface';
import type {
  CargoEngineerComment,
  CargoEngineerCommentPayload,
  FeedSearchQuery,
  Order,
  OrderField,
  OrderFieldResult,
  SearchResponse,
  TariffRequestMulti,
  AddDelegatePayload,
} from '../../interfaces/Orders.types';
import { SchedulerSearchResponse } from 'modules/OrderExecution/interfaces/SchedulerOrders.types';

@injectable()
export class DICargoService implements ICargoService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: ResponseService;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

   /* ========== Методы из DIOrderService ========== */
  getStateNumberList(stateNumber: string): Promise<string[] | void> {
    return this.http
      .get<any>(GET_STATE_NUMBER_LIST, { urlParams: { stateNumber } })
      .then(this.process.getResponseData)
      .catch((error: AxiosError) => {
        const errorDescription = error.response?.data.message || error.response?.data.error || error.message;
        this.logger.toError(errorDescription, 'Ошибка при поиске гос. номеров');
      });
  }

  getEmployeeList(name: string): Promise<ISearchEmployeeResponse[] | void> {
    return this.http
      .get<ISearchEmployeeResponse[]>(GET_EMPLOYEE_LIST, { urlParams: { name } })
      .then(this.process.getResponseData)
      .catch((error: AxiosError) => {
        const errorDescription = error.response?.data.message || error.response?.data.error || error.message;
        this.logger.toError(errorDescription, 'Ошибка при поиске сотрудника');
      });
  }

  getEmployeeOrganization(): Promise<IOrganization | void> {
    return this.http
      .get<IOrganization>(GET_EMPLOYEE_ORGANIZATION)
      .then(this.process.getResponseData)
      .catch((error: AxiosError) => {
        const errorDescription = error.response?.data.message || error.response?.data.error || error.message;
        this.logger.toError(errorDescription, 'Ошибка при получении организации');
      });
  }

  getOrganizationList(): Promise<IOrganization[] | void> {
    return this.http
      .get<any>(GET_ORGANIZATION_LIST)
      .then(this.process.getResponseData)
      .catch((error: AxiosError) => {
        const errorDescription = error.response?.data.message || error.response?.data.error || error.message;
        this.logger.toError(errorDescription, 'Ошибка при получении списка организаций');
      });
  }

  getDepartmentList(organizationId: UUID): Promise<IDepartmentListResponse[] | void> {
    return this.http
      .post<IDepartmentListResponse[]>(GET_DEPARTMENT_LIST, [organizationId])
      .then(this.process.getResponseData)
      .catch((error: AxiosError) => {
        const errorDescription = error.response?.data.message || error.response?.data.error || error.message;
        this.logger.toError(errorDescription, 'Ошибка при получении списка подразделений');
      });
  }

  // Получение одной заявки(Детальный просмотр)
  getCargoOrderActive = async (humanReadableId: string): Promise<Order> => {
    const REQUEST_LINK = REQUESTS_CARGO_ACTIVE_BY_HR_ID;
    return this.http
      .get<Order>(REQUEST_LINK, { urlParams: { humanReadableId } })
      .then(this.process.getResponseData);
  };

  // Получение списка заявок в "Исполнении заявок"(Монитор)
  getCargoOrderList = async (orgId: UUID, query: FeedSearchQuery): Promise<SearchResponse> => {
    // В params передаём только flat-версии statuses и requestType, а не оригинальные поля
    // чтобы избежать дублирования и ошибок API при одновременном наличии обоих форматов
    const { statuses, requestType, ...restQuery } = query;
    return this.http
      .get<SearchResponse>(REQUESTS_OTO_FEED_CARGO_BY_ORG_ID, {
        urlParams: { organizationId: orgId },
        params: {
          ...restQuery,
          statuses: statuses?.flat(),
          requestTypes: requestType?.flat()
        },
        paramsSerializer: (params) => stringify(params, { arrayFormat: 'repeat' })
      })
      .then(this.process.getResponseData);
  };

  // Получение расписания
  getCargoSchedulerList = async (orgId: UUID, query: FeedSearchQuery): Promise<SchedulerSearchResponse> => {
    // В params передаём только flat-версию statuses, а не оригинальное поле
    // чтобы избежать дублирования и ошибок API при одновременном наличии обоих форматов
    const { statuses, ...restQuery } = query;
    return this.http
      .get<SchedulerSearchResponse>(GET_TEMPLATE_BY_ORG_ID, {
        urlParams: { organizationId: orgId },
        params: {
          ...restQuery,
          statuses: statuses?.flat()
        },
        paramsSerializer: (params) => stringify(params, { arrayFormat: 'repeat' })
      })
      .then(this.process.getResponseData);
  };

  // Изменение заявки в детальном просмотре(Монитор)
  changeOrder = async (humanReadableId: string, orderFields: OrderField[]): Promise<OrderFieldResult[] | undefined> => {
    return this.http
      .patch<string>(`${MOCKED_API_PREFIX}${UPDATE_ORDER}`, orderFields, {
        urlParams: { humanReadableId },
        headers: { 'Content-Type': 'application/json' },
      })
      .then(response => {
        const responseData = response.config.data;
        this.logger.toMessage('success', 'Заявка изменена');
        return responseData;
      })
      .catch((error: AxiosError) => {
        const errorDescription = error.response?.data.message || error.response?.data.error || error.message;
        this.logger.toError(errorDescription, 'Ошибка при изменении заявки');
      });
  };

  // Изменение комментария в детальном просмотре(Монитор)
  changeEngineerComment = async (humanReadableId: string, comment: string): Promise<CargoEngineerComment[]> => {
    const patchData: CargoEngineerCommentPayload[] = [{
      field: 'COMMENT_ENGINEER',
      value: comment,
    }];

    return this.http
      .patch<string>(`${MOCKED_API_PREFIX}${CREATE_CARGO_ENGINEER_COMMENT}`, patchData, {
        urlParams: { humanReadableId: humanReadableId },
        headers: { 'Content-Type': 'application/json' },
      })
      .then(response => {
        const responseData = response.config.data;
        this.logger.toMessage('success', 'Комментарий инженера изменен');
        return responseData;
      })
      .catch((error: AxiosError) => {
        const errorDescription = error.response?.data.message || error.response?.data.error || error.message;
        this.logger.toError(errorDescription, 'Ошибка при изменении комментария');
      });
  };

  // Получение списка тарифов в детальном просмотре заявки(Монитор)
  calculateAllTariffsMulti = async (data: TariffRequestMulti | undefined): Promise<any> => {
    return this.http
      .post<TariffRequestMulti>(`${MOCKED_API_PREFIX}${GET_TARIFF_TRANSPORT_TYPE_ALL_MULTI}`, { ...data })
      .then(this.process.getResponseData)
      .catch(() => {
        return [];
      });
  }

  sendOrderToContractor = async (orderId: string) => {
    return this.http
      .put<string>(`${MOCKED_API_PREFIX}${SEND_ORDER_TO_CONTRACTOR}`, {}, { urlParams: { orderId } })
  }

  sendRouteToContractor = async (routeId: string) => {
    return this.http
      .put<string>(`${MOCKED_API_PREFIX}${SEND_ROUTE_TO_CONTRACTOR}`, {}, { urlParams: { routeId } })
  }

  // Добавление дополнительного контакта
  addAdditionalContact = async (humanReadableId: string, contactData: AddDelegatePayload[]): Promise<void> => {
    const patchData = contactData.map(contact => ({
      field: 'CONTACTS',
      value: [
        {
          ...contact,
        }
      ],
    }));

    await this.http
      .patch<void>(`${MOCKED_API_PREFIX}${ADD_ADDITIONAL_CONTACT}`, patchData, {
        urlParams: { humanReadableId },
        headers: { 'Content-Type': 'application/json' },
      })
      .then(response => {
        const responseData = response.config.data;
        this.logger.toMessage('success', 'Дополнительный контакт добавлен');
        return responseData;
      })
      .catch((error: AxiosError) => {
        const errorDescription = error.response?.data.message || error.response?.data.error || error.message;
        this.logger.toError(errorDescription, 'Ошибка при добавлении дополнительного контакта');
        throw error;
      });
  }

  // Методы с POST запросами (параметры в теле) для useDeferredSearch
  // Получение списка заявок по организации через POST
  getFeedOrderListPost = async (orgId: string | null | undefined, query: FeedSearchQuery, executorGroupIds?: string[], emptyExecutorGroup?: boolean): Promise<SearchResponse> => {
    // В теле запроса передаём только flat-версии statuses и requestType, а не оригинальные поля
    // чтобы избежать дублирования и ошибок API при одновременном наличии обоих форматов
    const { statuses, requestType, ...restQuery } = query;
    const requestBody: Record<string, unknown> = {
      ...restQuery,
      statuses: statuses?.flat(),
      requestTypes: requestType?.flat()
    };

    // Добавляем organizationId только если он передан (не undefined/null)
    if (orgId) {
      requestBody.organizationId = orgId;
    }

    // Добавляем executorGroupIds даже, если селект групп исполнителей пустой
    requestBody.executorGroupIds = executorGroupIds || [];

    // Добавляем emptyExecutorGroup всегда явно (контракт с бэкендом)
    requestBody.emptyExecutorGroup = emptyExecutorGroup ?? false;

    // Убираем параметры из URL - передаем только пустые urlParams
    return this.http
      .post<SearchResponse>(REQUESTS_OTO_FEED_CARGO_POST, requestBody, {
        urlParams: {}
      })
      .then(this.process.getResponseData);
  };
}
