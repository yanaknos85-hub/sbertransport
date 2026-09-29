import { IS_DEV } from 'constants/constants.env';
import { LabeledValue } from 'utils';

export enum EmployeeStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export enum EmployeeStatusTitle {
  NOT_SELECTED = 'Не выбрано',
  ACTIVE = 'Активный',
  INACTIVE = 'Неактивный',
}

export enum OrgStructureType {
  EXTERNAL = 'EXTERNAL',
  INTERNAL = 'INTERNAL',
}

export enum DepartmentStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export enum EmployeeGender {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
}

export enum EmployeeGenderTitle {
  MALE = 'Мужской',
  FEMALE = 'Женский',
}

export type EmployeeStatusType = keyof typeof EmployeeStatus;

export enum TripRequestStatuses {
  DRAFT = 'DRAFT',
  REGISTERED = 'REGISTERED',
  AWAITING_APPROVAL = 'AWAITING_APPROVAL',
  APPROVED = 'APPROVED',
  DECLINED = 'DECLINED',
  AWAITING_SEARCH = 'AWAITING_SEARCH',
  DRIVER_SEARCH = 'DRIVER_SEARCH',
  DRIVER_FOUND = 'DRIVER_FOUND',
  DRIVER_ARRIVED = 'DRIVER_ARRIVED',
  TRIP_IN_PROGRESS = 'TRIP_IN_PROGRESS',
  TRIP_FINISHED = 'TRIP_FINISHED',
  CANCELLED = 'CANCELLED',
  PAYMENT_AWAITING = 'PAYMENT_AWAITING',
}

export enum PublicTransportRequestStatuses {
  PUBLIC_PAYMENT_AWAITING = 'PUBLIC_PAYMENT_AWAITING',
  PUBLIC_PAYMENT_DONE = 'PUBLIC_PAYMENT_DONE',
  PUBLIC_PAYMENT_NOT_DONE = 'PUBLIC_PAYMENT_NOT_DONE',
  PUBLIC_AWAITING_APPROVAL = 'PUBLIC_AWAITING_APPROVAL',
  PUBLIC_APPROVED = 'PUBLIC_APPROVED',
  PUBLIC_TRIP_CONFIRMATION = 'PUBLIC_TRIP_CONFIRMATION',
  PUBLIC_TRIP_CONFIRMED = 'PUBLIC_TRIP_CONFIRMED',
  PUBLIC_AWAITING_AFFIRMATIVE = 'PUBLIC_AWAITING_AFFIRMATIVE',
  PUBLIC_AFFIRMED = 'PUBLIC_AFFIRMED',
  PUBLIC_ORDER_PAYMENT_FORMATION = 'PUBLIC_ORDER_PAYMENT_FORMATION',
  PUBLIC_CANCELLED = 'PUBLIC_CANCELLED',
}

export enum TaxiTransportRequestStatuses {
  TAXI_AWAITING_APPROVAL = 'TAXI_AWAITING_APPROVAL',
  TAXI_CANCELLED = 'TAXI_CANCELLED',
  TAXI_APPROVED = 'TAXI_APPROVED',
  TAXI_AWAITING_SEARCH = 'TAXI_AWAITING_SEARCH',
  TAXI_DRIVER_SEARCH = 'TAXI_DRIVER_SEARCH',
  TAXI_DRIVER_FOUND = 'TAXI_DRIVER_FOUND',
  TAXI_DRIVER_ON_THE_WAY = 'TAXI_DRIVER_ON_THE_WAY',
  TAXI_DRIVER_ARRIVED = 'TAXI_DRIVER_ARRIVED',
  TAXI_FREE_TIME_EXPIRED = 'TAXI_FREE_TIME_EXPIRED',
  TAXI_WAYPOINT_ARRIVED = 'TAXI_WAYPOINT_ARRIVED',
  TAXI_TRIP_IN_PROGRESS = 'TAXI_TRIP_IN_PROGRESS',
  TAXI_TRIP_FINISHED = 'TAXI_TRIP_FINISHED',
}

export enum PersonalTransportRequestStatuses {
  PERSONAL_AWAITING_APPROVAL = 'PERSONAL_AWAITING_APPROVAL',
  PERSONAL_APPROVED = 'PERSONAL_APPROVED',
  PERSONAL_AWAITING_SHARED_RIDE_APPROVAL = 'PERSONAL_AWAITING_SHARED_RIDE_APPROVAL',
  PERSONAL_SHARED_RIDE_DECLINED = 'PERSONAL_SHARED_RIDE_DECLINED',
  PERSONAL_SHARED_RIDE_APPROVED = 'PERSONAL_SHARED_RIDE_APPROVED',
  PERSONAL_ADDITIONAL_WAYPOINTS_APPROVAL = 'PERSONAL_ADDITIONAL_WAYPOINTS_APPROVAL',
  PERSONAL_ADDITIONAL_WAYPOINTS_APPROVED = 'PERSONAL_ADDITIONAL_WAYPOINTS_APPROVED',
  PERSONAL_ADDITIONAL_WAYPOINTS_DECLINED = 'PERSONAL_ADDITIONAL_WAYPOINTS_DECLINED',
  PERSONAL_TRIP_START_REMIND = 'PERSONAL_TRIP_START_REMIND',
  PERSONAL_TRIP_IN_PROGRESS = 'PERSONAL_TRIP_IN_PROGRESS',
  PERSONAL_WAYPOINT_ARRIVED = 'PERSONAL_WAYPOINT_ARRIVED',
  PERSONAL_AWAITING_TRIP_APPROVAL = 'PERSONAL_AWAITING_TRIP_APPROVAL',
  PERSONAL_AWAITING_TRIP_APPROVED = 'PERSONAL_AWAITING_TRIP_APPROVED',
  PERSONAL_AWAITING_TRIP_DECLINED = 'PERSONAL_AWAITING_TRIP_DECLINED',
  PERSONAL_ORDER_PAYMENT_FORMATION = 'PERSONAL_ORDER_PAYMENT_FORMATION',
  PERSONAL_PAYMENT_AWAITING = 'PERSONAL_PAYMENT_AWAITING',
  PERSONAL_PAYMENT_DONE = 'PERSONAL_PAYMENT_DONE',
  PERSONAL_PAYMENT_DECLINED = 'PERSONAL_PAYMENT_DECLINED',
  PERSONAL_CANCELLED = 'PERSONAL_CANCELLED',
  PERSONAL_TRIP_FINISHED = 'PERSONAL_TRIP_FINISHED',
}

export enum CarsharingTransportRequestStatuses {
  CARSHARING_AWAITING_APPROVAL = 'CARSHARING_AWAITING_APPROVAL',
  CARSHARING_APPROVED = 'CARSHARING_APPROVED',
  CARSHARING_DECLINED = 'CARSHARING_DECLINED',
  CARSHARING_AWAITING_SEARCH = 'CARSHARING_AWAITING_SEARCH',
  CARSHARING_TRIP_IN_PROGRESS = 'CARSHARING_TRIP_IN_PROGRESS',
  CARSHARING_AWAITING_TRIP_APPROVAL = 'CARSHARING_AWAITING_TRIP_APPROVAL',
  CARSHARING_TRIP_FINISHED = 'CARSHARING_TRIP_FINISHED',
  CARSHARING_CANCELLED = 'CARSHARING_CANCELLED',
}

export const PublicTransportRequestStatusesTitles: Record<PublicTransportRequestStatuses, string> = {
  [PublicTransportRequestStatuses.PUBLIC_PAYMENT_AWAITING]: 'Ожидание выплаты',
  [PublicTransportRequestStatuses.PUBLIC_PAYMENT_DONE]: 'Выплата произведена',
  [PublicTransportRequestStatuses.PUBLIC_PAYMENT_NOT_DONE]: 'Выплата не произведена',
  [PublicTransportRequestStatuses.PUBLIC_AWAITING_APPROVAL]: 'На согласовании',
  [PublicTransportRequestStatuses.PUBLIC_APPROVED]: 'Согласована',
  [PublicTransportRequestStatuses.PUBLIC_TRIP_CONFIRMATION]: 'Подтверждение поездки',
  [PublicTransportRequestStatuses.PUBLIC_TRIP_CONFIRMED]: 'Поездка подтверждена',
  [PublicTransportRequestStatuses.PUBLIC_AWAITING_AFFIRMATIVE]: 'Утверждение',
  [PublicTransportRequestStatuses.PUBLIC_AFFIRMED]: 'Утверждена',
  [PublicTransportRequestStatuses.PUBLIC_ORDER_PAYMENT_FORMATION]: 'Формирование приказа на выплату',
  [PublicTransportRequestStatuses.PUBLIC_CANCELLED]: 'Отменено',
};

export const TripRequestStatusesTitles: Record<TripRequestStatuses, string> = {
  [TripRequestStatuses.DRAFT]: 'Черновик',
  [TripRequestStatuses.REGISTERED]: 'Заявка зарегистрирована',
  [TripRequestStatuses.AWAITING_APPROVAL]: 'На согласовании',
  [TripRequestStatuses.APPROVED]: 'Согласована',
  [TripRequestStatuses.DECLINED]: 'Не согласована ',
  [TripRequestStatuses.AWAITING_SEARCH]: 'Ожидайте назначения водителя',
  [TripRequestStatuses.DRIVER_SEARCH]: 'Поиск водителя',
  [TripRequestStatuses.DRIVER_FOUND]: 'Водитель назначен',
  [TripRequestStatuses.DRIVER_ARRIVED]: 'Водитель ожидает в точке отправления',
  [TripRequestStatuses.TRIP_IN_PROGRESS]: 'Поездка началась',
  [TripRequestStatuses.TRIP_FINISHED]: 'Поездка завершена',
  [TripRequestStatuses.CANCELLED]: 'Поездка отменена',
  [TripRequestStatuses.PAYMENT_AWAITING]: 'Ожидание выплаты',
};

export const TaxiTransportRequestStatusesTitles: Record<TaxiTransportRequestStatuses, string> = {
  [TaxiTransportRequestStatuses.TAXI_AWAITING_APPROVAL]: 'На согласовании',
  [TaxiTransportRequestStatuses.TAXI_CANCELLED]: 'Отменено',
  [TaxiTransportRequestStatuses.TAXI_APPROVED]: 'Согласована',
  [TaxiTransportRequestStatuses.TAXI_AWAITING_SEARCH]: 'Ожидайте назначения водителя',
  [TaxiTransportRequestStatuses.TAXI_DRIVER_SEARCH]: 'Поиск водителя',
  [TaxiTransportRequestStatuses.TAXI_DRIVER_FOUND]: 'Водитель назначен',
  [TaxiTransportRequestStatuses.TAXI_DRIVER_ON_THE_WAY]: 'Водитель в пути',
  [TaxiTransportRequestStatuses.TAXI_DRIVER_ARRIVED]: 'Водитель ожидает в точке отправления',
  [TaxiTransportRequestStatuses.TAXI_FREE_TIME_EXPIRED]: 'Время бесплатного ожидания истекло',
  [TaxiTransportRequestStatuses.TAXI_WAYPOINT_ARRIVED]: 'Прибытие в промежуточный пункт на такси',
  [TaxiTransportRequestStatuses.TAXI_TRIP_IN_PROGRESS]: 'Поездка началась',
  [TaxiTransportRequestStatuses.TAXI_TRIP_FINISHED]: 'Поездка завершена',
};

export const PersonalTransportRequestStatusesTitles: Record<PersonalTransportRequestStatuses, string> = {
  [PersonalTransportRequestStatuses.PERSONAL_AWAITING_APPROVAL]: 'На согласовании',
  [PersonalTransportRequestStatuses.PERSONAL_APPROVED]: 'Согласована',
  [PersonalTransportRequestStatuses.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL]: 'Согласование присоединения к СП',
  [PersonalTransportRequestStatuses.PERSONAL_SHARED_RIDE_DECLINED]: 'Присоединение не согласовано',
  [PersonalTransportRequestStatuses.PERSONAL_SHARED_RIDE_APPROVED]: 'Присоединение согласовано',
  [PersonalTransportRequestStatuses.PERSONAL_ADDITIONAL_WAYPOINTS_APPROVAL]:
    'Согласование доп. точек в маршруте на личном транспорте',
  [PersonalTransportRequestStatuses.PERSONAL_ADDITIONAL_WAYPOINTS_APPROVED]:
    'Добавление дополнительных точек в маршрут было согласовано',
  [PersonalTransportRequestStatuses.PERSONAL_ADDITIONAL_WAYPOINTS_DECLINED]:
    'Добавление дополнительных точек в маршрут было отклонено',
  [PersonalTransportRequestStatuses.PERSONAL_TRIP_START_REMIND]: 'Напоминание о начале поездки',
  [PersonalTransportRequestStatuses.PERSONAL_TRIP_IN_PROGRESS]: 'Поездка',
  [PersonalTransportRequestStatuses.PERSONAL_WAYPOINT_ARRIVED]: 'Прибытие в промежуточный пункт на личном транспорте',
  [PersonalTransportRequestStatuses.PERSONAL_AWAITING_TRIP_APPROVAL]: 'Утверждение маршрута',
  [PersonalTransportRequestStatuses.PERSONAL_AWAITING_TRIP_APPROVED]: 'Утверждено',
  [PersonalTransportRequestStatuses.PERSONAL_AWAITING_TRIP_DECLINED]: 'Не утверждено',
  [PersonalTransportRequestStatuses.PERSONAL_ORDER_PAYMENT_FORMATION]: 'Формирование приказа на выплату',
  [PersonalTransportRequestStatuses.PERSONAL_PAYMENT_AWAITING]: 'Ожидание выплаты',
  [PersonalTransportRequestStatuses.PERSONAL_PAYMENT_DONE]: 'Выплата произведена',
  [PersonalTransportRequestStatuses.PERSONAL_PAYMENT_DECLINED]: 'Выплата не произведена',
  [PersonalTransportRequestStatuses.PERSONAL_CANCELLED]: 'Отменено',
  [PersonalTransportRequestStatuses.PERSONAL_TRIP_FINISHED]: 'Поездка завершена',
};

export const CarsharingTransportRequestStatusesTitle: Record<CarsharingTransportRequestStatuses, string> = {
  [CarsharingTransportRequestStatuses.CARSHARING_AWAITING_APPROVAL]: 'На согласовании',
  [CarsharingTransportRequestStatuses.CARSHARING_APPROVED]: 'Согласована',
  [CarsharingTransportRequestStatuses.CARSHARING_DECLINED]: 'Не согласована',
  [CarsharingTransportRequestStatuses.CARSHARING_AWAITING_SEARCH]: 'Поездка запланирована',
  [CarsharingTransportRequestStatuses.CARSHARING_TRIP_IN_PROGRESS]: 'Поездка началась',
  [CarsharingTransportRequestStatuses.CARSHARING_AWAITING_TRIP_APPROVAL]: 'Утверждение маршрута',
  [CarsharingTransportRequestStatuses.CARSHARING_TRIP_FINISHED]: 'Поездка завершена',
  [CarsharingTransportRequestStatuses.CARSHARING_CANCELLED]: 'Отменено',
};

export const TripRequestTitles: Record<
  keyof typeof PublicTransportRequestStatuses | keyof typeof TripRequestStatuses,
  string
> = {
  ...PublicTransportRequestStatusesTitles,
  ...TripRequestStatusesTitles,
  ...PersonalTransportRequestStatusesTitles,
  ...CarsharingTransportRequestStatusesTitle,
  ...TaxiTransportRequestStatusesTitles,
};

export type TTripRequestStatuses = keyof typeof TripRequestStatuses;

export const TripStatusesChangeable: TTripRequestStatuses[] = [
  TripRequestStatuses.REGISTERED,
  TripRequestStatuses.AWAITING_APPROVAL,
  TripRequestStatuses.APPROVED,
  TripRequestStatuses.AWAITING_SEARCH,
  TripRequestStatuses.DRIVER_SEARCH,
];

export const TripStatusesCancellable: TTripRequestStatuses[] = [
  ...TripStatusesChangeable,
  TripRequestStatuses.DRIVER_FOUND,
  TripRequestStatuses.DRIVER_ARRIVED,
];

export const TripStatusesFinal: TTripRequestStatuses[] = [
  TripRequestStatuses.CANCELLED,
  TripRequestStatuses.DECLINED,
  TripRequestStatuses.TRIP_FINISHED,
];

export const RUBLE_SIGN = '₽';

export const YEAR = 'year';
export const MONTH = 'month';

export const DATE_FORMAT = {
  BASE: 'YYYY-MM-DD',
  BASE_REVERTED: 'DD-MM-YYYY',
  BASE_REVERTED_DOTS: 'DD.MM.YYYY',
  MONTH_NAME: 'D MMM YYYY',
  TIME_BASE: 'HH:mm:ss',
  TIME_SHORT: 'H:mm',
  TIME_SHORT_MINUTE: 'mm',
  DATE_WITH_TIME: 'DD-MM-YYYY H:mm',
  DATE_WITH_TIME_LEADING_ZEROS: 'DD-MM-YYYY HH:mm',
  DATE_WITH_TIME_DOTS: 'DD.MM.YYYY HH:mm',
  DATE_WITH_TIME_DOTS_ZONE: 'DD.MM.YYYY HH:mm ([GMT]Z)',
  DATE_WITH_TIME_SECONDS: 'DD-MM-YYYY H:mm:ss',
  DATE_WITH_TIME_SECONDS_DOTS: 'DD.MM.YYYY. HH:mm:ss',
  DATE_WITH_UTC_TIME: 'YYYY-MM-DDTHH:mm',
  BEFORE_THE_SPECIFIED_DATE: 'до указанной даты',
  TO_THE_CURRENT_DATE: 'по текущую дату',
  GSM_DATE: 'DD.MM.YYYY_HH.mm.ss',
  YEAR: 'YYYY',
  DATE_WITH_TIME_SEPARATED_BY_COMMAS: 'DD.MM.YYYY, HH:mm',
};

export const FILE_FORMAT = {
  XLS: '.xls',
  XLSX: '.xlsx',
};

export const TYPE_RESPONSE = {
  excel: 'application/vnd.ms-excel',
  sheet: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  csv: 'text/csv',
  json: 'application/json',
};

export enum Roles {
  ADMIN_DATA_MASTER = 'ROLE_ADMIN_DATA_MASTER',
  ADMIN_CORP_CLIENT = 'ROLE_ADMIN_CORP_CLIENT',
  ENGINEER_OTO = 'ROLE_ENGINEER_OTO',
  ENGINEER_CORP_CLIENT = 'ROLE_ENGINEER_CORP_CLIENT',
  DISPATCHER_SUPPORT_SERVICE = 'ROLE_DISPATCHER_SUPPORT_SERVICE',
  OWNER_LIMIT_DEPARTMENT = 'ROLE_OWNER_LIMIT_DEPARTMENT',
  MAINTENANCE_ENGINEER = 'ROLE_MAINTENANCE_ENGINEER',
  TELEMECHANIC = 'ROLE_TELEMECHANIC',
}

export const rolesCanSwitchOrg = [Roles.ADMIN_DATA_MASTER, Roles.DISPATCHER_SUPPORT_SERVICE];

export enum DefaultValues {
  emptyValueInTable = '-',
}

export type TransportTypes = 'CARGO' | 'PASSENGER' | 'REPAIR';

export enum IntegrationTypes {
  DISPATCHER = 'DISPATCHER',
  EMAIL_XML_API = 'EMAIL_XML_API',
  // CHERNOV_JSON_API = "CHERNOV_JSON_API",
  EMAIL_XML_WOUR_API = 'EMAIL_XML_WOUR_API',
  JSON_API_1_0 = 'JSON_API_1_0',
  NONE = 'NONE',
  // GETT_API = "GETT_API",
  // CITY_API = "CITY_API",
  // YANDEX_API = "YANDEX_API",
}

export type IntegrationTypesDropdown = Exclude<IntegrationTypes, IntegrationTypes.DISPATCHER>;

export type IntegrationTypeTitles = {
  [key in IntegrationTypesDropdown]: string;
};

export const integrationTypeTitles: IntegrationTypeTitles = {
  [IntegrationTypes.EMAIL_XML_API]: 'XML',
  [IntegrationTypes.EMAIL_XML_WOUR_API]: 'XML без запросов обновления',
  [IntegrationTypes.JSON_API_1_0]: 'API',
  [IntegrationTypes.NONE]: 'Без интеграции',
};

export type FullIntegrationTypeTitles = {
  [key in IntegrationTypes]: string;
};

export const fullIntegrationTypeTitles: FullIntegrationTypeTitles = {
  ...integrationTypeTitles,
  [IntegrationTypes.DISPATCHER]: 'Диспетчерская',
};

export const PASSWORD_PROTECTED = '[protected]';

export enum DispatcherProjections {
  FULL = 'FULL',
  SELECT = 'SELECT',
}

export enum IntegrationExceptionTypes {
  NotBlank = 'NotBlank',
}

export type IntegrationExcetionTitles = {
  [key in IntegrationExceptionTypes]: string;
};

export const integrationExceptionTitles: IntegrationExcetionTitles = {
  [IntegrationExceptionTypes.NotBlank]: 'Поле не должно быть пустым',
};

export enum Contacts {
  outTel = '8(800) 707-48-82',
  linkOutTel = 'tel:88007074882',
  insideTel = '8(559) 90-047',
  mailSbertranposrt = 'catransport@sberbank.ru',
  linkInsideTel = 'tel:855990047',
  chatSbertransport = 'sberchat.sberbank.ru/@SBERTRANSPORT',
  linkChatSbertransport = 'https://sberchat.sberbank.ru/@SBERTRANSPORT',
  linkMailSbertranposrt = 'mailto:catransport@sberbank.ru',
}

export const CUBE_MM_TO_METERS = 1_000_000;

export const YEARS_OPTIONS_LENGTH = 5;

export const ORGANIZATION_ID = 'organizationId';
export const EXECUTOR_GROUP_ID = 'executorGroupId';
export const IS_ORGANIZATION_FLAG = 'isOrganizationFlag';

export const emptySign = '-';

export enum ClientTypes {
  ClientMobile = 'CLIENT_MOBILE',
  Dispatcher = 'DISPATCHER',
  Driver = 'DRIVER',
  WebCorp = 'WEB_CORP',
  Parking = 'PARKING',
  Client = 'CLIENT',
  SMD = 'SMD',
}

export const X_CLIENT_TYPE = ClientTypes.WebCorp;

export const CLIENT_TYPE_TITLES: Record<ClientTypes, string> = {
  CLIENT_MOBILE: 'МП Клиент Всего',
  DISPATCHER: 'Диспетчерская',
  DRIVER: 'МП Водитель Всего',
  WEB_CORP: 'Веб корп',
  PARKING: 'Парковки',
  CLIENT: 'Клиент',
  SMD: 'СМД',
};

export enum OperatingSystems {
  Android = 'Android',
  iOS = 'iOS',
}

export enum Devices {
  PC = 'Personal computer',
  Smartphone = 'Smartphone',
}

export const DEVICE_TITLES: Record<Devices, string> = {
  [Devices.PC]: 'Персональный компьютер',
  [Devices.Smartphone]: 'Смартфон',
};

export const TransportByServiceType: Record<TransportTypes, string[]> = {
  CARGO: ['DEDICATED', 'COURIER', 'DOMESTIC_COURIER', 'INTERREGIONAL', 'INDIVIDUAL'],
  PASSENGER: ['TAXI', 'PERSONAL', 'PUBLIC', 'CARSHARING', 'BICYCLE', 'WALK', 'SCOOTER'],
  REPAIR: ['OFFICIAL', 'PRIVATE', 'SPECIAL'],
};

export enum CustomErrorCode {
  MF_LOAD = 666,
  UNKNOWN = 1000,
  TYPES = 1001,
  TIMEOUT = 1002,
  CORS = 1003,
  UNDEFINED_FIELD = 1004,
}

const defaultSubTitle = 'Пожалуйста, обратитесь в поддержку';

export const errorText: Record<number, { title: string; subtitle: string }> = {
  400: {
    title: 'Ошибка заполнения данных',
    subtitle: defaultSubTitle,
  },
  401: {
    title: 'Пожалуйста, авторизуйтесь',
    subtitle: '',
  },
  403: {
    title: 'У Вас недостаточно прав',
    subtitle: defaultSubTitle,
  },
  408: {
    title: 'Слишком большой объем данных',
    subtitle: defaultSubTitle,
  },
  409: {
    title: 'Конфликт данных',
    subtitle: defaultSubTitle,
  },
  417: {
    title: 'Ошибка данных',
    subtitle: defaultSubTitle,
  },

  500: {
    title: 'Ошибка сервера',
    subtitle: defaultSubTitle,
  },
  503: {
    title: 'Проводятся работы на сервере',
    subtitle: defaultSubTitle,
  },

  // Все ошибки, для которых нет текста, выводятся как CustomErrorCode.UNKNOWN
  [CustomErrorCode.MF_LOAD]: {
    title: `Ошибка загрузки ${IS_DEV && 'микрофронта'}`,
    subtitle: IS_DEV ? '' : defaultSubTitle,
  },
  [CustomErrorCode.UNKNOWN]: {
    title: 'Ошибка системы',
    subtitle: defaultSubTitle,
  },
  [CustomErrorCode.TYPES]: {
    title: 'Ошибка отображения данных',
    subtitle: defaultSubTitle,
  },
  [CustomErrorCode.TIMEOUT]: {
    title: 'Слишком большой объем данных',
    subtitle: defaultSubTitle,
  },
};

export const SUPPORT_PHONE = {
  code: '88007074882',
  title: '8-800-707-48-82',
};

export const SDO_SUPPORT_PHONE = {
  code: '88001000302',
  title: '8(800) 100-03-02',
};

export const TEST_MODE = 'TEST_MODE';

export enum AllTransportTypes {
  TAXI = 'TAXI',
  PERSONAL = 'PERSONAL',
  PUBLIC = 'PUBLIC',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',
  SCOOTER = 'SCOOTER',
  COURIER = 'COURIER',
  DEDICATED = 'DEDICATED',
  INDIVIDUAL = 'INDIVIDUAL',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
  OFFICIAL = 'OFFICIAL',
  SPECIAL = 'SPECIAL',
  PRIVATE = 'PRIVATE',
  GROUP_TRANSFER = 'GROUP_TRANSFER',
  YANDEX = 'YANDEX',
}

export const deadlineStateOptions: LabeledValue[] = [
  { value: 'true', label: 'Нарушен' },
  { value: 'false', label: 'Не нарушен' },
];

export const paymentPeriodOptions: LabeledValue[] = [
  { value: 1, label: 'Первый период (1–7 число)' },
  { value: 2, label: 'Второй период (8–15 число)' },
  { value: 3, label: 'Третий период (16–23 число)' },
  { value: 4, label: 'Четвертый период (24-конец месяца)' },
];

export const savingsOptions: LabeledValue[] = [
  { value: 'true', label: 'Да' },
  { value: 'false', label: 'Нет' },
];
export interface DepartmentLevels {
  department1: string[];
  department2: string[];
  department3: string[];
  department4: string[];
  department5: string[];
  department6: string[];
  departmentLevel: number;
}

export const ratingOptions: LabeledValue[] = [
  { label: 1, value: 1 },
  { label: 2, value: 2 },
  { label: 3, value: 3 },
  { label: 4, value: 4 },
  { label: 5, value: 5 },
  { label: 'Без оценки', value: 0 },
];

export const publicCompensationDocumentExistOptions: LabeledValue[] = [
  { value: 'true', label: 'Да' },
  { value: 'false', label: 'Нет' },
];

export const initialDepartments: DepartmentLevels = {
  department1: [],
  department2: [],
  department3: [],
  department4: [],
  department5: [],
  department6: [],
  departmentLevel: 0,
};

export enum ContractTypes {
  INCOME = 'INCOME',
  OUTCOME = 'OUTCOME',
  TRANSIT = 'TRANSIT',
}

export enum ContractRestrictionTypes {
  NONE = 'NONE',
  BLACK_LIST = 'BLACK_LIST',
}

export enum TariffTypes {
  INCOME = 'INCOME',
  OUTCOME = 'OUTCOME',
  TRANSIT = 'TRANSIT',
}

export enum TariffRestrictionTypes {
  NONE = 'NONE',
  BLACK_LIST = 'BLACK_LIST',
}

export const sTransport = 'ООО Транспортные решения';

export enum VatValue {
  NULL = 'NULL',
  ZERO = 0,
  REDUCED_FIVE = 5,
  REDUCED_SEVEN = 7,
  BENEFITED = 10,
  BASE_22 = 22,
}

export const vatValueTitles: Record<VatValue, string> = {
  [VatValue.NULL]: 'Без НДС',
  [VatValue.ZERO]: '0%',
  [VatValue.REDUCED_FIVE]: '5%',
  [VatValue.REDUCED_SEVEN]: '7%',
  [VatValue.BENEFITED]: '10%',
  [VatValue.BASE_22]: '22%',
};

export enum SortDirection {
  ASC = 'ASC',
  DESC = 'DESC',
}

export const VALUE_NOT_FOUND = '-';
