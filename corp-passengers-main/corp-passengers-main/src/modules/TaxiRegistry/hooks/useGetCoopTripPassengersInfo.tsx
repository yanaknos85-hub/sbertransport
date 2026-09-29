import { TripInfoForReporting } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { fullNameLastFirstPat } from 'utils/employee';
import { convertToRubles, formatRubles } from 'utils';
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
import { PassengerInfo, Records } from '../types/types';
import { TaxiTripFactData } from 'stores/TaxiRegistry/models/TaxiRegistry.interface';

export const useGetCoopTripPassengerInfo = (
  trips: TripInfoForReporting[],
  currentTrip: TripResponse,
  labels: Record<string, string>,
  factTrip: TaxiTripFactData
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
      plannedCost: formatRubles(convertToRubles(item.expected.cost)),
      cost: formatRubles(factTrip.factDataDTO?.tripFactPrice && convertToRubles(factTrip.factDataDTO?.tripFactPrice)),
      plannedDistance: formatDistance(item.expected.distance),
      distance: formatDistance(factTrip.factDataDTO?.tripFactDistance),
      plannedTime: getTimeString(item.expected.time),
      time: getTimeString(item.factData?.tripFactDuration),
      limit: item.humanReadableLimitId ?? '-',
      comment: item.commentForDriver ?? labels.absent,
      rating: item.requestRating?.rating ?? labels.absent,
      ratingComment: item.requestRating?.ratingComment ?? labels.absent,
      VspGosbTbExist: getIndividualTripVspGosbTbExist(item.expected.waypoints),
      creationTime: item.creationTime,
      minTaxiTariffCost: formatRubles(item.minTaxiTariffCost),
      totalSharedRequestCount: item.totalSharedRequestCount,
      passengerCount: item.passengerCount,
      departmentEconomy: formatRubles(item.departmentEconomy),
      limitDebit: formatRubles(item.limitDebit),
      financialImpact: formatRubles(item.financialImpact),
      financialImpactShared: formatRubles(item.financialImpactShared),
      financialImpactJoined: formatRubles(item.financialImpactJoined),
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
    [labels.minTaxiTariffCost, getCoopTripParam(passengersInfo, PassengerInfo.minTaxiTariffCost)],
    [labels.totalSharedRequestCount, getCoopTripParam(passengersInfo, PassengerInfo.totalSharedRequestCount)],
    [labels.passengerCount, getCoopTripParam(passengersInfo, PassengerInfo.passengerCount)],
    [labels.departmentEconomy, getCoopTripParam(passengersInfo, PassengerInfo.departmentEconomy)],
    [labels.limitDebit, getCoopTripParam(passengersInfo, PassengerInfo.limitDebit)],
    [labels.financialImpact, getCoopTripParam(passengersInfo, PassengerInfo.financialImpact)],
    [labels.financialImpactShared, getCoopTripParam(passengersInfo, PassengerInfo.financialImpactShared)],
    [labels.financialImpactJoined, getCoopTripParam(passengersInfo, PassengerInfo.financialImpactJoined)],
  ] as Records;
};
