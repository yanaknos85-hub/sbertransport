import { AxiosError, AxiosResponse } from 'axios';
import { either } from 'fp-ts';
import { pipe } from 'fp-ts/lib/function';
import { injectable } from 'inversify';
import * as t from 'io-ts';
import { Type } from 'io-ts';
import reporter, { formatValidationErrors } from 'io-ts-reporters';
import isPlainObject from 'lodash/isPlainObject';
import { isAxiosResponse } from 'utils';
import { logger } from 'utils/logger/logger';

export interface IResponseService {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  getResponseData<T>(response: AxiosResponse<T> | T, typeName?: Type<any>): T;
  getResponseError<T>(error: Error | AxiosError<T>): Promise<Error>;
  getResponseStatus(response: AxiosResponse): number;
  processStatus(status: number, successMessage: string, failedMessage?: string): boolean | undefined;
  decodeResponseData<T, U = T>(type: Type<U, T>): ({ data }: AxiosResponse<T>) => U;
}

@injectable()
export class ResponseService implements IResponseService {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  getResponseData = <T>(response: AxiosResponse<T>, typeName?: Type<any>): T => {
    if (!isAxiosResponse(response)) {
      console.warn('There is no data field in response object');
      return response;
    }

    if (typeName !== undefined) {
      this.checkType(response.data, typeName).forEach(val => this.handleTypeError(val, response.data));
    }

    return response.data;
  };

  decodeResponseData = <T, U = T>(type: Type<U, T> = t.any) => ({ data }: AxiosResponse<T>): U => (
    pipe(
      type.decode(data),
      either.mapLeft(formatValidationErrors),
      either.fold(errors => {
        console.error(errors.join('\n'), data);

        // if (isTestMode()) {
        //   throw new Error(errors.join('\n'));
        // } else {
        //   this.logger.notify('error', errorText[CustomErrorCode.TYPES].subtitle, errorText[CustomErrorCode.TYPES].title, 5, CustomErrorCode.TYPES);
        // }

        return data as unknown as U;
      }, t.identity)
    )
  );

  getResponseStatus = (response: AxiosResponse): number => {
    if ('status' in response) {
      return response.status;
    }
    console.warn('There is no status field in response object');
    return response;
  };

  getResponseError = <T>(error: Error | AxiosError<T>): Promise<Error> => {
    if (isPlainObject(error) && 'response' in error) {
      return Promise.reject(
        Error(
          `${error?.response?.statusText} ${error?.response?.status}` || `Неизвестная ошибка ${error?.response?.status}`
        )
      );
    }
    return Promise.reject(error);
  };

  processStatus(
    status: number,
    successMessage: string,
    failedMessage = 'Ошибка при выполнении запроса'
  ): boolean | undefined {
    const success = [200];
    const failure = [404, 403];

    if (success.includes(status)) {
      logger('success', successMessage);
      return true;
    }

    if (failure.includes(status)) {
      logger('fail', failedMessage);
      return false;
    }

    return undefined;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private checkType = <T>(value: T, type: Type<any>): string[] => {
    let report: string[] = [];

    if (Array.isArray(value)) {
      value.forEach(x => {
        const validation = type.decode(x);
        report = [...report, ...reporter.report(validation)];
      });
    } else {
      const validation = type.decode(value);
      report = reporter.report(validation);
    }

    return report;
  };

  private handleTypeError = (type: string, data: unknown): void => {
    if (console[type]) {
      console[type]('Type Error', data);
    }
  };
}
