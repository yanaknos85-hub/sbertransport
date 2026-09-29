import {
  APIQueryResult, useAPI
} from 'api';
import { UUID } from 'utils/io-ts';
import { TripsReports, TripsReportsFilters } from './trips-reports.types';
import { TRIPS_REPORT_LIST } from './trips-reports.constants';
import { QueryConfig } from 'react-query';
import { finalTripStatuses } from 'constants/trips.constants';

export const PASS_TRIPS_REPORTS_KEY = 'pass-trips';

declare module 'api' {
  interface Cache {
    passTripsReports: {
      key: [typeof PASS_TRIPS_REPORTS_KEY, UUID, TripsReportsFilters];
      value: TripsReports;
    };
  }
}

/** Пагинированный список пассажирских поездок для отчетности */
export const useTripsReports = (
  {
    contractorId,
    query = {
      page: 0,
      size: 10,
      statuses: finalTripStatuses,
    },
  }: {
    contractorId: UUID;
    query?: TripsReportsFilters;
  },
  config?: QueryConfig<TripsReports>
): APIQueryResult<TripsReports> => useAPI(
  [PASS_TRIPS_REPORTS_KEY, contractorId, query],
  ({ http, process }) => http
    .get<TripsReports>(TRIPS_REPORT_LIST, {
      urlParams: { contractorId },
      params: {
        ...query,
        statuses: query.statuses?.length ? query.statuses : finalTripStatuses,
      },
    })
    .then(process.decodeResponseData(TripsReports)),
  config
);
