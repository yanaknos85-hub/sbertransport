import { useAPI, useAPIMutation } from 'api';
import {
  CARSHARING_ANALYSIS,
  GENERAL_ANALYSIS,
  LIMITS_STATS_GENERAL,
  PERSONAL_ANALYSIS,
  PUBLIC_ANALYSIS,
  TAXI_ANALYSIS
} from 'constants/constants.api';
import { MutationResultPair } from 'react-query';
import { AnalysisTaxiQuery, AnalysisTaxiResponse } from 'stores/Analysis/TaxiAnalysis';
import { IResponseTotal, ILimitsResponseTotal } from 'shared/charts/types/types';

interface QueryType {
  organizationId: string[];
  year: number;
  monthList: number[] | string[];
  transportTypes: string[];
}

export interface QueryTypeLimit {
  organizationId: string[];
  year: number;
  month: string[];
  transportType: string[];
}

declare module 'api' {
  interface Cache {
    analysisTaxi: {
      key: ['analysisTaxi'];
      value: { responseData: AnalysisTaxiResponse };
    };
    analysisPersonal: {
      key: ['analysisPersonal'];
      value: { responseData: AnalysisTaxiResponse };
    };
    analysisPublic: {
      key: ['analysisPublic'];
      value: { responseData: AnalysisTaxiResponse };
    };
    analysisCarsharing: {
      key: ['analysisCarsharing'];
      value: { responseData: AnalysisTaxiResponse };
    };
    analysisGeneral: {
      key: ['analysisGeneral', QueryType];
      value: { charts: IResponseTotal[] };
    };
    limitsStatsGeneral: {
      key: ['limitsStatsGeneral', QueryTypeLimit];
      value: { charts: ILimitsResponseTotal[] };
    };
  }
}

const raw2JournalCache = (responseData: AnalysisTaxiResponse) => ({ responseData });

export const useAnalysisTaxi = (query: AnalysisTaxiQuery) => useAPI(['analysisTaxi'], ({ http, process }) => http
  .post<AnalysisTaxiResponse>(TAXI_ANALYSIS, AnalysisTaxiQuery.encode(query))
  .then(process.decodeResponseData(AnalysisTaxiResponse))
  .then(raw2JournalCache)
);
export const useAnalysisPersonal = (query: AnalysisTaxiQuery) => useAPI(['analysisPersonal'], ({ http, process }) => http
  .post<AnalysisTaxiResponse>(PERSONAL_ANALYSIS, AnalysisTaxiQuery.encode(query))
  .then(process.decodeResponseData(AnalysisTaxiResponse))
  .then(raw2JournalCache)
);
export const useAnalysisPublic = (query: AnalysisTaxiQuery) => useAPI(['analysisPublic'], ({ http, process }) => http
  .post<AnalysisTaxiResponse>(PUBLIC_ANALYSIS, AnalysisTaxiQuery.encode(query))
  .then(process.decodeResponseData(AnalysisTaxiResponse))
  .then(raw2JournalCache)
);
export const useAnalysisCarsharing = (query: AnalysisTaxiQuery) => useAPI(['analysisCarsharing'], ({ http, process }) => http
  .post<AnalysisTaxiResponse>(CARSHARING_ANALYSIS, AnalysisTaxiQuery.encode(query))
  .then(process.decodeResponseData(AnalysisTaxiResponse))
  .then(raw2JournalCache)
);

export const useGetAnalysis = (query: QueryType) => useAPI(
  ['analysisGeneral', query],
  ({ http, process }) => http.post<IResponseTotal[]>(GENERAL_ANALYSIS, query).then(process.decodeResponseData()),
  { cacheTime: 5 * 60 * 1000, staleTime: 5 * 60 * 1000 }
);

export const useAnalysisGeneral = (): MutationResultPair<{ charts: IResponseTotal[] }, unknown, QueryType, unknown> => (
  useAPIMutation(
    ({ http, process }, query) => (
      http.post<IResponseTotal[]>(GENERAL_ANALYSIS, query).then(process.decodeResponseData())
    ), {
      onSuccess: () => undefined,
    }
  )
);

export const useAnalysisStatsGeneral = (query: QueryType) => useAPI(
  ['analysisGeneral', query],
  ({ http, process }) => http
    .post<IResponseTotal[]>(GENERAL_ANALYSIS, query, { headers: { 'X-Version': 2 } })
    .then(process.decodeResponseData()),
  { enabled: false }
);

export const useLimitsStatsGeneral = (params: QueryTypeLimit) => useAPI(
  ['limitsStatsGeneral', params],
  ({ http, process }) => http
    .post<ILimitsResponseTotal[]>(LIMITS_STATS_GENERAL, params, { headers: { 'X-Version': 2 } })
    .then(process.decodeResponseData()),
  { enabled: false }
);
