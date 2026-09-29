import { useGetTaxiTripStatus } from 'api/register-search';

import {
  getCoopTripName,
  getIndividualTripAddresses,
  getIndividualTripAddressWaitTime,
  getIndividualTripApprovedBy,
  getIndividualTripDepartment,
  getIndividualTripVspGosbTbExist,
  getRequestOptions,
  getTripStatus
} from 'utils/reportsUtils';
import { formatTime, getTimeString } from 'utils/formatTime';
import { useTransportTypeTariff } from 'api/tariffs';
import { useGetLimitByRequestId } from 'api/limits';
import { useSingleContractor } from 'api/contractors';
import { TRANSPORT_TYPE } from 'stores/Limits/Models/ResharedDepartmentLimits';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { fullNameLastFirstPat } from 'utils/employee';
import { formatRubles } from 'utils';
import { formatDistance } from 'utils/formatDistance';
import { usePosition } from 'api/positions';
import { TaxiTripFactData } from 'stores/TaxiRegistry/models/TaxiRegistry.interface';
import { UUID } from 'utils/io-ts';
import { Records } from '../types/types';

export const useIndividualTripDescription = (
  trip: TripResponse,
  factTrip: TaxiTripFactData,
  labels: Record<string, string>
): Records => {
  const { data: tripStatus } = useGetTaxiTripStatus();
  const { data: position } = usePosition(trip.passenger?.organizationId, trip.passenger?.positionId);
  const { data: limit } = useGetLimitByRequestId(trip.id);
  const { data: tariff } = useTransportTypeTariff(trip.tariffId, TRANSPORT_TYPE.TAXI);
  const { data: contractor } = useSingleContractor(tariff?.contractorId as UUID);

  const {
    departureAddress, destinationAddress, intermediateAddress,
  } = getIndividualTripAddresses(
    trip.expected.waypoints
  );
  const { departureWaitTime, destinationWaitTime } = getIndividualTripAddressWaitTime(trip.expected.waypoints);

  return [
    [labels.requestIdVisible, trip.humanReadableId],
    [labels.requestStatusVisible, getTripStatus(tripStatus, trip.status)],
    [labels.creationTimeVisible, formatTime(trip.creationTime)],
    [labels.desiredDateVisible, formatTime(trip.desiredDate)],
    [labels.factTripStartTimeVisible, formatTime(factTrip.factDataDTO?.tripStartTime)],
    [labels.tariffIdVisible, tariff?.humanReadableId ?? '-'],
    [labels.carrierVisible, contractor?.name ?? '-'],
    [labels.tripTypeVisible, getCoopTripName(trip.coopTrip)],
    [labels.passengersCountVisible, trip.passengerCount ?? '1'],
    [labels.passengerFioVisible, fullNameLastFirstPat(trip.passenger)],
    [labels.approvedByFioVisible, getIndividualTripApprovedBy(trip.approvedBy)],
    [labels.passengerDepartmentVisible, getIndividualTripDepartment(trip.passenger)],
    [labels.passengerPositionVisible, position?.positionName ?? '-'],
    [labels.tripPurposeVisible, trip.purpose?.label ?? '-'],
    [labels.waypointFromVisible, departureAddress],
    [labels.intermediateAddressVisible, intermediateAddress],
    [labels.waypointToVisible, destinationAddress],
    [labels.vspGosbTbExistVisible, getIndividualTripVspGosbTbExist(factTrip.waypoints)],
    [labels.waitingTimeDepartureAddress, departureWaitTime],
    [labels.waitingTimeDestinationAddress, destinationWaitTime],
    [labels.plannedPriceVisible, formatRubles(trip.expected.cost)],
    [labels.tripFactPriceVisible, formatRubles(factTrip.factDataDTO?.tripFactPrice)],
    [labels.plannedRangeVisible, formatDistance(trip.expected.distance)],
    [labels.actualRangeVisible, formatDistance(factTrip.factDataDTO?.tripFactDistance)],
    [labels.plannedDurationVisible, getTimeString(trip.expected.time)],
    [labels.actualDurationVisible, getTimeString(factTrip.factDataDTO?.tripFactDuration)],
    [labels.limitIdVisible, limit?.humanReadableId ?? '-'],
    [labels.commentForDriverVisible, trip.commentForDriver ?? labels.absent],
    [labels.requestRatingVisible, trip.requestRating?.rating ?? labels.absent],
    [labels.requestRatingCommentVisible, trip.requestRating?.ratingComment ?? labels.absent],
    [labels.preferences, getRequestOptions(trip.requestOptions, labels.absent)],
    [labels.dateContractorFactData, '-'], // TODO waiting for SS
  ];
};
