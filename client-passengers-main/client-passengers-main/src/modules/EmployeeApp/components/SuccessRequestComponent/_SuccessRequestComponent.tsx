import { Result } from 'antd';
import { observer } from 'mobx-react';
import React, { useEffect, useState } from 'react';
import type { FC } from 'react';
import { Link } from 'react-router-dom';

import * as routes from 'constants/constants.routes';

import { EmployeeAppLinksTitles } from 'modules/EmployeeApp/EmployeeApp.constants';

import { StoreNames } from 'stores/StoreNames.enum';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { SpinWrapped } from 'shared/components';

const SuccessRequestComponent: FC = observer(() => {
  const {
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();
  const [loading, setLoading] = useState<boolean>(true);

  const humanReadableId = tripStore.lastTipRequest?.humanReadableId;
  const id = tripStore.lastTipRequest?.id;

  useEffect(() => {
    setTimeout(() => {
      setLoading(false);
    }, 5000);
  }, []);

  return (
    <Result
      status="success"
      title="Всё получилось!"
      subTitle={(
        <>
          Заявка
          {' '}
          {id && humanReadableId && (
            <>
              { loading
                ? <SpinWrapped />
                : <Link key="new" to={`${routes.TRIPS_LIST}/planned/${id}`}>{humanReadableId}</Link>}
              {' '}
            </>
          )}
          создана и доступна в разделе
          {' '}
          <Link key="new" to={routes.TRIPS_LIST_PLANNED}>
            «
            {EmployeeAppLinksTitles.trips}
            »
          </Link>
        </>
      )}
      extra={[
        <Link key="new" to={routes.TRANSPORT_2_0_CREATE}>
          Создать новую заявку
        </Link>,
        <br key="br" />,
        <Link key="trips" to={routes.TRIPS_LIST_PLANNED}>
          {EmployeeAppLinksTitles.trips}
        </Link>,
      ]}
    />
  );
});

export default SuccessRequestComponent;
