import { LIMIT_REQUEST_STATUS } from 'stores/Limits/Limit.interface';

export const isInitRequest = (status: LIMIT_REQUEST_STATUS | string): boolean => status === LIMIT_REQUEST_STATUS.INIT;

export const isRequestCancelled = (status: LIMIT_REQUEST_STATUS | string): boolean => status === LIMIT_REQUEST_STATUS.CANCELLED;
