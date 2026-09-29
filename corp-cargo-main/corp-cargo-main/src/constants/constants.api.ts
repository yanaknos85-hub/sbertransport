import { IS_BASIC_AUTH, IS_MOCKED_API } from './constants.env';

export const MOCKED_API_PREFIX = IS_MOCKED_API ? 'mock/' : '';

export const CARGO = `cargo`;
export const PERSON = `person`;
export const SEARCH = 'search';
export const APPROVE = 'approve';
export const DECLINE = 'decline';
export const CANCEL = 'cancel';
export const FINISH = 'finish';
export const RATE = 'rate';

export const AUTH = 'auth';
export const SUDIR = 'sudir';
export const LOGIN = `/${!IS_BASIC_AUTH ? SUDIR : AUTH}/login`;
export const CODE = `/${AUTH}/code`;
export const LOGOUT = `/${!IS_BASIC_AUTH ? SUDIR : AUTH}/logout`;
export const UPDATE_USER_PASS = `/${AUTH}/changePassword`;
export const RESET_USER_PASS = `/${AUTH}/resetPassword/:userId`;
export const GET_ROLES_BY_EMPLOYEE_AUTH = `/${AUTH}/roles/:userId`;
export const GET_ROLES_BY_EMPLOYEE_SUDIR = `/${SUDIR}/roles/:userId`;

export const GEO = 'geo';
export const GET_ADDRESS_BY_COORDINATES = `/${GEO}/address`;
export const GET_COORDINATES_BY_ADDRESS = `${GET_ADDRESS_BY_COORDINATES}`;

export const EMPLOYEES = 'employees';
export const EMPLOYEES_ID = `${EMPLOYEES}/:empId`;

export const ORGANIZATIONS = 'organizations';
export const ORGANIZATIONS_ID = `${ORGANIZATIONS}/:orgId`;

export const ORGANIZATIONS_GROUPS = `${ORGANIZATIONS}/groups`;

export const DEPARTMENTS = 'departments';
export const DEPARTMENTS_ID = `${DEPARTMENTS}/:depId`;
export const POSITIONS = 'positions';
export const POSITIONS_ID = `${POSITIONS}/:posId`;

export const LOCATIONS = 'meeting';
export const LOCATIONS_ID = `${LOCATIONS}/:locId`;

export const EMPLOYEES_ATTRIBUTES = 'attributes';
export const EMPLOYEES_ATTRIBUTES_ID = `${EMPLOYEES_ATTRIBUTES}/:eAttrId`;

export const DELEGATES = 'delegates';
export const SUPERVISORS = 'supervisors';
export const LIMITS = 'limits';

export const GET_SELF_EMPLOYEE = `/${ORGANIZATIONS}/self`;
export const GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS = `/${ORGANIZATIONS_ID}/${EMPLOYEES}/`;
export const GET_DEPARTMENT_USER = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${EMPLOYEES}/user/:userId`;
export const EMPLOYEE_PARAMS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS_ID}/${EMPLOYEES_ID}`;

export const GET_DELEGATE = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/:delId`;
export const EMPLOYEES_SEARCH = `/${ORGANIZATIONS}/employees`; // поиск пользователя в группах исполнителей
export const DEPARTMENT_SEARCH = `/${ORGANIZATIONS}/departments`; // поиск подразделения в группах исполнителей
export const SEARCH_EMPLOYEE = `/${ORGANIZATIONS}/:orgId/employees?status=ACTIVE`;
export const SEARCH_EMPLOYEE_EXECUTOR_GROUP = `/${ORGANIZATIONS}/:orgId/employees/?status=ACTIVE`;
export const ADDRESSES = 'addresses';

export const SELF_ADDRESSES = `${GET_SELF_EMPLOYEE}/${ADDRESSES}`;

// deprecated сильное связывание
export const LIMIT_BY_REQUEST = 'limitbyrequest';
export const GET_LIMIT_BY_REQUEST = `/${LIMITS}/${LIMITS}/${LIMIT_BY_REQUEST}/:requestId`;

export const GET_ALL_ORGANIZATIONS = `/${ORGANIZATIONS}/`;
export const GET_ALL_ORGANIZATIONS_GROUPS = `/${ORGANIZATIONS_GROUPS}`;

export const GET_POSITION = `/${ORGANIZATIONS_ID}/${POSITIONS_ID}`;
export const GET_ALL_POSITIONS = `/${ORGANIZATIONS_ID}/${POSITIONS}/`;
export const POSITION_ADD_PARAMS = GET_ALL_POSITIONS;
export const POSITION_PARAMS = GET_POSITION;
export const DELETE_POSITION = GET_POSITION;

export const TRIP_PURPOSES = 'purposes';
export const TRIP_PURPOSE_ID = `${TRIP_PURPOSES}/:purId`;
export const GET_TRIP_PURPOSE = `/${ORGANIZATIONS_ID}/${TRIP_PURPOSE_ID}`;
export const GET_ACTIVE_TRIP_PURPOSES = `/${ORGANIZATIONS_ID}/${TRIP_PURPOSES}`;
export const GET_ALL_TRIP_PURPOSES = `/${ORGANIZATIONS_ID}/${TRIP_PURPOSES}/all`;
export const TRIP_PURPOSE_CREATE = GET_ACTIVE_TRIP_PURPOSES;
export const TRIP_PURPOSE_UPDATE = GET_TRIP_PURPOSE;
export const DELETE_TRIP_PURPOSE = GET_TRIP_PURPOSE;

export const GET_ALL_LOCATIONS = `${SELF_ADDRESSES}/${LOCATIONS}`;
export const ADD_LOCATION = `${SELF_ADDRESSES}/${LOCATIONS}`;
export const UPDATE_LOCATION = `${SELF_ADDRESSES}/${LOCATIONS_ID}`;
export const DELETE_LOCATION = `${SELF_ADDRESSES}/${LOCATIONS_ID}`;

export const GET_EMPLOYEES_ATTRIBUTE = `${ORGANIZATIONS}/self/${EMPLOYEES_ATTRIBUTES_ID}`;
export const GET_ALL_EMPLOYEES_ATTRIBUTES = `${ORGANIZATIONS}/self/${EMPLOYEES_ATTRIBUTES}/`;
export const EMPLOYEES_ATTRIBUTE_ADD_PARAMS = GET_ALL_EMPLOYEES_ATTRIBUTES;
export const EMPLOYEES_ATTRIBUTE_PARAMS = GET_EMPLOYEES_ATTRIBUTE;
export const DELETE_EMPLOYEES_ATTRIBUTE = GET_EMPLOYEES_ATTRIBUTE;

export const GET_ACTIVE_DEPARTMENTS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS}`;
export const GET_DEPARTMENTS_BY_NAME_SUBSTRING = `/${ORGANIZATIONS_ID}/${DEPARTMENTS}`;
export const GET_ALL_DEPARTMENTS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS}/`;
export const GET_DEPARTMENT = `/${ORGANIZATIONS_ID}/${DEPARTMENTS_ID}`;
export const DEPARTMENTS_ADD_PARAMS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS}/`;
export const DEPARTMENT_EDIT_PARAMS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS_ID}`;
export const DELETE_DEPARTMENT = `/${ORGANIZATIONS_ID}/${DEPARTMENTS_ID}`;

export const GET_ORGANIZATION_GROUP = `${GET_ALL_ORGANIZATIONS_GROUPS}/:groupId`;

export const TARIFFS = 'tariffs';
export const TARIFF = 'tariff';
export const TARIFFS_CARGO = 'tariff-cargo';
export const TARIFF_FLEET = 'tariff-fleet';
export const PACK = 'pack';
export const PACK_ID = ':packId';
export const CALCULATE_TRIP_COST = `/${TARIFFS}/calculate`;
export const PUBLIC = 'public';
export const TAXI = 'taxi';
export const CARSHARING = 'carsharing';
export const BICYCLE = 'bicycle';
export const PERSONAL = 'personal';
export const SCOOTER = 'scooter';
export const DEDICATED = 'dedicated';
export const COURIER = 'courier';
export const INTERREGIONAL = 'interregional';
export const DOMESTIC_COURIER = 'domestic-courier';
export const INDIVIDUAL = 'individual';

// deprecated сильное связывание
export const GET_PUBLIC_TARIFF = `/${TARIFFS}/${PUBLIC}/:tariffId`;
export const GET_TAXI_TARIFF = `/${TARIFFS}/${TAXI}/:tariffId`;
export const GET_CARSHARING_TARIFF = `/${TARIFFS}/${CARSHARING}/:tariffId`;
export const GET_BICYCLE_TARIFF = `/${TARIFFS}/${BICYCLE}/:tariffId`;
export const GET_PERSONAL_TARIFF = `/${TARIFFS}/${PERSONAL}/:tariffId`;
export const GET_SCOOTER_TARIFF = `/${TARIFFS}/${SCOOTER}/:tariffId`;
export const GET_DEDICATED_TARIFF = `/${TARIFFS}/${DEDICATED}/:tariffId`;
export const GET_COURIER_TARIFF = `/${TARIFFS}/${COURIER}/:tariffId`;
export const GET_INTERREGIONAL_TARIFF = `/${TARIFFS}/${INTERREGIONAL}/:tariffId`;
export const GET_DOMESTIC_COURIER_TARIFF = `/${TARIFFS}/${DOMESTIC_COURIER}/:tariffId`;
export const GET_ALL_REPORTS = `/${TARIFFS}/${SEARCH}/`;

export const GET_ALL_TARIFFS_PACK = `/${TARIFFS_CARGO}/${PACK}/${TARIFF}`;
export const GET_PACK_CARGO = `/${TARIFFS_CARGO}/${PACK}`;
export const TARIFF_PACK_BY_ID = `${GET_ALL_TARIFFS_PACK}/${PACK_ID}`;
export const GET_TARIFF_PACK = `${GET_ALL_TARIFFS_PACK}/${PACK_ID}`;

// Тарифы
export const GET_ALL_TARIFFS_CARGO = `/${TARIFFS_CARGO}/`;
export const CREATE_TARIFF_CARGO = `/${TARIFFS_CARGO}/:transTypeId`;
export const GET_TARIFF_CARGO = `/${TARIFFS_CARGO}/:transTypeId/:tariffId`;
export const EDIT_TARIFF_CARGO = `/${TARIFFS_CARGO}/:transTypeId/:tariffId`;
export const DELETE_TARIFF_CARGO = `/${TARIFFS_CARGO}/:transTypeId/:tariffId`;
export const GET_AUTO_GUIDE = `/${TARIFFS_CARGO}/auto/enum`;
export const GET_DEDICATED_TARIFF_CARGO = `/${TARIFFS_CARGO}/${DEDICATED}/:tariffId`;
export const GET_COURIER_TARIFF_CARGO = `/${TARIFFS_CARGO}/${COURIER}/:tariffId`;
export const GET_INTERREGIONAL_TARIFF_CARGO = `/${TARIFFS_CARGO}/${INTERREGIONAL}/:tariffId`;
export const GET_DOMESTIC_COURIER_TARIFF_CARGO = `/${TARIFFS_CARGO}/${DOMESTIC_COURIER}/:tariffId`;
export const GET_INDIVIDUAL_TARIFF_CARGO = `/${TARIFFS_CARGO}/${INDIVIDUAL}/:tariffId`;

/* Договоры(Тарифы) */
export const CONTRACTS_CARGO = `${TARIFFS_CARGO}/contracts`;
export const GET_CONTRACTS_CARGO = `/${CONTRACTS_CARGO}/`;
export const SEARCH_CONTRACTS_CARGO = `/${CONTRACTS_CARGO}/search`;
export const CONTRACT_ADD_PARAMS_CARGO = `/${CONTRACTS_CARGO}`;
export const CONTRACT_PARAMS_CARGO = `/${CONTRACTS_CARGO}/:contractId`;
export const DELETE_CONTRACT_CARGO = CONTRACT_PARAMS_CARGO;

export const DEADLINE = 'deadline';
export const DEADLINE_SETTINGS = `${DEADLINE}/:organizationId/settings`;
export const DEADLINE_SETTINGS_UPDATE = `${DEADLINE_SETTINGS}/:settingId`;
export const DEADLINE_SETTINGS_DEFAULT = `${DEADLINE_SETTINGS_UPDATE}/defaults`;

export const TRANSPORTTYPES = `/${ORGANIZATIONS}/transport-types`;
export const GET_TRANSPORT_TYPES = `/organizations/transportorg/all/org/:organizationId`;
export const SAVE_TRANSPORT_TYPES = `/organizations/transportorg/batch/org`;
export const GET_AVAILABLE_TRANSPORT_TYPES = `/organizations/transportorg/org/:organizationId`;

export const REQUESTS = 'requests';
export const REQUEST_CARGO = 'request-cargo';
export const REPORTS = 'reports';
export const CARGO_REPORTS = 'reports-cargo';
export const ROUTE_LIST = 'routelist';
export const COMPENSATION = 'compensation';
export const CARGO_COMPENSATION = 'compensation-cargo';
export const CLOSE_ASYNC = 'closeAsync';
export const PURPOSES = 'purposes';
export const CONSTANTS = 'constants';
export const STATUS = 'status';

// deprecated сильное связывание
export const GET_ALL_TRIP_STATUS = `/${CONSTANTS}/${STATUS}`;
export const GET_CAR_SHАRING_TRIP_STATUS = `/${CONSTANTS}/${STATUS}/carsharing`;
export const GET_PERSONAL_TRIP_STATUS = `/${CONSTANTS}/${STATUS}/personal-oto`;
export const GET_PUBLIC_TRIP_STATUS_REPORTS = `/${CONSTANTS}/${STATUS}/public`;
export const GET_TAXI_TRIP_STATUS = `/${CONSTANTS}/${STATUS}/taxi`;

export const GET_CARGO_TRIP_STATUS = `/${CONSTANTS}/${STATUS}/cargo`;
export const GET_CARGO_TRANSPORT_TYPES = `/${CONSTANTS}/cargo-transport-types`;

// deprecated
export const GET_TRIP_REQUESTS = `/${REQUESTS}/:transportType/:requestId`;
export const FACT_DATA = 'fact_data';
export const GET_TRIP_REQUESTS_FACT_DATA = `/${REQUESTS}/:transportType/${FACT_DATA}/:requestId`;

export const START_TRIP = 'startTrip';
export const DRIVER_ASSIGNED = 'driver-assigned';
export const UPDATE_STATUS = `/${REQUESTS}/${STATUS}/:requestId/:status`;

export const PUBLIC_REPORT = `/${REPORTS}/:orgId/public_report`;
export const PERSONAL_REPORT = `/${REPORTS}/:orgId/personal_report`;
export const TAXI_REPORT = `/${REPORTS}/:orgId/taxi_report`;
export const CAR_SHARING_REPORT = `/${REPORTS}/:orgId/carsharing_report`;

export const GET_DELEGATES = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/${SUPERVISORS}/:supId`;
export const ADD_DELEGATE = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/`;
export const DELETE_DELEGATE = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/:delId`;

export const LIMITS_SEARCH = `/${LIMITS}/${LIMITS}/searchPageable/:orgId`;

export const ROLES = 'roles';
export const GET_ROLES = `/${ROLES}/`;
export const GET_ROLE_BY_CODE = `/${ROLES}/:code`;
export const CREATE_ROLE = `/${ROLES}/`;
export const DELETE_ROLE = `/${ROLES}/:code`;
export const UPDATE_ROLE = `/${ROLES}/:code`;
export const SERVICE = 'service';
export const METHOD = 'method';
export const UI_INFO_SERVICE = 'uiinfoservice';
export const GET_ROLE_SERVICES = `/${UI_INFO_SERVICE}/${SERVICE}/list?roleCode=:roleCode`;
export const GET_ROLE_METHODS = `/${UI_INFO_SERVICE}/${METHOD}/list?serviceId=:serviceId&roleCode=:roleCode`;
export const GET_SERVICES = `/${UI_INFO_SERVICE}/${SERVICE}/list/`;
export const GET_SERVICE_METHODS = `/${UI_INFO_SERVICE}/${METHOD}/list?serviceId=:serviceId`;

export const SHARED_RIDE_SETTING = `shared_ride_settings`;
export const GET_SHARED_RIDE_SETTING_TYPES = `/${ORGANIZATIONS}/shared_ride_settings_types`;
export const GET_ALL_SHARED_RIDE_SETTINGS = `/${ORGANIZATIONS}/:orgId/${SHARED_RIDE_SETTING}`;
export const CREATE_SHARED_RIDE_SETTINGS = `/${ORGANIZATIONS}/:orgId/${SHARED_RIDE_SETTING}`;
export const UPDATE_SHARED_RIDE_SETTINGS = `/${ORGANIZATIONS}/:orgId/${SHARED_RIDE_SETTING}`;
export const SHARED_RIDE = 'shared_ride';
export const SEARCH_SHARED_RIDES = `/${ORGANIZATIONS}/:orgId/${SHARED_RIDE}/plan_order`;

export const CONTRACTORS = 'contractors';
export const GET_ALL_CONTRACTORS = `/${CONTRACTORS}/`;
export const GET_CONTRACTOR_DISPATCHERS = `/${CONTRACTORS}/:contractorId/dispatcher/`;
export const GET_CONTRACTOR_DISPATCHER = `${GET_CONTRACTOR_DISPATCHERS}:dispId/`;

export const TRANSPORT_SERVICE_TYPES_CARGO = `/${TARIFFS_CARGO}/transport-service-type`;

/* TODO Оставшиеся константы, убрать вместе с перенесенными методами в cargoStore */
export const REPAIR = 'repair';
export const MONITORING = 'monitoring';
export const MONITORING_ID = `${MONITORING}/:orderId`;
export const REQUEST = 'request';
export const REQUEST_ID = `${REQUEST}/:orderId`;
export const ORGANIZATION = 'organization';

export const REPAIR_ORDER_DETAILED = `/engineers/monitor/${REPAIR}`;
export const GET_EMPLOYEE_LIST = `${MOCKED_API_PREFIX}/${REPAIR}/employee?searchName=:name`;
export const GET_ORGANIZATION_LIST = `${MOCKED_API_PREFIX}/${REPAIR}/organization`;
export const GET_EMPLOYEE_ORGANIZATION = `${MOCKED_API_PREFIX}/${REPAIR}/${ORGANIZATION}/employee`;
export const GET_DEPARTMENT_LIST = `${MOCKED_API_PREFIX}/${REPAIR}/${ORGANIZATION}/department`;
export const GET_DEADLINE = `${MOCKED_API_PREFIX}/${REPAIR}/${MONITORING}/deadline/${REQUEST_ID}`;
export const GET_STATE_NUMBER_LIST = `${MOCKED_API_PREFIX}/${REPAIR}/vehicle/state-number?searchText=:stateNumber`;

/* Уведомления */
export const SETTINGS = 'settings';
export const NOTIFICATIONS = 'notifications';
export const MASS = 'mass';
export const GET_ALL_NOTIFICATIONS_SETTINGS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}/`;
export const CREATE_NOTIFICATIONS_SETTINGS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}`;
export const UPDATE_NOTIFICATIONS_SETTINGS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}/:notId`;
export const DELETE_NOTIFICATIONS_SETTINGS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}/:notId`;
export const CREATE_NOTIFICATIONS_SETTINGS_MASS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}/${MASS}`;
export const UPDATE_NOTIFICATIONS_SETTINGS_MASS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}/${MASS}`;
export const SHARED_RIDE_DETAILED = `/:requestId/shared`;
export const GET_PUBLIC_COMPENSATIONS = `/${CONSTANTS}/public-compensation-types`;

export const USERS = 'users';
export const USERS_PARAMS = `${USERS}`;
export const GET_ALL_USERS_ATTRIBUTES = `/${REPORTS}/attributes`;
export const GET_CARGO_USERS_ATTRIBUTES = `/${CARGO_REPORTS}/:userId/attributes`;
export const SAVE_PERSONAL_USERS_ATTRIBUTES = `${GET_ALL_USERS_ATTRIBUTES}/personal`;

/* Заявки(Реестры) */
export const CARGO_REPORT = `/${CARGO_REPORTS}/:orgId/cargo_report`;
// Запрос по группам исполнителей
export const CARGO_REPORT_EXECUTOR = `/${CARGO_REPORTS}/cargo_report`;
export const GET_REGISTRY_REPORT = `/reports/files/trip-requests-`;
export const GET_CARGO_REGISTRY_REPORT = `/${CARGO_REPORTS}/:orgId/xlsx/trip-requests/cargo`;
export const GET_CSE_REGISTRY_REPORT = `/${CARGO_REPORTS}/:orgId/xlsx/trip-requests/cse`; // для скачивания kce
export const GET_PAYMENT_REPORT = `/reports/files/payment-report-`;
export const GET_DEFAULT_USERS_ATTRIBUTES = `/reports/default/attributes`;
export const GET_DEFAULT_CARGO_USERS_ATTRIBUTES = `/${CARGO_REPORTS}/default/attributes`;

/* Маршруты(Реестры) */
export const GET_CARGO_ROUTES_REGISTRY_REPORT = `/${CARGO_REPORTS}/:orgId/xlsx/trip-${ROUTE_LIST}/cargo`; // Получение файла маршрутов в реестрах
export const CARGO_ROUTE_REPORT = `/${CARGO_REPORTS}/cargo_${ROUTE_LIST}`; // Запрос списка маршрутов в реестрах
export const CARGO_REGISTRY_ROUTES_DEFAULT_ATTRIBUTES = `/${CARGO_REPORTS}/${ROUTE_LIST}/default/attributes`; // Запрос атрибутов маршрутов по умолчанию
export const GET_CARGO_REGISTRY_ROUTES_USERS_ATTRIBUTES = `/${CARGO_REPORTS}/${ROUTE_LIST}/:userId/attributes`; // Запрос атрибутов пользователя
export const SET_CARGO_REGISTRY_ROUTES_USERS_ATTRIBUTES = `/${CARGO_REPORTS}/${ROUTE_LIST}/:userId/attributes/cargo`; // Настройка атрибутов пользователя

/* Компенсации(Реестры) */
export const GET_COMPENSATION_REGISTRY_REPORT = `/${CARGO_COMPENSATION}/:orgId/xlsx/trip-${COMPENSATION}/cargo`; // Получение файла компенсаций в реестрах
export const CARGO_COMPENSATION_REPORT = `/${CARGO_COMPENSATION}/:orgId/cargo_${COMPENSATION}`; // Запрос списка компенсаций в реестрах
export const CARGO_COMPENSATION_REPORT_DETAILED = `/${CARGO_COMPENSATION}/cargo_${COMPENSATION}/:requestId`; // Запрос детального просмотра компенсации в реестрах
export const CREATE_COMPENSATION_TASK = `/${CARGO_COMPENSATION}/tasks/create`; // Создание задачи на формирование реестра компенсации
export const CARGO_REGISTRY_COMPENSATION_DEFAULT_ATTRIBUTES = `/${CARGO_REPORTS}/${COMPENSATION}/default/attributes`; // Запрос атрибутов компенсаций по умолчанию
export const GET_CARGO_REGISTRY_COMPENSATION_USERS_ATTRIBUTES = `/${CARGO_REPORTS}/${COMPENSATION}/:userId/attributes`; // Запрос атрибутов пользователя
export const SET_CARGO_REGISTRY_COMPENSATION_USERS_ATTRIBUTES = `/${CARGO_REPORTS}/${COMPENSATION}/:userId/attributes/cargo`; // Настройка атрибутов пользователя
export const GET_PAYMENT_REGISTRY_COMPENSATION = `/${CARGO_COMPENSATION}/payment`; // Выплата компенсаций
export const CANCEL_REGISTRY_COMPENSATION = `/${CARGO_COMPENSATION}/cancel`; // Отмена выплаты компенсаций

/* Выгрузки компенсаций(Реестры) */
export const GET_COMPENSATION_TASKS = `/${CARGO_COMPENSATION}/tasks/:orgId/`; // получение списка задач по компенсации
export const CANCEL_COMPENSATION_TASK = `/${CARGO_COMPENSATION}/tasks/:taskId`; // Остановка задачи пока статус еше не стал выполнено

export const GET_TAXI_REGISTRY = `${REPORTS}/xls/import/taxi/registry`;
export const GET_TAXI_REGISTRY_FROM_SERVER = `${GET_TAXI_REGISTRY}/download/:contractorId/:year/:month`;

/* Исполнение заявок(Монитор) */
export const V2 = 'v2';
export const CALCULATE = 'calculate';
export const OTO = 'oto'; // deprecated проверить
export const OTO_CARGO = 'oto-cargo';
export const JOURNAL = 'journal';
export const ROUTE_CARGO = 'route-cargo';
export const REQUESTS_CARGO = `request-cargo`;
export const HOME_CLICK = 'home-click';
export const REQUESTS_OTO_FEED_PERSON_BY_ORG_ID = `${OTO}/${PERSON}/:organizationId`; // deprecated
export const REQUESTS_OTO_FEED_CARGO_BY_ORG_ID = `${OTO_CARGO}/${CARGO}/:organizationId`; // deprecated
export const REQUESTS_CARGO_ACTIVE_BY_HR_ID = `${ROUTE_CARGO}/request/${OTO}/:humanReadableId`;
export const REQUESTS_CARGO_ACTIVE = `${REQUESTS_CARGO}/${CARGO}`;
export const ADD_ADDITIONAL_CONTACT = `${V2}/${REQUESTS_CARGO_ACTIVE}/${V2}/:humanReadableId`;
export const UPDATE_CARGO_HOME_CLICK_STATUS = `${HOME_CLICK}/:requestId/${STATUS}/:status`;
export const CREATE_CARGO_ENGINEER_COMMENT = `${V2}/${REQUESTS_CARGO_ACTIVE}/${V2}/:humanReadableId`;
export const CANCEL_ORDER = `${V2}/${REQUESTS_CARGO}/${CARGO}/:requestId`;
export const UPDATE_ORDER = `${V2}/${REQUESTS_CARGO_ACTIVE}/${V2}/:humanReadableId`;
export const GET_TARIFF_TRANSPORT_TYPE_ALL_MULTI = `/${TARIFFS_CARGO}/${V2}/${CALCULATE}/all`;

/* Исполнение заявок(Расписания) */
export const GET_TEMPLATE_BY_ORG_ID = `${REQUESTS_OTO_FEED_CARGO_BY_ORG_ID}/template`;

/* Исполнение заявок POST метод */
export const REQUESTS_OTO_FEED_CARGO_POST = `${OTO_CARGO}/${CARGO}`;

/* Approvals */
export const APPROVALS = 'approvals';
export const APPROVALS_SETTINGS_TAXI = `${APPROVALS}/:orgId/${SETTINGS}/request/taxi`;
export const APPROVALS_SETTINGS_TAXI_UPDATE = `${APPROVALS}/:orgId/${SETTINGS}/request/taxi/:apprId`;
export const APPROVALS_SETTINGS_PUBLIC = `${APPROVALS}/:orgId/${SETTINGS}/request/public`;
export const APPROVALS_SETTINGS_PUBLIC_UPDATE = `${APPROVALS}/:orgId/${SETTINGS}/request/public/:apprId`;
export const APPROVALS_SETTINGS_OTHER_POST = `${APPROVALS}/:orgId/${SETTINGS}/request/other`;
export const APPROVALS_SETTINGS_OTHER_GET_PUT = `${APPROVALS}/:orgId/${SETTINGS}/request/other/:transType`;
export const APPROVALS_SETTINGS_TAXI_DEFAULT = `${APPROVALS}/:orgId/${SETTINGS}/request/taxi/:apprId/restore`;
export const APPROVALS_SETTINGS_PUBLIC_DEFAULT = `${APPROVALS}/:orgId/${SETTINGS}/request/public/:apprId/restore`;
export const APPROVALS_SETTINGS_OTHER_DEFAULT = `${APPROVALS}/:orgId/${SETTINGS}/request/other/:transType/restore`;

/* Геозоны */
export const GEO_ZONES = 'geo-zones';
export const GET_ALL_GEO_ZONES = `${GEO_ZONES}/`;
export const GEO_ZONES_ID = `${GEO_ZONES}/:zoneId`;

export const CLEAR_QUERY_CONFIG = {
  // Не храним кэш
  cacheTime: 0,
  staleTime: 0,
  // Не будет отображать fallback из Suspense
  suspense: false,
  // Не делаем refetch на фокус окна
  refetchOnWindowFocus: false,
  // В случае ошибки, не будет повторно вызывать запрос
  retry: false,
};

/* Справочник упаковки(Параметры сервиса) */
export const CARGO_PACKAGES = `packages`;
export const CARGO_PACKAGE = `package`;
export const GET_ALL_CARGO_PACKAGE = `${CONTRACTORS}/:contractorId/${CARGO}/${CARGO_PACKAGES}/`;
export const CREATE_CARGO_PACKAGE = `${CONTRACTORS}/:contractorId/${CARGO}/${CARGO_PACKAGE}/`;
export const UPDATE_CARGO_PACKAGE = `${CONTRACTORS}/:contractorId/${CARGO}/${CARGO_PACKAGE}/:packageId/`;
export const DELETE_CARGO_PACKAGE = `${CONTRACTORS}/:contractorId/${CARGO}/${CARGO_PACKAGE}/:packageId/`;

/* Справочник грузов(Параметры сервиса) */
export const CARGO_TYPES = `types`;
export const CARGO_TYPE = `type`;
export const CATEGORIES = `categories`;
export const CARGO_TYPE_ID = `:cargoTypeId`;
export const GET_ALL_CARGO_TYPES = `${TARIFFS_CARGO}/:organizationId/${CARGO}/${CARGO_TYPE}`;
export const GET_CARGO_TYPE = `${ORGANIZATIONS}/${CARGO}/${CARGO_TYPE}/${CARGO_TYPE_ID}`; // deprecated
export const CREATE_CARGO_TYPE = `${TARIFFS_CARGO}/:organizationId/${CARGO}/${CARGO_TYPE}`;
export const UPDATE_CARGO_TYPE = `${TARIFFS_CARGO}/:organizationId/${CARGO}/${CARGO_TYPE}/${CARGO_TYPE_ID}`;
export const DELETE_CARGO_TYPE = `${TARIFFS_CARGO}/:organizationId/${CARGO}/${CARGO_TYPE}/${CARGO_TYPE_ID}`;
export const GET_ALL_CARGO_TYPE_NAMES = `${TARIFFS_CARGO}/${CARGO}/${CARGO_TYPE}/${CARGO_TYPES}`;
export const GET_ALL_CARGO_CATEGORY_NAMES = `${TARIFFS_CARGO}/${CARGO}/${CARGO_TYPE}/${CATEGORIES}`;

/* Справочник автомобилей(Параметры сервиса) */
export const CARGO_AUTO = `auto`;
export const CARGO_AUTO_ID = `:autoId`;
export const GET_ALL_CARGO_AUTO = `${TARIFFS_CARGO}/${CARGO_AUTO}`;
export const GET_CARGO_AUTO = `${TARIFFS_CARGO}/${CARGO_AUTO}/${CARGO_AUTO_ID}`;
export const GET_CAPACITY_CARGO_AUTO = `${TARIFFS_CARGO}/${CARGO_AUTO}/capacity`;
export const GET_CATEGORY_CARGO_AUTO = `${TARIFFS_CARGO}/${CARGO_AUTO}/cargo-categories`;
export const CREATE_CARGO_AUTO = GET_ALL_CARGO_AUTO;
export const UPDATE_CARGO_AUTO = GET_CARGO_AUTO;
export const DELETE_CARGO_AUTO = GET_CARGO_AUTO;

export const CARGO_DELIVERY_TIME_SETTINGS = `${TARIFFS_CARGO}/deliverytime`;

export const GET_CARGO_REQUEST_STATUS = `/${CONSTANTS}/${STATUS}/${CARGO}`;
export const UPDATE_CARGO_REQUEST_STATUS = `${V2}/${REQUEST_CARGO}/${CARGO}/:requestId/${STATUS}/:status`;
export const CARGO_REQUEST_COMPLETE_TRANSFER = `${V2}/${REQUEST_CARGO}/${CARGO}/:requestId`;
export const CARGO_HOME_CLICK_REQUEST_COMPLETE_TRANSFER = `/${HOME_CLICK}/:requestId`;
export const CARGO_REQUEST_COMPLETE_SHIPMENT = `${V2}/${REQUEST_CARGO}/${CARGO}/:requestId`;
export const CARGO_REQUEST_HOME_CLICK_COMPLETE_SHIPMENT = `/${HOME_CLICK}/:requestId`;

/* Планировщик */
export const SRM = 'srm';
export const START = 'start';
export const SEND = 'send';
export const CONTRACTOR = 'contractor';
export const ADD_CARGO = 'add';
export const PLANNER_REQUEST = 'request';
export const PLANNER_SEARCH_ROUTE = 'searchRoute';
export const DELETE_WAYPOINT = 'waypoint';
export const AUTO_CONTRACTOR = 'autoContractor';

export const PLANNER_ROUTE_CARGO_MULTIPLE = 'route-cargo';
export const AUTO_PLANNING_MULTIPLE = `/${PLANNER_ROUTE_CARGO_MULTIPLE}/${SRM}/${START}`; // Ручной запуск SRM
export const PLANNER_CREATE_ROUTE_MULTIPLE = `/${PLANNER_ROUTE_CARGO_MULTIPLE}/`; // Добавление нового маршрута
export const PLANNER_SEARCH_ROUTES_MULTIPLE = `/${PLANNER_ROUTE_CARGO_MULTIPLE}/${PLANNER_SEARCH_ROUTE}`; // Поиск маршрутов c пагинацией
export const PLANNER_GET_ROUTE_MULTIPLE = `/${PLANNER_ROUTE_CARGO_MULTIPLE}/:routeId`; // Получение маршрута
export const PLANNER_GET_ORDER_MULTIPLE = `/${PLANNER_ROUTE_CARGO_MULTIPLE}/${PLANNER_REQUEST}/:orderId`; // Получение заявки
export const PLANNER_SEARCH_ORDERS_MULTIPLE = `/${PLANNER_ROUTE_CARGO_MULTIPLE}/${SEARCH}`; // Поиск заявок для маршрута c пагинацией
export const PLANNER_ADD_ORDER_TO_ROUTE_MULTIPLE = `/${PLANNER_ROUTE_CARGO_MULTIPLE}/${ADD_CARGO}`; // Смена адресов местами/смена контрагента в детальном просмотре маршрута
export const PLANNER_ADD_ORDER_TO_ROUTE_MULTIPLE_V2 = `/${PLANNER_ROUTE_CARGO_MULTIPLE}/:routeListId/requests`; // Добавление заявки в маршрут
export const PLANNER_DELETE_ROUTE_MULTIPLE = `/${PLANNER_ROUTE_CARGO_MULTIPLE}/:routeId`; // Удаление маршрута
export const PLANNER_UPDATE_ROUTE_STATUS_MULTIPLE = `/${PLANNER_ROUTE_CARGO_MULTIPLE}/:routelistId/${STATUS}/:status`; // Изменение статуса
export const PLANNER_DELETE_ADDRESS_POINT_MULTIPLE = `/${PLANNER_ROUTE_CARGO_MULTIPLE}/${DELETE_WAYPOINT}/:waypointId`; // Удаление точки из маршрута
export const PLANNER_GET_CONTRACTORS_MULTIPLE = `/${TARIFFS_CARGO}/${DEDICATED}/${AUTO_CONTRACTOR}?regionId=:regionId&organizationId=:organizationId&desiredDate=:desiredDate&cargoCategory=:cargoCategory`;
export const PLANNER_SEND_TO_CONTRACTOR_MULTIPLE = `${PLANNER_ROUTE_CARGO_MULTIPLE}/:routeId/${SEND}/${CONTRACTOR}`; // Отправка маршрута контрагенту
export const SEND_ORDER_TO_CONTRACTOR = `${PLANNER_ROUTE_CARGO_MULTIPLE}/${PLANNER_REQUEST}/:orderId/${SEND}/${CONTRACTOR}`; // Формирование маршрута
export const SEND_ROUTE_TO_CONTRACTOR = `${PLANNER_ROUTE_CARGO_MULTIPLE}/:routeId/${SEND}/${CONTRACTOR}`; // Формирование маршрута
export const REQUESTS_JOURNAL_ROUTES_MULTIPLE = `${PLANNER_ROUTE_CARGO_MULTIPLE}/${JOURNAL}/${SEARCH}`; // Поиск маршрутов для монитора (c пагинацией)
export const REQUESTS_JOURNAL_ROUTE_MULTIPLE = `${PLANNER_ROUTE_CARGO_MULTIPLE}/${JOURNAL}/:requestId`; // Получение маршрута для монитора
export const PLANNER_STATUS_CARGO_HISTORY_MULTIPLE = `${PLANNER_ROUTE_CARGO_MULTIPLE}/:routelistId/status/history`; // Зачем?
export const CANCEL_ROUTE = `${PLANNER_ROUTE_CARGO_MULTIPLE}/:requestId`;
export const REQUESTS_CHANGE_ROUTE = `${PLANNER_ROUTE_CARGO_MULTIPLE}/${JOURNAL}/:requestId`;

export const USER_AGENT = '/user-agent/';
export const QUANTITY = `${USER_AGENT}quantity/`;

/* Группы исполнителей(Параметры сервиса) */
export const GET_EMPLOYEES_FOR_EXECUTOR_GROUP = `/${ORGANIZATIONS}/${EMPLOYEES}/search_eg`;

export const GET_DEPARTMENT_LEVEL = `/${REPORTS}/:orgId/department`;

export const GET_STATUS_CODE = `/${REPORTS}/:transportType/statusCode`;

export const GET_ALL_ORGANIZATIONS_SEARCH_TERM = `${REPORTS}/search-organisation/`;

export const GET_CARGO_BUSINESS_REPORTS = `/${REPORTS}/files/trip-requests-public-analytics`;

/* Бизнес отчеты(Реестры) */
export const GET_TASKS = `/${CARGO_REPORTS}/tasks/`; // получение таблицы
export const CREATE_TASK = `/${CARGO_REPORTS}/tasks/create`; // создание задачи
export const DOWNLOAD_TASK = `/${CARGO_REPORTS}/files/`; // скачивание файла после того как статус станет выполнено
export const CANCEL_TASK = `/${CARGO_REPORTS}/tasks/cancel/:taskId`; // остановка задачи пока статус еше не стал выполнено

/* ЭТрН (Подписание) */
export const ETRN = 'etrn';
export const ETRN_CARGO = `/${ETRN}-cargo`;
export const ETRN_CARGO_LIST = `${ETRN_CARGO}/list`;
export const ETRN_CARGO_CARD = `${ETRN_CARGO}/:cardId`;
export const ETRN_CARGO_LOCK = `${ETRN_CARGO}/:cardId/lock`;
export const ETRN_CARGO_ELIGIBILITY = `${ETRN_CARGO}/attorneyCheck`;
export const ETRN_CARGO_TITLE = `${ETRN_CARGO}/:etrnId/title/`;
export const ETRN_CARGO_TITLE_SEND = `${ETRN_CARGO}/:etrnId/title/send`;
