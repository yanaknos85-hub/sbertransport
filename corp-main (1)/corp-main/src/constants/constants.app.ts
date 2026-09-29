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
  ENGINEER_OTO = 'ROLE_ENGINEER_OTO',
  OWNER_LIMIT_DEPARTMENT = 'ROLE_OWNER_LIMIT_DEPARTMENT',
  DISPATCHER_ROOM_ADMIN = 'ROLE_DISPATCHER_ROOM_ADMIN',
  GEO_ADMIN = 'ROLE_GEO_ADMIN',
  ACCESS_ADMIN = 'ROLE_ACCESS_ADMIN',
  ADMIN_CORP_CLIENT = 'ROLE_ADMIN_CORP_CLIENT',
  PARKING_ADMIN = 'ROLE_PARKING_ADMIN',
  PARKING_ADMIN_ORGANIZATION = 'ROLE_PARKING_ADMIN_ORGANIZATION',
  ADMIN_DATA_MASTER = 'ROLE_ADMIN_DATA_MASTER',
  AUDITOR = 'ROLE_AUDITOR',
  EXTERNAL_CLIENT = 'ROLE_EXTERNAL_CLIENT',
  EXTERNAL_REQUEST = 'ROLE_EXTERNAL_REQUEST',
  DRIVER = 'ROLE_DRIVER',
  DELEGATE_REQUEST_CORP_CLIENT = 'ROLE_DELEGATE_REQUEST_CORP_CLIENT',
  DISPATCHER_CORP_CLIENT = 'ROLE_DISPATCHER_CORP_CLIENT',
  DISPATCHER_SUPPORT_SERVICE = 'ROLE_DISPATCHER_SUPPORT_SERVICE',
  DISPATCHER_CONTRACTOR = 'ROLE_DISPATCHER_CONTRACTOR',
  ENGINEER_CORP_CLIENT = 'ROLE_ENGINEER_CORP_CLIENT',
  MAINTENANCE_ENGINEER = 'ROLE_MAINTENANCE_ENGINEER',
  CLIENT_MANAGER = 'ROLE_CLIENT_MANAGER',
  PARKING_COORDINATOR = 'ROLE_PARKING_COORDINATOR',
  COURIER = 'ROLE_COURIER',
  MEDIC = 'ROLE_MEDIC',
  MAIN_DISPATCHER_CONTRACTOR = 'ROLE_MAIN_DISPATCHER_CONTRACTOR',
  NEW_ROLE = 'ROLE_NEW_ROLE',
  AI_AGENT_MASTER = 'ROLE_AI_AGENT_MASTER',
  CHIEF_CORP_CLIENT = 'ROLE_CHIEF_CORP_CLIENT',
  EMPLOYEE_CORP_CLIENT = 'ROLE_EMPLOYEE_CORP_CLIENT',
  TELEMECHANIC = 'ROLE_TELEMECHANIC',
  TELEMECHANIC_ORGANIZATION = 'ROLE_TELEMECHANIC_ORGANIZATION',
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

export enum SDOContacts {
  tel = '8(800) 100-03-02',
  linkTel = 'tel:88001000302',
  mailSbertransport = 'support@sbertransport.ru',
  linkMailSbertransport = 'mailto:support@sbertransport.ru',
}

export const CUBE_MM_TO_METERS = 1_000_000;

export const YEARS_OPTIONS_LENGTH = 5;

export const ORGANIZATION_ID = 'organizationId';
export const ORGANIZATION_NAME = 'organizationName';
export const EXECUTOR_GROUP_ID = 'executorGroupId';
export const IS_ORGANIZATION_FLAG = 'isOrganizationFlag';
export const EXECUTOR_GROUP_ID_PASSENGERS = 'executorGroupIdPassengers';
export const EXECUTOR_GROUP_ID_CARGO = 'executorGroupIdCargo';
export const EXECUTOR_GROUP_ALL_ID = 'allGroups';
export const EXECUTOR_GROUP_EMPTY_ID = 'noGroups';
export const EMPTY_EXECUTOR_GROUP_CARGO = 'emptyExecutorGroup_cargo';

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
  // MF - Module Federation
  MF_LOAD = 'MF-001', // Модуль не загружен

  // F - Frontend
  UNKNOWN = 'F-002', // Неизвестная ошибка
  TYPES = 'F-003', // С бэка пришел не тот тип, который ожидается
  UNDEFINED_FIELD = 'F-006', // Пытается прочитать поле у null или undefined

  // A - API
  TIMEOUT = 'A-004', // Таймаут
  CORS = 'A-005', // CORS
}

export const errorText: Record<string, { title: string; subtitle: string }> = {
  400: {
    title: 'Упс... Ошибка заполнения данных',
    subtitle: 'Проверьте корреткность данных. Если ошибка не исчезнет, обратитесь в поддержку.',
  },
  401: {
    title: 'Пожалуйста, авторизуйтесь',
    subtitle: '',
  },
  403: {
    title: 'Упс... У Вас недостаточно прав',
    subtitle: 'Если Вам необходим доступ, обратитесь в поддержку',
  },
  408: {
    title: 'Упс... Превышено время ожидания данных',
    subtitle: 'Попробуйте перезагрузить страницу. Если ошибка не исчезнет, обратитесь в поддержку.',
  },
  409: {
    title: 'Упс... Конфликт данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  417: {
    title: 'Упс... Ошибка данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },

  500: {
    title: 'Упс... Ошибка сервера',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  503: {
    title: 'Упс... Проводятся работы на сервере',
    subtitle: 'Попробуйте позже. Если ошибка сохранится, обратитесь в поддержку.',
  },

  // Все ошибки, для которых нет текста, выводятся как CustomErrorCode.UNKNOWN
  [CustomErrorCode.UNKNOWN]: {
    title: 'Упс... Ошибка системы',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  [CustomErrorCode.TYPES]: {
    title: 'Упс... Ошибка отображения данных',
    subtitle: '',
  },
  [CustomErrorCode.TIMEOUT]: {
    title: 'Упс... Превышено время ожидания данных',
    subtitle: 'Попробуйте перезагрузить страницу. Если ошибка не исчезнет, обратитесь в поддержку.',
  },
  [CustomErrorCode.MF_LOAD]: {
    title: 'Ошибка загрузки модуля',
    subtitle: 'Попробуйте почистить кэш. Если ошибка не исчезнет, обратитесь в поддержку.',
  },
  [CustomErrorCode.UNDEFINED_FIELD]: {
    title: 'Упс... Ошибка чтения данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  [CustomErrorCode.CORS]: {
    title: 'Упс... Ошибка CORS',
    subtitle: 'Попробуйте перезагрузить страницу. Если ошибка не исчезнет, обратитесь в поддержку.',
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

export const SDO_CLIENT_URL = 'https://client-ext.s-transport.ru';

export const TEST_MODE = 'TEST_MODE';

/** Доступ в АС доступен пользователям со след. ролями */
export const AcceptableUserRoles = [
  'ROLE_PARKING_ADMIN_ORGANIZATION',
  'ROLE_CHIEF_CORP_CLIENT',
  'ROLE_MAINTENANCE_ENGINEER',
  'ROLE_DISPATCHER_SUPPORT_SERVICE',
  'ROLE_TELEMECHANIC',
  'ROLE_TELEMECHANIC_ORGANIZATION',
  'ROLE_MEDIC',
  'ROLE_ACCESS_ADMIN',
  'ROLE_ADMIN_CORP_CLIENT',
  'ROLE_PARKING_ADMIN',
  'ROLE_ADMIN_DATA_MASTER',
  'ROLE_DISPATCHER_CORP_CLIENT',
  'ROLE_ENGINEER_CORP_CLIENT',
  'ROLE_PARKING_COORDINATOR',
];

export enum OrgStructureType {
  EXTERNAL = 'EXTERNAL',
  INTERNAL = 'INTERNAL',
}

export enum EmployeeStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export const FleetSingleRoles = [Roles.TELEMECHANIC, Roles.TELEMECHANIC_ORGANIZATION, Roles.MEDIC];

export enum ContractTypes {
  INCOME = 'INCOME',
  OUTCOME = 'OUTCOME',
  TRANSIT = 'TRANSIT',
}

export const contractTypesTitles: Record<ContractTypes, string> = {
  [ContractTypes.INCOME]: 'Доходные',
  [ContractTypes.OUTCOME]: 'Расходные',
  [ContractTypes.TRANSIT]: 'Транзитные',
};

export enum TariffTypes {
  INCOME = 'INCOME',
  OUTCOME = 'OUTCOME',
  TRANSIT = 'TRANSIT',
}

export const tariffTypesTitles: Record<ContractTypes, string> = {
  [ContractTypes.INCOME]: 'Доходные',
  [ContractTypes.OUTCOME]: 'Расходные',
  [ContractTypes.TRANSIT]: 'Транзитные',
};
export const API_2GIS_KEY = '731c6739-3e33-4939-bdcb-555c7a77bc46';
export const API_2GIS_SDO_KEY = '1ba2da76-5f44-400b-a9e6-5e2197ea2ccf';

export interface DepartmentLevels {
  department1: string[];
  department2: string[];
  department3: string[];
  department4: string[];
  department5: string[];
  department6: string[];
  departmentLevel: number;
}

export const initialDepartments: DepartmentLevels = {
  department1: [],
  department2: [],
  department3: [],
  department4: [],
  department5: [],
  department6: [],
  departmentLevel: 0,
};
