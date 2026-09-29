import moment from 'moment';
import React from 'react';
import { useRouteMatch } from 'react-router-dom';

import { DATE_FORMAT, RUBLE_SIGN } from 'constants/constants.app';

import { TripTypes, TripRequestFieldsTitles as ft } from 'modules/EmployeeApp/shared/TripRequestDict.enum';

import { GeoWaypoints } from 'shared/hooks/geo/useGeoWaypoints';
import { ActualRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { TripRequestModel } from 'stores/Trip/models';
import { getDistanceString, getTimeString } from 'utils';

import { UUID } from 'utils/io-ts';

import EditableWaypoints from '../../../components/EditableWaypoints';
import { useLimitRequestState } from '../../hooks/useLimitRequestState';
import { useRequestEmployee } from '../../hooks/useRequestEmployee';
import { getWaypointFields } from 'modules/EmployeeApp/components/TripsPage/TripRequestDetailedView/WayPoints/utils';
import { useCurrentTripRequest } from 'shared/hooks/trip';
import { DeleteWaypointData, useDeleteWaypoint } from 'api/check-in';

export function useDescriptionData({
  request,
  actualRoute,
}: {
  request: TripRequestModel;
  geoWaypoints: GeoWaypoints;
  actualRoute: ActualRoute;
}): [string, string | number | JSX.Element | JSX.Element[]][] {
  const match = useRouteMatch<{ reqId: UUID }>();
  const { reqId } = match.params;
  const [deleteWaypoint] = useDeleteWaypoint();
  const { department } = useRequestEmployee(request);
  const { limitTypeString } = useLimitRequestState(reqId, !!request?.id);
  const { refetchRequestTrip } = useCurrentTripRequest();

  const getAuthor = (): string => {
    const { author }: TripRequestModel = request;
    if (author.patronymic) {
      return `${author.lastName} ${author.firstName} ${author.patronymic}`;
    }
    return `${author.lastName} ${author.firstName}`;
  };

  const waypointFields = getWaypointFields(actualRoute.waypoints);
  const prepareDeleteWaypointData = (indexAdress: number): DeleteWaypointData => {
    const address = waypointFields.filter((el, index) => index === indexAdress)[0];

    return {
      requestId: request.id || '',
      latitude: address?.latitude,
      longitude: address?.longitude,
      orderingIndex: waypointFields.findIndex(el => el.fieldName === address.fieldName),
    };
  };

  const sendDeleteWaypoint = (index: number) => {
    const data = prepareDeleteWaypointData(index);
    deleteWaypoint(data).then(() => refetchRequestTrip().then(() => window.location.reload()));
  };

  const renderRoute = (): JSX.Element => (
    <>
      <>
        <EditableWaypoints
          removeWaypoint={sendDeleteWaypoint}
          waypoints={actualRoute.waypoints}
          request={request}
        />
      </>
    </>
  );

  const renderDesiredDate = (): string => moment(request.desiredDate).format(DATE_FORMAT.DATE_WITH_TIME);

  const renderRestOfLimit = (): string => `${request.getRestOfLimitInRubles(true) || 0} ${RUBLE_SIGN}`;

  const absentText = 'отсутствует';

  return [
    [ft.id, request.humanReadableId],
    [ft.passenger, getAuthor() || ''],
    [ft.department, department],
    [ft.desiredDate, renderDesiredDate()],
    [ft.transportType, request.transportTypeString],
    [ft.tripType, request.coopTrip ? TripTypes.shared : TripTypes.single],
    [ft.passengerCount, request.passengerCount],
    [ft.purpose, request.purpose.label ?? 'не выбрана'],
    [ft.route, renderRoute()],
    [ft.limitType, limitTypeString],
    [ft.restOfLimit, renderRestOfLimit()],
    [ft.totalTripDuration, getTimeString(actualRoute.time)],
    [ft.totalTripDistance, getDistanceString(actualRoute.distance)],
    [ft.totalTripCost, actualRoute.costString],
    [ft.commentForPurpose, request?.commentForPurpose ? request.commentForPurpose : absentText],
  ];
}
