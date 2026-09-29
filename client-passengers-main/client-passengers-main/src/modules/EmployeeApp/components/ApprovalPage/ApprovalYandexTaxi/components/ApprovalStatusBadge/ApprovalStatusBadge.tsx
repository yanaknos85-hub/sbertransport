import React, { FC } from 'react';
import classNames from 'classnames';

import { YandexTaxiRequestStatus } from 'api/yandexTaxi/yandex-taxi.constants';

import styles from './approvalStatusBadge.module.scss';

interface ApprovalStatusBadgeProps {
  status: YandexTaxiRequestStatus;
  title: string;
}

export const ApprovalStatusBadge: FC<ApprovalStatusBadgeProps> = ({ status, title }) => {
  const style = classNames(
    styles.badge,
    {
      [styles.positive]: status === YandexTaxiRequestStatus.CONFIRMED
      || status === YandexTaxiRequestStatus.PAYMENT_DONE,
      [styles.error]:
        status === YandexTaxiRequestStatus.CANCELLED
        || status === YandexTaxiRequestStatus.DECLINED
        || status === YandexTaxiRequestStatus.PAYMENT_NOT_DONE,
      [styles.change]:
        status === YandexTaxiRequestStatus.NEW
        || status === YandexTaxiRequestStatus.CONFIRMATION_NEEDED
        || status === YandexTaxiRequestStatus.CONFIRMATION
        || status === YandexTaxiRequestStatus.DATA_NEEDED
        || status === YandexTaxiRequestStatus.PAYMENT_AWAITING
        || status == YandexTaxiRequestStatus.ORDER_PAYMENT_FORMATION
        || status === YandexTaxiRequestStatus.GENAI_CHECK,
    }
  );

  return (
    <span className={style}>{ title }</span>
  );
};
