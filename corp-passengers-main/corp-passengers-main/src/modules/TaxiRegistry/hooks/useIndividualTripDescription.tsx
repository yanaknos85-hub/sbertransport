import { useGetTaxiTripStatus, useSearchTaxiRegister } from 'api/register-search';

import {
  getCoopTripName,
  getIndividualTripAddresses,
  getAllWaitTime,
  getIndividualTripApprovedBy,
  getTripStatus
} from 'utils/reportsUtils';
import { formatTime, getTimeString } from 'utils/formatTime';
import { useTransportTypeTariff } from 'api/tariffs';
import { useGetLimitByRequestId } from 'api/limits';
import { useSingleContractor } from 'api/contractors';
import { TRANSPORT_TYPE } from 'stores/Limits/Models/ResharedDepartmentLimits';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { fullNameLastFirstPat } from 'utils/employee';
import { convertToRubles } from 'utils';
import { formatDistance } from 'utils/formatDistance';
import { TaxiTripFactData } from 'stores/TaxiRegistry/models/TaxiRegistry.interface';
import { UUID } from 'utils/io-ts';
import { Records } from '../types/types';
import { useProfile } from 'api/profile';
import { DATE_FORMAT, sTransport } from 'constants/constants.app';

export const useIndividualTripDescription = (
  trip: TripResponse,
  factTrip: TaxiTripFactData,
  labels: Record<string, string>,
  taxiClasses: Record<string, string>
): {
  generalInfo: Records;
  userInfo: Records;
  tripInfo: Records;
  sctructureInfo: Records;
  planFactInfo: Records;
  additionalInfo: Records;
} => {
  const { data: tripStatus } = useGetTaxiTripStatus();
  const { data: limit } = useGetLimitByRequestId(trip.id);
  const { data: tariff } = useTransportTypeTariff(trip.tariffId, TRANSPORT_TYPE.TAXI);
  const { data: contractor } = useSingleContractor(tariff?.contractorId as UUID);
  const { organizationId } = useProfile().data;

  const { data: reportTrip } = useSearchTaxiRegister(
    !trip.coopTrip
      ? { coopTrip: false, requestHumanId: trip.humanReadableId }
      : { coopTrip: true, sharedRideId: trip.sharedRideId },
    {},
    organizationId
  );

  const passengers = reportTrip.content;
  const passengersInfo = [];

  if (trip.coopTrip) {
    passengers.sort((prev, next) => prev.creationTime - next.creationTime);

    for (let i = 1; i < passengers.length; i++) {
      // @ts-ignore
      passengersInfo.push([`ФИО пассажира ${i + 1}`, fullNameLastFirstPat(passengers[i].author)]);
      // @ts-ignore
      passengersInfo.push([`Табельный номер пассажира ${i + 1}`, passengers[i].author.personnelNumber]);
    }
  }

  const finalReportTrip = trip.coopTrip ? passengers[0] : reportTrip.content[0];

  const {
    departureAddress, destinationAddress, intermediateAddress,
  } = getIndividualTripAddresses(
    trip.expected.waypoints
  );

  return {
    generalInfo: [
      [labels.organizationName, finalReportTrip.organizationOfficialName || '-'],
      [labels.requestIdVisible, trip.humanReadableId],
      [labels.creationTimeVisible, formatTime(trip.creationTime, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      [labels.requestStatusVisible, getTripStatus(tripStatus, trip.status)],
      [labels.desiredDateVisible, formatTime(trip.desiredDate, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      [labels.tripTypeVisible, getCoopTripName(trip.coopTrip)],
      [labels.typeOfTariff, taxiClasses[`${finalReportTrip.taxiClass}`]],
    ] as Records,

    userInfo: [
      [labels.personelNumberVisible, finalReportTrip.author.personnelNumber],
      [labels.passengerFioVisible, fullNameLastFirstPat(finalReportTrip.author)],
      [labels.totalSharedRequestCount, finalReportTrip.totalSharedRequestCount || '-'],
      [labels.numberOfSeats, finalReportTrip.passengerCount || '-'],
      [labels.tripPurposeVisible, trip.purpose?.label ?? '-'],
      [labels.limitIdVisible, limit?.humanReadableId ?? '-'],
      [labels.tariffIdVisible, tariff?.humanReadableId ?? '-'],
      [labels.commentForDriver, finalReportTrip.commentForDriver || '-'],
    ] as Records,

    tripInfo: [
      [labels.waypointFromVisible, departureAddress],
      [labels.intermediateAddressVisible, intermediateAddress],
      [labels.routeWaypointsCountVisible, trip.expected.waypoints.length],
      [labels.destinationWaitTime, getAllWaitTime(trip.expected.waypoints)],
      [labels.waypointToVisible, destinationAddress],
      [labels.finishedDate, formatTime(finalReportTrip.finishedTime, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      [labels.requestClosedDatetime, formatTime(finalReportTrip.requestClosedDatetime, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      [labels.deadlineVisible, formatTime(finalReportTrip.deadline, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      [labels.driverArrivedDatetimeVisible, formatTime(finalReportTrip.driverArrivedDatetime, '-', DATE_FORMAT.DATE_WITH_TIME_SECONDS)],
      [labels.violation, finalReportTrip.deadlineViolation],
    ] as Records,

    sctructureInfo: [
      [labels.codeDep, finalReportTrip.department.code],
      [labels.depOne, finalReportTrip.passengerDepartment1 ?? '-'],
      [labels.depTwo, finalReportTrip.passengerDepartment2 ?? '-'],
      [labels.depThree, finalReportTrip.passengerDepartment3 ?? '-'],
      [labels.depFour, finalReportTrip.passengerDepartment4 ?? '-'],
      [labels.depFive, finalReportTrip.passengerDepartment5 ?? '-'],
      [labels.depSix, finalReportTrip.passengerDepartment6 ?? '-'],
      [labels.passengerPositionVisible, trip.passenger.positionName],
      [labels.kkPersonalNumberVisible, trip.passenger.personnelNumber],
      [labels.fioPassenger, fullNameLastFirstPat(trip.passenger)],
    ] as Records,

    planFactInfo: [
      [labels.contractor, contractor?.name ? contractor.name : !tariff?.contractorId ? sTransport : '-'],
      ['Предварительный километраж в заявке, км', formatDistance(trip.expected.distance)],
      ['Фактический километраж в поездке, км', formatDistance(factTrip.factDataDTO?.tripFactDistance)],
      ['Предварительная стоимость в заявке, руб', convertToRubles(trip.expected.cost)],
      ['Фактическая стоимость в поездке, руб', factTrip.factDataDTO?.tripFactPrice ? convertToRubles(factTrip.factDataDTO?.tripFactPrice) : '-'],
      ['Предварительное время в заявке, час/мин', getTimeString(trip.expected.time)],
      ['Фактическое время поездки в поездке, час/мин', factTrip.factDataDTO?.tripFactDuration ? getTimeString(factTrip.factDataDTO?.tripFactDuration) : '-'],
      ['Суммарное фактическое время ожидания, мин', getTimeString(factTrip.factDataDTO?.tripFactWaitTime)],
      ['Доля в общей стоимости поездки для участников', '-'],
      ['Экономия, руб', convertToRubles(finalReportTrip.savingsCash ?? 0) || '-'],
      ['Экономия, %', finalReportTrip.savingsProcents || '-'],
    ] as Records,

    additionalInfo: [
      ...passengersInfo,
      ['Табельный номер согласующего заявку', trip.approvedBy?.personnelNumber],
      [labels.approvedByFioVisible, getIndividualTripApprovedBy(trip.approvedBy)],
      ['Инициатор совместной поездки', trip.coopTrip ? 'Да' : 'Нет'],
      [labels.requestRatingVisible, trip.requestRating?.rating ?? labels.absent],
      [labels.requestRatingCommentVisible, trip.requestRating?.ratingComment ?? labels.absent],
      ['ID Совместной поездки', trip.sharedRideId || '-'],
    ] as Records,
  };
};
