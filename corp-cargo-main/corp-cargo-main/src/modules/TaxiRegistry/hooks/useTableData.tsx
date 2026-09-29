import React, { useMemo, useState } from 'react';
import { useProfile } from 'api/profile';
import { RegisterSearchResponse, useGetTaxiTripStatus, useSearchTaxiCoopTrips } from 'api/register-search';

import { formatTime } from 'utils/formatTime';
import {
  getCoopTripName,
  getCoopTrips,
  getCurrentSharedRideIdArray,
  getTripParams,
  getTripStatus
} from 'utils/reportsUtils';
import { DATE_FORMAT } from 'constants/constants.app';
import { TableRecord } from '../types/types';

const formatTaxiTime = (time: string | number | null | undefined) => formatTime(time, '-', DATE_FORMAT.DATE_WITH_TIME_DOTS);

export const useTableData = (): {
  setSearchResult: (searchResult: RegisterSearchResponse) => void;
  totalElements: number;
  dataSource: TableRecord[];
  searchResult: RegisterSearchResponse | undefined;
} => {
  const [searchResult, setSearchResult] = useState<RegisterSearchResponse>();

  const { content: tripRequests, totalElements } = useMemo<Omit<RegisterSearchResponse, 'pageable'>>(
    () => searchResult || { content: [], totalElements: 0 },
    [searchResult]
  );

  const { organizationId } = useProfile().data;
  const statuses = useGetTaxiTripStatus().data;

  const shareRideIdArray = getCurrentSharedRideIdArray(tripRequests);

  const { data: coopTripsResponse } = useSearchTaxiCoopTrips(organizationId, shareRideIdArray);

  const dataSource: TableRecord[] = React.useMemo(
    () => tripRequests.map(trip => {
      const coopTrips = getCoopTrips(coopTripsResponse, trip.sharedRideId);
      const {
        costCenter, fio, cost, distance, expectedСost,
      } = getTripParams(trip, coopTrips);
      const carInfo = `
          ${trip.vehicle?.brand || ''}
          ${trip.vehicle?.model || ''}
          ${trip.vehicle?.stateNumber || ''}
        `;

      return {
        ...trip,
        requestIdVisible: trip.humanReadableId,
        mvzVisible: costCenter,
        desiredDateVisible: formatTaxiTime(trip.desiredDate),
        deadlineVisible: formatTaxiTime(trip.deadline),
        driverArrivedDatetimeVisible: formatTaxiTime(trip.driverArrivedDatetime),
        counterpartyNameVisible: trip.contractor?.name ?? '-',
        approveDateVisible: formatTaxiTime(trip.approveDate),
        passengerFioVisible: fio,
        requestStatusVisible: getTripStatus(statuses, trip.status),
        plannedPriceVisible: expectedСost,
        tripFactPriceVisible: cost,
        expectedDistanceVisible: distance,
        organizationalUnitCode: trip.department?.code ?? '-',
        carVisible: carInfo.trim() || '-',
        tripTypeVisible: getCoopTripName(trip.coopTrip),
      };
    }),
    [tripRequests, coopTripsResponse, statuses]
  );

  return {
    setSearchResult,
    totalElements,
    dataSource,
    searchResult,
  };
};
