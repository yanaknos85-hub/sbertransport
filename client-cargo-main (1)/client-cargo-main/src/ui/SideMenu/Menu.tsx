import React, { useMemo, useState } from 'react';
import { Router, RouterProps } from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import AccessControl from 'shared/components/AccessControl';

import { IS_REMOTE } from 'constants/constants.env';
import {
  APPROVEMENT_CARGOS,
  APPROVEMENT_REGULAR_CARGOS,
  CARGO_CREATE,
  CARGOS,
  EXCHANGE
} from 'constants/constants.routes';
import { useRole } from 'utils/useRole';

import Check from './images/Check';
import Exchange from './images/Exchange';
import House from './images/House';
import Truck from './images/Truck';
import { handleStop, MenuItem, OnlySpoiler } from './MenuItem';

export const Menu: React.FC<{ isExpanded: boolean }> = ({ isExpanded }) => {
  const [approveIsOpen, setApproveIsOpen] = useState(true);
  const [getHistory, setHistory] = useState<RouterProps>({ history: {} } as RouterProps);

  const userRoles = useRole();

  useMemo((): void => {
    // eslint-disable-next-line react-hooks/rules-of-hooks
    setHistory({ history: History() });
  }, []);

  return (
    <Router history={getHistory.history}>
      {/* Журналы */}
      <MenuItem
        to={`${CARGOS}`}
        icon={<Truck />}
        $isExpanded={isExpanded}
      >
        Доставки
      </MenuItem>

      {!IS_REMOTE && (
        <>
          {/* Создание заявки */}
          <MenuItem
            to={CARGO_CREATE}
            icon={<House />}
            $isExpanded={isExpanded}
          >
            Создать доставку
          </MenuItem>
          {/* Биржа */}
          <AccessControl
            userPermissions={userRoles}
            allowedPermissions={['ROLE_COURIER']}
          >
            <MenuItem
              to={EXCHANGE}
              icon={<Exchange />}
              $isExpanded={isExpanded}
            >
              Биржа заявок
            </MenuItem>
          </AccessControl>
          {/* Согласования */}
          <OnlySpoiler
            open={approveIsOpen}
            setOpen={setApproveIsOpen}
            $isExpanded={isExpanded}
          >
            <MenuItem
              to={`${APPROVEMENT_CARGOS}/active`}
              icon={<Check />}
              $hasInnerChilds={true}
              isOpen={approveIsOpen}
              $isExpanded={isExpanded}
            >
              Согласования
            </MenuItem>

            {approveIsOpen && (
              <>
                {[
                  {
                    to: `${APPROVEMENT_CARGOS}/active`,
                    name: 'Доставки/Компенсации',
                  },
                  {
                    to: `${APPROVEMENT_REGULAR_CARGOS}/active`,
                    name: 'Регулярные доставки',
                  },
                ].map(item => (
                  item && item.name && (
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
                  )
                ))}
              </>
            )}
          </OnlySpoiler>
        </>
      )}
    </Router>
  );
};
