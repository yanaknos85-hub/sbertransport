import { Timeline } from 'antd';
import React, { useEffect } from 'react';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { SpinWrapped } from 'shared/components';
import { useDefaultChangeTripRequest } from 'shared/hooks/trip/useChangeTripRequest';
import { TripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';

import DraggableWaypoints from './components/DraggableWaypoints';
import WaypointItem from './components/WaypointItem';
import { getWaypointFields } from './utils';

import styles from './styles.module.scss';

const WayPoints = ({ tripRequestRoute }: { tripRequestRoute: TripRequestRoute }): JSX.Element => {
  const {
    actualRoute, inProgress: calcInProgress, request, requestReadyForUpdate,
  } = tripRequestRoute;

  const { changeTripRequest } = useDefaultChangeTripRequest({ request });

  useEffect(() => {
    if (requestReadyForUpdate) {
      changeTripRequest({ ...request, expected: actualRoute });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [requestReadyForUpdate]);

  const waypointFields = getWaypointFields(actualRoute.waypoints);

  const needCheckIn = (): boolean => waypointFields.some(waypointField => waypointField.checkinAutomatic === false);
  const isPersonalTransport = request?.transportType === TransportTypeEnum.PERSONAL;
  const isCheckInState = isPersonalTransport && request?.status === 'PERSONAL_TRIP_IN_PROGRESS' && needCheckIn();

  // FIXME Требования касательно добавления точек маршрута не описаны.
  // const renderAddWaypoint = (): JSX.Element | null =>
  //   isCheckInState ? (
  //     <Button
  //       type="link"
  //       icon={<PlusIcon />}
  //       size="middle"
  //       block
  //       className={styles.wayPointsAddNew}
  //       onClick={geoWaypoints.addWaypoint}
  //     >
  //       Добавить точку маршрута
  //     </Button>
  //   ) : null;

  const renderLoader = (): JSX.Element | null => calcInProgress ? (
    <div className={styles.wayPointsLoader}>
      <SpinWrapped text="Обновление данных" />
    </div>
  ) : null;

  return (
    <div className={styles.wayPointsContainer}>
      <Timeline mode="left">
        {isPersonalTransport ? (
          <DraggableWaypoints
            tripRequestRoute={tripRequestRoute}
            isCheckInState={isCheckInState}
            waypointFields={waypointFields}
          />
        ) : (
          waypointFields.map((waypointField, waypointIndex) => (
            <WaypointItem
              key={waypointField.fieldName}
              tripRequestRoute={tripRequestRoute}
              waypointField={waypointField}
              isCheckInState={isCheckInState}
              isPersonalTransport={isPersonalTransport}
              waypointIndex={waypointIndex}
            />
          ))
        )}
      </Timeline>

      {/* <div className={styles.wayPointsInfo}>
        <span>{`${getTimeString(actualRoute.time) ?? ''}`}</span>
        <span>{`${actualRoute.costString}`}</span>
        <span>{`${getDistanceString(actualRoute.distance) ?? ''}`}</span>
      </div> */}

      {/* {renderAddWaypoint()} */}
      {renderLoader()}
    </div>
  );
};

export default WayPoints;
