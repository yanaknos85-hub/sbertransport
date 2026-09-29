import React, { FC, useEffect } from 'react';
import { observer } from 'mobx-react';
import { useTranslation } from 'i18n';

import { useIndividualTripDescription } from 'modules/CarSharingRegistry/hooks/useIndividualTripDescription';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { UUID } from 'utils/io-ts';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import * as routes from 'constants/constants.routes';
import { useCarSharingReport } from 'api/car-sharing-report';
import Statuses from 'modules/Registry/components/Statuses/Statuses';
import { formatFullName } from 'utils/formatFullName';
import styles from './styles.module.scss';

export interface IndividualTripProps { trip: TripResponse }

export const IndividualTrip: FC<IndividualTripProps> = observer(({ trip }) => {
  const { t } = useTranslation();
  const labels = t.Forms.registryFilterFields;
  const { passengerStore } = useAppStoreContext();

  const { data: reportData } = useCarSharingReport(
    { pageSetting: { page: 0, size: 1 }, requestHumanId: trip.humanReadableId },
    trip.passenger?.organizationId
  );

  const { approvedBy } = trip;
  const {
    generalInfo,
    userInfo,
    tripInfo,
    sctructureInfo,
    planFactInfo,
    additionalInfo,
    estimation,
  } = useIndividualTripDescription(trip, reportData);

  useEffect(() => {
    if (trip.payRequestIds?.length) {
      passengerStore.getRelatedOrder(trip.payRequestIds[0] as UUID, TransportTypes.PUBLIC);
    }
  }, []);

  useEffect(() => {
    return () => passengerStore.resetRelatedOrder();
  }, []);

  const tableRow = (
    label: string | JSX.Element,
    description: string | number | string[] | number[] | null | undefined,
    i: number
  ) => {
    return (
      <div className={styles.tableRow} key={i}>
        <span className={styles.tableRow__title}>{label}</span>
        {label === labels.relatedApplication && trip.payRequestIds?.length ? (
          <a className={styles.tableRow__link} href={`${routes.REGISTRY_PASSENGERS_PUBLIC}/${trip.payRequestIds[0]}`}>
            {description}
          </a>
        ) : label === labels.requestStatus ? (
          <Statuses
            currentName={description as string}
            transportType={trip.transportType}
            tripId={trip.id}
            statusCodeDescription={trip.statusCodeDescription}
            approver={approvedBy ? {
              id: approvedBy.id,
              fullName: formatFullName(approvedBy.firstName, approvedBy.lastName, approvedBy.patronymic),
            } : null}
          />
        ) : (
          <span className={styles.tableRow__label}>{description}</span>
        )}
      </div>
    );
  };

  return (
    <div className={styles.tablesWrapper}>
      <div className={styles.tablesColumn}>
        <div className={styles.tablesColumn_wrapper}>
          <div className={styles.table}>
            <span className={styles.table__title}>Общие данные по заявке</span>
            <div className={styles.table__rows}>
              {generalInfo.map(([label, description], i) => {
                return tableRow(label, description, i);
              })}
            </div>
          </div>
          <div className={styles.table}>
            <span className={styles.table__title}>Инициатор</span>
            <div className={styles.table__rows}>
              {userInfo.map(([label, description], i) => {
                return tableRow(label, description, i);
              })}
            </div>
          </div>
        </div>
        <div className={styles.tablesColumn_wrapper}>
          <div className={styles.table}>
            <span className={styles.table__title}>Поездка</span>
            <div className={styles.table__rows}>
              {tripInfo.map(([label, description], i) => {
                return tableRow(label, description, i);
              })}
            </div>
          </div>
          <div className={styles.table}>
            <span className={styles.table__title}>Стоимость</span>
            <div className={styles.table__rows}>
              {planFactInfo.map(([label, description], i) => {
                return tableRow(label, description, i);
              })}
            </div>
          </div>
        </div>
        <div className={styles.tablesColumn_wrapper}>
          <div className={styles.table}>
            <span className={styles.table__title}>Маршрут</span>
            <div className={styles.table__rows}>
              {sctructureInfo.map(([label, description], i) => {
                return tableRow(label, description, i);
              })}
            </div>
          </div>
          <div className={styles.table}>
            <span className={styles.table__title}>Транспортное средство</span>
            <div className={styles.table__rows}>
              {additionalInfo.map(([label, description], i) => {
                return tableRow(label, description, i);
              })}
            </div>
          </div>
        </div>
        <div className={styles.tablesColumn_wrapper}>
          <div className={styles.table}>
            <span className={styles.table__title}>Оценка</span>
            <div className={styles.table__rows}>
              {estimation.map(([label, description], i) => {
                return tableRow(label, description, i);
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
});
