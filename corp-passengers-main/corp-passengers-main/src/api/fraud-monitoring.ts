import type { AxiosError } from 'axios';
import type { QueryConfig } from 'react-query';

import type { APIQueryResult } from 'api';
import { useAPI } from 'api';
import { FRAUD_MONITORING_DETAILS, FRAUD_MONITORING_REPORT } from 'constants/constants.api';
import type {
  TFraudMonitoringDetailsResponse,
  TFraudMonitoringReportRequest,
  TFraudMonitoringReportResponse
} from 'modules/FraudMonitoring/fraudMonitoring.interface';
import {
  FraudMonitoringDetailsResponse,
  FraudMonitoringReportResponse
} from 'modules/FraudMonitoring/fraudMonitoring.interface';

enum CACHE_KEY {
  FRAUD_MONITORING_SEARCH = 'fraud-monitoring-search',
  FRAUD_MONITORING_DETAILS = 'fraud-monitoring-details',
}

declare module 'api/index' {
  interface Cache {
    fraudMonitoringSearch: {
      key: [CACHE_KEY.FRAUD_MONITORING_SEARCH, TFraudMonitoringReportRequest];
      value: TFraudMonitoringReportResponse;
    };
    fraudMonitoringDetails: {
      key: [CACHE_KEY.FRAUD_MONITORING_DETAILS, string];
      value: TFraudMonitoringDetailsResponse;
    };
  }
}

export const useFraudMonitoringSearch = (
  query: TFraudMonitoringReportRequest,
  config?: QueryConfig<TFraudMonitoringReportResponse>
): APIQueryResult<TFraudMonitoringReportResponse, AxiosError | unknown> => useAPI(
  [CACHE_KEY.FRAUD_MONITORING_SEARCH, query],
  ({ http, process }) => http
    .post<TFraudMonitoringReportResponse>(FRAUD_MONITORING_REPORT, query)
    .then<TFraudMonitoringReportResponse>(process.decodeResponseData(FraudMonitoringReportResponse)),
  config
);

export const useFraudMonitoringDetails = (
  requestId: string,
  config?: QueryConfig<TFraudMonitoringDetailsResponse>
): APIQueryResult<TFraudMonitoringDetailsResponse, AxiosError | unknown> => useAPI(
  [CACHE_KEY.FRAUD_MONITORING_DETAILS, requestId],
  ({ http, process }) => http
    .get<TFraudMonitoringDetailsResponse>(FRAUD_MONITORING_DETAILS, { urlParams: { requestId } })
    .then<TFraudMonitoringDetailsResponse>(process.decodeResponseData(FraudMonitoringDetailsResponse)),
  config
);
