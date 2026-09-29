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
  PUBLIC_PAYMENT_AWAITING = 'PUBLIC_PAYMENT_AWAITING',
  PUBLIC_PAYMENT_DONE = 'PUBLIC_PAYMENT_DONE',
  PUBLIC_PAYMENT_NOT_DONE = 'PUBLIC_PAYMENT_NOT_DONE',
}

export enum TripRequestStatusesTitles {
  DRAFT = 'Черновик',
  REGISTERED = 'Заявка зарегистрирована',
  AWAITING_APPROVAL = 'На согласовании',
  APPROVED = 'Согласована',
  DECLINED = 'Не согласована ',
  AWAITING_SEARCH = 'Ожидайте назначения водителя',
  DRIVER_SEARCH = 'Поиск водителя',
  DRIVER_FOUND = 'Водитель назначен',
  DRIVER_ARRIVED = 'Водитель ожидает в точке отправления',
  TRIP_IN_PROGRESS = 'Поездка началась',
  TRIP_FINISHED = 'Поездка завершена',
  CANCELLED = 'Поездка отменена',
}

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
  TIME_BASE_SHORT: 'HH:mm',
  TIME_SHORT: 'H:mm',
  TIME_SHORT_MINUTE: 'mm',
  DATE_WITH_TIME: 'DD-MM-YYYY H:mm',
  DATE_WITH_TIME_LEADING_ZEROS: 'DD-MM-YYYY HH:mm',
  DATE_WITH_TIME_DOTS: 'DD.MM.YYYY HH:mm',
  DATE_WITH_TIME_DOTS_ZONE: 'DD.MM.YYYY HH:mm ([GMT]Z)',
  DATE_WITH_TIME_SECONDS: 'DD-MM-YYYY H:mm:ss',
  DATE_WITH_TIME_SECONDS_DOTS: 'DD.MM.YYYY. HH:mm:ss',
  DATE_WITH_UTC_TIME: 'YYYY-MM-DDTHH:mm',
  DATE_WITH_TIME_ISO_SECONDS: 'YYYY-MM-DDTHH:mm:ss',
  DATE_WITH_TIME_ISO_SECONDS_SPACE: 'YYYY-MM-DD HH:mm:ss',
  BEFORE_THE_SPECIFIED_DATE: 'до указанной даты',
  TO_THE_CURRENT_DATE: 'по текущую дату',
  GSM_DATE: 'DD.MM.YYYY_HH.mm.ss',
  YEAR: 'YYYY',
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
  ROLE_DATA_ANALYST = 'ROLE_DATA_ANALYST',
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
export const EXECUTOR_GROUP_ALL_ID = 'allGroups';
export const EXECUTOR_GROUP_EMPTY_ID = 'noGroups';
export const EMPTY_EXECUTOR_GROUP_CARGO = 'emptyExecutorGroup_cargo';

export const SPECIAL_OPTIONS_LABELS: Record<string, string> = {
  [EXECUTOR_GROUP_ALL_ID]: 'Все группы',
  [EXECUTOR_GROUP_EMPTY_ID]: 'Без групп',
};

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

export enum ContractTypes {
  INCOME = 'INCOME',
  OUTCOME = 'OUTCOME',
  TRANSIT = 'TRANSIT',
}

export enum ContractRestrictionTypes {
  NONE = 'NONE',
  BLACK_LIST = 'BLACK_LIST',
  SELECT_LIST = 'SELECT_LIST',
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
  ZERO = 0,
  REDUCED_FIVE = 5,
  REDUCED_SEVEN = 7,
  BENEFITED = 10,
  BASE = 20,
  BASE_2026 = 22,
}

export const vatValueTitles: Record<VatValue, string> = {
  [VatValue.ZERO]: '0%',
  [VatValue.REDUCED_FIVE]: '5%',
  [VatValue.REDUCED_SEVEN]: '7%',
  [VatValue.BENEFITED]: '10%',
  [VatValue.BASE]: '20%',
  [VatValue.BASE_2026]: '22%',
};

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
