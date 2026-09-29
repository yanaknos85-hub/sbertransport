import React, { FC, Suspense, SyntheticEvent } from 'react';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { Link, useHistory, useRouteMatch } from 'react-router-dom';

import { useTripRequest } from 'api/registry';

import withErrorBoundary from 'shared/decorators/withErrorBoundary';

import { UUID } from 'utils/io-ts';
import { TransportTypes } from '../../types/types';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { GroupTransferTrip } from './GroupTransferTrip';
import { useProfile } from 'api/profile';
import { useSearchGroupTransferRegistry } from 'api/group-transfer-registry';
import * as routes from 'constants/constants.routes';

import styles from './detailedView.module.scss';

const DetailedView: FC = () => {
  const match = useRouteMatch<{ id: string }>();
  const { id } = match.params;
  const history = useHistory();

  const { organizationId } = useProfile().data;
  const { data: tripResponse } = useTripRequest({
    transportType: TransportTypes.GROUP_TRANSFER,
    requestId: id as UUID,
  });
  const { data: { content: factTrips } } = useSearchGroupTransferRegistry(
    {
      requestHumanId: tripResponse.humanReadableId,
      pageSetting: {
        page: 0,
        size: 100,
      },
    },
    { enabled: true },
    organizationId
  ); // todo заменить поиск на возврат одной заявки по id, когда будет бэк. данные в реквест и репортс разные - нужны оба

  const goBack = (e: SyntheticEvent) => {
    e.preventDefault();
    history.goBack();
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
        <GroupTransferTrip trip={tripResponse} groupTransferTrip={factTrips[0]} />
      </Suspense>
    </div>
  );
};

export default withErrorBoundary(DetailedView);
