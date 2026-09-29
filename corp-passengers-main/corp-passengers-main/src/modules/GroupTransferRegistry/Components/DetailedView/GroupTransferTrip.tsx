import React, { FC } from 'react';
import { TripResponse } from 'stores/Registry/Registry.interface';
import { GroupTransferReportItem } from 'stores/GroupTransferRegistry/GroupTransferRegistry';
import { useTranslation } from 'i18n';
import { useGroupTransferTripDescriptions } from '../../hooks/useGroupTransferTripDescription';
import Statuses from 'modules/Registry/components/Statuses/Statuses';
import { formatFullName } from 'utils/formatFullName';
import styles from './detailedView.module.scss';

export interface GroupTransferTripProps {
  trip: TripResponse;
  groupTransferTrip: GroupTransferReportItem;
}

export const GroupTransferTrip: FC<GroupTransferTripProps> = ({ trip, groupTransferTrip }) => {
  const { t } = useTranslation();
  const labels = t.Forms.informationAttributesOfRegistries;

  const { approvedBy } = trip;
  const {
    generalInfo,
    userInfo,
    tripInfo,
    sctructureInfo,
    planFactInfo,
    additionalInfo,
  } = useGroupTransferTripDescriptions(trip, groupTransferTrip, labels);

  const tableRow = (
    label: string | JSX.Element,
    description: string | number | string[] | number[] | null | undefined,
    i: number
  ) => (
    <div className={styles.tableRow} key={i}>
      <span className={styles.tableRow__title}>{label}</span>
      {label === labels.requestStatusVisible ? (
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

  return (
    <div className={styles.tablesWrapper}>
      <div className={styles.tablesColumn}>
        <div className={styles.table}>
          <span className={styles.table__title}>Общие данные по заявке</span>
          <div className={styles.table__rows}>
            {generalInfo.map(([label, description], i) => {
              return tableRow(label, description, i);
            })}
          </div>
        </div>
        <div className={styles.table}>
          <span className={styles.table__title}>Данные по пользователю</span>
          <div className={styles.table__rows}>
            {userInfo.map(([label, description], i) => {
              return tableRow(label, description, i);
            })}
          </div>
        </div>
        <div className={styles.table}>
          <span className={styles.table__title}>Данные по поездке</span>
          <div className={styles.table__rows}>
            {tripInfo.map(([label, description], i) => {
              return tableRow(label, description, i);
            })}
          </div>
        </div>
      </div>
      <div className={styles.tablesColumn}>
        <div className={styles.table}>
          <span className={styles.table__title}>Данные по структуре</span>
          <div className={styles.table__rows}>
            {sctructureInfo.map(([label, description], i) => {
              return tableRow(label, description, i);
            })}
          </div>
        </div>
        <div className={styles.table}>
          <span className={styles.table__title}>Плановые и фактические данные</span>
          <div className={styles.table__rows}>
            {planFactInfo.map(([label, description], i) => {
              return tableRow(label, description, i);
            })}
          </div>
        </div>
        <div className={styles.table}>
          <span className={styles.table__title}>Дополнительно</span>
          <div className={styles.table__rows}>
            {additionalInfo.map(([label, description], i) => {
              return tableRow(label, description, i);
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
