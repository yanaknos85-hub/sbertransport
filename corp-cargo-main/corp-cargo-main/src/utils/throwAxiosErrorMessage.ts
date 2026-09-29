import { AxiosError } from 'axios';
import { getErrorMessage } from './Misc';

/**
 * @function throwAxiosErrorMessage Предназначена для выбрасывания сообщения ошибки AxiosError
 * @param {AxiosError} error - ошибка AxiosError
 * @throws {string} - текст ошибки
 */

export const throwAxiosErrorMessage = (error: AxiosError): never => {
  throw new Error(getErrorMessage(error as AxiosError));
};
