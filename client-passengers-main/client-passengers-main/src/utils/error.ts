import { AxiosError } from 'axios';
import { errorMessagesWithTranslate } from 'constants/constants.app';

export const getErrorMessage = (error: AxiosError): string => error.response?.data.message || error.response?.data.error || error.message;

export const getErrorMessageWithTranslation = (error: AxiosError, defaultMessage?: string) => {
  const message = JSON.parse(error?.request.response)?.message;

  return errorMessagesWithTranslate?.[message] ?? defaultMessage;
};
