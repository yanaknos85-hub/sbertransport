import { TripResponse } from 'stores/Registry/Registry.interface';
import React, { FC, useEffect } from 'react';
import { useTranslation } from 'i18n';
import { usePersonalSearch } from 'api/personal-search';
import { useIndividualTripDescription } from '../../hooks/useIndividualTripDescription';
import { UUID } from 'utils/io-ts';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import styles from './styles.module.scss';
import { observer } from 'mobx-react';
import * as routes from 'constants/constants.routes';
import Statuses from 'modules/Registry/components/Statuses/Statuses';
import { formatFullName } from 'utils/formatFullName';

export interface IndividualTripProps { trip: TripResponse }

export const IndividualTrip: FC<IndividualTripProps> = observer(({ trip }) => {
  const { t } = useTranslation();
  const labels = t.Forms.registryFilterFields;
  const { passengerStore } = useAppStoreContext();

  const { data: reportData } = usePersonalSearch(
    { pageSetting: { page: 0, size: 1 }, id: trip.id },
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
  } = useIndividualTripDescription(trip, reportData, labels, passengerStore.relatedOrder);

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
            isSharedRideSub={!!reportData?.content[0]?.coopTrip && !reportData?.content[0]?.sharedRideOwner}
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
});
