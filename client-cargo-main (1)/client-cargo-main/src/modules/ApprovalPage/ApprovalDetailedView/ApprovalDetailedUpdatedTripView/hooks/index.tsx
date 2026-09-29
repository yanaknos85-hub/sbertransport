import React from 'react';
import { useRouteMatch } from 'react-router-dom';
import moment from 'moment';
import { TripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { TripRequestFieldsTitles as ft, TripTypes } from 'shared/TripRequestDict.enum';
import { getDistanceString, getTimeString } from 'utils';

import { useUpdatedTripApprove, useUpdatedTripDecline } from 'api/approvals-updated';
import { TripRequestModel } from 'stores/Trip/models';
import { IDeclineReason } from 'stores/Trip/Trip.interface';
import { DATE_FORMAT, RUBLE_SIGN } from 'constants/constants.app';
import { UUID } from 'utils/io-ts';

import EditableDiffRoute from '../../../components/EditableDiffRoute';
import { useLimitRequestState } from '../../hooks/useLimitRequestState';
import { useRequestEmployee } from '../../hooks/useRequestEmployee';

const renderDiffRoute = ({
  request,
  updatedTripRequestRoute,
  onChangeRequest,
}: {
  request: TripRequestModel | undefined;
  updatedTripRequestRoute: TripRequestRoute;
  onChangeRequest: () => void;
}): JSX.Element | string => updatedTripRequestRoute.request ? (
  <EditableDiffRoute
    oldWaypoints={request?.expected.waypoints || []}
    updatedTripRequestRoute={updatedTripRequestRoute}
    onChangeRequest={onChangeRequest}
  />
) : (
  'Нет данных о изменённой заявке!'
);

export function useDescriptionData({
  tripRequestRoute,
  updatedTripRequestRoute,
  onChangeRequest,
}: {
  tripRequestRoute: TripRequestRoute;
  updatedTripRequestRoute: TripRequestRoute;
  onChangeRequest: () => void;
}): [string, string | number | JSX.Element | JSX.Element[]][] {
  const match = useRouteMatch<{ reqId: UUID }>();
  const { reqId } = match.params;

  const { request, actualRoute } = tripRequestRoute;

  const { employee, department } = useRequestEmployee(request);
  const { limitTypeString } = useLimitRequestState(reqId, !!request?.id);

  const renderDesiredDate = (): string => moment(request?.desiredDate).format(DATE_FORMAT.DATE_WITH_TIME);

  const renderRestOfLimit = (): string => `${request?.getRestOfLimitInRubles(true) || 0} ${RUBLE_SIGN}`;

  return [
    [ft.id, request?.humanReadableId || ''],
    [ft.passenger, employee?.fullNameWithCode || ''],
    [ft.department, department],
    [ft.desiredDate, renderDesiredDate()],
    [ft.transportType, request?.transportTypeString || ''],
    [ft.tripType, request?.coopTrip ? TripTypes.shared : TripTypes.single],
    [ft.passengerCount, request?.passengerCount || ''],
    [ft.purpose, request?.purpose.label ?? 'не выбрана'],
    [ft.route, renderDiffRoute({
      updatedTripRequestRoute, request, onChangeRequest,
    })],
    [ft.limitType, limitTypeString],
    [ft.restOfLimit, renderRestOfLimit()],
    [ft.totalTripDuration, getTimeString(actualRoute.time)],
    [ft.totalTripDistance, getDistanceString(actualRoute.distance)],
    [ft.totalTripCost, actualRoute.costString],
  ];
}

export const useUpdatedTripHandlers = ({
  approvalId,
  back,
  toggleReasonModal,
}: {
  approvalId: string | undefined;
  back(): void;
  toggleReasonModal: () => void;
}): {
    handleApprove: () => void;
    handleDecline: (id: string, reason: IDeclineReason) => void;
  } => {
  const { logger } = useAppStoreContext();
  const [tripApprove] = useUpdatedTripApprove();
  const [tripDecline] = useUpdatedTripDecline();

  const handleApprove = () => {
    if (approvalId) {
      tripApprove({ id: approvalId }).then(() => {
        back();
      });
    } else {
      logger.toMessage('error', 'Не найден id согласования');
    }
  };

  const handleDecline = (id: string, reason: IDeclineReason) => {
    tripDecline({ id, reason }).then(() => {
      toggleReasonModal();
      back();
    });
  };

  return { handleApprove, handleDecline };
};
