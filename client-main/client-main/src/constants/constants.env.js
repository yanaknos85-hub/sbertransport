/* eslint-disable max-len */
import { _env } from 'utils';

export const APP_NAME = _env('REACT_APP_NAME') || 'main';

export const IS_REMOTE = _env('REACT_APP_REMOTE') === 'TRUE';

export const IS_DEV = _env('NODE_ENV') === 'development';

export const SERVER = _env('REACT_APP_TRANSPORT_API') || '';
export const TO_ADMIN = _env('TO_ADMIN_APP') || '';

export const SUBDOMEN = !IS_DEV ? '.transportonline.info' : '';

export const START_APP = _env('REACT_APP_TRANSPORT_START_APP') || '';
export const NETWORK_LOOP = _env('REACT_APP_NETWORK_LOOP') || '';

export const BASIC_AUTH = _env('REACT_APP_BASIC_AUTH') || '';
export const IS_BASIC_AUTH = BASIC_AUTH === 'TRUE';

export const MOCKED_API = _env('REACT_APP_MOCKED_API') || '';
export const IS_MOCKED_API = MOCKED_API === 'TRUE';

export const MOCKED_AUTH = _env('REACT_APP_MOCKED_AUTH') || '';
export const IS_MOCKED_AUTH = MOCKED_AUTH === 'TRUE';

export const MOCKED_API_PREFIX = IS_MOCKED_AUTH ? 'mock/' : '';

export const TILES = 'tiles';
export const SEARCH = 'search';
export const APPROVE = 'approve';
export const DECLINE = 'decline';
export const CANCEL = 'cancel';
export const FINISH = 'finish';
export const RATE = 'rate';
export const EDIT = 'edit';
export const CALCULATE = 'calculate';
export const ENUM = 'enum';

export const AUTH = 'auth';
export const SUDIR = 'sudir';
export const MAP_TILES = `/${TILES}?x={x}&y={y}&z={z}`;
export const LOGIN = `/${IS_BASIC_AUTH ? AUTH : SUDIR}/login`;
export const LOGOUT = `/${IS_BASIC_AUTH ? AUTH : SUDIR}/logout`;
export const UPDATE_USER_PASS = `/${AUTH}/changePassword`;
export const SUDIR_AUTH = 'sudir/oauth2/authorization/sudir';

export const PAGE = 'page';

export const V2 = 'v2';
export const REQUESTS_CARGO = `request-cargo`;
export const CARGO_COMPENSATION = 'compensation-cargo';

export const USER = 'user';
export const USERS = 'users';
export const GET_USER_AVATAR = `/${USERS}/avatar`;

export const GEO = 'geo';
export const GET_ADDRESS_BY_COORDINATES = `/${GEO}/address`;
export const GET_COORDINATES_BY_ADDRESS = `${GET_ADDRESS_BY_COORDINATES}`;
export const CALC_ROUTE = `/${GEO}/route`;

export const EMPLOYEES = 'employees';
export const EMPLOYEES_ID = `${EMPLOYEES}/:empId`;
export const ORGANIZATIONS = 'organizations';
export const ORGANIZATIONS_ID = `${ORGANIZATIONS}/:orgId`;
export const DEPARTMENTS = 'departments';
export const DEPARTMENTS_ID = `${DEPARTMENTS}/:depId`;
export const POSITIONS = 'positions';
export const POSITIONS_ID = `${POSITIONS}/:posId`;
export const CARS = 'cars';
export const PERSONAL_CARS = 'personalCars';
export const PERSONAL = 'personal';
export const MOTORCYCLE = 'MOTORCYCLE';
export const DELEGATES = 'delegates';
export const CANDIDATES = 'candidates';
export const SUPERVISORS = 'supervisors';
export const SHARED = 'shared';
export const SELF = 'self';
export const TERMINAL = 'terminal';
export const NON_TERMINAL = 'non_terminal';
export const APPROVABLE = 'approvable';
export const APPROVALS = 'approvals';
export const TYPE = 'type';

export const PERSONAL_CARS_ADDING = '/employees/personalCars/adding';

export const GET_SELF_EMPLOYEE = `/${ORGANIZATIONS}/self`;
export const GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS = `/${ORGANIZATIONS}/:orgId/${EMPLOYEES}/`;
export const GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${EMPLOYEES}/`;
export const EMPLOYEE_PARAMS = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${EMPLOYEES}/:empId`;
export const EMPLOYEES_PARAMS = `/${ORGANIZATIONS}/:orgId/${EMPLOYEES}/:empIds`;
export const EMPLOYEES_SEARCH = `/${ORGANIZATIONS}/employees`;
export const PERSONAL_CARS_BASE = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${EMPLOYEES}/:empId/${CARS}/`;
export const PERSONAL_CARS_GET_OR_ADD_PARAMS = `${PERSONAL_CARS_BASE}`;
export const PERSONAL_CARS_EDIT_OR_DELETE_PARAMS = `${PERSONAL_CARS_BASE}:autoId`;

export const ADDRESSES = 'addresses';
export const FAVORITE = 'favorite';

export const SELF_ADDRESSES = `${GET_SELF_EMPLOYEE}/${ADDRESSES}`;

export const SELF_FREQUENT_ADDRESSES = `${SELF_ADDRESSES}/frequently`;
export const SELF_FREQUENT_ADDRESSES_PARAMS = `${SELF_ADDRESSES}/frequently/:addressId`;

export const SELF_FAVORITE_ADDRESSES = `${SELF_ADDRESSES}/${FAVORITE}`;
export const SELF_FAVORITE_ADDRESSES_PARAMS = `${SELF_FAVORITE_ADDRESSES}/:addressId`;

export const SELF_CORPORATE_ADDRESSES = `${SELF_ADDRESSES}/meeting`;

export const LIMITS = 'limits';
export const GET_ACCOUNT_BONUSES = `/${LIMITS}/bonus/`;
export const GET_SPENDINGS = `${LIMITS}/spendings`;
export const GET_DEPLIMITS = `/${LIMITS}/deplimits/`;
export const GET_DEPLIMITS_BY_DEP_AND_YEAR = `/${LIMITS}/deplimits/getByDepartmentAndYear/:depId/year/:year`;
export const GET_EMPLIMITS = `/${LIMITS}/emplimits/`;
export const GET_LIMIT_TRANSFER_HISTORY = `/${LIMITS}/limittransferhistory/getByLimit`;
export const GET_DEPLIMITS_BY_DEP = `/${LIMITS}/deplimits/getByDepartment`;
export const GET_SIBLINGS = `${GET_DEPLIMITS}siblings/`;
export const GET_LIMIT_SHARING = `/${LIMITS}/limitsharing/getByLimit/full/`;
export const GET_EMP_LIMIT = `/${LIMITS}/emplimits/getByEmployeeAndYear/`;
export const GET_LIMITS_REQUESTS_BY_AUTHOR = `/${LIMITS}/requests/getByAuthor`;
export const GET_LIMITS_REQUESTS_BY_APPROVER = `/${LIMITS}/requests/getByApprover`;
export const GET_LIMITS_REQUESTS_STATS = `/${LIMITS}/requests/stats?active=:isActive`;
export const GET_ACTIVE_LIMITS_REQUESTS_BY_APPROVER = `/${LIMITS}/requests/getActiveByApprover`;
export const GET_OLD_LIMITS_REQUESTS_BY_APPROVER = `/${LIMITS}/requests/getOldByApprover`;
export const GET_ALL_REQUESTS = `/${LIMITS}/requests`;
export const APPROVE_REQUEST = `/${LIMITS}/requests/approve`;
export const GET_REQUESTS_DEP = `${GET_ALL_REQUESTS}/dep`;
export const GET_REQUESTS_EMP = `${GET_ALL_REQUESTS}/emp`;
export const GET_LIMIT_CHILDREN = `${LIMITS}/?departmentId=:departmentId`;

export const GET_ALL_ORGANIZATIONS = `/${ORGANIZATIONS}/`;
export const GET_ALL_POSITIONS = `/${ORGANIZATIONS_ID}/${POSITIONS}/`;

export const GET_ALL_DEPARTMENTS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS}/`;
export const GET_DEPARTMENT = `/${ORGANIZATIONS_ID}/${DEPARTMENTS_ID}`;
export const DEPARTMENTS_ADD_PARAMS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS}/`;
export const DEPARTMENT_EDIT_PARAMS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS_ID}`;
export const DELETE_DEPARTMENT = `/${ORGANIZATIONS_ID}/${DEPARTMENTS_ID}`;

export const GET_ORGANIZATION = `/${ORGANIZATIONS_ID}`;
export const GET_POSITION = `/${ORGANIZATIONS_ID}/${POSITIONS_ID}`;
export const TARIFFS = 'tariffs';

export const CALCULATE_TARIFFS_COST = `/${TARIFFS}/${CALCULATE}`;
export const CALCULATE_PERSONAL_CARS_TRIP_COSTS = `/${TARIFFS}/${CALCULATE}/${ENUM}/${PERSONAL}`;
export const GET_ALL_TARIFFS = `/${TARIFFS}`;
export const GET_ALL_TARIFFS_TAXI = `/${TARIFFS}/taxi`;
export const GET_ALL_TARIFFS_PERSONAL = `/${TARIFFS}/personal`;
export const GET_ALL_TARIFFS_PUBLIC = `/${TARIFFS}/public`;
export const GET_TARIFF = `/${TARIFFS}/:transTypeId/:tariffId`;
export const EDIT_TARIFF = `/${TARIFFS}/:transTypeId/:tariffId`;
export const DELETE_TARIFF = `/${TARIFFS}/:transTypeId/:tariffId`;

export const TRANSPORTTYPES = `${ORGANIZATIONS}/transport-types`;
export const AVAILABLE_TRANSPORTTYPES = `${ORGANIZATIONS}/transportorg/org/:orgId`;
export const AVAILABLE_TRANSPORTTYPES_BY_SERVICE = `${ORGANIZATIONS}/transportorg/:serviceType/org/:orgId`;
export const APPROVEMENT = 'approvals';
export const REQUESTS = 'requests';
export const REQUEST = 'request';
export const FINAL = 'final';
export const DEPLIMITS = 'deplimits';
export const PURPOSES = 'purposes';
export const TRIP = 'trip';
export const LIST = 'list';
export const ACTIVE = 'active';
export const CLOSED = 'closed';
export const RIDE = 'ride';
const FILES = 'files';

export const SAVE_REQUEST = `/${REQUESTS}/`;
export const GET_ALL_PURPOSES = `/${ORGANIZATIONS}/:orgId/${PURPOSES}`;
export const GET_PURPOSES_BY_EMPLOYEE = `/${ORGANIZATIONS}/:orgId/${PURPOSES}/search_by_employee`;
export const GET_THIRD_PARTY_AGREEMENT_FOR_PERSONAL_TRANSPORT = `${GET_SELF_EMPLOYEE}/${CARS}/agreement/third-party`;

export const REQUEST_APPROVE_PARAMS = `/${REQUESTS}/${APPROVE}/:reqId`;
export const REQUEST_DECLINE_PARAMS = `/${REQUESTS}/${DECLINE}/:reqId`;

export const GET_REQUEST_PARAMS = `/${REQUESTS}/:reqId`;
export const DELETE_REQUEST_PARAMS = `/${REQUESTS}/:reqId`;
export const EDIT_REQUEST_PARAMS = `/${REQUESTS}/:reqId`;
export const UPDATE_APPROVED_TRIP_REQUEST = `/${REQUESTS}/update/approved/:reqId`;
export const RATE_REQUEST_PARAMS = `/${REQUESTS}/${RATE}/:reqId`;
export const FINISH_REQUEST_PARAMS = `/${REQUESTS}/${FINISH}/:reqId`;
export const CANCEL_REQUEST_PARAMS = `/${REQUESTS}/${CANCEL}/:reqId`;
export const SEARCH_REQUESTS = `/${REQUESTS}/${SEARCH}`;
export const GET_SUITABLE_COOPERATIVE_TRIPS = `/${REQUESTS}/${SHARED}/suitable`;
export const ACCEPT_SUITABLE_COOPERATIVE_TRIP = `/${REQUESTS}/${SHARED}/:sharedId`;
export const SHARED_RIDE_SETTINGS = '/shared_ride_settings';
export const GET_TRIP_FROM_COOP = `/${REQUESTS}/:reqId/${SHARED}`;
export const CHANGE_REQUEST_STATUS = `/${REQUESTS}/status/:reqId/:status`;
export const REQUEST_CHECK_IN_MANUAL = `/${REQUESTS}/checkinManual`;
export const REQUEST_ABSENCE_REASON = `/${REQUESTS}/absenceReason`;
export const COMPLETE_TRIP_REQUEST = `/${REQUESTS}/complete/:reqId`;
export const SAVE_FILE = `/${REQUESTS}/files/upload/:folder`;
export const SAVE_CONFIRM_SUBURB_FILE = `/${REQUESTS}/public/suburb/:requestId/confirm`;
export const SAVE_CONFIRM_CARD_FILE = `/${REQUESTS}/public/cards/:requestId/confirm`;
export const SAVE_TRAVEL_CARD_REQUEST = `/${REQUESTS}/public/cards`;
export const SAVE_SUBURB_COMPENSATION = `/${REQUESTS}/public/suburb`;
export const SAVE_CITY_TRIP_COMPENSATION = `/${REQUESTS}/public/city`;
export const GET_REQUEST_WITH_SUBURB_COMPENSATION = `/${REQUESTS}/public/suburb/:requestId`;
export const GET_REQUEST_WITH_TRAVEL_CARD_COMPENSATION = `/${REQUESTS}/public/cards/:requestId`;
export const GET_REQUESTS_FILES_DOWNLOAD = `${REQUESTS}/${FILES}/download/:folder/:fileName`;

export const UPLOADFILE = `/${ORGANIZATIONS}/dataimport/load/:orgId/:strategyMode/:nsi/:decSeparator/:separator`;
export const PRELOADFILE = `/${ORGANIZATIONS}/dataimport/preload/:orgId/:strategyMode/:nsi/:decSeparator/:separator`;
export const GET_CANDIDATES_IN_DELEGATES_PARAMS = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/${CANDIDATES}/:supId/:transTypeId`;
export const GET_DELEGATES = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/${SUPERVISORS}/:supId`;
export const ADD_DELEGATE = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/`;
export const UPDATE_DELEGATE = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/:delId`;
export const DELETE_DELEGATE = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/:delId`;
export const GET_SELF_CONDIDATES_TO_DELEGATES = '/organizations/self/delegates/candidates/:transportType';
export const LIMITREQUESTS_CRUD = `/limits/${DEPLIMITS}`;
export const GET_LIMIT_BY_REQUEST_ID = '/limits/limits/limitbyrequest/:requestId';
export const LIMITREQUESTS_PARAMS = `/limits/${DEPLIMITS}/:reqId`;
export const LIMITREQUESTS_CANCEL_PARAMS = `/limits/${DEPLIMITS}/${CANCEL}/:reqId`;
export const LIMITREQUESTS_APPROVE_PARAMS = `/limits/${DEPLIMITS}/${APPROVE}/:reqId`;
export const LIMITSETTINGS = '/limits/limitsettings';
export const LIMIT_REQUEST_CANCEL = `/${LIMITS}/${REQUESTS}/${CANCEL}`;
export const GET_REQUEST_TRIP_INFO = `/${REQUESTS}/:reqId`;
export const GET_UPDATED_REQUEST_TRIP_INFO = `/${REQUESTS}/with-update/:reqId`;

export const GET_REQUEST_STATUSES = `/constants/status`;
export const GET_PUBLIC_COMPENSATION_TYPES = `/constants/public-compensation-types`;
export const GET_PUBLIC_TRANSPORT_TYPES = `/constants/public-transport-types`;
export const CREATE_PUBLIC_TRIP_COMPENSATION = `/${REQUESTS}/public/compensation`;
export const SELF_TERMINAL = `${REQUESTS}/${SELF}/${TERMINAL}`;
export const SELF_NON_TERMINAL = `${REQUESTS}/${SELF}/${NON_TERMINAL}`;

export const FIND_PURPOSE_BY_ID = `${GET_ALL_PURPOSES}/:purId`;

export const GET_APPROVAL_BY_REQUEST_ID = `/${APPROVEMENT}/update/trip/:reqId`;

export const APPROVEMENT_REQUEST_TRIP = `/${APPROVEMENT}/${REQUEST}/${TRIP}`;
export const APPROVEMENT_REQUEST_TRIP_APPROVE = `${APPROVEMENT_REQUEST_TRIP}/${APPROVE}/:id`;
export const APPROVEMENT_REQUEST_TRIP_DECLINE = `${APPROVEMENT_REQUEST_TRIP}/${DECLINE}/:id`;

export const APPROVEMENT_UPDATED_TRIP = `/${APPROVEMENT}/update/${TRIP}`;
export const APPROVEMENT_UPDATED_TRIP_ACTIVE = `${APPROVEMENT_UPDATED_TRIP}/${LIST}/${ACTIVE}`;
export const APPROVEMENT_UPDATED_TRIP_CLOSED = `${APPROVEMENT_UPDATED_TRIP}/${LIST}/${CLOSED}`;
export const APPROVEMENT_UPDATED_TRIP_APPROVE = `${APPROVEMENT_UPDATED_TRIP}/${APPROVE}/:id`;
export const APPROVEMENT_UPDATED_TRIP_DECLINE = `${APPROVEMENT_UPDATED_TRIP}/${DECLINE}/:id`;

export const APPROVEMENT_FINAL_TRIP = `/${APPROVEMENT}/${FINAL}/${TRIP}`;
export const APPROVEMENT_FINAL_TRIP_APPROVE = `${APPROVEMENT_FINAL_TRIP}/${APPROVE}/:id`;
export const APPROVEMENT_FINAL_TRIP_DECLINE = `${APPROVEMENT_FINAL_TRIP}/${DECLINE}/:id`;

export const APPROVEMENT_SHARED_RIDE = `/${APPROVEMENT}/${SHARED}/${RIDE}`;
export const APPROVEMENT_SHARED_RIDE_APPROVE = `${APPROVEMENT_SHARED_RIDE}/${APPROVE}/:id`;
export const APPROVEMENT_SHARED_RIDE_DECLINE = `${APPROVEMENT_SHARED_RIDE}/${DECLINE}/:id`;

export const CARGO = 'cargo';

export const POST_TARIFF_TRANSPORT_TYPE = `/${TARIFFS}/${CALCULATE}/${ENUM}`;
export const POST_TARIFF_TRANSPORT_TYPE_ALL = `/${TARIFFS}/${CALCULATE}/cargoCalculate`;

export const SETTINGS = 'settings';
export const PUBLIC = 'public';
export const GET_APPROVAL_PUBLIC = `/${APPROVEMENT}/:orgId/${SETTINGS}/${REQUEST}/${PUBLIC}`;

export const GET_EXTERNAL_PRICES = `/${REQUESTS}/externalPrices`;

export const CARGO_TYPE_SEARCH = `${ORGANIZATIONS}/${CARGO}/type/search`;
export const CARGO_TYPE_POST = `${ORGANIZATIONS}/${CARGO}/type`;

export const UPLOAD_OSAGO_CAR_FILE = `osago/predict`;

export const GET_COUNT_ACTIVE_APPROVALS = `${APPROVEMENT}/${ACTIVE}/count`;

export const VEHICLES = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${EMPLOYEES}/:empId/${CARS}/`;
export const VEHICLES_DETAILED = `${VEHICLES}:vehicleId`;
export const VEHICLES_PDN = `${GET_SELF_EMPLOYEE}/${CARS}/agreement/third-party`;

export const GET_INSTRUCTIONS = `/instructions/web_instructions.json`;

export const GET_ON_THE_LINE_STATUS = `/telemechanic/request/on-the-line`;

// Уведомления
export const CLASS = 'class';
export const PAGE_SIZE = 'pageSize';
export const SORT_FIELD = 'sortField';
export const DIRECTION_ASC = 'directionAsc';
export const NOTIFICATIONS = 'notifications';
export const GET_NOTIFICATIONS = `/${NOTIFICATIONS}/${USER}/${SETTINGS}?${PAGE}=:page&${PAGE_SIZE}=:pageSize&${DIRECTION_ASC}=:directionAsc&${SORT_FIELD}=:sortField&${CLASS}=:class`;
export const POST_NOTIFICATIONS = `/${NOTIFICATIONS}/${USER}/${SETTINGS}/:id`;

// Подключение и удаление роли внутреннего курьера
export const GET_COURIER_ROLES = `${GET_SELF_EMPLOYEE}/courier`;

// Подтверждения
export const CONFIRMATION_SERVICE = '/confirmation';
export const PHONE_CONFIRMATION = `${CONFIRMATION_SERVICE}/phone`;
export const BLOCK_PHONE_CONFIRMATION = `${CONFIRMATION_SERVICE}/block`;

// Список личных грузов
export const TARIFF_CARGO = 'tariff-cargo';
export const GET_PERSONAL_CARGO_LIST = `${TARIFF_CARGO}/${CARGO}/${TYPE}/${PERSONAL}`;
export const DELETE_PERSONAL_CARGO_LIST = `${TARIFF_CARGO}/:orgId/${CARGO}/${TYPE}/:id`;

// Счетчики согласований грузовых заявок
export const GET_COUNT_CARGO_APPROVALS = `${V2}/${REQUESTS_CARGO}/${APPROVALS}/counters`;
export const GET_COUNT_CARGO_COMPENSATION_APPROVALS = `${CARGO_COMPENSATION}/${APPROVALS}/counters`;

// Виджет компенсаций
export const GET_CARGO_COMPENSATION_STATISTICS = `${CARGO_COMPENSATION}/${SELF}/statistics`;
