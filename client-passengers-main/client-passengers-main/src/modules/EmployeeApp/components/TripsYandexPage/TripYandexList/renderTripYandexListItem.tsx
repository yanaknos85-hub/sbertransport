import React from 'react';
import { Button, Divider, List } from 'antd';
import { YandexTaxiRequest } from 'api/yandexTaxi/yandex-taxi.types';

import '../../TripsPage/TripRequestList/item.scss';
import { TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { DATE_FORMAT } from 'constants/constants.app';
import moment from 'moment';
import cn from 'classnames';
import RouteMap from 'utils/RouteMap/RouteMap';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { plainToNew } from 'utils';
import { YandexTaxiRequestStatus } from 'api/yandexTaxi/yandex-taxi.constants';
import { YandexTripStatusBadge } from '../components/YandexTripStatusBadge/YandexTripStatusBadge';
import { ReactComponent as WarningIcon } from 'shared/components/Images/warning.svg';

import styles from './item.module.scss';
import { getFraudComments } from 'utils/fraud/getFraudComments';

interface Props {
  request: YandexTaxiRequest;
  purposeLabel: string;
  isFinishedStatus: boolean;
  onCancel: (request: YandexTaxiRequest) => void;
  onConfirm: (request: YandexTaxiRequest) => void;
}

export const RenderTripYandexListItem = ({
  request,
  purposeLabel,
  onCancel,
  onConfirm,
  isFinishedStatus,
}: Props) => {
  const onSubmit = (event: React.MouseEvent<HTMLButtonElement>) => {
    event.stopPropagation();
    onConfirm(request);
  };

  const handleOnCancel = (event: React.MouseEvent<HTMLButtonElement>) => {
    event.stopPropagation();
    onCancel(request);
  };

  const isAbleToCancelTrip = [YandexTaxiRequestStatus.CONFIRMATION, YandexTaxiRequestStatus.CONFIRMED].includes(request.status as YandexTaxiRequestStatus);

  const fraudComments = getFraudComments(request.fraudComment);
  const hasFraud = fraudComments.length !== 0;

  return (
    <List.Item onClick={() => onConfirm(request)}>
      <div className={cn(styles.containerWrapper, { [styles.fraudListItem]: hasFraud })}>
        { hasFraud && (
        <div className={styles.fraudContainer}>
          <WarningIcon />
          Нарушение правил оформления поездки
        </div>
        )}
        <div className={styles.container}>
          <div className={styles.header}>
            <div className={styles.titleContainer}>
              <span className={styles.title}>
                {request.humanReadableId}
              </span>
              { request?.status && (
              <YandexTripStatusBadge status={request.status as YandexTaxiRequestStatus} />
              )}
            </div>
            <span className={styles.date}>
              {moment(request.tripDate).format(DATE_FORMAT.MONTH_NAME_NOT_YEAR_WITH_TIME)}
            </span>
            <span className={styles.subTitle}>
              {purposeLabel}
            </span>
          </div>
          <Divider />
          <div className={styles.transportType}>
            <div className={styles.transportTypeWrapper}>
              <div className={styles.TransportPictureWrapper}>
                <div className={styles.yandexTaxiImage} />
              </div>
              <span>{TransportTypeTitlesEnum.YANDEX}</span>
            </div>
          </div>
          <Divider />
          <div className={styles.mapWrapper}>
            <RouteMap
              className={styles.routeMap}
              addresses={plainToNew<WaypointModel[]>(WaypointModel, request.waypoints)}
            />
          </div>
          { !isFinishedStatus && request.status !== YandexTaxiRequestStatus.ORDER_PAYMENT_FORMATION && (
          <>
            <Divider />
            <div className={styles.buttonsWrapper}>
              <Button className={styles.actionButton} onClick={handleOnCancel}>
                Отменить
              </Button>
              {
                !isAbleToCancelTrip && (
                <Button
                  onClick={onSubmit}
                  className={cn(styles.actionButton, styles.approveButton)}
                >
                  Подтвердить
                </Button>
                )
              }
            </div>
          </>
          )}
        </div>
      </div>
    </List.Item>
  );
};
