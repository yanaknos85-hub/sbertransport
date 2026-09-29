// Сюда выносим все ручки по Cargo. Не импортируем ничего из constants.env!

import { MOCKED_API_PREFIX } from 'constants/constants.env';
export { MOCKED_API_PREFIX };

export const REQUESTS = 'request-cargo';
export const TARIFFS = 'tariff-cargo';
export const SELF = 'self';
export const CALCULATE = 'calculate';
export const ENUM = 'enum';
export const GEO = 'geo';
export const CARGO = 'cargo';
export const REGULAR_CARGO_TEMPLATE = 'template';
export const APPROVABLE = 'approvable';
export const ORGANIZATIONS = 'organizations';
export const TERMINAL = 'terminal';
export const NON_TERMINAL = 'non_terminal';
export const PACK = 'pack';
export const V2 = 'v2';
export const CARGO_TYPE = `type`;
export const CATEGORIES = `categories`;
export const TYPE = 'type';
export const PERSONAL = 'personal';
export const AVAILABLE = 'available';
export const GEO_ZONES = 'geo-zones';

export const ADD_CARGO_MULTI = `/${V2}/${REQUESTS}/${CARGO}`;
export const ADD_REGULAR_CARGO_MULTI = `/${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}`;
export const GET_REGULAR_COST_MULTIPLE = `/${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}/cost?periodType=:periodType&dayOfWeek=:dayOfWeek&beginDate=:beginDate&endDate=:endDate&cost=:cost&weekOfMonth=:weekOfMonth&monthOfQuartal=:monthOfQuartal`;

export const ADD_CARGO_MASS_MULTIPLE = `/${V2}/${REQUESTS}/${CARGO}/list`;
export const ADD_REGULAR_CARGO_MASS_MULTIPLE = `/${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}/list/async`;
export const GET_MASS_REGULAR_REQUEST_MULTIPLE = `/${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}/list`;
export const POST_TARIFF_TRANSPORT_TYPE = `/${TARIFFS}/${CALCULATE}/${ENUM}`;
export const POST_TARIFF_TRANSPORT_TYPE_ALL = `/${TARIFFS}/${CALCULATE}/cargoCalculate`;
export const POST_TARIFF_TRANSPORT_TYPE_ALL_MULTI = `/${TARIFFS}/${V2}/${CALCULATE}`;

export const GET_MASS_REQUEST_MULTI = `/${V2}/${REQUESTS}/import`;
export const MASS_REQUEST_SAVE_FILE_MULTIPLE = `/${V2}/${REQUESTS}/import`;
export const MASS_REQUEST_SAVE_FILE_REGULAR = `/${V2}/${REQUESTS}/import/${REGULAR_CARGO_TEMPLATE}`;

export const CALC_ROUTE = `/${GEO}/route`;

export const MULTIPLE_CARGO_SELF_TERMINAL = `${V2}/${REQUESTS}/${CARGO}/${SELF}/${TERMINAL}`;
export const MULTIPLE_CARGO_SELF_NON_TERMINAL = `${V2}/${REQUESTS}/${CARGO}/${SELF}/${NON_TERMINAL}`;
export const MULTIPLE_CARGO_REQUEST = `${V2}/${REQUESTS}/${CARGO}/:rqUuid`;
export const MULTIPLE_CARGO_HISTORY = `${V2}/${REQUESTS}/${CARGO}/:rqUuid/status/history`;
export const MULTIPLE_CARGO_APPROVABLE_TERMINAL = `${V2}/${REQUESTS}/${CARGO}/${APPROVABLE}/${TERMINAL}`;
export const MULTIPLE_CARGO_APPROVABLE_NON_TERMINAL = `${V2}/${REQUESTS}/${CARGO}/${APPROVABLE}/${NON_TERMINAL}`;
export const MULTIPLE_CARGO_APPROVE = `${V2}/${REQUESTS}/${CARGO}/approve`;
export const MULTIPLE_CARGO_DECLINE = `${V2}/${REQUESTS}/${CARGO}/decline`;
export const MULTIPLE_CARGO_CANCEL = `${V2}/${REQUESTS}/${CARGO}/:rqUuid/cancel`;

export const MULTIPLE_REGULAR_CARGO_SELF_TERMINAL = `${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}/${SELF}/${TERMINAL}`;
export const MULTIPLE_REGULAR_CARGO_SELF_NON_TERMINAL = `${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}/${SELF}/${NON_TERMINAL}`;
export const MULTIPLE_REGULAR_CARGO_APPROVABLE_TERMINAL = `${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}/${APPROVABLE}/${TERMINAL}`;
export const MULTIPLE_REGULAR_CARGO_APPROVABLE_NON_TERMINAL = `${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}/${APPROVABLE}/${NON_TERMINAL}`;
export const MULTIPLE_REGULAR_CARGO_REQUEST = `${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}/:rqUuid`;
export const MULTIPLE_REGULAR_CARGO_CARGO_HISTORY = `${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}/:rqUuid/status/history`;
export const MULTIPLE_REGULAR_CARGO_APPROVE = `${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}/approve`;
export const MULTIPLE_REGULAR_CARGO_DECLINE = `${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}/decline`;
export const MULTIPLE_REGULAR_CARGO_CANCEL = `${V2}/${REQUESTS}/${CARGO}/${REGULAR_CARGO_TEMPLATE}/:rqUuid/cancel`;

export const CARGO_TYPE_SEARCH = `${TARIFFS}/:organizationId/${CARGO}/${TYPE}/${AVAILABLE}`;
export const CARGO_TYPE_POST = `${ORGANIZATIONS}/:organizationId/${CARGO}/type`;

export const CARGO_EVALUATION_POST = `${V2}/${REQUESTS}/evaluation`;

export const GET_COORDINATES_BY_VSP_ADDRESS = `vsp-service-cargo/address`;

export const GET_PACK_CARGO = `/${TARIFFS}/${PACK}`;

export const GET_ALL_CARGO_CATEGORY_NAMES = `${ORGANIZATIONS}/${CARGO}/${CARGO_TYPE}/${CATEGORIES}`;

// Переезд
export const GET_FLATS = `${TARIFFS}/${CARGO}/type/group/search?name=:flatType`;

// Создание личного груза
export const CREATE_PERSONAL_CARGO_LIST = `${TARIFFS}/${CARGO}/${TYPE}`;

// Компенсация
export const GET_COMPENSATION_NON_TERMINAL = `compensation-cargo/approvable/non_terminal`;
export const GET_COMPENSATION_TERMINAL = `compensation-cargo/approvable/terminal`;
export const APPROVE_COMPENSATION = `compensation-cargo/approve`;
export const DECLINE_COMPENSATION = `compensation-cargo/decline`;

// Подтверждение получения груза
export const CARGO_ACCEPT_RECEIVE = `/${V2}/${REQUESTS}/${CARGO}/:rqUuid/accept`;
