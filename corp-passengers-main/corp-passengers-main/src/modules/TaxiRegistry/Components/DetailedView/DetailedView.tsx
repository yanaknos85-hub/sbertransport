import React, {
  FC, Suspense, SyntheticEvent, useState
} from 'react';
import { Link, useHistory, useRouteMatch } from 'react-router-dom';
import { useTranslation } from 'i18n';

import { useTripRequest, useTripRequestWithFactData, useTaxiReintegration } from 'api/registry';

import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import * as routes from 'constants/constants.routes';

import { ArrowLeftOutlined } from '@ant-design/icons';
import { Button } from 'antd';
import { UUID } from 'utils/io-ts';
import { TRANSPORT_TYPE } from 'stores/Limits/Models/ResharedDepartmentLimits';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import styles from './styles.module.scss';
import { IndividualTrip } from './IndividualTrip';

const DetailedView: FC = () => {
  const [isDisabled, setIsDisabled] = useState(false);
  const match = useRouteMatch<{ id: string }>();
  const { id } = match.params;
  const history = useHistory();
  const { t } = useTranslation();

  const { data: tripResponse } = useTripRequest({ transportType: TRANSPORT_TYPE.TAXI, requestId: id as UUID });
  const { data: factTrip } = useTripRequestWithFactData({ transportType: TRANSPORT_TYPE.TAXI, requestId: id as UUID });
  const [taxiReintegration] = useTaxiReintegration();

  const goBack = (e: SyntheticEvent) => {
    e.preventDefault();
    history.goBack();
  };

  const handleClick = () => {
    taxiReintegration({ requestId: id as UUID });
    setIsDisabled(true);
  };

  return (
    <div className={styles.detailedView}>
      <div className={styles.detailedView__header}>
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
        <Button
          onClick={handleClick}
          type="primary"
          disabled={isDisabled}
        >
          {t.Registry.registryReintegrationButton}
        </Button>
      </div>
      <Suspense fallback={<SpinWrapped />}>
        <IndividualTrip trip={tripResponse} factTrip={factTrip} />
      </Suspense>
    </div>
  );
};
export default withErrorBoundary(DetailedView);
