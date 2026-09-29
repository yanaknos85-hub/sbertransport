/* eslint-disable @typescript-eslint/no-unused-vars */
import React, { useEffect, useState } from 'react';

import RouteMapCheckIn from './RouteMapCheckIn/RouteMapCheckIn';
import { getWaypointFields } from 'modules/EmployeeApp/components/TripsPage/TripRequestDetailedView/WayPoints/utils';
import { TripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { useDefaultChangeTripRequest } from 'shared/hooks/trip/useChangeTripRequest';
import { TripRequestModel } from 'stores/Trip/models';

import styles from './modal.module.scss';

const RouteMapListCheckIn = ({
  tripRequestRoute,
  request,
}: {
  tripRequestRoute: TripRequestRoute;
  request: TripRequestModel;
}): JSX.Element => {
  const {
    actualRoute, requestReadyForUpdate,
  } = tripRequestRoute;
  const waypointFields = getWaypointFields(request.expected.waypoints);
  const { changeTripRequest } = useDefaultChangeTripRequest({ request });

  const needCheckIn = (): boolean => waypointFields.some(waypointField => waypointField.checkinAutomatic === false);
  const isPersonalTransport = request?.transportType === TransportTypeEnum.PERSONAL;
  const isCheckInState
    = isPersonalTransport && needCheckIn();

  useEffect(() => {
    if (requestReadyForUpdate) {
      changeTripRequest({ ...request, expected: actualRoute });
    }
  }, [requestReadyForUpdate, tripRequestRoute]);

  return (
    <div className={styles.wrapperMain}>
      <RouteMapCheckIn
        tripRequestRoute={tripRequestRoute}
        isCheckInState={isCheckInState}
        addresses={waypointFields}
      />
    </div>
  );
};

export default RouteMapListCheckIn;
