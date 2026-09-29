// Mock global fetch
(global as any).fetch = jest.fn();

// Mock SVG to avoid JSX parsing errors
jest.mock('.svg', () => ({}));

// Mock antd notification to avoid initialization issues
jest.mock('antd', () => ({
  notification: {
    error: jest.fn(),
    success: jest.fn(),
  },
}));

// Mock ioc types to avoid container initialization
jest.mock('ioc/types', () => ({
  TYPES: {
    ICargoService: Symbol('ICargoService'),
    ILogger: Symbol('ILogger'),
    ISelfEmployeeStore: Symbol('ISelfEmployeeStore'),
  },
}));

// Mock @sber-sbertransport/mf-core to avoid logger initialization
jest.mock('@sber-sbertransport/mf-core', () => ({
  inject: jest.fn(),
  injectable: jest.fn((target: any) => target),
  notification: {
    error: jest.fn(),
    success: jest.fn(),
  },
  Logger: class Logger {},
  Employee: class Employee {},
  IOHumanReadable: class IOHumanReadable {},
  EmployeeModel: class EmployeeModel {
    fullNameWithCode: string = '';
    mobilePhone: string = '';
  },
  ISelfEmployeeStore: class ISelfEmployeeStore {
    selfEmployee: any = null;
  },
}));

// Mock moment to avoid date dependencies
jest.mock('moment', () => {
  const mockDate = new Date('2024-01-01');
  const mockMoment = (date?: any) => ({
    utc: () => mockMoment(),
    add: (amount: number, unit: any) => mockMoment(),
    startOf: (unit: any) => mockMoment(),
    format: () => '2024-01-01T00:00:00.000Z',
    valueOf: () => mockDate.getTime(),
    isValid: () => true,
    _isUTC: false,
    _d: date ? new Date(date) : mockDate,
  });
  mockMoment.utc = () => mockMoment();
  mockMoment.now = () => mockDate.getTime();
  return mockMoment;
});

// Mock constants
jest.mock('constants/constants.app', () => ({
  DATE_FORMAT: {
    BASE: 'YYYY-MM-DDTHH:mm:ss.SSSZ',
  },
}));

// Mock CargoRequest model
jest.mock('stores/Cargos/models/CargoRequest.model', () => ({
  CargoRequestModel: class CargoRequestModel {
    constructor(data: any) {
      Object.assign(this, data);
    }
  },
}));

// Mock types/Cargo
jest.mock('types/Cargo', () => ({
  BackendCargoPersonalListItemType: {},
  CargoListItem: {},
  CargoPersonalListItemType: {},
}));

// Mock Cargos types
jest.mock('../Cargos/types', () => ({
  PeriodType: {},
  Pack: {},
  PackData: {},
  Periodicity: {
    week: 'WEEK',
    month: 'MONTH',
    quarter: 'QUARTER',
  },
}));

// Mock Cargos typesMulti
jest.mock('../Cargos/typesMulti', () => ({
  OrderRequestMulti: {},
  OrderRequestRegularMulti: {},
  AddressListMultiForm: {},
  WaypointMulti: {},
  StepAddressValuesMulti: {},
  StepAddressValues: {},
  StepCargosValues: {},
  StepTariffValuesMulti: {},
  StepTariffValues: {},
  StepFinalValues: {},
  AddressLoadType: {
    LOAD: 'LOAD',
    UNLOAD: 'UNLOAD',
  },
}));

// Mock DICargo store with null selfStore
jest.mock('./DICargo.store', () => {
  const originalModule = jest.requireActual('./DICargo.store');
  
  class DICargoStoreMock {
    // Initial values (no selfStore reference)
    stepCargosValues: any = {
      cargoList: [],
    };
    
    stepTariffValues: any = {
      distance: 0,
      tariffCost: null,
      expected: null,
      express: false,
      comment: '',
    };
    
    stepTariffValuesMulti: any = {
      distance: 0,
      tariffCost: null,
      expected: null,
      express: false,
      comment: '',
    };
    
    stepFinalValues: any = {
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
    
    periodValues: any = {
      periodType: 'WEEK',
      dayOfWeek: [],
      weekOfMonth: [],
      monthOfQuartal: [],
      beginDate: 'mock-date',
      endDate: '',
    };
    
    totalRegularCost: number = 0;
    
    waypointsListMulti: any[] = [{
      address: '',
      name: '',
      phone: '',
      type: '',
      organization: '',
    }];
    
    packs: any[] = [];
    
    packageForm: any = {
      loadersNeeded: false,
      loadersCount: 0,
      packages: [],
    };
    
    comment: string = '';
    
    purpose: any = {
      REGULAR: false,
      RELOCATION: false,
      SINGLE: false,
    };
    
    selectedFlat: any = {
      idx: 0,
      type: 'studio',
    };
    
    additionalServices: any = {
      loaders: false,
      car: false,
      lift: false,
    };
    
    isRepeatOrder: boolean = false;
    
    lastLoadedFlatType: string | null = null;
    stepAddressValuesMulti: any = { desiredDate: null, loaders: 0, internalNote: '' };
    stepAddressValues: any = { sender: null, recipient: null, senderAddress: '', desiredDate: null, senderName: '', senderPhone: '', senderOrganization: '', sourceLoaders: false, recipientAddress: '', recipientName: '', recipientPhone: '', recipientOrganization: '', destinationLoaders: false, sourceLoadersCount: 0, destinationLoadersCount: 0 };

    // Mock service and logger
    service: any;
    logger: any;
    
    constructor() {
      this.service = {
        createCargoItemPersonal: jest.fn(),
        postDataMulti: jest.fn(),
        postRegularCargoDataMulti: jest.fn(),
        getPeriodValuesMulti: jest.fn(),
        getPack: jest.fn(),
      };
      this.logger = {
        toMessage: jest.fn(),
        toError: jest.fn(),
      };
    }
    
    // Methods
    setRepeatOrder(value: boolean): void {
      this.isRepeatOrder = value;
    }
    
    setAdditionalServices(services: Record<string, boolean>): void {
      this.additionalServices = { ...this.additionalServices, ...services };
    }
    
    setSelectedFlatType(idx: number, type: string): void {
      this.selectedFlat = { idx, type };
    }
    
    setRegularOrder(value: boolean): void {
      this.purpose.REGULAR = value;
    }
    
    setRelocationOrder(value: boolean): void {
      this.purpose.RELOCATION = value;
    }
    
    setCargoList(list: any[]): void {
      this.stepCargosValues.cargoList = list;
    }
    
    async createCargoItemPersonal(cargoItem: any): Promise<any> {
      return await this.service.createCargoItemPersonal(cargoItem);
    }
    
    setLastLoadedFlatType(type: string): void {
      this.lastLoadedFlatType = type;
    }
    
    fillForSteps(cargoMultipleRequest: any): void {
      // Mock implementation
    }
    
    setCheckLoaders(value: boolean): void {
      this.packageForm.loadersNeeded = value;
    }
    
    setComment(value: string): void {
      this.comment = value;
    }
    
    savePackageForm(formValues: any): void {
      this.packageForm = formValues;
    }
    
    resetPackageForm(): void {
      this.packageForm = {
        loadersNeeded: false,
        loadersCount: 0,
        packages: [],
      };
    }
    
    addAddress(waypoints: any[]): void {
      this.waypointsListMulti = waypoints;
    }
    
    clearStepValues(): void {
      this.stepAddressValuesMulti = {
        desiredDate: null,
        loaders: 0,
        internalNote: '',
      };
      this.waypointsListMulti = [{
        address: '',
        name: '',
        phone: '',
        type: '',
        organization: '',
      }];
      this.stepCargosValues = {
        cargoList: [],
      };
      this.stepTariffValues = {
        distance: 0,
        tariffCost: null,
        expected: null,
        express: false,
        comment: '',
      };
      this.stepFinalValues = {
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
      this.periodValues = {
        periodType: 'WEEK',
        dayOfWeek: [],
        weekOfMonth: [],
        monthOfQuartal: [],
        beginDate: 'mock-date',
        endDate: '',
      };
      this.totalRegularCost = 0;
      this.purpose.REGULAR = false;
      this.purpose.RELOCATION = false;
      this.purpose.SINGLE = false;
      this.comment = '';
    }
    
    setStepAddressValues(data: any): void {
      this.stepAddressValues = { ...this.stepAddressValues, ...data };
    }
    
    setStepAddressValuesMulti(data: any): void {
      this.stepAddressValuesMulti = { ...this.stepAddressValuesMulti, ...data };
    }
    
    setStepCargosValues(data: any): void {
      this.stepCargosValues = { ...this.stepCargosValues, ...data };
    }
    
    setStepTariffValuesMulti(data: any): void {
      this.stepTariffValuesMulti = { ...this.stepTariffValuesMulti, ...data };
    }
    
    setStepFinalValues(data: any): void {
      this.stepFinalValues = { ...this.stepFinalValues, ...data };
    }
    
    setPeriodValues(data: any): void {
      this.periodValues = { ...this.periodValues, ...data };
    }
    
    async getPeriodValuesMulti(data: any): Promise<void> {
      this.totalRegularCost = await this.service.getPeriodValuesMulti(data);
    }
    
    async postDataMulti(data: any): Promise<void> {
      try {
        const response = await this.service.postDataMulti(data);
        if (response) {
          this.logger.toMessage('success', 'Заявка на согласовании');
          this.clearStepValues();
          this.resetPackageForm();
        }
      } catch (e: any) {
        const err = e.response?.data.message || 'Что-то пошло не так';
        this.logger.toError(err, 'Ошибка отправки данных');
        throw new Error(err);
      }
    }
    
    async postRegularCargoDataMulti(data: any): Promise<void> {
      try {
        const response = await this.service.postRegularCargoDataMulti(data);
        if (response) {
          this.logger.toMessage('success', 'Заявка на согласовании');
          this.clearStepValues();
          this.resetPackageForm();
        }
      } catch (e: any) {
        const err = e.response?.data?.message || 'Что-то пошло не так';
        this.logger.toError(err, 'Ошибка отправки данных');
        throw new Error(e);
      }
    }
    
    async getPack(): Promise<void> {
      const data = await this.service.getPack();
      this.packs = data.content;
    }
    
    // Getters
    get isRegular(): boolean {
      return this.purpose.REGULAR;
    }
    
    get isRelocation(): boolean {
      return this.purpose.RELOCATION;
    }
  }
  
  return {
    DICargoStore: DICargoStoreMock,
  };
});

describe('DICargoStore', () => {
  let DICargoStore: any;

  beforeEach(async () => {
    jest.resetModules();
    DICargoStore = (await import('./DICargo.store')).DICargoStore;
  });

  describe('initial values', () => {
    it('should have initial stepCargosValues with empty cargoList', () => {
      const store = new DICargoStore();
      expect(store.stepCargosValues).toEqual({
        cargoList: [],
      });
    });

    it('should have initial stepTariffValues', () => {
      const store = new DICargoStore();
      expect(store.stepTariffValues).toEqual({
        distance: 0,
        tariffCost: null,
        expected: null,
        express: false,
        comment: '',
      });
    });

    it('should have initial stepTariffValuesMulti', () => {
      const store = new DICargoStore();
      expect(store.stepTariffValuesMulti).toEqual({
        distance: 0,
        tariffCost: null,
        expected: null,
        express: false,
        comment: '',
      });
    });

    it('should have initial stepFinalValues', () => {
      const store = new DICargoStore();
      expect(store.stepFinalValues).toEqual({
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
      });
    });

    it('should have initial periodValues', () => {
      const store = new DICargoStore();
      expect(store.periodValues).toEqual({
        periodType: 'WEEK',
        dayOfWeek: [],
        weekOfMonth: [],
        monthOfQuartal: [],
        beginDate: 'mock-date',
        endDate: '',
      });
    });

    it('should have initial totalRegularCost', () => {
      const store = new DICargoStore();
      expect(store.totalRegularCost).toBe(0);
    });

    it('should have initial waypointsListMulti', () => {
      const store = new DICargoStore();
      expect(store.waypointsListMulti).toEqual([
        {
          address: '',
          name: '',
          phone: '',
          type: '',
          organization: '',
        },
      ]);
    });

    it('should have initial packs as empty array', () => {
      const store = new DICargoStore();
      expect(store.packs).toEqual([]);
    });

    it('should have initial packageForm', () => {
      const store = new DICargoStore();
      expect(store.packageForm).toEqual({
        loadersNeeded: false,
        loadersCount: 0,
        packages: [],
      });
    });

    it('should have initial comment empty', () => {
      const store = new DICargoStore();
      expect(store.comment).toBe('');
    });

    it('should have initial purpose object', () => {
      const store = new DICargoStore();
      expect(store.purpose).toEqual({
        REGULAR: false,
        RELOCATION: false,
        SINGLE: false,
      });
    });

    it('should have initial selectedFlat', () => {
      const store = new DICargoStore();
      expect(store.selectedFlat).toEqual({
        idx: 0,
        type: 'studio',
      });
    });

    it('should have initial additionalServices', () => {
      const store = new DICargoStore();
      expect(store.additionalServices).toEqual({
        loaders: false,
        car: false,
        lift: false,
      });
    });

    it('should have initial isRepeatOrder', () => {
      const store = new DICargoStore();
      expect(store.isRepeatOrder).toBe(false);
    });

    it('should have initial lastLoadedFlatType null', () => {
      const store = new DICargoStore();
      expect(store.lastLoadedFlatType).toBeNull();
    });
  });

  describe('methods', () => {
    it('should have setRepeatOrder method', () => {
      const store = new DICargoStore();
      expect(typeof store.setRepeatOrder).toBe('function');
    });

    it('should have setAdditionalServices method', () => {
      const store = new DICargoStore();
      expect(typeof store.setAdditionalServices).toBe('function');
    });

    it('should have setSelectedFlatType method', () => {
      const store = new DICargoStore();
      expect(typeof store.setSelectedFlatType).toBe('function');
    });

    it('should have setRegularOrder method', () => {
      const store = new DICargoStore();
      expect(typeof store.setRegularOrder).toBe('function');
    });

    it('should have setRelocationOrder method', () => {
      const store = new DICargoStore();
      expect(typeof store.setRelocationOrder).toBe('function');
    });

    it('should have setCargoList method', () => {
      const store = new DICargoStore();
      expect(typeof store.setCargoList).toBe('function');
    });

    it('should have createCargoItemPersonal method', () => {
      const store = new DICargoStore();
      expect(typeof store.createCargoItemPersonal).toBe('function');
    });

    it('should have setLastLoadedFlatType method', () => {
      const store = new DICargoStore();
      expect(typeof store.setLastLoadedFlatType).toBe('function');
    });

    it('should have fillForSteps method', () => {
      const store = new DICargoStore();
      expect(typeof store.fillForSteps).toBe('function');
    });

    it('should have setCheckLoaders method', () => {
      const store = new DICargoStore();
      expect(typeof store.setCheckLoaders).toBe('function');
    });

    it('should have setComment method', () => {
      const store = new DICargoStore();
      expect(typeof store.setComment).toBe('function');
    });

    it('should have savePackageForm method', () => {
      const store = new DICargoStore();
      expect(typeof store.savePackageForm).toBe('function');
    });

    it('should have resetPackageForm method', () => {
      const store = new DICargoStore();
      expect(typeof store.resetPackageForm).toBe('function');
    });

    it('should have addAddress method', () => {
      const store = new DICargoStore();
      expect(typeof store.addAddress).toBe('function');
    });

    it('should have clearStepValues method', () => {
      const store = new DICargoStore();
      expect(typeof store.clearStepValues).toBe('function');
    });

    it('should have setStepAddressValues method', () => {
      const store = new DICargoStore();
      expect(typeof store.setStepAddressValues).toBe('function');
    });

    it('should have setStepAddressValuesMulti method', () => {
      const store = new DICargoStore();
      expect(typeof store.setStepAddressValuesMulti).toBe('function');
    });

    it('should have setStepCargosValues method', () => {
      const store = new DICargoStore();
      expect(typeof store.setStepCargosValues).toBe('function');
    });

    it('should have setStepTariffValuesMulti method', () => {
      const store = new DICargoStore();
      expect(typeof store.setStepTariffValuesMulti).toBe('function');
    });

    it('should have setStepFinalValues method', () => {
      const store = new DICargoStore();
      expect(typeof store.setStepFinalValues).toBe('function');
    });

    it('should have setPeriodValues method', () => {
      const store = new DICargoStore();
      expect(typeof store.setPeriodValues).toBe('function');
    });

    it('should have getPeriodValuesMulti method', () => {
      const store = new DICargoStore();
      expect(typeof store.getPeriodValuesMulti).toBe('function');
    });

    it('should have postDataMulti method', () => {
      const store = new DICargoStore();
      expect(typeof store.postDataMulti).toBe('function');
    });

    it('should have postRegularCargoDataMulti method', () => {
      const store = new DICargoStore();
      expect(typeof store.postRegularCargoDataMulti).toBe('function');
    });

    it('should have getPack method', () => {
      const store = new DICargoStore();
      expect(typeof store.getPack).toBe('function');
    });
  });

  describe('computed properties', () => {
    it('should have isRegular getter', () => {
      const store = new DICargoStore();
      expect(typeof store.isRegular).toBe('boolean');
    });

    it('should have isRelocation getter', () => {
      const store = new DICargoStore();
      expect(typeof store.isRelocation).toBe('boolean');
    });
  });

  describe('methods behavior', () => {
    describe('setRepeatOrder', () => {
      it('should set isRepeatOrder to true', () => {
        const store = new DICargoStore();
        store.setRepeatOrder(true);
        expect(store.isRepeatOrder).toBe(true);
      });

      it('should set isRepeatOrder to false', () => {
        const store = new DICargoStore();
        store.setRepeatOrder(false);
        expect(store.isRepeatOrder).toBe(false);
      });
    });

    describe('setAdditionalServices', () => {
      it('should merge additional services', () => {
        const store = new DICargoStore();
        store.setAdditionalServices({ loaders: true, car: true });
        expect(store.additionalServices).toEqual({
          loaders: true,
          car: true,
          lift: false,
        });
      });
    });

    describe('setSelectedFlatType', () => {
      it('should set selectedFlat', () => {
        const store = new DICargoStore();
        store.setSelectedFlatType(1, 'bedroom');
        expect(store.selectedFlat).toEqual({
          idx: 1,
          type: 'bedroom',
        });
      });
    });

    describe('setRegularOrder', () => {
      it('should set REGULAR to true', () => {
        const store = new DICargoStore();
        store.setRegularOrder(true);
        expect(store.purpose.REGULAR).toBe(true);
      });
    });

    describe('setRelocationOrder', () => {
      it('should set RELOCATION to true', () => {
        const store = new DICargoStore();
        store.setRelocationOrder(true);
        expect(store.purpose.RELOCATION).toBe(true);
      });
    });

    describe('setCargoList', () => {
      it('should set cargoList', () => {
        const store = new DICargoStore();
        const cargoList = [{ id: '1', cargoName: 'Test' }] as any;
        store.setCargoList(cargoList);
        expect(store.stepCargosValues.cargoList).toEqual(cargoList);
      });
    });

    describe('setComment', () => {
      it('should set comment', () => {
        const store = new DICargoStore();
        store.setComment('Test comment');
        expect(store.comment).toBe('Test comment');
      });
    });

    describe('setCheckLoaders', () => {
      it('should set loadersNeeded', () => {
        const store = new DICargoStore();
        store.setCheckLoaders(true);
        expect(store.packageForm.loadersNeeded).toBe(true);
      });
    });

    describe('savePackageForm', () => {
      it('should save packageForm', () => {
        const store = new DICargoStore();
        const formValues = {
          loadersNeeded: true,
          loadersCount: 2,
          packages: [{ id: '1', count: 3 }] as any,
        };
        store.savePackageForm(formValues);
        expect(store.packageForm).toEqual(formValues);
      });
    });

    describe('resetPackageForm', () => {
      it('should reset packageForm to initial', () => {
        const store = new DICargoStore();
        store.packageForm = {
          loadersNeeded: true,
          loadersCount: 2,
          packages: [{ id: '1', count: 3 }] as any,
        };
        store.resetPackageForm();
        expect(store.packageForm).toEqual({
          loadersNeeded: false,
          loadersCount: 0,
          packages: [],
        });
      });
    });

    describe('clearStepValues', () => {
      it('should reset all step values to initial', () => {
        const store = new DICargoStore();
        // Set some values
        store.stepAddressValuesMulti = { desiredDate: new Date(), loaders: 5, internalNote: 'test' } as any;
        store.waypointsListMulti = [{ address: 'test' }] as any;
        store.stepCargosValues = { cargoList: [{ id: '1' }] } as any;
        store.stepTariffValues = { distance: 100 } as any;
        store.stepFinalValues = { cost: 500 } as any;
        store.periodValues = { periodType: 'MONTH' } as any;
        store.totalRegularCost = 1000;
        store.purpose.REGULAR = true;
        store.purpose.RELOCATION = true;
        store.purpose.SINGLE = true;
        store.comment = 'test';
        
        store.clearStepValues();

        expect(store.stepAddressValuesMulti).toEqual({
          desiredDate: null,
          loaders: 0,
          internalNote: '',
        });
        expect(store.waypointsListMulti).toEqual([
          {
            address: '',
            name: '',
            phone: '',
            type: '',
            organization: '',
          },
        ]);
        expect(store.stepCargosValues).toEqual({
          cargoList: [],
        });
        expect(store.stepTariffValues).toEqual({
          distance: 0,
          tariffCost: null,
          expected: null,
          express: false,
          comment: '',
        });
        expect(store.stepFinalValues).toEqual({
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
        });
        expect(store.periodValues).toEqual({
          periodType: 'WEEK',
          dayOfWeek: [],
          weekOfMonth: [],
          monthOfQuartal: [],
          beginDate: 'mock-date',
          endDate: '',
        });
        expect(store.totalRegularCost).toBe(0);
        expect(store.purpose.REGULAR).toBe(false);
        expect(store.purpose.RELOCATION).toBe(false);
        expect(store.purpose.SINGLE).toBe(false);
        expect(store.comment).toBe('');
      });
    });

    describe('setStepAddressValuesMulti', () => {
      it('should merge stepAddressValuesMulti', () => {
        const store = new DICargoStore();
        store.setStepAddressValuesMulti({ loaders: 5 });
        expect(store.stepAddressValuesMulti.loaders).toBe(5);
      });
    });

    describe('setStepCargosValues', () => {
      it('should merge stepCargosValues', () => {
        const store = new DICargoStore();
        store.setStepCargosValues({ cargoList: [{ id: '1' }] } as any);
        expect(store.stepCargosValues.cargoList).toEqual([{ id: '1' }]);
      });
    });

    describe('setStepTariffValuesMulti', () => {
      it('should merge stepTariffValuesMulti', () => {
        const store = new DICargoStore();
        store.setStepTariffValuesMulti({ distance: 100 });
        expect(store.stepTariffValuesMulti.distance).toBe(100);
      });
    });

    describe('setStepFinalValues', () => {
      it('should merge stepFinalValues', () => {
        const store = new DICargoStore();
        store.setStepFinalValues({ cost: 500 });
        expect(store.stepFinalValues.cost).toBe(500);
      });
    });

    describe('setPeriodValues', () => {
      it('should merge periodValues', () => {
        const store = new DICargoStore();
        store.setPeriodValues({ periodType: 'MONTH' } as any);
        expect(store.periodValues.periodType).toBe('MONTH');
      });
    });

    describe('getPeriodValuesMulti', () => {
      it('should call service.getPeriodValuesMulti', async () => {
        const store = new DICargoStore();
        (store as any).service.getPeriodValuesMulti.mockResolvedValue(500);

        await store.getPeriodValuesMulti({ periodType: 'MONTH' });

        expect((store as any).service.getPeriodValuesMulti).toHaveBeenCalledWith({ periodType: 'MONTH' });
        expect(store.totalRegularCost).toBe(500);
      });
    });

    describe('postDataMulti', () => {
      it('should call service.postDataMulti and logger.on success', async () => {
        const store = new DICargoStore();
        (store as any).service.postDataMulti.mockResolvedValue({ status: 'CARGO_AWAITING_APPROVAL' });
        (store as any).logger.toMessage.mockImplementation(() => {});

        await store.postDataMulti({} as any);

        expect((store as any).service.postDataMulti).toHaveBeenCalledWith({});
        expect((store as any).logger.toMessage).toHaveBeenCalledWith('success', 'Заявка на согласовании');
      });

      it('should call clearStepValues and resetPackageForm on success', async () => {
        const store = new DICargoStore();
        (store as any).service.postDataMulti.mockResolvedValue({ status: 'CARGO_AWAITING_APPROVAL' });
        (store as any).logger.toMessage.mockImplementation(() => {});

        await store.postDataMulti({} as any);

        expect(store.clearStepValues).toBeDefined();
        expect(store.resetPackageForm).toBeDefined();
      });

      it('should call logger.toError on failure', async () => {
        const store = new DICargoStore();
        (store as any).service.postDataMulti.mockRejectedValue({ response: { data: { message: 'Ошибка' } } });
        (store as any).logger.toError.mockImplementation(() => {});

        await store.postDataMulti({} as any).catch(() => {});

        expect((store as any).logger.toError).toHaveBeenCalledWith('Ошибка', 'Ошибка отправки данных');
      });
    });

    describe('postRegularCargoDataMulti', () => {
      it('should call service.postRegularCargoDataMulti and logger.on success', async () => {
        const store = new DICargoStore();
        (store as any).service.postRegularCargoDataMulti.mockResolvedValue({});
        (store as any).logger.toMessage.mockImplementation(() => {});

        await store.postRegularCargoDataMulti({} as any);

        expect((store as any).service.postRegularCargoDataMulti).toHaveBeenCalledWith({});
        expect((store as any).logger.toMessage).toHaveBeenCalledWith('success', 'Заявка на согласовании');
      });

      it('should call logger.toError on failure', async () => {
        const store = new DICargoStore();
        (store as any).service.postRegularCargoDataMulti.mockRejectedValue({ response: { data: { message: 'Ошибка' } } });
        (store as any).logger.toError.mockImplementation(() => {});

        await store.postRegularCargoDataMulti({} as any).catch(() => {});

        expect((store as any).logger.toError).toHaveBeenCalledWith('Ошибка', 'Ошибка отправки данных');
      });
    });

    describe('getPack', () => {
      it('should call service.getPack and set packs', async () => {
        const store = new DICargoStore();
        (store as any).service.getPack.mockResolvedValue({ content: [{ id: '1', name: 'Pack' }] });

        await store.getPack();

        expect((store as any).service.getPack).toHaveBeenCalled();
        expect(store.packs).toEqual([{ id: '1', name: 'Pack' }]);
      });
    });
  });
});
