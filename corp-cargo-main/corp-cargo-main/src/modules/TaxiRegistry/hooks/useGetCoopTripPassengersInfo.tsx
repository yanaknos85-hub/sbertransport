import { TripInfoForReporting } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { fullNameLastFirstPat } from 'utils/employee';
import { formatRubles } from 'utils';
import { formatDistance } from 'utils/formatDistance';
import { getTimeString } from 'utils/formatTime';
import {
  getCoopTripCompanionFIO,
  getCoopTripParam,
  getIndividualTripAddresses,
  getIndividualTripAddressWaitTime,
  getIndividualTripApprovedBy,
  getIndividualTripVspGosbTbExist
} from 'utils/reportsUtils';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { CoopTripPassengerInfo, PassengerInfo, Records } from '../types/types';

export const useGetCoopTripPassengerInfo = (
  trips: TripInfoForReporting[],
  currentTrip: TripResponse,
  labels: Record<string, string>
): Records => {
  const coopTrip = new Map();
  trips.forEach(trip => coopTrip.set(trip.passenger.id, trip));

  const passengersInfo = [] as Record<string, string | number>[];
  coopTrip.forEach((item: TripInfoForReporting) => {
    const {
      departureAddress, destinationAddress, intermediateAddress,
    } = getIndividualTripAddresses(
      item.expected.waypoints
    );

    const { departureWaitTime, destinationWaitTime } = getIndividualTripAddressWaitTime(item.expected.waypoints); // TODO need to add waitTime from SS

    const passengerInfo = {
      fullName: fullNameLastFirstPat(item.passenger),
      approvedBy: getIndividualTripApprovedBy(item.approvedBy, currentTrip.approvedBy),
      department: item.department.departmentName ?? '-',
      position: item.position.positionName ?? '-',
      departureAddress,
      destinationAddress,
      intermediateAddress,
      departureWaitTime,
      destinationWaitTime,
      purpose: item.purpose.purpose ?? '-',
      plannedCost: formatRubles(item.expected.cost),
      cost: formatRubles(item.factData?.tripFactPrice),
      plannedDistance: formatDistance(item.expected.distance),
      distance: formatDistance(item.factData?.tripFactDistance),
      plannedTime: getTimeString(item.expected.time),
      time: getTimeString(item.factData?.tripFactDuration),
      limit: item.humanReadableLimitId ?? '-',
      comment: item.commentForDriver ?? labels.absent,
      rating: item.requestRating?.rating ?? labels.absent,
      ratingComment: item.requestRating?.ratingComment ?? labels.absent,
      VspGosbTbExist: getIndividualTripVspGosbTbExist(item.expected.waypoints),
      creationTime: item.creationTime,
    };
    passengersInfo.push(passengerInfo);
  });

  // @ts-ignore
  passengersInfo.sort((prev, next) => prev.creationTime - next.creationTime);

  return [
    [labels.passengerFioVisible, getCoopTripCompanionFIO(passengersInfo, PassengerInfo.fullName)],
    [labels.approvedByFioVisible, getCoopTripParam(passengersInfo, PassengerInfo.approvedBy)],
    [labels.passengerDepartmentVisible, getCoopTripParam(passengersInfo, PassengerInfo.department)],
    [labels.passengerPositionVisible, getCoopTripParam(passengersInfo, PassengerInfo.position)],
    [labels.tripPurposeVisible, getCoopTripParam(passengersInfo, PassengerInfo.purpose)],
    [labels.waypointFromVisible, getCoopTripParam(passengersInfo, PassengerInfo.departureAddress)],
    [labels.intermediateAddressVisible, getCoopTripParam(passengersInfo, PassengerInfo.intermediateAddress)],
    [labels.waypointToVisible, getCoopTripParam(passengersInfo, PassengerInfo.destinationAddress)],
    [labels.vspGosbTbExistVisible, getCoopTripParam(passengersInfo, PassengerInfo.VspGosbTbExist)],
    [labels.waitingTimeDepartureAddress, getCoopTripParam(passengersInfo, PassengerInfo.departureWaitTime)],
    [labels.waitingTimeDestinationAddress, getCoopTripParam(passengersInfo, PassengerInfo.destinationWaitTime)],
    [labels.plannedPriceVisible, getCoopTripParam(passengersInfo, PassengerInfo.plannedCost)],
    [labels.tripFactPriceVisible, getCoopTripParam(passengersInfo, PassengerInfo.cost)],
    [labels.plannedRangeVisible, getCoopTripParam(passengersInfo, PassengerInfo.plannedDistance)],
    [labels.actualRangeVisible, getCoopTripParam(passengersInfo, PassengerInfo.distance)],
    [labels.plannedDurationVisible, getCoopTripParam(passengersInfo, PassengerInfo.plannedTime)],
    [labels.actualDurationVisible, getCoopTripParam(passengersInfo, PassengerInfo.time)],
    [labels.limitIdVisible, getCoopTripParam(passengersInfo, PassengerInfo.limit)],
  ] as Records;
};
