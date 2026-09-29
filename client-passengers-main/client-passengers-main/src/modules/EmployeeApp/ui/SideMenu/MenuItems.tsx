/* eslint-disable no-unused-expressions */
import React, { useMemo, useState } from 'react';
import { Router, RouterProps } from 'react-router-dom';
import { IS_REMOTE } from 'constants/constants.env';
import * as routes from 'constants/constants.routes';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { SubMenu } from './SubMenu';

import Car from './images/Car';
import Check from './images/Check';
import { MenuItem, OnlySpoiler, handleStop } from './MenuItem';
import { isPassengersPage } from './RoutesHelper';

export const MenuItems: React.FC<{ isExpanded: boolean }> = ({ isExpanded }) => {
  const {
    [StoreNames.configStore]: configStore,
  } = useAppStoreContext();
  const { history } = configStore;
  const [approvIsOpen, setApprovOpen] = useState(true);
  const [getHistory, setHistory] = useState<RouterProps>({ history: {} } as RouterProps);

  useMemo((): void => {
    setHistory({ history });
  }, [history]);

  return (
    <Router history={getHistory.history}>
      <MenuItem
        to={`${routes.TRIPS_LIST}/planned`}
        icon={<Car />}
        isActive={isPassengersPage}
        $isExpanded={isExpanded}
      >
        Мои поездки
      </MenuItem>

      {!IS_REMOTE && (
        <>
          <MenuItem
            to={routes.TRANSPORT_2_0_CREATE}
            icon={<Car />}
            $isExpanded={isExpanded}
          >
            Создать поездку
          </MenuItem>
          <MenuItem
            to={routes.TRIPS_CREATE_YANDEX_TAXI}
            icon={<Car />}
            $isExpanded={isExpanded}
          >
            Заказ Яндекс Go
          </MenuItem>
          <MenuItem
            to={routes.TRIPS_CREATE_PUBLIC}
            icon={<Car />}
            $isExpanded={isExpanded}
          >
            Создать публичную поездку
          </MenuItem>
          <MenuItem
            to={routes.TRIPS_CREATE_PERSONAL}
            icon={<Car />}
            $isExpanded={isExpanded}
          >
            Создать персональную поездку
          </MenuItem>
          <MenuItem
            to={routes.TRIPS_CREATE_COOPERATIVE}
            icon={<Car />}
            $isExpanded={isExpanded}
          >
            Корпоративный транспорт
          </MenuItem>
          <MenuItem
            to={routes.TRIPS_CREATE_CARSHARING}
            icon={<Car />}
            $isExpanded={isExpanded}
          >
            Каршеринг
          </MenuItem>
          <OnlySpoiler
            open={approvIsOpen}
            setOpen={setApprovOpen}
            $isExpanded={isExpanded}
          >
            <MenuItem
              to={routes.APPROVEMENT}
              icon={<Check />}
              $hasInnerChilds={true}
              isOpen={approvIsOpen}
              $isExpanded={isExpanded}
            >
              Согласования
            </MenuItem>

            {approvIsOpen && (
              <>
                {[
                  {
                    to: `${routes.APPROVEMENT_TRIPS}/active`,
                    name: 'Поездки',
                  },
                ].map(item => (
                  <MenuItem
                    key={item.name}
                    to={item.to}
                    icon={<Check />}
                    onClick={handleStop}
                    $isExpanded={isExpanded}
                    $isSubMenu={true}
                  >
                    {item.name}
                  </MenuItem>
                ))}
              </>
            )}
          </OnlySpoiler>
        </>
      )}
    </Router>
  );
};

export {
  MenuItems as Menu,
  SubMenu
};
