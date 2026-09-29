import { TripResponse } from 'stores/Registry/Registry.interface';
import {
  getCoopTripCompanionFIO,
  getCoopTripContractorName,
  getCoopTripOrganizationId,
  getCoopTripParam,
  getIndividualTripPersonnelNumber
} from 'utils/reportsUtils';
import { fullNameLastFirstPat } from 'utils/employee';
import { TripRequestReport } from 'stores/PersonalSearch/PersonalSearch.interface';
import { useOrganizations } from 'api/organizations';
import { Records } from '../../TaxiRegistry/types/types';
import { PassengerInfo } from '../types/types';

export const useGetCoopTripPassengersInfo = (
  trips: TripRequestReport[],
  currentTrip: TripResponse,
  labels: Record<string, string>
): Records => {
  const { content: organizations } = useOrganizations().data.organizationResponse;

  const coopTrip = new Map();
  trips.forEach(trip => coopTrip.set(trip.passenger?.id, trip));

  const passengersInfo = [] as Record<string, string | number>[];
  coopTrip.forEach((item: TripRequestReport) => {
    const passengerInfo = {
      fullName: fullNameLastFirstPat(item.passenger),
      personnelNumber: getIndividualTripPersonnelNumber(item.passenger),
      contractorName: getCoopTripContractorName(organizations, currentTrip.passengers, item.passenger),
      department: item.department?.departmentName ?? '-',
      organizationId: getCoopTripOrganizationId(currentTrip.passengers, item.passenger),
      purpose: item.purpose?.purpose ?? '-',
      travelingBehaviorVaries: item.passenger?.itinerantType ?? '-',
      costCenter: item.costCenter ?? '-',
      creationTime: item.creationTime ?? '-',
    };
    passengersInfo.push(passengerInfo);
  });

  passengersInfo.sort((prev, next) => {
    if (typeof prev.creationTime === 'number' && typeof next.creationTime === 'number') {
      return prev.creationTime - next.creationTime;
    }
    return 0;
  });

  return [
    [labels.corpClientFio, getCoopTripCompanionFIO(passengersInfo, PassengerInfo.fullName)],
    [labels.corpClientPersonnelNumber, getCoopTripParam(passengersInfo, PassengerInfo.personnelNumber)],
    [labels.contractorName, getCoopTripParam(passengersInfo, PassengerInfo.contractorName)],
    [labels.structureDepartment, getCoopTripParam(passengersInfo, PassengerInfo.department)],
    [labels.orgId, getCoopTripParam(passengersInfo, PassengerInfo.organizationId)],
    [labels.purposeTrip, getCoopTripParam(passengersInfo, PassengerInfo.purpose)],
    [labels.travelingBehaviorVaries, getCoopTripParam(passengersInfo, PassengerInfo.travelingBehaviorVaries)],
    [labels.costCenter, getCoopTripParam(passengersInfo, PassengerInfo.costCenter)],
  ] as Records;
};
