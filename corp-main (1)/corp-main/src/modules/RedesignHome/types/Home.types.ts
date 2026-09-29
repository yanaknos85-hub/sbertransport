export enum Buttons {
  MANAGE_CONTRACT_BASE = 'MANAGE_CONTRACT_BASE',
  BUDGET = 'BUDGET',
  TARIFFS_SETTINGS = 'TARIFFS_SETTINGS',
  SERVICE_PARAMS = 'SERVICE_PARAMS',
  INDICATORS = 'INDICATORS',
  MANAGE_EMPLOYEES = 'MANAGE EMPLOYEES',
  ANALYTICAL_REPORTING = 'ANALYTICAL_REPORTING',
  BUSINESS_REPORTS = 'BUSINESS_REPORTS',
  CORPORATE_ADDRESSES = 'CORPORATE_ADDRESSES',
}

export enum Widgets {
  BUDGET = 'BUDGET',
}

export interface QueryType {
  organizationId: string;
  year: number;
  transportTypes: string[];
}

export enum ServicesTypes {
  ALL = 'ALL',
  PASSENGERS = 'PASSENGERS',
  CARGO = 'CARGO',
  REPAIR = 'REPAIR',
}

export enum TransportTypes {
  DEDICATED = 'DEDICATED',
  COURIER = 'COURIER',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
  OFFICIAL = 'OFFICIAL',
  SPECIAL = 'SPECIAL',
  PRIVATE = 'PRIVATE',
  CARSHARING = 'CARSHARING',
  PUBLIC = 'PUBLIC',
  TAXI = 'TAXI',
  WALK = 'WALK',
  PERSONAL = 'PERSONAL',
  BICYCLE = 'BICYCLE',
}

export enum servicesTypes {
  passengers = 'Пассажирские перевозки',
  cargo = 'Грузовые перевозки',
  carService = 'Содержание транспорта',
  parking = 'Парковки',
}

export enum descriptionServicesTypes {
  passengers = 'Цифровой сервис, позволяющий быстро и удобно организовать деловые поездки всеми доступными транспортными средствами за минимальные средства с автоматизированным процессом управления бюджетом',
  cargo = 'Заказывайте доставку документов, техники. Создавайте сложные заявки через Excel*',
  carService = 'Комплексное решение по обслуживанию автопарка для обеспечения эффективности работы транспорта',
  parking = 'Выбор парковочных мест в корпоративных и городских локациях в производственных и в личных целя',
}

export interface ITypesService {
  name: string;
  title: string;
  checked: boolean;
  description: string;
}

export interface IHotButtons {
  id?: string;
  titleKey: string;
  checked?: boolean;
  icon?: string;
  link?: string;
}

export interface IData {
  type: string;
  value: number;
}

export interface ISla {
  dataSla: IData[];
}

export interface ICsi {
  dataCsi: IData[];
}

export interface ITotalCounts {
  dataTotalCount: IData[];
}

export interface IDataPayment {
  dataPayment: IData[];
}

export interface IMetricsMain {
  budget?: null;
  sla?: null | ISla;
  csi?: null | ICsi;
  totalCounts?: null | ITotalCounts;
  payments?: null | IDataPayment;
}

export interface IMetricsMainData {
  startDate?: moment.Moment | string;
  finishDate?: moment.Moment | string;
  serviceTypes?: string[];
  organizationIds?: string[];
  executorGroupIds?: string[];
  department1?: string[];
  department2?: string[];
}

export enum popularServicesTitle {
  manageContractBase = 'Управлять договорной базой',
  budget = 'Перераспределить бюджет',
  tariffsSettings = 'Настроить тарифы',
  serviceParamsSettings = 'Настроить парметры сервиса',
  manageEmployeesSettings = 'Управлять сотрудниками',
  analyticalReporting = 'Аналитическая отчетность',
  businessReports = 'Бизнес отчеты',
  corporateAddresses = 'Корпоративные адреса',
}

export interface ISections {
  percentage: number;
  color: string;
}
