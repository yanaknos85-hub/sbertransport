import { IS_BASIC_AUTH, IS_MOCKED_API } from './constants.env';

export const MOCKED_API_PREFIX = IS_MOCKED_API ? 'mock' : '';

export const CARGO = `cargo`;
export const PERSON = `person`;
export const SEARCH = 'search';
export const APPROVE = 'approve';
export const DECLINE = 'decline';
export const CANCEL = 'cancel';
export const FINISH = 'finish';
export const RATE = 'rate';
export const EDIT = 'edit';
export const ADD = 'ADD';
export const UPDATE = 'UPDATE';

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
export const CALC_ROUTE = `/${GEO}/route`;

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

export const CARS = 'cars';
export const PERSONAL_CARS = 'personalCars';
export const DELEGATES = 'delegates';
export const CANDIDATES = 'candidates';
export const SUPERVISORS = 'supervisors';
export const LIMITS = 'limits';

export const PERSONAL_CARS_ADDING = '/employees/personalCars/adding';

export const GET_SELF_EMPLOYEE = `/${ORGANIZATIONS}/self`;
export const CONSENT = `/${ORGANIZATIONS}/self/consent/`;
export const GET_ALL_EMPLOYEES = `/${ORGANIZATIONS}/${EMPLOYEES}`;
export const GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS = `/${ORGANIZATIONS_ID}/${EMPLOYEES}/`;
export const GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${EMPLOYEES}/`;
export const GET_DEPARTMENT_USER = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${EMPLOYEES}/user/:userId`;
export const EMPLOYEE_PARAMS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS_ID}/${EMPLOYEES_ID}`;
export const EMPLOYEE_ADD_PARAMS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS_ID}/${EMPLOYEES}/`;
export const EMPLOYEES_PARAMS = `/${ORGANIZATIONS}/:orgId/${EMPLOYEES}/:empIds`;
export const EMPLOYEES_SEARCH = `/${ORGANIZATIONS}/employees/search?fio=:name`;
export const PERSONAL_CARS_ADD_PARAMS = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${EMPLOYEES_ID}/${CARS}/`;
export const PERSONAL_CARS_EDIT_PARAMS = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${EMPLOYEES_ID}/${CARS}/:personalCarId`;
export const PERSONAL_CARS_DELETE_PARAMS = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${EMPLOYEES_ID}/${CARS}/:personalCarId`;
export const GET_SELF_CONDIDATES_TO_DELEGATES = `${GET_SELF_EMPLOYEE}/delegates/candidates/:transportType`;
export const GET_DELEGATE = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/:delId`;
// export const GET_EMPLOYEES = `/${ORGANIZATIONS}/${EMPLOYEES}/${SEARCH}`;
export const SEARCH_EMPLOYEE = `/${ORGANIZATIONS}/:orgId/employees?status=ACTIVE`;
export const ADDRESSES = 'addresses';
export const FAVORITE = 'favorite';

export const SELF_ADDRESSES = `${GET_SELF_EMPLOYEE}/${ADDRESSES}`;

export const SELF_FREQUENT_ADDRESSES = `${SELF_ADDRESSES}/frequently`;
export const SELF_FREQUENT_ADDRESSES_PARAMS = `${SELF_ADDRESSES}/frequently/:addressId`;

export const SELF_FAVORITE_ADDRESSES = `${SELF_ADDRESSES}/${FAVORITE}`;
export const SELF_FAVORITE_ADDRESSES_PARAMS = `${SELF_FAVORITE_ADDRESSES}/:addressId`;

export const GET_DEP_LIMITS = `/${LIMITS}/deplimits`;
export const GET_EMP_LIMITS = `/${LIMITS}/emplimits`;
export const CREATE_DEP_LIMIT = `/${LIMITS}/deplimits`;
export const DELETE_DEP_LIMIT = `/${LIMITS}/deplimits`;
export const DELETE_ALL_LIMITS_IN_CORP_CLIENT = `/${LIMITS}/${LIMITS}/deleteByServiceTypeAndYear/:organizationId/serviceType/:serviceType/year/:year`;
export const LIMIT_SHARING = `/${LIMITS}/limitsharing`;
export const CURRENT_LIMIT_SHARING = `${LIMIT_SHARING}/getByLimit/:limitId`;
export const LIMIT_SETTINGS = `/${LIMITS}/limitsettings`;
export const LIMIT_TRANSFER_HISTORY = `/${LIMITS}/limittransferhistory`;
export const LIMIT_SHARING_PERCENTS = `/${LIMITS}/limitsharingprocents`;
export const CURRENT_LIMIT_SHARING_PERCENTS = `${LIMIT_SHARING_PERCENTS}/getByLimit/:limitId`;
export const GET_LIMIT_CHILDREN = `/${LIMITS}/${LIMITS}/limitchildren/:limitId?limitType=:limitType`;
export const LIMIT_SHARING_PER_PERIOD = `/${LIMITS}/limitsharingperperiod/:limitSharingId`;
export const GET_ALL_LIMIT_REQUESTS_BY_AUTHOR = `/${LIMITS}/requests/getByAuthor`;
export const GET_ALL_LIMIT_REQUESTS_BY_APPROVER = `/${LIMITS}/requests/getByApprover`;

export const EMP_LIMITS_MAKE_EMP_LIMITS = `${GET_EMP_LIMITS}/make_emp_limits`;
export const DEP_LIMIT_SHARE_PRIMARY = `${CREATE_DEP_LIMIT}/share_primary`;
export const DEP_LIMIT_SHARE_SECONDARY = `${CREATE_DEP_LIMIT}/share_secondary`;
export const DEP_LIMIT_SHARE_ECONOMY = `${CREATE_DEP_LIMIT}/share_economy`;
export const ECONOMY = `/economy`;
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
export const GET_ACTIVE_EMPLOYEES_ATTRIBUTES = `${ORGANIZATIONS}/self/${EMPLOYEES_ATTRIBUTES}/active`;
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

export const GET_ORGANIZATION = `/${ORGANIZATIONS_ID}`;
export const GET_ORGANIZATION_GROUP = `${GET_ALL_ORGANIZATIONS_GROUPS}/:groupId`;

export const TARIFFS = 'tariffs';
export const TARIFF = 'tariff';
export const TARIFFS_CARGO = 'tariff-cargo';
export const TARIFF_FLEET = 'tariff-fleet';
export const PACK = 'pack';
export const PACK_ID = ':packId';
export const CALCULATE_TRIP_COST = `/${TARIFFS}/calculate`;
export const GET_ALL_TARIFFS = `/${TARIFFS}/`;
export const GET_ALL_TARIFFS_TAXI = `/${TARIFFS}/taxi`;
export const GET_ALL_TARIFFS_PERSONAL = `/${TARIFFS}/personal`;
export const GET_TARIFF = `/${TARIFFS}/:transTypeId/:tariffId`;
export const EDIT_TARIFF = `/${TARIFFS}/:transTypeId/:tariffId`;
export const DELETE_TARIFF = `/${TARIFFS}/:transTypeId/:tariffId`;
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
export const GROUP_TRANSFER = 'group_transfer';

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
export const GET_GROUP_TRANSFER_TARIFF = `/${TARIFFS}/${GROUP_TRANSFER}/:tariffId`;
export const GET_ALL_REPORTS = `/${TARIFFS}/${SEARCH}/`;
export const GET_ALL_TARIFFS_CARGO = `/${TARIFFS_CARGO}/`;

export const GET_ALL_TARIFFS_PACK = `/${TARIFFS_CARGO}/${PACK}/${TARIFF}`;
export const GET_PACK_CARGO = `/${TARIFFS_CARGO}/${PACK}`;
export const TARIFF_PACK_BY_ID = `${GET_ALL_TARIFFS_PACK}/${PACK_ID}`;
export const GET_TARIFF_PACK = `${GET_ALL_TARIFFS_PACK}/${PACK_ID}`;

export const CREATE_TARIFF_CARGO = `/${TARIFFS_CARGO}/:transTypeId`;
export const GET_TARIFF_CARGO = `/${TARIFFS_CARGO}/:transTypeId/:tariffId`;
export const EDIT_TARIFF_CARGO = `/${TARIFFS_CARGO}/:transTypeId/:tariffId`;
export const DELETE_TARIFF_CARGO = `/${TARIFFS_CARGO}/:transTypeId/:tariffId`;
export const GET_AUTO_GUIDE = `/${TARIFFS_CARGO}/auto`;

export const GET_DEDICATED_TARIFF_CARGO = `/${TARIFFS_CARGO}/${DEDICATED}/:tariffId`;
export const GET_COURIER_TARIFF_CARGO = `/${TARIFFS_CARGO}/${COURIER}/:tariffId`;
export const GET_INTERREGIONAL_TARIFF_CARGO = `/${TARIFFS_CARGO}/${INTERREGIONAL}/:tariffId`;
export const GET_DOMESTIC_COURIER_TARIFF_CARGO = `/${TARIFFS_CARGO}/${DOMESTIC_COURIER}/:tariffId`;
export const GET_INDIVIDUAL_TARIFF_CARGO = `/${TARIFFS_CARGO}/${INDIVIDUAL}/:tariffId`;

export const CONTRACTS = `${TARIFFS}/contracts`;
export const GET_CONTRACTS = `/${CONTRACTS}/`;
export const SEARCH_CONTRACTS = `/${CONTRACTS}/search`;
export const CONTRACT_ADD_PARAMS = `/${CONTRACTS}/`;
export const CONTRACT_PARAMS = `/${CONTRACTS}/:contractId`;
export const DELETE_CONTRACT = CONTRACT_PARAMS;

export const CONTRACTS_CARGO = `${TARIFFS_CARGO}/contracts`;
export const GET_CONTRACTS_CARGO = `/${CONTRACTS_CARGO}/`;
export const SEARCH_CONTRACTS_CARGO = `/${CONTRACTS_CARGO}/search`;
export const CONTRACT_ADD_PARAMS_CARGO = `/${CONTRACTS_CARGO}/`;
export const CONTRACT_PARAMS_CARGO = `/${CONTRACTS_CARGO}/:contractId`;
export const DELETE_CONTRACT_CARGO = CONTRACT_PARAMS_CARGO;

export const CAR_SERVICE_CONTRACT = `${TARIFF_FLEET}/contract`;
export const CAR_SERVICE_CONTRACT_BY_ID = `${CAR_SERVICE_CONTRACT}/:contractId`;
export const SEARCH_CAR_SERVICE_CONTRACT = `${CAR_SERVICE_CONTRACT}/search`;

export const DEADLINE = 'deadline';
export const DEADLINE_SETTINGS = `${DEADLINE}/:organizationId/settings`;
export const DEADLINE_SETTINGS_UPDATE = `${DEADLINE_SETTINGS}/:settingId`;
export const DEADLINE_SETTINGS_DEFAULT = `${DEADLINE_SETTINGS_UPDATE}/defaults`;

export const TRANSPORTTYPES = `/${ORGANIZATIONS}/transport-types`;
export const GET_TRANSPORT_TYPES = `/organizations/transportorg/all/org/:organizationId`;
export const SAVE_TRANSPORT_TYPES = `/organizations/transportorg/batch/org`;
export const GET_AVAILABLE_TRANSPORT_TYPES = `/organizations/transportorg/org/:organizationId`;
export const GET_AVAILABLE_TRANSPORT_TYPES_BY_SERVICE_TYPE = `/organizations/transportorg/:transportServiceTypeId/org/:organizationId`;
export const GET_TARIFF_TRANSPORT = `tariffs/transport/search`;

export const REQUESTS = 'requests';
export const REQUEST_CARGO = 'request-cargo';
export const REPORTS = 'reports';
export const CARGO_REPORTS = 'reports-cargo';
export const GET_ASYNC = 'getAsync';
export const CLOSE_ASYNC = 'closeAsync';
export const LIMITREQUESTS = 'limitrequests';
export const PURPOSES = 'purposes';
export const CONSTANTS = 'constants';
export const STATUS = 'status';
export const REPAIR = 'repair';
export const MAINTENANCE = 'maintenance';
export const WASHING = 'washing';
export const EVACUATION = 'evacuation';
export const TELEMECHANIC = 'telemechanic';
export const TIRE_SERVICE = 'tire';
export const VEHICLE = 'vehicle';
export const CLASS = 'class';

export const GET_DEPARTMENT_LEVEL = `/${REPORTS}/:orgId/department`;
export const GET_ORGANIZATION_DEPARTMENT_LEVEL = `request/external/${ORGANIZATIONS_ID}/departments/filter-registry`;

export const GET_STATUS_CODE = `/${REPORTS}/:transportType/statusCode`;

export const GET_ALL_ORGANIZATIONS_SEARCH_TERM = `${REPORTS}/search-organisation/`;

export const SAVE_REQUEST = `/${REQUESTS}/`;
export const GET_ALL_TRIP_STATUS = `/${CONSTANTS}/${STATUS}`;
export const GET_ALL_PURPOSES = `/${REQUESTS}/${PURPOSES}`;
export const GET_CAR_SHАRING_TRIP_STATUS = `/${CONSTANTS}/${STATUS}/carsharing`;
export const GET_PERSONAL_TRIP_STATUS = `/${CONSTANTS}/${STATUS}/personal-oto`;
export const GET_PUBLIC_TRIP_STATUS = `/${CONSTANTS}/${STATUS}/public-oto`;
export const GET_PUBLIC_TRIP_STATUS_REPORTS = `/${CONSTANTS}/${STATUS}/public`;
export const GET_TAXI_TRIP_STATUS = `/${CONSTANTS}/${STATUS}/taxi`;
export const GET_CARGO_TRIP_STATUS = `/${CONSTANTS}/${STATUS}/cargo`;
export const REQUEST_APPROVE_PARAMS = `/${REQUESTS}/${APPROVE}/:reqId`;
export const REQUEST_DECLINE_PARAMS = `/${REQUESTS}/${DECLINE}/:reqId`;
export const GET_PUBLIC_TRANSPORT_TYPES = `/${CONSTANTS}/public-transport-types`;
export const GET_CARGO_TRANSPORT_TYPES = `/${CONSTANTS}/cargo-transport-types`;
export const GET_REQUEST_HISTORY = `/${REQUESTS}/history/:requestId`;

export const GET_REQUEST_PARAMS = `/${REQUESTS}/:requestId`;
export const DELETE_REQUEST_PARAMS = `/${REQUESTS}/:requestId`;
export const EDIT_REQUEST_PARAMS = `/${REQUESTS}/:requestId`;
export const RATE_REQUEST_PARAMS = `/${REQUESTS}/${RATE}/:requestId`;
export const FINISH_REQUEST_PARAMS = `/${REQUESTS}/${FINISH}/:reqId`;
export const CANCEL_REQUEST_PARAMS = `/${REQUESTS}/${CANCEL}/:requestId`;
export const SEARCH_REQUESTS = `/${REQUESTS}/${SEARCH}`;
export const GET_TRIP_REQUESTS = `/${REQUESTS}/:transportType/:requestId`;
export const GET_SHARED_TRIP_REQUESTS = `/${REQUESTS}/:requestId/shared`;
export const FACT_DATA = 'fact_data';
export const GET_TRIP_REQUESTS_FACT_DATA = `/${REQUESTS}/:transportType/${FACT_DATA}/:requestId`;
export const REINTEGRATION = `${REQUESTS}/reintegration`;

export const START_TRIP = 'startTrip';
export const DRIVER_ASSIGNED = 'driver-assigned';
export const UPDATE_STATUS = `/${REQUESTS}/${STATUS}/:requestId/:status`;
export const CANCEL_STATUS = `/${REQUESTS}/${CANCEL}/:requestId`;
export const DRIVER_ASSIGNED_STATUS = `/${REQUESTS}/${DRIVER_ASSIGNED}/:requestId`;
export const REQUEST_START_TRIP = `/${REQUESTS}/${START_TRIP}`;

export const CONSTANT_TRANSPORT = `/${REQUESTS}/constant/transport`;
export const GET_TAXI_CLASSES = `${CONSTANT_TRANSPORT}/taxi/${CLASS}/`;
export const GET_GROUP_TRANSFER_CLASSES = `${CONSTANT_TRANSPORT}/transfer/${CLASS}/`;

export const PUBLIC_REPORT = `/${REPORTS}/:orgId/public_report`;
export const PERSONAL_REPORT = `/${REPORTS}/:orgId/personal_report`;
export const TAXI_REPORT = `/${REPORTS}/:orgId/taxi_report`;
export const CARGO_REPORT = `/${CARGO_REPORTS}/:orgId/cargo_report`;
export const CAR_SHARING_REPORT = `/${REPORTS}/:orgId/carsharing_report`;
export const GROUP_TRANSFER_REPORT = `/${REPORTS}/:orgId/group_transfer_report`;
export const PUBLIC_REPORT_EXECUTOR = `/${REPORTS}/public_report`;
export const PERSONAL_REPORT_EXECUTOR = `/${REPORTS}/personal_report`;
export const TAXI_REPORT_EXECUTOR = `/${REPORTS}/taxi_report`;
export const CAR_SHARING_REPORT_EXECUTOR = `/${REPORTS}/carsharing_report`;
export const GROUP_TRANSFER_REPORT_EXECUTOR = `/${REPORTS}/group_transfer_report`;

export const REPAIR_REPORT = `/${REPAIR}/report`;
export const REPAIR_ORGANIZATIONS = `/${REPAIR}/organization`;
export const REPAIR_EMPLOYEE_ORGANIZATION = `/${REPAIR}/organization/employee`;
export const REPAIR_DEPARTMENTS = `/${REPAIR}/organization/department`;
export const REPAIR_FILES = `/${REPAIR}/files`;
export const MAINTENANCE_REPORT = `/${MAINTENANCE}/report`;
export const MAINTENANCE_FILES = `/${MAINTENANCE}/files`;
export const WASHING_REPORT = `/${WASHING}/report`;
export const WASHING_FILES = `/${WASHING}/files`;
export const EVACUATION_REPORT = `/${EVACUATION}/report`;
export const EVACUATION_FILES = `/${EVACUATION}/files`;
export const TELEMECHANIC_REPORT = `/${TELEMECHANIC}/report`;
export const TELEMECHANIC_FILES = `/${TELEMECHANIC}/files`;
export const TIRE_SERVICE_REPORT = `/${TIRE_SERVICE}/report`;
export const TIRE_SERVICE_FILES = `/${TIRE_SERVICE}/files`;
export const TAXI_ANALYSIS = `/${REPORTS}/analysis/taxi`;
export const PERSONAL_ANALYSIS = `/${REPORTS}/analysis/personal`;
export const PUBLIC_ANALYSIS = `/${REPORTS}/analysis/public`;
export const CARSHARING_ANALYSIS = `/${REPORTS}/analysis/carsharing`;
export const GENERAL_ANALYSIS = `/${REPORTS}/analysis/general`;

export const DIRECTORY_BRANDS = `/${VEHICLE}/brand`;
export const DIRECTORY_BRANDS_ALL = `${DIRECTORY_BRANDS}/all`;
export const DIRECTORY_USING_TYPE = `/${VEHICLE}/type`;
export const DIRECTORY_USING_TYPE_ALL = `${DIRECTORY_USING_TYPE}/all`;
export const DIRECTORY_CATEGORIES = `/${VEHICLE}/category`;
export const DIRECTORY_CATEGORIES_ALL = `${DIRECTORY_CATEGORIES}/all`;
export const DIRECTORY_ENGINE_TYPE = `/${VEHICLE}/engine-type`;
export const DIRECTORY_ENGINE_TYPE_ALL = `${DIRECTORY_ENGINE_TYPE}/all`;
export const DIRECTORY_STATUS = `/${VEHICLE}/status`;
export const DIRECTORY_STATUS_ALL = `${DIRECTORY_STATUS}/all`;
export const DIRECTORY_WHEEL_DRIVE = `/${VEHICLE}/drive`;
export const DIRECTORY_WHEEL_DRIVE_ALL = `${DIRECTORY_WHEEL_DRIVE}/all`;
export const DIRECTORY_USING_SUB_TYPE = `/${VEHICLE}/subtype`;
export const DIRECTORY_USING_SUB_TYPE_ALL = `${DIRECTORY_USING_SUB_TYPE}/all`;
export const DIRECTORY_FUEL_TYPES = `/${VEHICLE}/fuel-type`;
export const DIRECTORY_FUEL_TYPES_ALL = `${DIRECTORY_FUEL_TYPES}/all`;
export const DIRECTORY_MODELS = `/${VEHICLE}/model`;
export const DIRECTORY_MODELS_ALL = `${DIRECTORY_MODELS}/all`;
export const DIRECTORY_CARS = `/${VEHICLE}/${VEHICLE}`;
export const DIRECTORY_CARS_ALL = `${DIRECTORY_CARS}/all`;

export const UPLOADFILE = `/${ORGANIZATIONS}/dataimport/load/:orgId/:strategyMode/:nsi/:decSeparator/:separator`;
export const PRELOADFILE = `/${ORGANIZATIONS}/dataimport/preload/:orgId/:strategyMode/:nsi/:decSeparator/:separator`;

export const GET_CANDIDATES_IN_DELEGATES_PARAMS = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/${CANDIDATES}/:supId/:transTypeId`;
export const GET_DELEGATES = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/${SUPERVISORS}/:supId`;
export const ADD_DELEGATE = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/`;

export const DELETE_DELEGATE = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/:delId`;

export const LIMITREQUESTS_CRUD = `/limits/${LIMITREQUESTS}`;
export const LIMITREQUESTS_PARAMS = `/limits/${LIMITREQUESTS}/:reqId`;
export const LIMITREQUESTS_CANCEL_PARAMS = `/limits/${LIMITREQUESTS}/${CANCEL}/:reqId`;
export const LIMITREQUESTS_APPROVE_PARAMS = `/limits/${LIMITREQUESTS}/${APPROVE}/:reqId`;
export const LIMITS_IMPORT = '/limits/limits/import/:orgId/:separator';
export const LIMITS_IMPORT_SIMULATION = '/limits/limits/import/simulation/:orgId/:separator';
export const LIMITS_SEARCH = `/${LIMITS}/${LIMITS}/searchPageable/:orgId`;

export const GET_LIMIT_AUDIT = `/auditAsync/org/:orgId/year/:year`;
export const GET_LIMIT_CLOSE = `/${LIMITS}/${LIMITS}/${CLOSE_ASYNC}/org/:orgId/limit/:limitId`;
export const GET_LIMIT_ASYNC = `/${LIMITS}/${LIMITS}/${GET_ASYNC}`;
export const GET_LIMITS_EXPORT = `/exportAsync/org/:orgId/yaer/:year`;
export const GET_LIMIT_BY_HRID = `/${LIMITS}/${LIMITS}/getByHumanReadableId/:humanReadableId`;
export const LIMITS_STATS_GENERAL = `/${LIMITS}/statistic`;

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

export const CONTRACTORS = 'contractors';
export const GET_ALL_CONTRACTORS = `/${CONTRACTORS}/`;
export const GET_CONTRACTOR_DISPATCHERS = `/${CONTRACTORS}/:contractorId/dispatcher/`;
export const GET_CONTRACTOR_DISPATCHER = `${GET_CONTRACTOR_DISPATCHERS}:dispId/`;
export const DELETE_CONTRACTOR_DISPATCHER = `/${CONTRACTORS}/:contractorId/dispatcher/`;
export const TRANSPORT_SERVICE_TYPES = `/${TARIFFS}/transport-service-type`;
export const TRANSPORT_SERVICE_TYPES_CARGO = `/${TARIFFS_CARGO}/transport-service-type`;
export const CREATE_TARIFF = `/${TARIFFS}/:transTypeId`;

export const SHARED_RIDE = 'shared_ride';
export const SEARCH_SHARED_RIDES = `/${ORGANIZATIONS}/:orgId/${SHARED_RIDE}/plan_order`;

export const SETTINGS = 'settings';
export const NOTIFICATIONS = 'notifications';
export const MASS = 'mass';
export const GET_ALL_NOTIFICATIONS_SETTINGS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}/`;
export const GET_NOTIFICATIONS_SETTINGS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}/:notId`;
export const CREATE_NOTIFICATIONS_SETTINGS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}`;
export const UPDATE_NOTIFICATIONS_SETTINGS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}/:notId`;
export const DELETE_NOTIFICATIONS_SETTINGS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}/:notId`;
export const CREATE_NOTIFICATIONS_SETTINGS_MASS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}/${MASS}`;
export const UPDATE_NOTIFICATIONS_SETTINGS_MASS = `/${NOTIFICATIONS}/:orgId/${SETTINGS}/${MASS}`;
export const SHARED_RIDE_DETAILED = `/:requestId/shared`;
export const COMPENSATION = 'compensation';
export const GET_ALL_COMPENSATIONS = `/${TARIFFS}/public/${COMPENSATION}/type`;
export const GET_PUBLIC_COMPENSATIONS = `/${CONSTANTS}/public-compensation-types`;

export const USERS = 'users';
export const USERS_PARAMS = `${USERS}`;
export const GET_ALL_USERS_ATTRIBUTES = `/${REPORTS}/attributes`;
export const GET_CARGO_USERS_ATTRIBUTES = `/${CARGO_REPORTS}/:userId/attributes`;
export const SAVE_PERSONAL_USERS_ATTRIBUTES = `${GET_ALL_USERS_ATTRIBUTES}/personal`;
export const GET_REGISTRY_REPORT = `/reports/files/trip-requests-`;
export const GET_CARGO_REGISTRY_REPORT = `/${CARGO_REPORTS}/:orgId/xlsx/trip-requests/cargo`;
export const GET_CSE_REGISTRY_REPORT = `/${CARGO_REPORTS}/:orgId/xlsx/trip-requests/cse`; // для скачивания kce
export const GET_PAYMENT_REPORT = `/reports/files/payment-report-`;
export const GET_DEFAULT_USERS_ATTRIBUTES = `/reports/default/attributes`;
export const GET_DEFAULT_CARGO_USERS_ATTRIBUTES = `/${CARGO_REPORTS}/default/attributes`;
export const GET_USER_AVATAR = `/${USERS}/avatar/:userId`;

export const GET_PUBLIC_BUSINESS_REPORTS = `/${REPORTS}/files/trip-requests-public-analytics`;
export const GET_PERSONAL_BUSINESS_REPORTS = `/${REPORTS}/files/trip-requests-personal-analytics`;
export const GET_GROUP_TRANSFER_BUSINESS_REPORTS = `/${REPORTS}/files/trip-requests-group_transfer-analytics`;
export const GET_CARSHARING_BUSINESS_REPORTS = `/${REPORTS}/files/trip-requests-carsharing-analytics`;
export const GET_TAXI_BUSINESS_REPORTS = `/${REPORTS}/files/trip-requests-taxi-analytics`;

export const GET_TAXI_REGISTRY = `${REPORTS}/xls/import/taxi/registry`;
export const GET_TAXI_REGISTRY_FROM_SERVER = `${GET_TAXI_REGISTRY}/download/:contractorId/:year/:month`;

export const GET_REPAIR_REGISTRY_REPORT = `${REPAIR_FILES}/registry`;
export const GET_MAINTENANCE_REGISTRY_REPORT = `${MAINTENANCE_FILES}/registry`;
export const GET_WASHING_REGISTRY_REPORT = `${WASHING_FILES}/registry`;
export const GET_EVACUATION_REGISTRY_REPORT = `${EVACUATION_FILES}/registry`;
export const GET_TELEMECHANIC_REGISTRY_REPORT = `${TELEMECHANIC_FILES}/registry`;
export const GET_TIRE_SERVICE_REGISTRY_REPORT = `${TIRE_SERVICE_FILES}/registry`;

export const ROUTE = 'route';

// monitor
export const V2 = 'v2';
export const OTO = 'oto';
export const REQUESTS_OTO_FEED_PERSON_BY_ORG_ID = `${OTO}/${PERSON}/:organizationId`;
export const REQUESTS_OTO_FEED_PERSON = `${OTO}/${PERSON}/`;
export const GET_FEED_BY_REQUEST_ID = `${OTO}/:requestId`;
export const REQUESTS_OTO_FEED_CARGO_BY_ORG_ID = `${OTO}/${CARGO}/:organizationId`;

export const REQUESTS_CARGO = `request-cargo`;
export const REQUESTS_CARGO_ACTIVE = `${REQUESTS_CARGO}/${CARGO}`;
export const REQUESTS_CARGO_ACTIVE_BY_ORG_ID = `${REQUESTS_CARGO_ACTIVE}/${V2}/${OTO}/:requestId`;
export const REQUESTS_CARGO_ACTIVE_BY_MULTIPLE_ORG_ID = `${V2}/${REQUESTS_CARGO_ACTIVE}/${OTO}/:requestId`;
export const HOME_CLICK = 'home-click';
export const UPDATE_CARGO_HOME_CLICK_STATUS = `${HOME_CLICK}/:requestId/${STATUS}/:status`;
export const REQUESTS_CARGO_ACTIVE_BY_SOURCE_HOME_CLICK = `${HOME_CLICK}/${V2}/${OTO}/:requestId`;
export const CREATE_CARGO_ENGINEER_COMMENT = `${REQUESTS_CARGO_ACTIVE}/${OTO}/:requestId/`;
export const REQUESTS_OTO_ROUTES = `${REQUESTS_CARGO}/${CARGO}/${ROUTE}/${OTO}/${SEARCH}`;
export const REQUESTS_OTO_ROUTE = `${REQUESTS_CARGO_ACTIVE}/${ROUTE}/${OTO}/:requestId`;

// approvals
export const APPROVALS = 'approvals';
export const APPROVALS_SETTINGS_TAXI = `${APPROVALS}/:orgId/${SETTINGS}/request/taxi`;
export const APPROVALS_SETTINGS_TAXI_UPDATE = `${APPROVALS}/:orgId/${SETTINGS}/request/taxi/:apprId`;
export const APPROVALS_SETTINGS_PUBLIC = `${APPROVALS}/:orgId/${SETTINGS}/request/public`;
export const APPROVALS_SETTINGS_PUBLIC_UPDATE = `${APPROVALS}/:orgId/${SETTINGS}/request/public/:apprId`;
export const APPROVALS_SETTINGS_GROUP_TRANSFER = `${APPROVALS}/:orgId/${SETTINGS}/request/group_transfer`;
export const APPROVALS_SETTINGS_GROUP_TRANSFER_UPDATE = `${APPROVALS}/:orgId/${SETTINGS}/request/group_transfer/:apprId`;
export const APPROVALS_SETTINGS_OTHER_POST = `${APPROVALS}/:orgId/${SETTINGS}/request/other`;
export const APPROVALS_SETTINGS_OTHER_GET_PUT = `${APPROVALS}/:orgId/${SETTINGS}/request/other/:transType`;

export const APPROVALS_SETTINGS_TAXI_DEFAULT = `${APPROVALS}/:orgId/${SETTINGS}/request/taxi/:apprId/restore`;
export const APPROVALS_SETTINGS_PUBLIC_DEFAULT = `${APPROVALS}/:orgId/${SETTINGS}/request/public/:apprId/restore`;
export const APPROVALS_SETTINGS_GROUP_TRANSFER_DEFAULT = `${APPROVALS}/:orgId/${SETTINGS}/request/group_transfer/:apprId/restore`;
export const APPROVALS_SETTINGS_OTHER_DEFAULT = `${APPROVALS}/:orgId/${SETTINGS}/request/other/:transType/restore`;

export const APPROVALS_SETTINGS_TRANSPORT_TYPE = `${APPROVALS}/:orgId/${SETTINGS}/request/:transType`;

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

export const CARGO_PACKAGES = `packages`;
export const CARGO_PACKAGE = `package`;
export const GET_ALL_CARGO_PACKAGE = `${CONTRACTORS}/:contractorId/${CARGO}/${CARGO_PACKAGES}`;
export const CREATE_CARGO_PACKAGE = `${CONTRACTORS}/:contractorId/${CARGO}/${CARGO_PACKAGE}`;
export const UPDATE_CARGO_PACKAGE = `${CONTRACTORS}/:contractorId/${CARGO}/${CARGO_PACKAGE}/:packageId`;
export const DELETE_CARGO_PACKAGE = `${CONTRACTORS}/:contractorId/${CARGO}/${CARGO_PACKAGE}/:packageId`;

export const CARGO_TYPES = `types`;
export const CARGO_TYPE = `type`;
export const CARGO_TYPE_ID = `:cargoTypeId`;
export const GET_ALL_CARGO_TYPES = `${ORGANIZATIONS}/${CARGO}/${CARGO_TYPE}`;
export const GET_CARGO_TYPE = `${ORGANIZATIONS}/${CARGO}/${CARGO_TYPE}/${CARGO_TYPE_ID}`;

export const CREATE_CARGO_TYPE = GET_ALL_CARGO_TYPES;
export const UPDATE_CARGO_TYPE = GET_CARGO_TYPE;
export const DELETE_CARGO_TYPE = GET_CARGO_TYPE;

export const CARGO_AUTO = `auto`;
export const CARGO_AUTO_ID = `:autoId`;
export const GET_ALL_CARGO_AUTO = `${TARIFFS_CARGO}/${CARGO_AUTO}/`;
export const GET_CARGO_AUTO = `${TARIFFS_CARGO}/${CARGO_AUTO}/${CARGO_AUTO_ID}`;
export const GET_CAPACITY_CARGO_AUTO = `${TARIFFS_CARGO}/${CARGO_AUTO}/capacity`;

export const CREATE_CARGO_AUTO = GET_ALL_CARGO_AUTO;
export const UPDATE_CARGO_AUTO = GET_CARGO_AUTO;
export const DELETE_CARGO_AUTO = GET_CARGO_AUTO;

export const GET_ALL_CARGO_TYPE_NAMES = `${ORGANIZATIONS}/${CARGO}/${CARGO_TYPE}/${CARGO_TYPES}`;

export const CARGO_DELIVERY_TIME_SETTINGS = `tariff-cargo/deliverytime`;

export const GET_CARGO_REQUEST_STATUS = `/${CONSTANTS}/${STATUS}/${CARGO}`;
export const UPDATE_CARGO_REQUEST_STATUS = `/${REQUEST_CARGO}/${CARGO}/:requestId/${STATUS}/:status`;
export const CARGO_REQUEST_COMPLETE_TRANSFER = `/${REQUEST_CARGO}/${CARGO}/:requestId/complete/transfer/:transferTime`;
export const CARGO_HOME_CLICK_REQUEST_COMPLETE_TRANSFER = `/${HOME_CLICK}/:requestId/complete/transfer/:transferTime`;
export const CARGO_REQUEST_COMPLETE_SHIPMENT = `/${REQUEST_CARGO}/${CARGO}/:requestId/complete/shipment/:shipmentTime`;
export const CARGO_REQUEST_HOME_CLICK_COMPLETE_SHIPMENT = `/${HOME_CLICK}/:requestId/complete/shipment/:shipmentTime`;

export const GET_INTEGRATION_LIST_TAXI = `/${CONSTANTS}/taxi-integration-types`;

export const EXPORT_STATS = 'exportstats';

export const SRM = 'srm';
export const TEST = 'test';
export const SEND = 'send';
export const CONTRACTOR = 'contractor';
export const ADD_CARGO = 'add';
export const PLANNER_REQUEST_CARGO = 'request-cargo';
export const PLANNER_SEARCH_ROUTE = 'searchRoute';
export const DELETE_WAYPOINT = 'waypoint';
export const AUTO_CONTRACTOR = 'autoContractor';
export const AUTO_PLANNING = `/${REQUESTS_CARGO}/${CARGO}/${SRM}/${TEST}`;
export const PLANNER_CREATE_ROUTE = `/${PLANNER_REQUEST_CARGO}/${CARGO}/${ROUTE}`;
export const PLANNER_GET_ROUTE = `/${PLANNER_REQUEST_CARGO}/${CARGO}/${ROUTE}/:routeId`;
export const PLANNER_DELETE_ROUTE = `/${PLANNER_REQUEST_CARGO}/${CARGO}/${ROUTE}/:routeId`;
export const PLANNER_DELETE_ADDRESS_POINT = `/${PLANNER_REQUEST_CARGO}/${CARGO}/${ROUTE}/${DELETE_WAYPOINT}/:waypointId`;
export const PLANNER_GET_ORDER = `/${PLANNER_REQUEST_CARGO}/${CARGO}/:orderId`;
export const PLANNER_ADD_ORDER_TO_ROUTE = `/${PLANNER_REQUEST_CARGO}/${CARGO}/${ROUTE}/${ADD_CARGO}`;
export const PLANNER_UPDATE_ROUTE_STATUS = `/${PLANNER_REQUEST_CARGO}/${CARGO}/${ROUTE}/:routelistId/${STATUS}/:status`;
export const PLANNER_SEARCH_ORDERS = `/${PLANNER_REQUEST_CARGO}/${CARGO}/${ROUTE}/${SEARCH}`;
export const PLANNER_SEARCH_ROUTES = `/${PLANNER_REQUEST_CARGO}/${CARGO}/${ROUTE}/${PLANNER_SEARCH_ROUTE}`;
export const PLANNER_GET_CONTRACTORS = `/${TARIFFS_CARGO}/${DEDICATED}/${AUTO_CONTRACTOR}?regionId=:regionId&organizationId=:organizationId&desiredDate=:desiredDate`;
export const PLANNER_SEND_TO_CONTRACTOR = `${REQUEST_CARGO}/${CARGO}/${ROUTE}/:routeId/${SEND}/${CONTRACTOR}`;

export const USER_AGENT = '/user-agent/';

export const QUANTITY = `${USER_AGENT}quantity/`;

// телемеханик - монитор заявок
export const MONITORING = 'monitoring';
export const TELEMECHANIC_PHOTO = 'photo';
export const TELEMECHANIC_GET_ORDERS = `/${TELEMECHANIC}/${MONITORING}`;
export const TELEMECHANIC_GET_ORDER = `/${TELEMECHANIC}/${MONITORING}/:requestId`;
export const TELEMECHANIC_UPDATE_ORDER = `/${TELEMECHANIC}/${MONITORING}`;
export const TELEMECHANIC_GET_PHOTO = `/${TELEMECHANIC}/${MONITORING}/${TELEMECHANIC_PHOTO}/:photoId`;

// Проверка реестров
export const REGISTRY_SERVICE = '/registry';
export const REGISTRY = `${REGISTRY_SERVICE}/`; // слэш в конце, отдельная константа, чтобы не менять везде середину
export const REGISTRY_SAMPLE = `${REGISTRY_SERVICE}/sample`;
export const REGISTRY_ID = `${REGISTRY_SERVICE}/:registryId`;
export const REGISTRY_CHECK = `${REGISTRY_SERVICE}/:registryId/check`;
export const REGISTRY_AGREEMENT = `${REGISTRY_SERVICE}/:registryId/agreement`;
export const REGISTRY_SEND = `${REGISTRY_SERVICE}/:registryId/send`;

// Fuel
export const PARKING = 'parking';
export const POST_FUEL_CONSUMPTION = `/${PARKING}/predict/gsm`;

// настройки колонок таблицы для реестров
export const GET_UI_PREFERENCES = `reports/users/ui-preferences/`;
export const SET_UI_PREFERENCES = `reports/users/ui-preferences`;

// настройки колонок таблицы для монитора исполнения
export const GET_UI_PREFERENCES_OTO = `oto/users/ui-preferences/`;
export const SET_UI_PREFERENCES_OTO = `oto/users/ui-preferences`;

// Мониторинг нарушений
export const FRAUD_MONITORING = 'fraud-monitoring';
export const FRAUD_MONITORING_REPORT = `${MOCKED_API_PREFIX}/${FRAUD_MONITORING}/report`;
export const FRAUD_MONITORING_DETAILS = `${MOCKED_API_PREFIX}/${FRAUD_MONITORING}/:requestId`;
