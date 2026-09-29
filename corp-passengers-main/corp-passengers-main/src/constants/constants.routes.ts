// EXTERNAL юзается если точно такие же пути должны быть в мейне
// такие исключение делаются, если роутинг обрабатывается на самом мейне, а не тут - в ремоуте

export const EXTERNAL = '/client'; // разводная точка маршрутизации между MF

export const APP = '/passengers'; // главная точка маршрутизации приложения

export const PLATFORM = '/platform';

export const MAIN = `${EXTERNAL}${APP}`;

export const MAIN_PLATFORM = `${EXTERNAL}${PLATFORM}`;

export const AUTH = '/oauth';

export const HOME = `${MAIN}/home`;
export const PAGE = `${MAIN}/page`;
export const PAGE_404 = `${MAIN}/404`;

export const DIRECTORIES = `${MAIN}/directories`;

export const EMPLOYEES = `${DIRECTORIES}/employees`;
export const EMPLOYEE = `${EMPLOYEES}/:id/departments/:id`;
export const EMPLOYEE_ADD = `${EMPLOYEES}/add`;

export const DEPARTMENTS = `${DIRECTORIES}/departments`;
export const DEPARTMENT = `${DEPARTMENTS}/:id`;

export const LOCATIONS = `${DIRECTORIES}/locations`;
export const LOCATION = `${LOCATIONS}/:id`;

export const ORGANIZATIONS = `${DIRECTORIES}/organizations`;
export const ORGANIZATIONS_GROUPS = `${ORGANIZATIONS}/groups`;
export const ORGANIZATION = `${ORGANIZATIONS}/:id`;

export const ROLES = `${DIRECTORIES}/roles`;
export const ROLE = `${ROLES}/:id`;

export const EMPLOYEE_ATTRIBUTES = `${DIRECTORIES}/attributes`;

export const EMPLOYEE_POSITIONS = `${DIRECTORIES}/positions`;
export const EMPLOYEE_POSITION = `${EMPLOYEE_POSITIONS}/:id`;

export const ENGINEERS = `${MAIN}/engineers`;

export const EXECUTOR_GROUPS = `${EXTERNAL}/executor-group`;

export const PLANNER_SRM = `${EXTERNAL}/planner`;

export const ORDER_EXECUTION = `${EXTERNAL}/order-execution`;
export const ORDER_EXECUTION_PASSENGERS = `${ORDER_EXECUTION}/passengers`;

export const REPORTS = `${EXTERNAL}/reports`;

export const ANALYTICS = `${REPORTS}/analytics`;

export const REGISTRY = `${REPORTS}/registry`;

export const BUSINESS_REPORTS = `${REPORTS}/business-reports`;

export const REGISTRY_PASSENGERS = `${REGISTRY}/passengers`;
export const BUSINESS_REPORTS_PASSENGERS = `${BUSINESS_REPORTS}/passengers`;

export const REGISTRY_PASSENGERS_PUBLIC = `${REGISTRY_PASSENGERS}/public`;
export const REGISTRY_PASSENGERS_PUBLIC_VIEW = `${REGISTRY_PASSENGERS_PUBLIC}/:id`;

export const REGISTRY_PASSENGERS_PERSONAL = `${REGISTRY_PASSENGERS}/personal`;
export const REGISTRY_PASSENGERS_PERSONAL_VIEW = `${REGISTRY_PASSENGERS_PERSONAL}/:id`;

export const REGISTRY_PASSENGERS_TAXI = `${REGISTRY_PASSENGERS}/taxi`;
export const REGISTRY_PASSENGERS_TAXI_VIEW = `${REGISTRY_PASSENGERS_TAXI}/:id`;

export const REGISTRY_PASSENGERS_CARSHARING = `${REGISTRY_PASSENGERS}/carsharing`;
export const REGISTRY_PASSENGERS_CARSHARING_VIEW = `${REGISTRY_PASSENGERS_CARSHARING}/:id`;

export const REGISTRY_PASSENGERS_GROUP_TRANSFER = `${REGISTRY_PASSENGERS}/group_transfer`;
export const REGISTRY_PASSENGERS_GROUP_TRANSFER_VIEW = `${REGISTRY_PASSENGERS_GROUP_TRANSFER}/:id`;

export const REGISTRY_PASSENGERS_YANDEX_TAXI = `${REGISTRY_PASSENGERS}/yandex-taxi`;
export const REGISTRY_PASSENGERS_YANDEX_TAXI_VIEW = `${REGISTRY_PASSENGERS_YANDEX_TAXI}/:id`;

export const BUSINESS_REPORTS_PASSENGERS_PUBLIC = `${BUSINESS_REPORTS_PASSENGERS}/public`;
export const BUSINESS_REPORTS_PASSENGERS_PUBLIC_VIEW = `${BUSINESS_REPORTS_PASSENGERS_PUBLIC}/:id`;

export const BUSINESS_REPORTS_PASSENGERS_PERSONAL = `${BUSINESS_REPORTS_PASSENGERS}/personal`;
export const BUSINESS_REPORTS_PASSENGERS_PERSONAL_VIEW = `${BUSINESS_REPORTS_PASSENGERS_PERSONAL}/:id`;

export const BUSINESS_REPORTS_PASSENGERS_TAXI = `${BUSINESS_REPORTS_PASSENGERS}/taxi`;
export const BUSINESS_REPORTS_PASSENGERS_TAXI_VIEW = `${BUSINESS_REPORTS_PASSENGERS_TAXI}/:id`;

export const BUSINESS_REPORTS_PASSENGERS_CARSHARING = `${BUSINESS_REPORTS_PASSENGERS}/carsharing`;
export const BUSINESS_REPORTS_PASSENGERS_CARSHARING_VIEW = `${BUSINESS_REPORTS_PASSENGERS_CARSHARING}/:id`;

export const BUSINESS_REPORTS_PASSENGERS_GROUP_TRANSFER = `${BUSINESS_REPORTS_PASSENGERS}/group_transfer`;
export const BUSINESS_REPORTS_PASSENGERS_GROUP_TRANSFER_VIEW = `${BUSINESS_REPORTS_PASSENGERS_GROUP_TRANSFER}/:id`;

export const FRAUD_MONITORING = `${REPORTS}/fraud-monitoring`;
export const FRAUD_MONITORING_DETAILS = `${FRAUD_MONITORING}/:id`;

export const TRIP_SETTINGS = `${EXTERNAL}/trip-settings`;

export const SERVICE_TYPES = `${TRIP_SETTINGS}/service-types`;
export const PURPOSES = `${TRIP_SETTINGS}/purposes`;
export const APPROVALS = `${TRIP_SETTINGS}/approvals`;
export const SHARED_RIDES = `${TRIP_SETTINGS}/shared-rides`;

export const SERVICE_SETTINGS = `${EXTERNAL}/service-settings`;

export const DEADLINES = `${SERVICE_SETTINGS}/deadlines`;
export const NOTIFICATIONS = `${SERVICE_SETTINGS}/notifications`;
export const REFERENCE_BOOKS = `${SERVICE_SETTINGS}/reference-books`;

export const TARIFF_SETTINGS = `${EXTERNAL}/tariff-settings`;
export const CONTRACTORS = `${TARIFF_SETTINGS}/contractors`;
export const CONTRACTS = `${TARIFF_SETTINGS}/contracts`;
export const TARIFFS = `${TARIFF_SETTINGS}/tariffs`;
