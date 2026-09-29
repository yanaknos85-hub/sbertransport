import React, { FC, useEffect, Suspense } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';
import { UUID } from 'utils/io-ts';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import ShowOrder from './ShowOrder';
import { Source } from '../../../interfaces/Orders.types';

import '../styles.scss';

const MonitorCargoOrderDetailed: FC = () => {
  const { id, source } = useRouteMatch<{ id: UUID; source: Source }>().params;
  const { cargoStore } = useAppStoreContext();

  const mainClass = 'orderDetailed';

  useEffect(() => {
    cargoStore.getCargoOrderActive(id, source);

    return () => {
      cargoStore.clearOrderActive();
    };
  }, [id, source]);

  return (
    <Suspense fallback={<SpinWrapped />}>
      <div className={mainClass}>
        {cargoStore?.cargoOrderActive ? (
          <ShowOrder data={cargoStore.cargoOrderActive} className={mainClass} />
        ) : (
          <SpinWrapped />
        )}
      </div>
    </Suspense>
  );
};

export default observer(MonitorCargoOrderDetailed);
