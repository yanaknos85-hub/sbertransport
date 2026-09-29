import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import { YandexTaxiReportItem } from 'api/yandexTaxiRegistry/yandex-taxi-registry.types';

import {
  YandexTaxiRequestStatus,
  YandexTaxiRequestStatusTitles,
  YandexTaxiTariffTitles
} from 'api/yandexTaxiRegistry/yandex-taxi-registry.constants';
import { useAllEmployeesById } from 'api/employee';
import { formatTime } from 'utils/formatTime';
import { formatPassengerName } from 'utils/formatPassengerName';
import { DATE_FORMAT } from 'constants/constants.app';
import { formatRoute } from '../../utils/data';
import { Receipt } from './Receipt';

import styles from './styles.module.scss';

const EMPTY_VALUE = '-';

export interface YandexTaxiTripProps {
  trip: YandexTaxiReportItem;
}

export const DetailedTrip: FC<YandexTaxiTripProps> = ({ trip }) => {
  const { t } = useTranslation();
  const labels = t.Forms.informationAttributesOfRegistries;
  const { data: { byId: employees } } = useAllEmployeesById([trip.passengerId!]);

  const tripInfo = [
    [labels.passengerFioVisible, formatPassengerName(employees[trip.passengerId! as string])],
    [labels.taxiClass, YandexTaxiTariffTitles[trip.tariff]],
    [labels.desiredDate, formatTime(trip.tripDate, '-', DATE_FORMAT.DATE_WITH_TIME_DOTS)],
    [labels.route, formatRoute(trip.waypoints)],
    [labels.expectedCost, trip.plannedCost || EMPTY_VALUE],
    [labels.tripFactPrice, trip.factCost || EMPTY_VALUE],
    [labels.status, YandexTaxiRequestStatusTitles[trip.status as YandexTaxiRequestStatus]],
    [labels.authorComment, trip.comment || EMPTY_VALUE],
    [labels.denialReason, trip.reason || EMPTY_VALUE],
  ];

  const tableRow = (
    label: string | JSX.Element,
    description: string | number | string[] | number[] | null | undefined,
    key: string
  ) => {
    return (
      <div className={styles.tableRow} key={key}>
        <span className={styles.tableRow__title}>{label}</span>
        <span className={styles.tableRow__label}>{description}</span>
      </div>
    );
  };

  return (
    <div className={styles.tablesWrapper}>
      <div className={styles.tablesColumn}>
        <div className={styles.table}>
          <span className={styles.table__title}>Общие данные по заявке</span>
          <div className={styles.table__rows}>
            {tripInfo.map(([label, description]) => {
              return tableRow(label, description, label);
            })}
          </div>
        </div>
      </div>
      <div className={styles.tablesColumn}>
        {trip.receipt && <Receipt fileName={trip.receipt} requestId={trip.id!} />}
      </div>
    </div>
  );
};
