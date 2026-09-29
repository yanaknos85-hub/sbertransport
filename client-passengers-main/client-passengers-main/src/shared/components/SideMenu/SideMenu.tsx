/* eslint-disable no-unused-expressions */

import React, { useState } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { CargosTabsFilters, EmployeeAppLinks, EmployeeAppLinksTitles } from 'modules/EmployeeApp/EmployeeApp.constants';

import useNavigator from 'utils/navigator/useNavigator';

import Book from '../Images/view/menu 2.0/Book';
import Bubble from '../Images/view/menu 2.0/Bubble';
import Car from '../Images/view/menu 2.0/Car';
import Check from '../Images/view/menu 2.0/Check';
import ChevronRight from '../Images/view/menu 2.0/ChevronRight';
import Diagram from '../Images/view/menu 2.0/Diagram';
import House from '../Images/view/menu 2.0/House';
import Letter from '../Images/view/menu 2.0/Letter';
import Question from '../Images/view/menu 2.0/Question';
import TextBubble from '../Images/view/menu 2.0/TextBubble';
import Truck from '../Images/view/menu 2.0/Truck';
import Box from '../Images/view/menu 2.0/Box';
import {
  BottomMenu, Expander, Spacer, StyledMenu
} from './styled';
import { handleStop, MenuItem, OnlySpoiler } from './MenuItem';

const dataNumbers = ['1729581', '1654708', '1807030', '1806729', '999048409', '01795630', '1898468', '1898931'];

const SideMenu: React.FC<{ personnelNumber: string }> = ({ personnelNumber }) => {
  const match = useRouteMatch();

  const {
    isApprovementsSubPage,
    isCreateSubPage,
    isLimitsSubPage,
    isTripsSubPage,
    isCargosSubPage,
    isRegularCargosSubPage,
    isFavoriteSubPage,
  } = useNavigator();

  const [limitsIsOpen, setLimitsOpen] = useState(isLimitsSubPage() || false);
  const [approvIsOpen, setApprovOpen] = useState(isApprovementsSubPage() || false);
  const [isExpanded, expand] = useState(false);

  const hideMenuItem = !dataNumbers?.includes(personnelNumber);

  return (
    <StyledMenu isExpanded={isExpanded} className="side-menu">
      <MenuItem
        to={`${match.path}/create`}
        icon={<House />}
        isActive={isCreateSubPage}
        $isExpanded={isExpanded}
      >
        Главная
      </MenuItem>
      <MenuItem
        to={`${match.path}/favorite`}
        icon={<Book />}
        isActive={isFavoriteSubPage}
        $isExpanded={isExpanded}
      >
        Мои адреса
      </MenuItem>
      <MenuItem
        to={`${match.path}/${EmployeeAppLinks.trips}`}
        icon={<Car />}
        isActive={isTripsSubPage}
        $isExpanded={isExpanded}
      >
        Поездки
      </MenuItem>
      <MenuItem
        to={`${match.path}/${EmployeeAppLinks.cargos}`}
        icon={<Box />}
        isActive={isCargosSubPage}
        $isExpanded={isExpanded}
      >
        Доставки
      </MenuItem>

      {!hideMenuItem && (
        <MenuItem
          to={`${match.path}/${EmployeeAppLinks.regularCargos}`}
          icon={<Truck />}
          isActive={isRegularCargosSubPage}
          $isExpanded={isExpanded}
        >
          Регулярная доставка
        </MenuItem>
      )}

      <OnlySpoiler
        open={approvIsOpen}
        setOpen={setApprovOpen}
        $isExpanded={isExpanded}
      >
        <MenuItem
          to={`${match.path}/approvement/requests/active`}
          icon={<Check />}
          isActive={isApprovementsSubPage}
          $hasInnerChilds
          isOpen={approvIsOpen}
          $isExpanded={isExpanded}
        >
          Согласования
        </MenuItem>

        {approvIsOpen && (
          <>
            {[
              {
                to: `${match.path}/${EmployeeAppLinks.approvement}/requests/active`,
                name: EmployeeAppLinksTitles[EmployeeAppLinks.trips],
              },
              {
                to: `${match.path}/${EmployeeAppLinks.approvement}/${EmployeeAppLinks.cargos}/${CargosTabsFilters.active}`,
                name: EmployeeAppLinksTitles[EmployeeAppLinks.cargos],
              },
              {
                to: `${match.path}/${EmployeeAppLinks.approvement}/${EmployeeAppLinks.regularCargos}/active`,
                name: EmployeeAppLinksTitles[EmployeeAppLinks.regularCargos],
                hide: hideMenuItem,
              },
              {
                to: `${match.path}/${EmployeeAppLinks.approvement}/limits/active`,
                name: EmployeeAppLinksTitles[EmployeeAppLinks.limitsInfo],
              },
              {
                to: `${match.path}/${EmployeeAppLinks.approvement}/delegates`,
                name: EmployeeAppLinksTitles[EmployeeAppLinks.delegates],
              },
            ].map(item => !item.hide ? (
              <MenuItem
                key={item.name}
                to={item.to}
                icon={<Check />}
                onClick={handleStop}
                $isExpanded={isExpanded}
                $isSubMenu
              >
                {item.name}
              </MenuItem>
            ) : null
            )}
          </>
        )}
      </OnlySpoiler>

      <OnlySpoiler
        open={limitsIsOpen}
        setOpen={setLimitsOpen}
        $isExpanded={isExpanded}
      >
        <MenuItem
          to={`${match.path}/${EmployeeAppLinks.limits}/limitsInfo`}
          icon={<Diagram />}
          isActive={isLimitsSubPage}
          $hasInnerChilds
          isOpen={limitsIsOpen}
          $isExpanded={isExpanded}
        >
          Финансы
        </MenuItem>

        {limitsIsOpen && (
          <>
            {[
              { to: `${match.path}/${EmployeeAppLinks.limits}/limitsInfo`, name: 'Лимиты' },
              {
                to: `${match.path}/${EmployeeAppLinks.bonuses}/account`,
                name: `${EmployeeAppLinksTitles[EmployeeAppLinks.bonusesAccount]}`,
              },
              { to: `${match.path}/${EmployeeAppLinks.limits}/limitRequests/active`, name: 'Заявки на лимиты' },
            ].map(item => (
              <MenuItem
                key={item.name}
                to={item.to}
                icon={<Diagram />}
                onClick={handleStop}
                $isExpanded={isExpanded}
                $isSubMenu
              >
                {item.name}
              </MenuItem>
            ))}
          </>
        )}
      </OnlySpoiler>

      <MenuItem to={`${match.path}/${EmployeeAppLinks.support}`} icon={<Bubble />}>
        Поддержка
      </MenuItem>
      <Spacer />
      <BottomMenu>
        {isExpanded && (
          <>
            <MenuItem to={`${match.path}/messages`} icon={<Question />} />
            <MenuItem to={`${match.path}/descriptions`} icon={<TextBubble />} />
            <MenuItem to={`${match.path}/chat`} icon={<Letter />} />
          </>
        )}
        <Expander onClick={() => expand(!isExpanded)} isRotate={isExpanded}>
          <ChevronRight />
        </Expander>
      </BottomMenu>
    </StyledMenu>
  );
};

export default SideMenu;
