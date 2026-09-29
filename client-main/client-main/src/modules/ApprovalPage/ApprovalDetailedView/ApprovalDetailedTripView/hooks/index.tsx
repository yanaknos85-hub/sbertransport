import { ReloadOutlined } from '@ant-design/icons';
import { Button } from 'antd';
import moment from 'moment';
import React from 'react';
import { useRouteMatch } from 'react-router-dom';

import { DATE_FORMAT, RUBLE_SIGN } from 'constants/constants.app';

import { TripTypes, TripRequestFieldsTitles as ft } from 'shared/TripRequestDict.enum';

import { GeoWaypoints } from 'shared/hooks/geo/useGeoWaypoints';
import { ActualRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { TripRequestModel } from 'stores/Trip/models';
import { getDistanceString, getTimeString } from 'utils';

import { UUID } from 'utils/io-ts';
import { waypointsArraysOrderIsSimilar } from 'utils/waypoint';

import EditableWaypoints from '../../../components/EditableWaypoints';
import { useLimitRequestState } from '../../hooks/useLimitRequestState';
import { useRequestEmployee } from '../../hooks/useRequestEmployee';

export function useDescriptionData({
  request,
  geoWaypoints,
  actualRoute,
  updateTripRequest,
}: {
  request: TripRequestModel;
  geoWaypoints: GeoWaypoints;
  actualRoute: ActualRoute;
  updateTripRequest: () => void;
}): [string, string | number | JSX.Element | JSX.Element[]][] {
  const match = useRouteMatch<{ reqId: UUID }>();
  const { reqId } = match.params;

  const { department } = useRequestEmployee(request);
  const { limitTypeString } = useLimitRequestState(reqId, !!request?.id);
  const waypointsIsChanged = !waypointsArraysOrderIsSimilar(request.expected.waypoints, actualRoute.waypoints);

  const getAuthor = (): string => {
    const { author }: TripRequestModel = request;
    if (author.patronymic) {
      return `${author.lastName} ${author.firstName} ${author.patronymic}`;
    }
    return `${author.lastName} ${author.firstName}`;
  };

  const renderRoute = (): JSX.Element => (
    <>
      <EditableWaypoints
        removeWaypoint={geoWaypoints.removeWaypoint}
        waypoints={actualRoute.waypoints}
        request={request}
      />
      {waypointsIsChanged && (
        <Button icon={<ReloadOutlined />} onClick={updateTripRequest}>
          Обновить заявку
        </Button>
      )}
    </>
  );

  const renderDesiredDate = (): string => moment(request.desiredDate).format(DATE_FORMAT.DATE_WITH_TIME);

  const renderRestOfLimit = (): string => `${request.getRestOfLimitInRubles(true) || 0} ${RUBLE_SIGN}`;

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
  ];
}
