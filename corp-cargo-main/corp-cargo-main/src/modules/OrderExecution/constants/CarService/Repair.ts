import { MOCKED_API_PREFIX } from 'constants/constants.env';
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
