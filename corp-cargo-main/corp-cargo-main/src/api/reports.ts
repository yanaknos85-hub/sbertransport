/* eslint-disable no-use-before-define */
import { IHttpService, ResponseService, ILogger } from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { MutationResultPair } from 'react-query';
import contentDisposition from 'content-disposition';
import { TYPE_RESPONSE } from 'constants/constants.app';
import { isJsonString } from 'utils/isJSONString';
import { AxiosError } from 'axios';
import {
  CARGO_REPORTS,
  GET_CARGO_BUSINESS_REPORTS,
  GET_CARGO_REGISTRY_REPORT,
  GET_CARGO_ROUTES_REGISTRY_REPORT,
  GET_CSE_REGISTRY_REPORT,
  GET_PAYMENT_REPORT,
  GET_REGISTRY_REPORT,
  GET_TAXI_REGISTRY_FROM_SERVER,
  REPORTS
} from '../constants/constants.api';
import { getFilenameFromHeader, handleClick } from '../utils/downloads';

declare module 'api' {
  interface Cache {
    uploadStatus: { key: ['uploadStatus']; value: DataForm | null };
  }
}

export const DataForm = t.strict({
  id: t.string,
  contractor: t.strict({
    id: t.string,
  }),
  date: t.string,
  valid: t.boolean,
  stringsQnt: t.number,
  blankCells: t.array(t.string),
  incorrectTypeCells: t.array(t.string),
});

export const InfoContractor = t.intersection([t.type({ id: t.string }), t.partial({ name: tt.nullable(t.string) })]);
export type InfoContractor = t.TypeOf<typeof InfoContractor>;
export type DataForm = t.TypeOf<typeof DataForm>;

export const DownloadFileList = t.strict({
  contractor: InfoContractor,
  taxiTripRegisters: t.array(
    t.intersection([
      t.type({
        id: t.string, valid: t.boolean, date: t.string,
      }),
      t.partial({ stringsQnt: t.number, uploadFileFullName: t.string }),
    ])
  ),
});
export type DownloadFileList = t.TypeOf<typeof DownloadFileList>;

export const AsyncReportResponse = t.type({
  url: t.string,
  started: t.boolean,
});

export type AsyncReportResponse = t.TypeOf<typeof AsyncReportResponse>;

export const AsyncReportResponseNew = t.type({
  result_url: t.string,
  started: t.boolean,
});

export type AsyncReportResponseNew = t.TypeOf<typeof AsyncReportResponseNew>;

export interface ProgressDownloading {
  in_progress: boolean;
}

/**
 * Запрос на формирование отчета по реестру (асинхрон)
 * @param http - axios
 * @param transportType - тип транспорта (PUBLIC | TAXI | PERSONAL | CARGO | CARGO_ROUTES)
 * @param requestBody - сформированное тело запроса с информации по фильтрации для выгрузки
 * @param mimeType - MIME тип для блоба
 * @param orgId - organization ID
 */
export const downloadRegistryXLSAsync = <T extends Record<string, unknown>>(
  http: IHttpService,
  transportType: string,
  mimeType: string,
  requestBody: T,
  orgId: string,
  logger: ILogger,
  setIsLoading?: React.Dispatch<React.SetStateAction<boolean>>,
  formatType = 'xls'
): Promise<void> => {
  const directory = transportType === 'cargo' || transportType === 'cargoRoutes' ? `/${CARGO_REPORTS}` : `/${REPORTS}`;

  // todo с этим нужно что-то делать
  const url
    = transportType === 'cargoRoutes' ? GET_CARGO_ROUTES_REGISTRY_REPORT : transportType === 'cargo'
      ? (formatType === 'cse' ? GET_CSE_REGISTRY_REPORT : GET_CARGO_REGISTRY_REPORT) : `${GET_REGISTRY_REPORT}${transportType}`;

  // TODO: рассмотреть остановку таймера
  const checkResponse = (filePath: string): void => {
    http
      .get<ArrayBuffer>(directory + filePath, { responseType: 'arraybuffer' })
      .then(fileResponse => {
        const decoder = new TextDecoder();
        const data = decoder.decode(fileResponse.data);

        let dataObj: ProgressDownloading = {
          in_progress: false,
        };

        if (isJsonString(data)) {
          dataObj = JSON.parse(data);
        }

        if (!dataObj.in_progress) {
          handleClick(fileResponse.data, mimeType, getFilenameFromHeader(fileResponse));
          setIsLoading && setIsLoading(false);
        } else {
          setTimeout(() => checkResponse(filePath), 1000);
        }
      });
  };

  if (transportType === 'cargo' || transportType === 'cargoRoutes') {
    return http
      .post<AsyncReportResponse>(url, requestBody, { urlParams: { orgId } })
      .then(resp => {
        setTimeout(() => checkResponse(resp.data.url), 1000);
      })
      .catch((error: AxiosError<AsyncReportResponseNew>) => {
        if (error.response?.status === 412) {
          logger.toMessage('error', 'Необходимо заполнить фильтр');
        }
      });
  }

  return http
    .get<AsyncReportResponseNew>(url, { params: { ...requestBody, organizationId: orgId } })
    .then(resp => {
      logger.toMessage('success', 'Ожидайте, пожалуйста. Файл формируется');
      setTimeout(() => checkResponse(resp.data.result_url), 1000);
    })
    .catch((error: AxiosError<AsyncReportResponseNew>) => {
      if (error.response?.status === 409) {
        logger.toMessage('success', 'Ожидайте, пожалуйста. Файл формируется');
        setTimeout(() => checkResponse(error.response?.data.result_url ?? ''), 1000);
      } else {
        throw new Error();
      }
    });
};

/**
 * Запрос на формирование реестра к выплате (асинхрон)
 * @param http - axios
 * @param transportType - тип транспорта (PUBLIC | TAXI | PERSONAL)
 * @param requestBody - сформированное тело запроса с информации по фильтрации для выгрузки
 * @param mimeType - MIME тип для блоба
 * @param orgId - organization ID
 */
export const downloadCompensationsXLSAsync = <T>(
  http: IHttpService,
  transportType: string,
  mimeType: string,
  params: T
): Promise<void> => {
  // TODO: рассмотреть остановку таймера
  const checkResponse = (filePath: string): void => {
    http.get(`/reports${filePath}`, { responseType: 'arraybuffer' }).then(fileResponse => {
      // TODO: переделать условие
      if (
        fileResponse.headers['content-type'] === TYPE_RESPONSE.excel
        || fileResponse.headers['content-type'] === TYPE_RESPONSE.sheet
      ) {
        handleClick(fileResponse.data, mimeType, getFilenameFromHeader(fileResponse));
      } else {
        setTimeout(() => checkResponse(filePath), 1000);
      }
    });
  };

  return http
    .get<AsyncReportResponseNew>(`${GET_PAYMENT_REPORT}${transportType}`, { params })
    .then(resp => {
      setTimeout(() => checkResponse(resp.data.result_url), 1000);
    });
};

export interface UploadInfo {
  contractorId: string;
  date: string;
  file: FormData;
}

export const uploadRegistryXLS = (http: IHttpService, process: ResponseService, data: FormData): Promise<DataForm> => http.post<DataForm>('reports/xls/import/taxi/registry', data).then(process.decodeResponseData(DataForm));

export const updateUploadRegistryXLS = (
  http: IHttpService,
  process: ResponseService,
  registryId: string,
  data: FormData
): Promise<DataForm> => http
  .post<DataForm>(`reports/xls/import/taxi/registry/${registryId}`, data)
  .then(process.decodeResponseData(DataForm));

// eslint-disable-next-line @stylistic/max-len
export const useCheckUploadedFile = (contractorId: string, date: string): APIQueryResult<DataForm | null, unknown> => useAPI(
  ['uploadStatus'],
  ({ http, process: { decodeResponseData } }) => http
  // @ts-ignore
    .request<DataForm>({
      url: `reports/xls/import/taxi/registry/${contractorId}/${date}`,
      hush: [404],
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } as any)
    .then(decodeResponseData(DataForm))
    .catch(err => {
      if (err.response.status === 404) {
        return null;
      }
      throw err;
    }),
  { cacheTime: 0 }
);

export const gettingListDownloadedContractorAll = (
  http: IHttpService,
  process: ResponseService
): Promise<DownloadFileList[]> => http
  .get<DownloadFileList[]>('reports/xls/import/taxi/registry/all')
  .then(process.decodeResponseData(t.array(DownloadFileList)));

export const useDownloadRegistry = (
  contractorId: string,
  contractorName: string
// eslint-disable-next-line @typescript-eslint/no-explicit-any
): MutationResultPair<any, any, any, any> => useAPIMutation(
  ({ http }, { year, month }: { year: string; month: string }) => http
    .get(GET_TAXI_REGISTRY_FROM_SERVER, {
      urlParams: {
        contractorId, year, month,
      }, responseType: 'arraybuffer',
    })
    .then(({ data, headers }) => {
      const a = document.createElement('a');
      const { type = headers['content-type'] } = headers['content-disposition']
        ? contentDisposition.parse(headers['content-disposition']).parameters
        : {};
      a.download = `registry-${contractorName}-${year}-${month}`;
      debugger; // eslint-disable-line no-debugger
      const objectURL = URL.createObjectURL(new Blob([data as ArrayBuffer], { type }));

      a.href = objectURL;
      a.click();
      URL.revokeObjectURL(objectURL);
    }),
  {
    onSuccess: ({ process, t: tr }) => {
      process.processStatus(200, tr.Forms.RegistryXLSModal.textDownloadSuccess);
    },
    onError: ({
      error, logger, t: tr,
    }) => {
      if (error.response.status === 404) {
        logger.toMessage('error', tr.Forms.RegistryXLSModal.downloadError);
      }
    },
  }
);

/**
 * Запрос на формирование отчета по вкладке бизнес-отчеты (асинхрон)
 * @param http - axios
 * @param transportType - тип транспорта (PUBLIC | TAXI | PERSONAL)
 * @param requestBody - сформированное тело запроса с информации по фильтрации для выгрузки
 * @param mimeType - MIME тип для блоба
 */
export const downloadBusinessReportsXLSAsync = <T extends Record<string, unknown>>(
  http: IHttpService,
  transportType: string,
  mimeType: string,
  requestBody: T,
  orgId: string,
  logger: ILogger,
  setIsLoading?: React.Dispatch<React.SetStateAction<boolean>>,
  formatType = 'xls'
): Promise<void> => {
  const url
    = transportType === 'cargo'
      ? formatType === 'cse'
        ? GET_CSE_REGISTRY_REPORT
        : GET_CARGO_REGISTRY_REPORT
      : transportType === 'public' ? GET_CARGO_BUSINESS_REPORTS
        : `${GET_REGISTRY_REPORT}${transportType}`;

  // TODO: рассмотреть остановку таймера
  const checkResponse = (filePath: string): void => {
    http
      .get<ArrayBuffer>(`/${transportType === 'cargo' ? CARGO_REPORTS : REPORTS}${filePath}`, { responseType: 'arraybuffer' })
      .then(fileResponse => {
        const decoder = new TextDecoder();
        const data = decoder.decode(fileResponse.data);

        let dataObj: ProgressDownloading = {
          in_progress: false,
        };

        if (isJsonString(data)) {
          dataObj = JSON.parse(data);
        }

        if (!dataObj.in_progress) {
          handleClick(fileResponse.data, mimeType, getFilenameFromHeader(fileResponse));
          setIsLoading && setIsLoading(false);
        } else {
          setTimeout(() => checkResponse(filePath), 1000);
        }
      })
      .catch(() => {
        setIsLoading && setIsLoading(false);
        logger.toMessage('error', 'Выгрузка превышает допустимые объемы. Уменьшите выбранные параметры или период');
      });
  };

  return http
    .get<AsyncReportResponseNew>(url, { params: { ...requestBody, organizationId: orgId } })
    .then(resp => {
      logger.toMessage('success', 'Ожидайте, пожалуйста. Файл формируется');
      setTimeout(() => checkResponse(resp.data.result_url), 1000);
    })
    .catch((error: AxiosError<AsyncReportResponseNew>) => {
      if (error.response?.status === 409) {
        logger.toMessage('success', 'Ожидайте, пожалуйста. Файл формируется');
        setTimeout(() => checkResponse(error.response?.data.result_url ?? ''), 1000);
      } else {
        throw new Error();
      }
    });
};
