import React from 'react';
import moment from 'moment';

import { useGettingAllTravelStatuses } from 'api/travel-status';
import { useDepartments } from 'api/departments';
import { useProfile } from 'api/profile';
import { useSearchPersonalCoopTrips } from 'api/personal-search';

import { TripRequestReport } from 'stores/PersonalSearch/PersonalSearch.interface';
import { DATE_FORMAT, RUBLE_SIGN } from 'constants/constants.app';
import { getTripStatus, getExpectedCost, getCoopTripName } from 'utils/reportsUtils';
import { formatDistance } from 'utils/formatDistance';
import { getCoopTrips, getCurrentSharedRideIdArray, getTripParams } from '../utils';
import { TableRecord } from '../types/types';

export const useJournalData = (
  data: TripRequestReport[]
): {
  dataSource: TableRecord[];
  initialPageSetting: { page: { pageNumber: number; pageSize: number } };
} => {
  const initialPageSetting = { page: { pageNumber: 0, pageSize: 10 } };

  const { organizationId } = useProfile().data;
  const tripStatus = useGettingAllTravelStatuses().data;
  // @ts-ignore
  const departments = useDepartments(organizationId).data;
  const departmentsList = departments.byId;

  const shareRideIdArray = getCurrentSharedRideIdArray(data);
  const { data: coopTripsResponse } = useSearchPersonalCoopTrips(organizationId, shareRideIdArray);

  const dataSource: TableRecord[] = React.useMemo(
    () => data.map((trip: TripRequestReport) => {
      const coopTrips = getCoopTrips(coopTripsResponse, trip.sharedRideId);
      const {
        costCenter, desiredDate, fio, orderPaymentFormationStartDate,
      } = getTripParams(trip, coopTrips);
      const expectedCost = getExpectedCost(trip.expected.cost).replace(RUBLE_SIGN, '');
      const personalCarInfo = `
          ${trip.personalCar?.brandName || ''}
          ${trip.personalCar?.model || ''}
          ${trip.personalCar?.registrationNumber || ''}
        `;

      return {
        id: trip.id,
        requestIdVisible: trip.humanReadableId,
        mvzVisible: costCenter,
        desiredDateVisible: desiredDate,
        controlPeriodOfPayment: '-',
        orderPaymentFormationStartDateVisible: !isNaN(Number(orderPaymentFormationStartDate))
          ? moment(Number(orderPaymentFormationStartDate)).format(DATE_FORMAT.DATE_WITH_TIME_DOTS)
          : orderPaymentFormationStartDate,
        passengerFioVisible: fio,
        requestStatusVisible: getTripStatus(tripStatus, trip?.status ? trip.status : ''),
        plannedPriceVisible: expectedCost,
        tripFactPriceVisible: expectedCost,
        plannedRangeVisible: formatDistance(trip.expected.distance),
        paymentPeriodVisible: trip.paymentPeriod ?? '-',
        departmentCodeVisible: trip.department?.code ?? '-',
        sharedRideOwnerVisible: trip.coopTrip ? (trip.sharedRideOwner ? 'Водитель' : 'Пассажир') : 'Водитель',
        carVisible: personalCarInfo.trim() || '-',
        carEngineVolumeVisible: trip.personalCar?.engineVolume ?? '-',
        tripTypeVisible: getCoopTripName(trip.coopTrip),
      };
    }),
    [coopTripsResponse, data, departmentsList, tripStatus, organizationId, shareRideIdArray]
  );

  return {
    dataSource,
    initialPageSetting,
  };
};
