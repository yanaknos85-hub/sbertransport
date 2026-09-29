import React, {
  FC, Suspense, useEffect, SyntheticEvent
} from 'react';

import { ArrowLeftOutlined } from '@ant-design/icons';
import { Link, useHistory, useRouteMatch } from 'react-router-dom';
import { useRegistryJournalData } from 'api/public-register-search';

import { useTripRequest } from 'api/registry';
import { UUID } from 'utils/io-ts';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import styles from './styles.module.scss';
import { useDescriptionItemRecords } from './UseDescriptionItemRecords';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { observer } from 'mobx-react';
import { useTranslation } from 'i18n';
import Statuses from 'modules/Registry/components/Statuses/Statuses';
import * as routes from 'constants/constants.routes';
import { formatFullName } from 'utils/formatFullName';

export const DetailedView: FC = observer(() => {
  const { t } = useTranslation();
  const labels = t.Forms.registryFilterFields;
  const match = useRouteMatch<{ id: string }>();
  const { id: requestId } = match.params;
  const { data: tripResponse, isLoading } = useTripRequest({
    transportType: 'PUBLIC',
    requestId: requestId as UUID,
  });
  const { approvedBy } = tripResponse;
  const { passengerStore } = useAppStoreContext();
  const { data: reportData } = useRegistryJournalData(
    // @ts-ignore
    { pageSetting: { page: 0, size: 1 }, id: tripResponse.id },
    tripResponse.passenger?.organizationId
  );
  const {
    generalInfo,
    userInfo,
    tripInfo,
    sctructureInfo,
    planFactInfo,
    additionalInfo,
  } = useDescriptionItemRecords(tripResponse, passengerStore.relatedOrder, reportData.responseData);
  const history = useHistory();

  useEffect(() => {
    if (tripResponse.payRequestIds?.length) {
      passengerStore.getRelatedOrder(tripResponse.payRequestIds[0] as UUID, TransportTypes.PERSONAL);
    }
  }, []);

  useEffect(() => {
    return () => passengerStore.resetRelatedOrder();
  }, []);

  if (isLoading) {
    return <SpinWrapped />;
  }

  const goBack = (e: SyntheticEvent) => {
    e.preventDefault();
    history.goBack();
  };

  const tableRow = (
    label: string | JSX.Element,
    description: string | number | string[] | number[] | null | undefined,
    i: number
  ) => {
    return (
      <div className={styles.tableRow} key={i}>
        <span className={styles.tableRow__title}>{label}</span>
        {label === labels.relatedApplication && tripResponse.payRequestIds?.length ? (
          <a className={styles.tableRow__link} href={`${routes.REGISTRY_PASSENGERS_PUBLIC}/${tripResponse.payRequestIds[0]}`}>
            {description}
          </a>
        ) : label === labels.requestStatus ? (
          <Statuses
            currentName={description as string}
            transportType={tripResponse.transportType}
            tripId={tripResponse.id}
            statusCodeDescription={tripResponse.statusCodeDescription}
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
    <div className={styles.detailedView}>
      <div className={styles.header}>
        <Link
          className={styles.header__title}
          to={routes.REGISTRY_PASSENGERS_TAXI}
          onClick={goBack}
        >
          <ArrowLeftOutlined />
          {`Заявка ${tripResponse.humanReadableId}`}
        </Link>
      </div>
      <Suspense fallback={<SpinWrapped />}>
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
      </Suspense>
    </div>
  );
});
