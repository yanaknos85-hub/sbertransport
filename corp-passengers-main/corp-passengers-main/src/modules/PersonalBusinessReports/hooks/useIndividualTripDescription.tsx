import { convertToRubles, formatRubles } from 'utils';
import { formatDistance } from 'utils/formatDistance';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { useOrganizations } from 'api/organizations';
import {
  getCarInfo,
  getCoopTripName,
  getIndividualTripContractorName,
  getIndividualTripDepartment,
  getIndividualTripOrganizationId,
  getIndividualTripPersonnelNumber,
  getIndividualTripPurpose,
  getMarriageCertificateNumber,
  getPaidPeriodValueByTripDateTime,
  getTripStatus
} from 'utils/reportsUtils';
import { formatTime } from 'utils/formatTime';
import { fullNameLastFirstPat } from 'utils/employee';
import { formatFullRoute } from 'utils/formatAddress';
import { useGetPersonalTripStatuses } from 'api/personal-search';
import { TripRequestReport } from 'stores/PersonalSearch/PersonalSearch.interface';
import { Records } from '../../TaxiRegistry/types/types';

export const useIndividualTripDescription = (
  trip: TripResponse,
  tripReport: TripRequestReport,
  labels: Record<string, string>
): Records => {
  const { content: organizations } = useOrganizations().data.organizationResponse;
  const { data: tripStatus } = useGetPersonalTripStatuses();

  const {
    ownerShip, registrationNumber, brand, engineVolume, insuranceNumber,
  } = getCarInfo(trip.personalCar);

  return [
    [labels.tripId, trip.humanReadableId ?? '-'],
    [labels.tripStatus, getTripStatus(tripStatus, trip.status)],

    [labels.creationDate, formatTime(trip.creationTime)],
    [labels.desiredDateRange, formatTime(tripReport.desiredDate)],
    [labels.approvalDate, formatTime(tripReport.approveDate)],
    [labels.orderPaymentFormationStart, formatTime(trip.orderPaymentFormationStartDate)],

    [labels.typeTrip, getCoopTripName(trip.coopTrip)],
    [labels.corpClientFio, fullNameLastFirstPat(trip.passenger)],
    [labels.corpClientPersonnelNumber, getIndividualTripPersonnelNumber(trip.passenger)],
    [labels.contractorName, getIndividualTripContractorName(organizations, trip.passenger)],
    [labels.structureDepartment, getIndividualTripDepartment(trip.passenger)],
    [labels.orgId, getIndividualTripOrganizationId(trip.passenger)],
    [labels.purposeTrip, getIndividualTripPurpose(trip.purpose)],
    [labels.travelingBehaviorVaries, tripReport.itinerantType ?? '-'],
    [labels.costCenter, tripReport.costCenter ?? '-'],
    [labels.route, formatFullRoute(trip.expected.waypoints)],
    [labels.waypointsCount, tripReport.expected.waypointsCount ?? '-'],
    [labels.checkinWaypointsCount, tripReport.expected.waypointsCountWithCheckIn],
    [labels.noCheckinWaypointsCount, tripReport.expected.waypointsCountWithoutCheckIn],
    [labels.plannedTripCost, formatRubles(convertToRubles(trip.expected.cost))],
    [labels.tripCost, formatRubles(tripReport.factData?.tripFactPrice)],
    [labels.plannedRangeVisible, formatDistance(trip.expected.distance)],
    [labels.expectedDistance, formatDistance(tripReport.factData?.tripFactDistance)],
    [labels.paidPeriod, getPaidPeriodValueByTripDateTime(trip.desiredDate)],
    [labels.ownership, ownerShip],
    [labels.registrationCertificate, getMarriageCertificateNumber(trip.personalCar, tripReport.passenger)],
    [labels.registrationNumber, registrationNumber],
    [labels.brandName, brand],
    [labels.engineVolume, engineVolume],
    [labels.insuranceNumber, insuranceNumber],
    ['Оценка пользователя', trip.requestRating?.rating ?? labels.absent],
    ['Комментарий к оценке', trip.requestRating?.ratingComment ?? labels.absent],
  ];
};
