import React, { useMemo, useState } from 'react';
import * as t from 'io-ts';

import { formatTime } from 'utils/formatTime';
import { getTripStatus } from 'utils/reportsUtils';
import { CarSharingSearchResponse } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { useGettingAllTravelStatuses } from 'api/travel-status';
import { TableRecord } from '../types';
import { DATE_FORMAT } from 'constants/constants.app';

export const useTableData = (): {
  setSearchResult: (searchResult: CarSharingSearchResponse) => void;
  totalElements: number;
  dataSource: TableRecord[];
  searchResult: CarSharingSearchResponse | undefined;
} => {
  const [searchResult, setSearchResult] = useState<CarSharingSearchResponse>();
  const tripStatus = useGettingAllTravelStatuses().data;

  const { content: tripRequests, totalElements } = useMemo<Omit<CarSharingSearchResponse, 'pageable'>>(
    () => searchResult || ({ content: [], totalElements: 0 } as unknown as t.TypeOf<typeof CarSharingSearchResponse>),
    [searchResult]
  );

  const dataSource: TableRecord[] = React.useMemo(
    () => tripRequests.map(
      trip => ({
        id: trip.id,
        requestIdVisible: trip.humanReadableId,
        mvzVisible: trip.costCenter ?? '-',
        desiredDateVisible: formatTime(trip.desiredDate, '-', DATE_FORMAT.DATE_WITH_TIME_DOTS),
        approveDateVisible: formatTime(trip.approveDate, '-', DATE_FORMAT.DATE_WITH_TIME_DOTS),
        employeeFioVisible: trip.fio ?? '-',
        requestStatusVisible: getTripStatus(tripStatus, trip.status),
        totalCostVisible: trip.totalCost ?? '-',
        drivingLengthVisible: trip.drivingLength ?? '-',
        organizationalUnitCode: trip.department.code ?? '-',
        carVisible: trip.car ?? '-',
      } as TableRecord)
    ),
    [tripRequests, tripStatus]
  );

  return {
    setSearchResult,
    totalElements,
    dataSource,
    searchResult,
  };
};
