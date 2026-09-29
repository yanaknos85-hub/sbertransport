import { AxiosError } from 'axios';
import { CustomErrorCode } from 'constants/app.constants';

/**
 * Определяет код ошибки на основе объекта ошибки (Axios или普通 Error)
 * @param e - объект ошибки
 * @returns числовой код ошибки (статус код, TIMEOUT, CORS, TYPES, UNDEFINED_FIELD, UNKNOWN)
 */
export const getErrorCode = (e: unknown): number => {
  const axiosError = e as AxiosError;

  if (axiosError.isAxiosError) {
    if (axiosError.response?.status) {
      return axiosError.response.status;
    }

    if (axiosError.code === 'ECONNABORTED' || axiosError.message.includes('timeout')) {
      return CustomErrorCode.TIMEOUT;
    }
  }

  const error = e as Error;

  if (error.message.includes('Network Error')) {
    return CustomErrorCode.CORS;
  }

  if (error.message.includes('Expecting')) {
    return CustomErrorCode.TYPES;
  }

  if (error.message.includes('Cannot read')) {
    return CustomErrorCode.UNDEFINED_FIELD;
  }

  return CustomErrorCode.UNKNOWN;
};
