import { useTranslation } from 'i18n';
import { useGettingAllTravelStatuses } from 'api/travel-status';
import { useOrganizations } from 'api/organizations';
import { formatBaseDate, formatTime } from 'utils/formatTime';
import { getCompensationType, getDetailedCost, getPaidPeriodValueByTripDateTime } from 'utils/reportsUtils';
import { formatFullRoute } from 'utils/formatAddress';
import { fullNameLastFirstPat } from 'utils/employee';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { Records } from '../../types/types';

export const useDescriptionItemRecords = (request: TripResponse): Records => {
  const { t } = useTranslation();
  const labels = t.Forms.registryFilterFields;
  const tripStatus = useGettingAllTravelStatuses().data;
  const { content: organizations } = useOrganizations().data.organizationResponse;
  const contractorName = organizations.find(org => org.id === request.passenger?.organizationId)?.officialName ?? '-';
  const currentTripStatus = tripStatus.find(status => status.name === request.status)?.rusName ?? '-';

  return [
    [labels.contractorName, contractorName],
    [labels.structureDepartment, request.passenger?.departmentName ?? '-'],
    [labels.orgId, request.passenger?.organizationId ?? '-'],
    [labels.costCenter, '-'], // todo no data
    [labels.tripId, request.humanReadableId ?? '-'],

    [labels.creationDate, formatTime(request.creationTime)],
    [labels.desiredDateRange, formatTime(request.desiredDate)],
    [labels.approvalDate, formatTime(request.approvalDate)],
    // не приходят минуты. попросить бек
    [labels.orderPaymentFormationStart, formatTime(request.orderPaymentFormationStartDate)],

    [labels.corpClientPersonnelNumber, request.passenger?.personnelNumber ?? '-'],
    [labels.corpClientFio, fullNameLastFirstPat(request.passenger)],
    [labels.travelingBehaviorVaries, '-'], // todo no data
    [labels.purposeTrip, request.purpose?.label ?? '-'],
    [labels.route, formatFullRoute(request.expected.waypoints)],
    [labels.compensationType, getCompensationType(request.transportCompensation)],
    [labels.tripStatus, currentTripStatus],
    [labels.tripCost, getDetailedCost(request.transportCompensation)],
    [labels.waypointsCount, request.expected.waypoints.length ?? '-'],
    [labels.checkinWaypointsCount, '-'], // todo no data
    [labels.noCheckinWaypointsCount, '-'], // todo no data
    [labels.paidPeriod, getPaidPeriodValueByTripDateTime(request.desiredDate)],
    [labels.requestRatingCommentVisible, request.requestRating?.rating ?? '-'],
    [labels.requestComment, request.requestRating?.ratingComment ?? '-'],
  ];
};
