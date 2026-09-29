// @ts-nocheck
import { faMapMarker, faThumbtack } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import moment from 'moment-timezone';
import React from 'react';
import { useRouteMatch } from 'react-router-dom';
import { DATE_FORMAT, RUBLE_SIGN } from 'constants/constants.app';
import { TripTypes, TripRequestFieldsTitles as ft } from 'shared/TripRequestDict.enum';
import { TripRequestModel } from 'stores/Trip/models';
import { UUID } from 'utils/io-ts';

import { useLimitRequestState } from '../../hooks/useLimitRequestState';
import { useRequestEmployee } from '../../hooks/useRequestEmployee';
import styles from '../../styles.module.scss';

export function useDescriptionData({
  request,
}: {
  request: TripRequestModel;
}): [string, string | number | JSX.Element | JSX.Element[]][] {
  const match = useRouteMatch<{ reqId: UUID }>();
  const { reqId } = match.params;

  const { employee, department } = useRequestEmployee(request);
  const { limitTypeString } = useLimitRequestState(reqId, !!request?.id);

  const renderWaypoints = (): JSX.Element[] => {
    return (
      request.expected.waypoints.map((x, index, array) => (
        <div
          className={styles.address}
          key={x.latitude}
          title={x.addressStringWithRegion}
        >
          <FontAwesomeIcon className={styles.addressIcon} icon={index === array.length - 1 ? faMapMarker : faThumbtack} />
          {x.addressStringWithRegion}
        </div>
      ))
    );
  };

  const renderDesiredDate = (): string => moment(request.desiredDate).format(DATE_FORMAT.DATE_WITH_TIME);

  const renderCost = (): string => `${request.getCostInRubles(true) || 0} ${RUBLE_SIGN}`;

  return [
    [ft.id, request.humanReadableId],
    [ft.passenger, employee?.shortName || ''],
    [ft.department, department],
    [ft.desiredDate, renderDesiredDate()],
    [ft.transportType, request.transportTypeString],
    [ft.tripType, request.coopTrip ? TripTypes.shared : TripTypes.single],
    [ft.passengerCount, request.passengerCount],
    [ft.purpose, request.purpose.label],
    [ft.route, renderWaypoints()],
    [ft.expectedCost, renderCost()],
    [ft.limitType, limitTypeString],
    [ft.expectedTime, request.expected.getTimeString()],
    [ft.expectedDistance, request.expected.getDistanceString()],
  ];
}
