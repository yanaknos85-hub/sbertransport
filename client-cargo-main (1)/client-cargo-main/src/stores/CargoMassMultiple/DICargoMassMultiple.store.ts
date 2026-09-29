import type { ILogger } from '@sber-sbertransport/mf-core';
import { notification } from 'antd';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import { action, computed, observable } from 'mobx';
import { SOMETHING_WRONG_TITLE } from 'shared/constants/constants';
import { RouteModel } from 'shared/models/geo/Route.model';
import { RequestRoute } from 'shared/models/geo/types';
import { plainToNew } from 'utils';

import type { TariffType } from 'stores/CargoTariff/CargoTariff.interface';
import { CargoListItem, RequestObject, TransportTypeEnum } from 'types/Cargo';
import { SortType, SortTypeOrder } from 'modules/CargoMassMultiple/components/SortOrder/SortOrder';
import { RequestObjectMulti } from 'modules/CargoMassMultiple/types';

import * as CargoMassMultipleInterface from './CargoMassMultiple.interface';
import { RequestModel } from './models/CargoMassRequestMultiple.model';

const ERROR_MESSAGE = 'Ошибка загрузки файла';

@injectable()
export class DICargoMassStoreMultiple implements CargoMassMultipleInterface.ICargoMassStoreMultiple {
  @inject(TYPES.ICargoMassServiceMultiple)
  private service!: CargoMassMultipleInterface.ICargoMassServiceMultiple;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  @observable
    uploadRequestId = 'a75b8d90-c1b2-4388-913c-2401c8c38bb3'; //  хардкодное значение, чтобы смотреть на тесте сразу таблицу!

  @observable
    regularMassId = '';

  @observable
    request: CargoMassMultipleInterface.Request | undefined;

  @observable
    requestList: CargoMassMultipleInterface.RequestListItem[] = [];

  @action
  async loadRequest(): Promise<CargoMassMultipleInterface.Request | undefined> {
    try {
      this.request = await this.getRequest(this.uploadRequestId);
      this.requestList = this.request?.data || [];
      return this.request;
    } catch (err) {
      return undefined;
    }
  }

  @action
  async loadRegularRequest(): Promise<CargoMassMultipleInterface.Request | undefined> {
    try {
      this.request = await this.getRegularRequest(this.regularMassId);
      return this.request;
    } catch (err) {
      return undefined;
    }
  }

  @action
  async loadRegularRequestMulti(): Promise<CargoMassMultipleInterface.Request | undefined> {
    const shouldContinue = true;
    while (shouldContinue) {
      try {
        this.request = await this.getRegularRequest(this.regularMassId);
        const status = this.request?.status;

        if (status === CargoMassMultipleInterface.RequestStatus.SUCCESS) {
          notification.success({
            message: 'Создание массовой регулярной доставки',
            description: this.request?.description || 'Создание прошло успешно',
          });

          if (this.requestList.length === 1) {
            this.successMessage('Заявка согласована');
          } else if (this.requestList.length > 1) {
            this.successMessage('Заявки согласованы');
          }
          return this.request;
        }

        if (status === CargoMassMultipleInterface.RequestStatus.ERROR) {
          notification.error({
            message: 'Создание массовой регулярной доставки',
            description: this.request?.description || SOMETHING_WRONG_TITLE,
          });
          return undefined;
        }

        await new Promise(resolve => setTimeout(resolve, 1000));
      } catch (err) {
        return undefined;
      }
    }
  }

  @action
  clearRequestList(): void {
    this.requestList = [];
  }

  @action.bound
  successMessage(description: string): void {
    this.logger.toMessage('success', description);
  }

  @action
  prepareRequest(): void {
    this.requestList = this.requestList.map(request => {
      const cargos = request.listCargo
        .filter(cargo => cargo.cargoName)
        // Пока позиция ставится автоматически,
      // т.к. при импорте можно запороть уникальность и не будет работать корректно удаление грузов
        .map((cargo, i) => ({ ...cargo, position: i + 1 }));

      // приходится переопределять свойсто чтобы оторваться от бекэнда
      // и вести динамичный подсчет
      request.totalSizes = DICargoMassStoreMultiple.totalCargoSizes(cargos);
      return { ...request, listCargo: [...cargos] };
    });
  }

  @action
  saveRequestItem(requestId: string, data: RequestModel): void {
    this.requestList = this.requestList.map(request => {
      if (request.id === requestId) {
        return data;
      }
      return request;
    });
    this.prepareRequest();
  }

  @action
  deleteRequestItem(requestId: string): number {
    this.requestList = this.requestList.filter(request => request.id !== requestId);
    this.prepareRequest();
    return this.requestList.length;
  }

  @action.bound
  async postData(data: RequestObjectMulti[]): Promise<void> {
    try {
      await this.service.postData(data);
      this.logger.toMessage('success', 'Заявки на согласовании'); // SYSTEM_MESSAGES.departmentEditSuccess
    } catch (e) {
      const err = (e as any).response?.data.message || SOMETHING_WRONG_TITLE;
      notification.error({
        message: err,
      });
      this.logger.toError(err, 'Ошибка отправки данных');
      throw new Error(err);
    }
  }

  @action.bound
  async postRegularData(data: RequestObject[]): Promise<void> {
    try {
      const response = await this.service.postRegularData(data);
      const { requestId } = response;
      this.regularMassId = requestId;
    } catch (e) {
      const err = (e as any).response?.data.message || SOMETHING_WRONG_TITLE;
      this.logger.toError(err, 'Ошибка отправки данных');
      throw new Error(err);
    }
  }

  @action.bound
  sortRequestList(type: SortType, order: SortTypeOrder): void {
    this.requestList = [...this.requestList].sort((a, b) => {
      let compareA = 0;
      let compareB = 0;

      if (type === SortType.CREATION_TIME) {
        compareA = Number(a.creationTime) || 0;
        compareB = Number(b.creationTime) || 0;
      }

      if (type === SortType.TIME) {
        compareA = a.tariffs.find(t => t.active)?.deliveryTime || 0;
        compareB = b.tariffs.find(t => t.active)?.deliveryTime || 0;
      }

      if (type === SortType.COST) {
        compareA = a.tariffs.find(t => t.active)?.cost || 0;
        compareB = b.tariffs.find(t => t.active)?.cost || 0;
      }

      if (type === SortType.VOLUME) {
        compareA = a.totalSizes.volume || 0;
        compareB = b.totalSizes.volume || 0;
      }

      if (type === SortType.DISTANCE) {
        compareA = a.expected.distance || 0;
        compareB = b.expected.distance || 0;
      }

      if (order === SortTypeOrder.DESC) {
        if (compareA > compareB) {
          return 1;
        }
        if (compareA < compareB) {
          return -1;
        }
      } else {
        if (compareB > compareA) {
          return 1;
        }
        if (compareB < compareA) {
          return -1;
        }
      }
      return 0;
    });
  }

  @action
  async saveFile(file: File): Promise<void> {
    try {
      const response = await this.service.saveFile(file);
      if (response) {
        const { requestId } = response;
        this.uploadRequestId = requestId;
      } else {
        throw new Error(ERROR_MESSAGE);
      }
    } catch (err: any) {
      const error = err.message || ERROR_MESSAGE;
      throw new Error(error);
    }
  }

  @action
  async saveFileRegular(file: File): Promise<void> {
    try {
      const response = await this.service.saveFileRegular(file);
      if (response) {
        const { requestId } = response;
        this.uploadRequestId = requestId;
      } else {
        throw new Error(ERROR_MESSAGE);
      }
    } catch (err: any) {
      const error = err.message || ERROR_MESSAGE;
      throw new Error(error);
    }
  }

  @action.bound
  selectTariffCargo(requestId: string, tariffType: TariffType): void {
    this.requestList = this.requestList.map(request => {
      if (request.id === requestId) {
        request.expected.cost = request.tariffs.find(tariff => tariff.transportType.name === tariffType)?.cost;
        request.tariffs = request.tariffs.map(tariff => {
          return ({
            ...tariff,
            active: tariff.transportType.name === tariffType,
          });
        });
      }
      return request;
    });
  }

  @action.bound
  selectAllTariffCargo(tariffType: TariffType): void {
    this.requestList = this.requestList.map(request => {
      const existTariffNames = request.tariffs.map(t => t.transportType.name);
      request.tariffs = request.tariffs.map(tariff => ({
        ...tariff,
        active: existTariffNames.includes(tariffType) ? tariff.transportType.name === tariffType : tariff.active,
      }));

      request.expected.cost = request.tariffs.find(tariff => tariff.transportType.name === tariffType)?.cost;

      return request;
    });
  }

  @computed
  get totalData(): CargoMassMultipleInterface.TotalData {
    return this.requestList.reduce(
      (acc, curr) => ({
        deliveryTimeDate: curr.deliveryTimeDate + acc.deliveryTimeDate,
        expected: {
          cost: (curr.expected?.cost || 0) + (acc.expected?.cost || 0),
          time: (curr.expected?.time || 0) + (acc.expected?.time || 0),
          distance: (curr.expected?.distance || 0) + (acc.expected?.distance || 0),
        },
        sizes: {
          ...(Object.keys(curr.totalSizes) as (keyof CargoMassMultipleInterface.TotalSizes)[]).reduce(
            (ac, key) => ({
              ...ac,
              [key]: curr.totalSizes[key] + ((acc.sizes || {})[key] || 0),
            }),
            {} as CargoMassMultipleInterface.TotalSizes
          ),
        },
        tariffCost: ((): CargoMassMultipleInterface.TariffCost => {
          const currentTariff = (curr.tariffs || []).find(tariff => tariff.active);
          if (!currentTariff) {
            return { ...acc.tariffCost };
          }
          const currentTariffName = currentTariff.transportType.name;
          const currDetails = acc.tariffCost.details[currentTariffName as TransportTypeEnum];
          return {
            totalCost: (acc.tariffCost.totalCost || 0) + currentTariff.cost,
            totalTime: (acc.tariffCost.totalTime || 0) + currentTariff.deliveryTime,
            details: {
              ...acc.tariffCost.details,
              [currentTariffName]: {
                cost: (currDetails.cost || 0) + currentTariff.cost,
                time: (currDetails.time || 0) + currentTariff.deliveryTime,
                count: (currDetails.count || 0) + 1,
              },
            },
          };
        })(),
      }),
      {
        deliveryTimeDate: 0,
        expected: {},
        sizes: {},
        tariffCost: {
          totalCost: 0,
          details: {
            [TransportTypeEnum.COURIER]: {},
            [TransportTypeEnum.DEDICATED]: {},
            [TransportTypeEnum.INTERREGIONAL]: {},
            [TransportTypeEnum.INDIVIDUAL]: {},
          },
        },
      } as CargoMassMultipleInterface.TotalData
    );
  }

  @computed
  get totalSizes(): CargoMassMultipleInterface.TotalSizes {
    return this.requestList.reduce(
      (acc, curr) => ({
        ...acc,
        ...(Object.keys(curr.totalSizes) as (keyof CargoMassMultipleInterface.TotalSizes)[]).reduce(
          (ac, key) => ({
            ...ac,
            [key]: curr.totalSizes[key] + (acc[key] || 0),
          }),
          {} as CargoMassMultipleInterface.TotalSizes
        ),
      }),
      {} as CargoMassMultipleInterface.TotalSizes
    );
  }

  @computed
  get totalExpected(): RouteModel {
    const data = this.requestList.reduce(
      (acc, curr) => ({
        ...acc,
        cost: (curr.expected.cost || 0) + (acc.cost || 0),
        time: curr.expected.time + (acc.time || 0),
        distance: curr.expected.distance + (acc.distance || 0),
      }),
      {} as RequestRoute
    );

    return plainToNew<RouteModel>(RouteModel, data);
  }

  private async getRequest(id: string): Promise<CargoMassMultipleInterface.Request | undefined> {
    const response = await this.service.getRequest(id);
    if (response) {
      return {
        ...response,
        data: plainToNew(RequestModel, response?.data || []),
      };
    }
    return undefined;
  }

  private async getRegularRequest(id: string): Promise<CargoMassMultipleInterface.Request | undefined> {
    const response = await this.service.getRegularRequest(id);
    if (response) {
      return {
        ...response,
        data: plainToNew(RequestModel, []),
      };
    }
    return undefined;
  }

  private static totalCargoSizes(list: CargoListItem[]): any {
    return list.reduce(
      (acc, curr) => ({
        ...acc,
        width: curr.width * curr.occupiedPlacesCount + (acc.width || 0),
        length: curr.length * curr.occupiedPlacesCount + (acc.length || 0),
        height: curr.height * curr.occupiedPlacesCount + (acc.height || 0),
        volume: curr.volume * curr.occupiedPlacesCount + (acc.volume || 0),
        weight: curr.weight * curr.occupiedPlacesCount + (acc.weight || 0),
        occupiedPlacesCount: curr.occupiedPlacesCount + (acc.occupiedPlacesCount || 0),
      }),
      {} as CargoMassMultipleInterface.TotalSizes
    );
  }
}
