export const MAIN = '/client'; // разводная точка маршрутизации между MF
export const AUTH = '/oauth';

export const MF_PLATFORM = `${MAIN}/platform`;
export const MF_PASSENGERS = `${MAIN}/passengers`;
export const MF_CARGO = `${MAIN}/cargo`;
export const MF_FLEET = `${MAIN}/fleet`;

export const HOME = `${MAIN}/home`;
export const REDESIGN_HOME = `${MAIN}/home2.0`;
export const PAGE = `${MAIN}/page`;
export const PAGE_404 = `${MAIN}/404`;
export const IMPORT_REPORT = `${MAIN}/import-report/:entity`;

export const ENGINEERS = `${MAIN}/engineers`;

export const ORDER_EXECUTION = `${MAIN}/order-execution`;
export const ORDER_EXECUTION_PASSENGERS = `${ORDER_EXECUTION}/passengers`;
export const ORDER_EXECUTION_CAR_SERVICE = `${ORDER_EXECUTION}/carService`;
export const ORDER_EXECUTION_PARKING = `${ORDER_EXECUTION}/parking`;

export const PLANNER_SRM = `${MAIN}/planner`;

export const REPORTS = `${MAIN}/reports`;

export const ANALYTICS = `${REPORTS}/analytics`;
export const ANALYTICS_V2 = `${REPORTS}/analytics2.0`;

export const REGISTRY = `${REPORTS}/registry`;

export const REGISTRY_PASSENGERS = `${REGISTRY}/passengers`;

export const REGISTRY_PASSENGERS_PUBLIC = `${REGISTRY_PASSENGERS}/public`;

export const BUSINESS_REPORTS = `${REPORTS}/business-reports`;
export const BUSINESS_REPORTS_PASSENGERS = `${BUSINESS_REPORTS}/passengers`;
export const BUSINESS_REPORTS_PASSENGERS_PUBLIC = `${BUSINESS_REPORTS_PASSENGERS}/public`;

export const FRAUD_MONITORING = `${REPORTS}/fraud-monitoring`;

export const TRIP_SETTINGS = `${MAIN}/trip-settings`;

export const SERVICE_TYPES = `${TRIP_SETTINGS}/service-types`;
export const PURPOSES = `${TRIP_SETTINGS}/purposes`;
export const APPROVALS = `${TRIP_SETTINGS}/approvals`;
export const SHARED_RIDES = `${TRIP_SETTINGS}/shared-rides`;

export const MANAGE_EMPLOYEES = `${MAIN}/platform/directories/employees`;

export const ANALYTICAL_REPORTING = `${MAIN}/reports/analytics`;

export const CORPORATE_ADDRESSES = `${MAIN}/platform/directories/locations`;

export const SERVICE_SETTINGS = `${MAIN}/service-settings`;

export const DEADLINES = `${SERVICE_SETTINGS}/deadlines`;
export const NOTIFICATIONS = `${SERVICE_SETTINGS}/notifications`;
export const REFERENCE_BOOKS = `${SERVICE_SETTINGS}/reference-books`;

export const TARIFF_SETTINGS = `${MAIN}/tariff-settings`;

export const CONTRACTORS = `${TARIFF_SETTINGS}/contractors`;
export const CONTRACTS = `${TARIFF_SETTINGS}/contracts`;
export const TARIFFS = `${TARIFF_SETTINGS}/tariffs`;

export const LIMITS_SETTINGS = `${MF_PLATFORM}/limits-new/distrib`;

export const CUSTOMERS = `${MAIN}/customers`;
export const CUSTOMERS_ORGANIZATIONS = `${CUSTOMERS}/organizations`;
export const CUSTOMERS_ORGANIZATIONS_GROUPS = `${CUSTOMERS_ORGANIZATIONS}/groups`;
export const CUSTOMERS_ORGANIZATION = `${CUSTOMERS_ORGANIZATIONS}/:id`;
export const CUSTOMERS_CONTRACTORS = `${CUSTOMERS}/contractors`;
export const CUSTOMERS_CONTRACTS = `${CUSTOMERS}/contracts`;
export const CUSTOMERS_TARIFFS = `${CUSTOMERS}/tariffs`;
export const CUSTOMERS_MUTUALITY = `${CUSTOMERS}/mutuality`;
export const CUSTOMERS_ANALYTICS = `${CUSTOMERS}/analytics`;

export const AUTOPARKS = `${MAIN}/autoparks`;
export const AUTOPARKS_TAB = `${MAIN}/autoparks/:autoparkTab`;

// Грузовые перевозки — константы для селектора грузов
export const ORDER_EXECUTION_CARGO = `${ORDER_EXECUTION}/cargo`;
export const ORDER_EXECUTION_CARGO_TEMPLATE = `${ORDER_EXECUTION_CARGO}/template`;
export const REGISTRY_CARGO_ORDERS = `${REGISTRY}/cargo/orders`;
export const REGISTRY_CARGO_ROUTES = `${REGISTRY}/cargo/routes`;
export const MULTI_LOGISTICS = `${MF_CARGO}/multi-logistics`;
