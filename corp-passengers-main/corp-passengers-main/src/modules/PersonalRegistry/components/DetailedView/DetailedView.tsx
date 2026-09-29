import { useTripRequest } from 'api/registry';

import React, { FC, Suspense, SyntheticEvent } from 'react';
import { Link, useHistory, useRouteMatch } from 'react-router-dom';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { UUID } from 'utils/io-ts';
import * as routes from 'constants/constants.routes';

import { ArrowLeftOutlined } from '@ant-design/icons';

import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import styles from './styles.module.scss';
import { TRANSPORT_TYPE } from 'stores/Limits/Models/ResharedDepartmentLimits';
import { IndividualTrip } from './IndividualTrip';

const DetailedView: FC = () => {
  const match = useRouteMatch<{ id: string }>();
  const { id } = match.params;
  const history = useHistory();

  const { data: tripResponse } = useTripRequest({ transportType: TRANSPORT_TYPE.PERSONAL, requestId: id as UUID });

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
