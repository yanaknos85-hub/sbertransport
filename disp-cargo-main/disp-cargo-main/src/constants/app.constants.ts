import { SortOrder } from 'antd/lib/table/interface';

// ============= Enums ======================

export enum CustomErrorCode {
  MF_LOAD = 666,
  UNKNOWN = 1000,
  TYPES = 1001,
  TIMEOUT = 1002,
  CORS = 1003,
  UNDEFINED_FIELD = 1004,
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
}

export enum TripType {
  Passenger = 'passenger',
  Cargo = 'cargo',
}

export enum TripTypes {
  Passenger = 'PASSENGER',
  Cargo = 'CARGO',
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

export enum AppTitles {
  Disp = 'Диспетчерская',
}

export const errorText: Record<number, { title: string; subtitle: string }> = {
  400: {
    title: 'Упс... Ошибка заполнения данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  401: {
    title: 'Пожалуйста, авторизуйтесь',
    subtitle: '',
  },
  403: {
    title: 'Упс... У Вас недостаточно прав',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  408: {
    title: 'Упс... Слишком большой объем данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
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
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },

  // Все ошибки, для которых нет текста, выводятся как CustomErrorCode.UNKNOWN
  [CustomErrorCode.UNKNOWN]: {
    title: 'Упс... Ошибка системы',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  [CustomErrorCode.TYPES]: {
    title: 'Упс... Ошибка отображения данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  [CustomErrorCode.TIMEOUT]: {
    title: 'Упс... Слишком большой объем данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
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

export const CUBE_CM_TO_METERS = 1_000_000;

export const API_2GIS_KEY = '731c6739-3e33-4939-bdcb-555c7a77bc46';
export const API_2GIS_SDO_KEY = '1ba2da76-5f44-400b-a9e6-5e2197ea2ccf';

export const AUTOPARK_ID = 'AUTOPARK_ID';
