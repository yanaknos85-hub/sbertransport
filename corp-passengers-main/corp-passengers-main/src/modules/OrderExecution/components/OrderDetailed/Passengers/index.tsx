import React, {
  FC, useEffect, Suspense, useCallback
} from 'react';
import { useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';

import { UUID } from 'utils/io-ts';
import { useProfile } from 'api/profile';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { Category } from 'modules/OrderExecution/constants/Tabs';
import * as routes from 'constants/constants.routes';
import ShowOrder from './ShowOrder';

import '../styles.scss';

const getTransportType = (path: string) => path.replace(`${routes.ORDER_EXECUTION_PASSENGERS}/`, '').replace('/:id', '').toLocaleUpperCase();

const MonitorPassengersDetailed: FC = () => {
  const {
    params: { id },
    path,
  } = useRouteMatch<{ id: UUID }>();
  const { passengerStore } = useAppStoreContext();
  const { data: profile } = useProfile();

  const routeTransportType = getTransportType(path);
  const transportType = routeTransportType.toLowerCase() === Category.bus
    ? Category.taxi.toUpperCase()
    : routeTransportType;
  const mainClass = 'orderDetailed passengersDetailed';

  useEffect(() => {
    passengerStore.getOrder(id, transportType);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const update = useCallback(() => {
    passengerStore.getOrder(id, transportType);
    if (profile.isOrganization) {
      passengerStore.getPersonOrderList();
    } else {
      passengerStore.getPersonOrderListExec();
    }
  }
  , [id]);

  return (
    <Suspense fallback={<SpinWrapped />}>
      <div className={mainClass}>
        {passengerStore.order ? (
          <ShowOrder
            data={passengerStore.order ?? {}}
            className={mainClass}
            update={update}
          />
        ) : (
          <SpinWrapped />
        )}
      </div>
    </Suspense>
  );
};

export default observer(MonitorPassengersDetailed);
