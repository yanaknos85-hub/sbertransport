
import React, { FC, Suspense, SyntheticEvent } from 'react';
import { Link, useHistory, useRouteMatch } from 'react-router-dom';
import { ArrowLeftOutlined } from '@ant-design/icons';

import * as routes from 'constants/constants.routes';
import { TRANSPORT_TYPE } from 'stores/Limits/Models/ResharedDepartmentLimits';

import { UUID } from 'utils/io-ts';
import { useTripRequest } from 'api/registry';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { IndividualTrip } from './IndividualTrip';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';

import styles from './styles.module.scss';

const DetailedView: FC = () => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const match = useRouteMatch<any>();
  const history = useHistory();
  const { id } = match.params;
  const { data: tripResponse } = useTripRequest({ transportType: TRANSPORT_TYPE.CARSHARING, requestId: id as UUID });

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
        <IndividualTrip trip={tripResponse} />
      </Suspense>
    </div>
  );
};

export default withErrorBoundary(DetailedView);
