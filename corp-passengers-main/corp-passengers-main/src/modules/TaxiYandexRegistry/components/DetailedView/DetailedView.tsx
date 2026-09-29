import React, {
  FC, Suspense, SyntheticEvent
} from 'react';
import { Link, useHistory, useRouteMatch } from 'react-router-dom';
import { ArrowLeftOutlined } from '@ant-design/icons';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import * as routes from 'constants/constants.routes';
import { useYandexTaxiRegistryById } from 'api/yandexTaxiRegistry/yandex-taxi-registry.api';

import { DetailedTrip } from './DetailedTrip';

import styles from './styles.module.scss';

export const DetailedView: FC = () => {
  const match = useRouteMatch<{ id: string }>();
  const { id } = match.params;
  const history = useHistory();
  const { data } = useYandexTaxiRegistryById(id);

  const goBack = (e: SyntheticEvent) => {
    e.preventDefault();
    history.goBack();
  };

  return (
    <div className={styles.detailedView}>
      <div className={styles.detailedView__header}>
        <div className={styles.header}>
          <Link
            className={styles.header__title}
            to={routes.REGISTRY_PASSENGERS_YANDEX_TAXI}
            onClick={goBack}
          >
            <ArrowLeftOutlined />
            {`Заявка ${data?.humanReadableId}`}
          </Link>
        </div>

      </div>
      <Suspense fallback={<SpinWrapped />}>
        <DetailedTrip trip={data} />
      </Suspense>
    </div>
  );
};
