import React from 'react';
import Text from 'antd/lib/typography/Text';

import { TypeOfAction, WaypointPassenger } from 'api/trips/trips.types';
import withErrorBoundary from 'components/withErrorBoundary';
import Panel from 'components/Panel/Panel';
import Flex from 'components/Flex/Flex';
import WaypointIcon from 'components/WaypointIcon/WaypointIcon';
import { getAddress } from 'utils/getAddress';
import { ReactComponent as Arrow } from 'assets/icons/arrow.svg';
import { getFullName } from 'utils/getFullName';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { EMPTY_CELL_CONTENT } from 'constants/app.constants';
import { WaypointType } from 'types/waypoint';

import PersonalCard from 'modules/Trip/components/PersonalCard/PersonalCard';
import { ITripInfo } from 'modules/Trip/types/tripPass.interface';
import { useTripInfo } from 'modules/Trip/context/TripInfo.context';

import styles from './PassengersCard.module.scss';

interface Passengers extends WaypointPassenger {
  boarding?: string;
  unBoarding?: string;
  waypointType?: WaypointType;
}

const getPassengers = (waypoints: ITripInfo['waypoints']) => {
  const passengers = {} as Record<string, Passengers>;

  waypoints.forEach(waypoint => {
    if (waypoint.passengers?.length) {
      waypoint.passengers.forEach(passenger => {
        if (passenger.firstName || passenger.patronymic || passenger.phone) {
          const key = `${passenger.phone}${getFullName({
            firstName: passenger.firstName,
            patronymic: passenger.patronymic,
          })}`;

          passengers[key] = {
            ...passenger,
            waypointType: waypoint.type,
            boarding:
              passenger.type === TypeOfAction.BOARDING ? waypoint.fullAddress : passengers[key]?.boarding ?? undefined,
            unBoarding:
              passenger.type === TypeOfAction.UNBOARDING
                ? waypoint.fullAddress
                : passengers[key]?.unBoarding ?? undefined,
          };
        }
      });
    }
  });

  if (Object.keys(passengers).length) {
    return Object.values(passengers).map((passenger, idx) => (
      <div className={styles.passCardContent} key={idx}>
        <Flex alignItems="center">
          <WaypointIcon type={passenger.waypointType ?? 'EXPECTED'} index={0} />
          {passenger.boarding}
          <Arrow />
          <WaypointIcon type={passenger.waypointType ?? 'EXPECTED'} index={1} />
          {passenger.unBoarding}
        </Flex>
        <Panel
          inner
          contentWidth
          key={`${idx}`}
        >
          <PersonalCard
            title={
              getFullName({
                firstName: passenger.firstName,
                patronymic: passenger.patronymic,
              }) ?? EMPTY_CELL_CONTENT
            }
            desc1={`Тел ${passenger.phone ?? EMPTY_CELL_CONTENT}`}
          />
        </Panel>
      </div>
    ));
  }

  return null;
};

/** Пассажиры */
export const PassengersCard = withErrorBoundary(() => {
  const { trip, waypoints } = useTripInfo();

  return (
    <Panel
      marginTop
      title={(
        <>
          <span>Заказчики </span>
          <Text type="secondary">{trip.passengerCount}</Text>
        </>
      )}
      smallVerticalPadding
    >
      <div className={styles.passCardContent}>
        {trip.requests.length
          ? trip.requests
            .filter(x => x.passenger)
            .map(x => {
              const lastRequestWaypoint = x.waypoints?.at(-1);

              const lastWaypoint
                  = waypoints.findLast(waypoint => waypoint.id === lastRequestWaypoint?.id)
                  ?? waypoints.findLast(
                    waypoint => waypoint.longitude === lastRequestWaypoint?.longitude
                    && waypoint.latitude === lastRequestWaypoint?.latitude
                  );

              return (
                <div className={styles.passCardContent} key={x.id}>
                  <Flex alignItems="center">
                    <WaypointIcon type={waypoints[0].type} index={0} />
                    {getAddress(waypoints[0])}
                    <Arrow />
                    <WaypointIcon
                      type={lastWaypoint?.type ?? 'EXPECTED'}
                      index={waypoints.findLastIndex(waypoint => waypoint.id === lastWaypoint?.id)}
                    />
                    {x.waypoints?.at(-1) && getAddress(x.waypoints.at(-1)!)}
                  </Flex>

                  <Panel inner contentWidth>
                    <PersonalCard
                      title={getFullName(x.passenger!)}
                      desc1={`Тел ${
                        x.passenger!.mobilePhone ? formatPhoneNumber(x.passenger!.mobilePhone) : EMPTY_CELL_CONTENT
                      }`}
                    />
                  </Panel>
                </div>
              );
            })
          : trip.waypoints.filter(w => w.contact?.name && w.contact?.phone).length
            ? trip.waypoints.map(w => w.contact?.name && w.contact?.phone && (
              <Panel
                inner
                contentWidth
                key={w.index}
              >
                <PersonalCard
                  title={w.contact?.name ?? EMPTY_CELL_CONTENT}
                  desc1={`Тел ${w.contact?.phone ?? EMPTY_CELL_CONTENT}`}
                />
              </Panel>
            ))
            : getPassengers(waypoints as ITripInfo['waypoints'])}
      </div>
    </Panel>
  );
});
