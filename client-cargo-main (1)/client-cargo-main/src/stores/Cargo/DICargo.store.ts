import type { ILogger, ISelfEmployeeStore } from '@sber-sbertransport/mf-core';
import { notification } from 'antd';
import { inject, injectable } from 'inversify';
import { TYPES } from 'ioc/types';
import { action, observable } from 'mobx';
import moment from 'moment';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';
import { DATE_FORMAT } from 'constants/constants.app';

import type { CargoListItem, CargoPersonalListItemType } from '../../types/Cargo';
import { BackendCargoPersonalListItemType } from '../../types/Cargo';
import * as types from '../Cargos/types';
import * as typesMulti from '../Cargos/typesMulti';
import type {
  ICargoService,
  ICargoStore,
  PackageForm,
  StepAddressValues,
  StepAddressValuesMulti,
  StepCargosValues,
  StepFinalValues,
  StepTariffValues,
  StepTariffValuesMulti
} from './Cargo.interface';

export enum Periodicity {
  week = 'WEEK',
  month = 'MONTH',
  quarter = 'QUARTER',
}

const POST_DATA_MESSAGE: Record<string, string> = {
  SOMETHING_WRONG_TITLE: 'Что-то пошло не так',
  CARGO_AWAITING_APPROVAL: 'Заявка на согласовании',
  CARGO_APPROVED: 'Заявка согласована',
  CARGO_AWAITING_DATA: 'Отправлено контрагенту',
  ERROR_POST_DATA: 'Ошибка отправки данных',
};

@injectable()
export class DICargoStore implements ICargoStore {
  @inject(TYPES.ICargoService)
  private service!: ICargoService;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  @inject(TYPES.ISelfEmployeeStore)
  private selfStore!: ISelfEmployeeStore;

  @observable
  private initialStepAddressValues = {
      sender: this.selfStore.selfEmployee,
      recipient: null,
      senderAddress: '',
      desiredDate: moment().utc().add(1, 'days').startOf('day'),
      senderName: this.selfStore.selfEmployee?.fullNameWithCode || '',
      senderPhone: this.selfStore.selfEmployee?.mobilePhone || '',
      senderOrganization: '',
      sourceLoaders: false,
      recipientAddress: '',
      recipientName: '',
      recipientPhone: '',
      recipientOrganization: '',
      destinationLoaders: false,
      sourceLoadersCount: 0,
      destinationLoadersCount: 0,
    };

  private initialStepCargosValues = {
    cargoList: [],
  };

  private initialStepTariffValues = {
    distance: 0,
    tariffCost: null,
    expected: null,
    express: false,
    comment: '',
  };

  private initialStepFinalValues = {
    cost: 0,
    deliveryTime: 0,
    occupiedPlacesCount: 0,
    width: 0,
    length: 0,
    height: 0,
    volume: 0,
    weight: 0,
    comment: '',
    loaders: 0,
  };

  initialPeriodValues: types.PeriodType = {
    periodType: Periodicity.week,
    dayOfWeek: [],
    weekOfMonth: [],
    monthOfQuartal: [],
    beginDate: moment()
      .startOf('day')
      .add(1, 'days')
      .format(DATE_FORMAT.BASE)
      .valueOf() as unknown as string,
    endDate: '',
  };

  private initialTotalRegularCost = 0;

  @observable
    stepAddressValues: StepAddressValues = {
      ...this.initialStepAddressValues,
    };

  @observable
    stepCargosValues: StepCargosValues = {
      ...this.initialStepCargosValues,
    };

  @observable
    stepTariffValues: StepTariffValues = {
      ...this.initialStepTariffValues,
    };

  @observable
    stepTariffValuesMulti: StepTariffValuesMulti = {
      ...this.initialStepTariffValues,
    };

  @observable
    stepFinalValues: StepFinalValues = {
      ...this.initialStepFinalValues,
    };

  @observable
    periodValues: types.PeriodType = {
      ...this.initialPeriodValues,
    };

  @observable
    totalRegularCost = this.initialTotalRegularCost;

  @observable
  private initialStepAddressValuesMulti = {
      sender: this.selfStore.selfEmployee,
      desiredDate: moment().add(1, 'days').startOf('day'),
      loaders: 0,
      internalNote: '',
    };

  @observable
    stepAddressValuesMulti: StepAddressValuesMulti = {
      ...this.initialStepAddressValuesMulti,
    };

  @observable
    waypointsListMulti: any[] = [{
      address: '',
      name: '',
      phone: '',
      type: '',
      organization: '',
    }];

  @observable
    packs: types.Pack[] = [];

  @observable
    packageForm: PackageForm = {
      loadersNeeded: false,
      loadersCount: 0,
      packages: [],
    };

  @observable
    comment = '';

  @observable
    purpose = {
      REGULAR: false,
      RELOCATION: false,
      SINGLE: false,
    };

  @observable
    selectedFlat: { idx: number; type: string } = {
      idx: 0, type: 'studio',
    };

  @observable
    additionalServices = {
      loaders: false,
      car: false,
      lift: false,
    };

  @observable
    isRepeatOrder = false;

  @action.bound
  setRepeatOrder(value: boolean): void {
    this.isRepeatOrder = value;
  }

  @action.bound
  setAdditionalServices(services: Record<string, boolean>): void {
    this.additionalServices = { ...this.additionalServices, ...services };
  }

  get isRegular() {
    return this.purpose.REGULAR;
  }

  get isRelocation() {
    return this.purpose.RELOCATION;
  }

  @action
  setSelectedFlatType(idx, type) {
    this.selectedFlat = { idx, type };
  }

  @action
  setRegularOrder(value: boolean): void {
    this.purpose.REGULAR = value;
  }

  @action
  setRelocationOrder(value: boolean): void {
    this.purpose.RELOCATION = value;
  }

  @action
  setCargoList(list: CargoListItem[]): void {
    this.stepCargosValues.cargoList = list;
  }

  @action.bound
  async createCargoItemPersonal(cargoItem: CargoPersonalListItemType): Promise<BackendCargoPersonalListItemType> {
    try {
      return await this.service.createCargoItemPersonal(cargoItem);
    } catch (error) {
      this.logger.toError('Ошибка при создании груза', String(error));
      if (error instanceof Error) {
        throw error;
      } else {
        throw new Error(String(error));
      }
    }
  }

  @observable
    lastLoadedFlatType: string | null = null;

  @action.bound
    setLastLoadedFlatType = (type: string) => {
      this.lastLoadedFlatType = type;
    };

  @action.bound
  fillForSteps(cargoMultipleRequest: CargoRequestModel): void {
    this.stepAddressValuesMulti.waypoints = cargoMultipleRequest.waypoints.map(({
      type,
      name,
      addressType,
      addressStringRepresentation,
      organization,
      contacts,
    }) => {
      const [{
        id,
        mobilePhone,
        fullName,
        employeeId,
      }] = contacts || [{}];

      const address = addressType !== 'ORDINARY' && name
        // Строка собирается так же как в модели Waypoint.model.ts buildAddressString()
        ? `${addressStringRepresentation} , ${name}`
        : addressStringRepresentation || '';

      return {
        address,
        id: id || '',
        name: fullName || '',
        phone: mobilePhone || '',
        employeeId: employeeId || '',
        organization: organization || '',
        type: type || '',
      };
    });
    this.waypointsListMulti = cargoMultipleRequest.waypoints.map(el => ({
      ...el,
      contacts: el.contacts ? el.contacts.map(({ id, ...rest }) => rest) : [],
    }));
    this.stepCargosValues.cargoList = cargoMultipleRequest.listCargo;
  }

  @action
  setCheckLoaders(value: boolean): void {
    this.packageForm.loadersNeeded = value;
  }

  @action
  setComment(value: string): void {
    this.comment = value;
  }

  @action
  savePackageForm(formValues: PackageForm): void {
    this.packageForm = formValues;
  }

  @action
  resetPackageForm(): void {
    this.packageForm = {
      loadersNeeded: false,
      loadersCount: 0,
      packages: [],
    };
  }

  @action
  addAddress(waypoints: typesMulti.WaypointMulti[]): void {
    this.waypointsListMulti = waypoints;
  }

  @action
  clearStepValues(): void {
    this.stepAddressValuesMulti = { ...this.initialStepAddressValuesMulti };
    this.waypointsListMulti = { ...this.waypointsListMulti };
    this.stepAddressValues = { ...this.initialStepAddressValues };
    this.stepCargosValues = { ...this.initialStepCargosValues };
    this.stepTariffValues = { ...this.initialStepTariffValues };
    this.stepFinalValues = { ...this.initialStepFinalValues };
    this.periodValues = { ...this.initialPeriodValues };
    this.totalRegularCost = this.initialTotalRegularCost;
    this.purpose.REGULAR = false;
    this.purpose.RELOCATION = false;
    this.purpose.SINGLE = false;
    this.comment = '';
  }

  @action
  setStepAddressValues(data: StepAddressValues): void {
    this.stepAddressValues = { ...this.stepAddressValues, ...data };
  }

  @action
  setStepAddressValuesMulti(data: typesMulti.StepAddressValuesMulti): void {
    this.stepAddressValuesMulti = { ...this.stepAddressValuesMulti, ...data };
  }

  @action
  setStepCargosValues(data: StepCargosValues): void {
    this.stepCargosValues = { ...this.stepCargosValues, ...data };
  }

  @action
  setStepTariffValuesMulti(data: StepTariffValuesMulti): void {
    this.stepTariffValuesMulti = { ...this.stepTariffValuesMulti, ...data };
  }

  @action
  setStepFinalValues(data: StepFinalValues): void {
    this.stepFinalValues = { ...this.stepFinalValues, ...data };
  }

  @action.bound
  setPeriodValues(data: types.PeriodType): void {
    this.periodValues = { ...this.periodValues, ...data };
  }

  @action.bound
  async getPeriodValuesMulti(data: Partial<types.PeriodType>): Promise<void> {
    this.totalRegularCost = await this.service.getPeriodValuesMulti(data);
  }

  @action.bound
  async postDataMulti(data: typesMulti.OrderRequestMulti): Promise<void> {
    try {
      const response = await this.service.postDataMulti(data);
      if (response) {
        this.logger.toMessage('success', POST_DATA_MESSAGE[response.status]);
        this.clearStepValues();
        this.resetPackageForm();
      }
    } catch (e: any) {
      const err = (e as any).response?.data.message || POST_DATA_MESSAGE.SOMETHING_WRONG_TITLE;
      this.logger.toError(err, POST_DATA_MESSAGE.ERROR_POST_DATA);
      notification.error({
        message: 'Создание доставки!',
        description: e.response.data.message || 'Что-то пошло не так',
      });
      throw new Error(err);
    }
  }

  @action.bound
  async postRegularCargoDataMulti(data: typesMulti.OrderRequestRegularMulti): Promise<void> {
    try {
      const response = await this.service.postRegularCargoDataMulti(data);
      if (response) {
        this.logger.toMessage('success', 'Заявка на согласовании');
        this.clearStepValues();
        this.resetPackageForm();
      }
    } catch (e: any) {
      const err = (e as any).response?.data?.message || POST_DATA_MESSAGE.SOMETHING_WRONG_TITLE;
      this.logger.toError(err, POST_DATA_MESSAGE.ERROR_POST_DATA);
      notification.error({
        message: 'Создание доставки!',
        description: e.response.data.message || 'Что-то пошло не так',
      });
      throw new Error(e);
    }
  }

  @action.bound
  async getPack(): Promise<void> {
    await this.service.getPack().then(data => {
      this.packs = data.content;
    });
  }
}
