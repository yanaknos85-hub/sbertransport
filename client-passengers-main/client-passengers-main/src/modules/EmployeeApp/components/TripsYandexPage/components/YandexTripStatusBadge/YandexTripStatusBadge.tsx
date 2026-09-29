import React from 'react';
import { YandexTaxiRequestStatus, YandexTaxiRequestStatusTitles } from 'api/yandexTaxi/yandex-taxi.constants';
import cn from 'classnames';

import styles from './YandexTripStatusBadge.module.scss';

interface YandexTripStatusBadgeProps {
  status: YandexTaxiRequestStatus;
}

export const YandexTripStatusBadge = ({ status }: YandexTripStatusBadgeProps) => {
  const getStatusColor = (status: YandexTaxiRequestStatus) => {
    switch (status) {
      case YandexTaxiRequestStatus.PAYMENT_DONE:
        return 'positive';
      case YandexTaxiRequestStatus.DECLINED:
      case YandexTaxiRequestStatus.PAYMENT_NOT_DONE:
        return 'error';
      case YandexTaxiRequestStatus.NEW:
      case YandexTaxiRequestStatus.PAYMENT_AWAITING:
      case YandexTaxiRequestStatus.CONFIRMATION_NEEDED:
      case YandexTaxiRequestStatus.CONFIRMATION:
      case YandexTaxiRequestStatus.ORDER_PAYMENT_FORMATION:
        return 'change';
      default:
        return 'none';
    }
  };
  return (
    <span className={cn(styles.badge, styles[getStatusColor(status)])}>{ YandexTaxiRequestStatusTitles[status] }</span>
  );
};
