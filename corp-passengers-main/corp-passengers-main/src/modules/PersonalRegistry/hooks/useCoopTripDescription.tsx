import { TripResponse } from 'stores/Registry/Registry.interface';
import {
  getCarInfo,
  getCoopTripName,
  getMarriageCertificateNumber,
  getPaidPeriodValueByTripDateTime,
  getTripStatus
} from 'utils/reportsUtils';
import { formatTime } from 'utils/formatTime';
import { formatFullRoute } from 'utils/formatAddress';
import { convertToRubles, formatRubles } from 'utils';
import { formatDistance } from 'utils/formatDistance';
import { useGetPersonalTripStatuses, usePersonalSearch } from 'api/personal-search';
import { Records } from '../../TaxiRegistry/types/types';
import { useGetCoopTripPassengersInfo } from './useGetCoopTripPassengersInfo';
import { parsejoinedPassengers } from 'utils/parsejoinedPassengers';

export const useCoopTripDescription = (trip: TripResponse, labels: Record<string, string>): Records => {
  const { data: tripStatus } = useGetPersonalTripStatuses();

  const { data: coopTrips } = usePersonalSearch(
    { pageSetting: { page: 0, size: 10 }, sharedRideId: trip.sharedRideId },
    trip.passenger?.organizationId
  );

  const currentTrip = coopTrips.content.find(item => item.id === trip.id);

  const coopTripDescriptions = useGetCoopTripPassengersInfo(coopTrips.content, trip, labels);
  const {
    ownerShip, registrationNumber, brand, engineVolume, insuranceNumber,
  } = getCarInfo(currentTrip?.personalCar);

  return [
    [labels.tripId, trip.humanReadableId],
    [labels.tripStatus, getTripStatus(tripStatus, trip.status)],

    [labels.creationDate, formatTime(trip.creationTime)],
    [labels.desiredDateRange, formatTime(trip.desiredDate)],
    [labels.approvalDate, formatTime(trip.approvalDate)],
    [labels.orderPaymentFormationStart, formatTime(currentTrip?.orderPaymentFormationStartDate)],

    [labels.departureDateAndTime, formatTime(currentTrip?.factData?.tripStartTime)],
    [labels.typeTrip, getCoopTripName(trip.coopTrip)],
    ...coopTripDescriptions,
    [labels.route, formatFullRoute(trip.expected.waypoints)],
    [labels.waypointsCount, currentTrip?.expected.waypointsCount],
    [labels.checkinWaypointsCount, currentTrip?.expected.waypointsCountWithCheckIn],
    [labels.noCheckinWaypointsCount, currentTrip?.expected.waypointsCountWithoutCheckIn],
    [labels.plannedTripCost, formatRubles(convertToRubles(trip.expected.cost))],
    [labels.tripCost, formatRubles(currentTrip?.factData?.tripFactPrice)],
    [labels.plannedRangeVisible, formatDistance(trip.expected.distance)],
    [labels.expectedDistance, formatDistance(currentTrip?.factData?.tripFactDistance)],
    [labels.paidPeriod, getPaidPeriodValueByTripDateTime(trip.desiredDate)],
    [labels.ownership, ownerShip],
    [labels.registrationCertificate, getMarriageCertificateNumber(currentTrip?.personalCar, currentTrip?.passenger)],
    [labels.registrationNumber, registrationNumber],
    [labels.brandName, brand],
    [labels.engineVolume, engineVolume],
    [labels.insuranceNumber, insuranceNumber],
    ['Оценка пользователя', trip.requestRating?.rating ?? labels.absent],
    ['Комментарий к оценке', trip.requestRating?.ratingComment ?? labels.absent],
    [labels.passengersWithMe, trip.joinedPassengers?.length ? parsejoinedPassengers(trip.joinedPassengers) : '-'],
  ];
};
