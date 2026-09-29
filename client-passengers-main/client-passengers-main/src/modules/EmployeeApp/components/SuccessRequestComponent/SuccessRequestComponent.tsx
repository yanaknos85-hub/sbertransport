import { Result } from 'antd';
import { observer } from 'mobx-react';
import React from 'react';
import type { FC } from 'react';
import { Link } from 'react-router-dom';

import * as routes from 'constants/constants.routes';

import { EmployeeAppLinksTitles } from 'modules/EmployeeApp/EmployeeApp.constants';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

const SuccessRequestComponent: FC = observer(() => {
  const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();

  const humanReadableId = tripStore.lastTipRequest?.humanReadableId;
  const id = tripStore.lastTipRequest?.id;

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
              <Link key="new" to={`${routes.TRIPS_LIST}/planned/${id}`}>
                {humanReadableId}
              </Link>
              {' '}
            </>
          )}
          создана и доступна в разделе
          {' '}
          <Link key="new" to={routes.TRIPS_LIST}>
            «
            {EmployeeAppLinksTitles.trips}
            »
          </Link>
        </>
      )}
      extra={[
        <Link key="new" to={routes.TRIPS_CREATE_TAXI}>
          Создать новую заявку
        </Link>,
        <br key="br" />,
        <Link key="trips" to={routes.TRIPS}>
          {EmployeeAppLinksTitles.trips}
        </Link>,
      ]}
    />
  );
});

export default SuccessRequestComponent;
