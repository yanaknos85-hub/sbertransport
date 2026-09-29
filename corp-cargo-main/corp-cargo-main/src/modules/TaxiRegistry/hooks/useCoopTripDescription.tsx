import { TripResponse } from 'stores/Registry/Registry.interface';
import { TaxiTripFactData } from 'stores/TaxiRegistry/models/TaxiRegistry.interface';
import { useGetTaxiTripStatus, useSearchTaxiRegister } from 'api/register-search';
import {
  getCoopTripCurrentSaving, getCoopTripName, getRequestOptions, getTripStatus
} from 'utils/reportsUtils';
import { formatTime } from 'utils/formatTime';
import { UUID } from 'utils/io-ts';
import { useSingleContractor } from 'api/contractors';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { useTransportTypeTariff } from 'api/tariffs';
import { useProfile } from 'api/profile';
import { Records } from '../types/types';
import { useGetCoopTripPassengerInfo } from './useGetCoopTripPassengersInfo';

export const useCoopTripDescription = (
  trip: TripResponse,
  factTrip: TaxiTripFactData,
  labels: Record<string, string>
): Records => {
  const { data: tripStatus } = useGetTaxiTripStatus();
  const { organizationId } = useProfile().data;

  const { data: coopTrips } = useSearchTaxiRegister(
    { coopTrip: true, sharedRideId: trip.sharedRideId },
    {},
    organizationId
  );

  const coopTripDescriptions = useGetCoopTripPassengerInfo(coopTrips.content, trip, labels);
  const { data: tariff } = useTransportTypeTariff(factTrip.tariffId, TransportTypes.TAXI);
  const { data: contractor } = useSingleContractor(tariff?.contractorId as UUID);

  return [
    [labels.requestIdVisible, trip.humanReadableId],
    [labels.requestStatusVisible, getTripStatus(tripStatus, trip.status)],
    [labels.creationTimeVisible, formatTime(trip.creationTime)],
    [labels.desiredDateVisible, formatTime(trip.desiredDate)],
    [labels.factTripStartTimeVisible, formatTime(factTrip.factDataDTO?.tripStartTime)],
    [labels.tariffIdVisible, tariff?.humanReadableId || '-'],
    [labels.carrierVisible, contractor?.name || '-'],
    [labels.tripTypeVisible, getCoopTripName(trip.coopTrip)],
    [labels.sharedRideIdVisible, trip.sharedRideId || '-'],
    [labels.passengersCountVisible, trip.passengers?.length || '-'],
    ...coopTripDescriptions,
    [labels.currentCustomerSavingVisible, getCoopTripCurrentSaving(factTrip.magentaSharedRequest?.kpi, factTrip.id)],
    [labels.preferences, getRequestOptions(trip.requestOptions, labels.absent)],
    // [labels.dateContractorFactData, '-'], // TODO waiting for SS
    ['Оценка поездки', trip.requestRating?.rating || '-'],
    ['Комментарий к оценке', trip.requestRating?.ratingComment || '-'],
  ];
};
