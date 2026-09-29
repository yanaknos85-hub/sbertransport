// EXTERNAL юзается если точно такие же пути должны быть в мейне
// такие исключение делаются, если роутинг обрабатывается на самом мейне, а не тут - в ремоуте

export const EXTERNAL = '/client'; // разводная точка маршрутизации между MF

export const APP = '/cargo'; // главная точка маршрутизации приложения

export const MAIN = `${EXTERNAL}${APP}`;

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

export const ORDER_EXECUTION = `${EXTERNAL}/order-execution`;
export const ORDER_EXECUTION_CARGO = `${ORDER_EXECUTION}/cargo`;

export const REPORTS = `${EXTERNAL}/reports`;

export const ANALYTICS = `${REPORTS}/analytics`;

export const REGISTRY = `${REPORTS}/registry`;

export const BUSINESS_REPORTS = `${REPORTS}/business-reports`;
export const BUSINESS_REPORTS_CARGO = `${BUSINESS_REPORTS}/cargo`;

export const REGISTRY_CARGO = `${REGISTRY}/cargo`;
export const REGISTRY_CARGO_VIEW = `${REGISTRY_CARGO}/:id`;

export const REGISTRY_CARGO_ORDERS = `${REGISTRY_CARGO}/orders`;
export const REGISTRY_CARGO_ORDERS_VIEW = `${REGISTRY_CARGO}/orders:id`;

export const REGISTRY_CARGO_ROUTES = `${REGISTRY_CARGO}/routes`;
export const REGISTRY_CARGO_ROUTES_VIEW = `${REGISTRY_CARGO}/routes:id`;

export const REGISTRY_CARGO_COMPENSATIONS = `${REGISTRY_CARGO}/compensations`;
export const REGISTRY_CARGO_COMPENSATIONS_VIEW = `${REGISTRY_CARGO}/compensations:id`;

export const REGISTRY_CARGO_COMPENSATIONS_REPORTS = `${REGISTRY_CARGO}/compensations-reports`;

export const TRIP_SETTINGS = `${EXTERNAL}/trip-settings`;

export const SERVICE_TYPES = `${TRIP_SETTINGS}/service-types`;
export const PURPOSES = `${TRIP_SETTINGS}/purposes`;
export const APPROVALS = `${TRIP_SETTINGS}/approvals`;
export const SHARED_RIDES = `${TRIP_SETTINGS}/shared-rides`;

export const SERVICE_SETTINGS = `${EXTERNAL}/service-settings`;

export const DEADLINES = `${SERVICE_SETTINGS}/deadlines`;
export const NOTIFICATIONS = `${SERVICE_SETTINGS}/notifications`;
export const REFERENCE_BOOKS = `${SERVICE_SETTINGS}/reference-books`;
export const AUTO_DIRECTORY = `${SERVICE_SETTINGS}/reference-books/cargo/cargoAutoSettings`;

export const TARIFF_SETTINGS = `${EXTERNAL}/tariff-settings`;

export const CONTRACTORS = `${TARIFF_SETTINGS}/contractors`;
export const CONTRACTS = `${TARIFF_SETTINGS}/contracts`;
export const TARIFFS = `${TARIFF_SETTINGS}/tariffs`;

export const MULTI_LOGISTICS = `${MAIN}/multi-logistics`;
export const PLANNER = `${MULTI_LOGISTICS}/planner`;
export const PLANNER_ROUTE_CREATE = `${PLANNER}/route-create`;
export const PLANNER_ROUTE_LIST = `${PLANNER}/route-list`;
export const PLANNER_ROUTE_DETAILED = `${PLANNER}/route-detailed`;
export const PLANNER_ORDER_DETAILED = `${PLANNER}/order-detailed`;
export const PLANNER_JOURNAL_DETAILED = `${MULTI_LOGISTICS}/journal`;

export const PLANNER_JOURNAL = `${MULTI_LOGISTICS}`;

export const ETRN_SIGNING = `${MULTI_LOGISTICS}/etrn-signing`;

export const MULTI_LOGISTICS_ETRN_TAB = `${MULTI_LOGISTICS}?tab=etrn`;

