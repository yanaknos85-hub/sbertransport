import { SortOrder } from 'antd/lib/table/interface';

export const ALPHABET = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

// ============= Enums ======================

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

export enum Roles {
  ROLE_GEO_ADMIN = 'ROLE_GEO_ADMIN',
  ROLE_ACCESS_ADMIN = 'ROLE_ACCESS_ADMIN',
  ROLE_ADMIN_CORP_CLIENT = 'ROLE_ADMIN_CORP_CLIENT',
  ROLE_PARKING_ADMIN = 'ROLE_PARKING_ADMIN',
  ROLE_PARKING_ADMIN_ORGANIZATION = 'ROLE_PARKING_ADMIN_ORGANIZATION',
  ROLE_ADMIN_DATA_MASTER = 'ROLE_ADMIN_DATA_MASTER',
  ROLE_AUDITOR = 'ROLE_AUDITOR',
  ROLE_EXTERNAL_CLIENT = 'ROLE_EXTERNAL_CLIENT',
  ROLE_EXTERNAL_REQUEST = 'ROLE_EXTERNAL_REQUEST',
  ROLE_DRIVER = 'ROLE_DRIVER',
  ROLE_DELEGATE_REQUEST_CORP_CLIENT = 'ROLE_DELEGATE_REQUEST_CORP_CLIENT',
  ROLE_DISPATCHER_CORP_CLIENT = 'ROLE_DISPATCHER_CORP_CLIENT',
  ROLE_DISPATCHER_SUPPORT_SERVICE = 'ROLE_DISPATCHER_SUPPORT_SERVICE',
  ROLE_ENGINEER_CORP_CLIENT = 'ROLE_ENGINEER_CORP_CLIENT',
  ROLE_MAINTENANCE_ENGINEER = 'ROLE_MAINTENANCE_ENGINEER',
  ROLE_PARKING_COORDINATOR = 'ROLE_PARKING_COORDINATOR',
  ROLE_NEW_ROLE = 'ROLE_NEW_ROLE',
  ROLE_CHIEF_CORP_CLIENT = 'ROLE_CHIEF_CORP_CLIENT',
  ROLE_EMPLOYEE_CORP_CLIENT = 'ROLE_EMPLOYEE_CORP_CLIENT',
  ROLE_TELEMECHANIC = 'ROLE_TELEMECHANIC',
  ROLE_DISPATCHER_ROOM_ADMIN = 'ROLE_DISPATCHER_ROOM_ADMIN',
  ROLE_FEDERAL_DISPATCHER_CONTRACTOR = 'ROLE_FEDERAL_DISPATCHER_CONTRACTOR',
  ROLE_MAIN_DISPATCHER_CONTRACTOR = 'ROLE_MAIN_DISPATCHER_CONTRACTOR',
  ROLE_DISPATCHER_CONTRACTOR = 'ROLE_DISPATCHER_CONTRACTOR',
}

export const rolesCanAccessAnalytics = [
  Roles.ROLE_DISPATCHER_ROOM_ADMIN,
  Roles.ROLE_FEDERAL_DISPATCHER_CONTRACTOR,
  Roles.ROLE_MAIN_DISPATCHER_CONTRACTOR,
  Roles.ROLE_DISPATCHER_CONTRACTOR];

export enum TripType {
  Passenger = 'passenger',
  Cargo = 'cargo',
}

export enum TripTypes {
  Passenger = 'PASSENGER',
  Cargo = 'CARGO',
  Universal = 'UNIVERSAL',
}

export enum StaffRoles {
  Drivers = 'drivers',
  Dispatchers = 'dispatchers',
}

export enum TransportTypes {
  TAXI = 'TAXI',
  PUBLIC = 'PUBLIC',
  PERSONAL = 'PERSONAL',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',
  SCOOTER = 'SCOOTER',
  GROUP_TRANSFER = 'GROUP_TRANSFER',
}

export enum PingPong {
  Ping = 'PING',
  Pong = 'PONG',
}

export enum WEBSOCKET_CONNECTION_STATUS {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
}

export enum EmployeeStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export enum AutoparkFiltersKeys {
  ADMIN_FILTERS_SETTING = 'ADMIN_FILTERS_SETTING',
  MANAGER_FILTERS_SETTING = 'MANAGER_FILTERS_SETTING',
}

// ============= Texts ======================

export const SUPPORT_PHONE = {
  code: '88007074882',
  title: '8-800-707-48-82',
};

export const SDO_SUPPORT_PHONE = {
  code: '88001000302',
  title: '8 (800) 100-03-02',
};

export enum Contacts {
  outTel = '8(800) 707-48-82',
  linkOutTel = 'tel:88007074882',
  insideTel = '8(559) 90-047',
  mailSbertransport = 'catransport@sberbank.ru',
  linkInsideTel = 'tel:855990047',
  chatSbertransport = 'sberchat.sberbank.ru/@SBERTRANSPORT',
  linkChatSbertransport = 'https://sberchat.sberbank.ru/@SBERTRANSPORT',
  linkMailSbertransport = 'mailto:catransport@sberbank.ru',
}

export enum SDOContacts {
  tel = '8(800) 100-03-02',
  linkTel = 'tel:88001000302',
  mailSbertransport = 'support@sbertransport.ru',
  linkMailSbertransport = 'mailto:support@sbertransport.ru',
}

export enum AppTitles {
  Disp = 'Диспетчерская',
  Crews = 'График работы',
  Staff = 'Персонал',
  Drivers = 'Водители',
  Dispatchers = 'Диспетчеры',
  Reports = 'Отчетность',
  Support = 'Поддержка',
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
// ============= Const ======================

export const TEST_MODE = 'TEST_MODE';

export type SortDirection = 'DESC' | 'ASC';

export const SortOrderToDirectionMap: Record<string, SortDirection> = {
  descend: 'DESC',
  ascend: 'ASC',
};

export const DirectionMapSortToOrder: Record<SortDirection, SortOrder> = {
  DESC: 'descend',
  ASC: 'ascend',
};

export const TYPE_RESPONSE = {
  excel: 'application/vnd.ms-excel',
  sheet: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  csv: 'text/csv',
  json: 'application/json',
};

export const DATE_FORMAT = {
  BASE_REVERTED_DOTS: 'DD.MM.YYYY',
  DATE_WITH_TIME: 'DD.MM.YYYY, HH:mm',
  DATE_WITH_TIME_SECONDS: 'DD-MM-YYYY, HH:mm:ss',
  DATE_WITH_TIME_SECONDS_DOTS: 'DD.MM.YYYY. HH:mm:ss',
  BEFORE_THE_SPECIFIED_DATE: 'до указанной даты',
  TO_THE_CURRENT_DATE: 'по текущую дату',
  DATE_TIME_WORD_MONTH: 'DD MMMM YYYY, HH:mm',
  TIME: 'HH:mm',
  ISO: 'YYYY-MM-DDTHH:mm',
};

export const TransportTypeDescriptions: Record<TransportTypes, string> = {
  TAXI: 'Такси',
  PUBLIC: 'Общественный',
  PERSONAL: 'Личный',
  CARSHARING: 'Каршеринг',
  BICYCLE: 'Велосипед',
  WALK: 'Пешком',
  SCOOTER: 'Самокат',
  GROUP_TRANSFER: 'Трансфер',
};

export const EMPTY_CELL_CONTENT = '-';

export const API_2GIS_KEY = '731c6739-3e33-4939-bdcb-555c7a77bc46';
export const API_2GIS_SDO_KEY = '1ba2da76-5f44-400b-a9e6-5e2197ea2ccf';

export const SEARCH_FILTERS_SETTING = 'SEARCH_FILTERS_SETTING';

/** Минимальная дата для получения статистики по автопарку */
export const MIN_ANALYTICS_START_DATE = 2020;
