/* eslint-disable no-unused-expressions */
import { OrgStructureType } from '@sber-sbertransport/mf-core/dist/constants/constants';
import React, { useEffect, useMemo, useState } from 'react';
import { NavLink, useRouteMatch } from 'react-router-dom';
import { IS_DEV } from 'constants/constants.env';
import * as routes from 'constants/constants.routes';
import { EmployeeAppLinksTitles } from 'constants/constants.app';
import useNavigator from 'utils/navigator/useNavigator';
import Check from 'shared/components/Images/view/menu 2.0/Check';
import ChevronRight from 'shared/components/Images/view/menu 2.0/ChevronRight';
import House from 'shared/components/Images/view/menu 2.0/House';
import { MenuItem, OnlySpoiler, handleStop } from './MenuItem';
import {
  BottomMenu,
  Expander,
  IconContainer,
  Spacer,
  StyledMenu,
  TitleMenu,
  TitleMenuItems,
  WrapperInfoMenuItem
} from './styled';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import AccessControl from 'shared/components/AccessControl';
import { StoreNames } from 'stores/StoreNames.enum';
import {
  Divider, Dropdown, Menu
} from 'antd';
import { observer } from 'mobx-react';
import Profile from './components/profile/Profile';
import { ReactComponent as Car } from 'shared/components/Images/menuIcons/Car.svg';
import { ReactComponent as Delivery } from 'shared/components/Images/menuIcons/Delivery.svg';
import { ReactComponent as BidCargo } from 'shared/components/Images/menuIcons/bidCargo.svg';
import { ReactComponent as BidTrip } from 'shared/components/Images/menuIcons/bidTrip.svg';
import { ReactComponent as Bonus } from 'shared/components/Images/menuIcons/bonus.svg';
import { ReactComponent as CarService } from 'shared/components/Images/menuIcons/carService.svg';
import { ReactComponent as Delegation } from 'shared/components/Images/menuIcons/delegation.svg';
import { ReactComponent as LimitRequest } from 'shared/components/Images/menuIcons/limitRequest.svg';
import { ReactComponent as Limits } from 'shared/components/Images/menuIcons/limits.svg';
import { ReactComponent as Library } from 'shared/components/Images/menuIcons/library.svg';
import { ReactComponent as Limit } from 'shared/components/Images/menuIcons/limit.svg';
import { ReactComponent as SupportMain } from 'shared/components/Images/menuIcons/supportMain.svg';
import { ReactComponent as Exchange } from 'shared/components/Images/menuIcons/exchange.svg';

import mfDataLoader from 'mf/MFDataLoader';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import TaxiApprovalsCount, { TaxiApprovalCounterType } from './components/TaxiApprovalsCount/TaxiApprovalsCount';
import CargoApprovalsCount from './components/CargoApprovalsCount/CargoApprovalsCount';
import { CargoApprovalCounterType, CargoApprovalMenuNames } from './components/CargoApprovalsCount/constants';

import { useRole } from '../../utils/useRole';
import { ROLE } from '../../constants/constants.app';

export interface MFRoutes {
  data: Record<string, string>;
}

// Грузим роуты из ремоутов
export const MFBootRoutes = {
  passengers: mfDataLoader(() => {
    try {
      return import('passengers/routes');
    } catch {
      return Promise.resolve();
    }
  }) as MFRoutes,
  cargo: mfDataLoader(() => {
    try {
      return import('cargo/routes');
    } catch {
      return Promise.resolve();
    }
  }) as MFRoutes,
  // fleet: mfDataLoader(() => {
  //   try {
  //     return import('fleet/routes');
  //   } catch {
  //     return Promise.resolve();
  //   }
  // }) as MFRoutes,
};

const TaxiApprovalsCounterType = {
  [EmployeeAppLinksTitles['trips']]: TaxiApprovalCounterType.taxi,
  [EmployeeAppLinksTitles['yandex']]: TaxiApprovalCounterType.yandex,
};

const CargoApprovalsCounterType = {
  [EmployeeAppLinksTitles['cargos']]: CargoApprovalCounterType.cargos,
  [EmployeeAppLinksTitles['regularCargos']]: CargoApprovalCounterType.regularCargos,
};

interface SideMenuProps {
  className?: string;
}

const SideMenu: React.FC<SideMenuProps> = observer(({ className }) => {
  const { isMobile } = usePlatformDetect();
  const checkAvailability = item => {
    if (item.name === EmployeeAppLinksTitles['carService'] && isMobile) return false;
    if (item.name === EmployeeAppLinksTitles['yandexTrips'] && !isMobile) return false;
    return true;
  };

  const match = useRouteMatch();
  const {
    isApprovementsSubPage,
    isLimitsSubPage,
    isCreateSupportPage,
    checkedActiveMenuItem,
    checkedActiveMainMenuItem,
  } = useNavigator();
  const {
    [StoreNames.selfStore]: { selfEmployee },
  } = useAppStoreContext();
  const { isDepartmentHead, orgStructureType } = selfEmployee;

  const userRoles = useRole();

  const [limitsIsOpen, setLimitsOpen] = useState(isLimitsSubPage() || false);
  const [approvIsOpen, setApprovOpen] = useState(isApprovementsSubPage() || false);
  const [myApplicationsIsOpen, setMyApplicationsIsOpen] = useState(false);
  const [isExpanded, expand] = useState(true);
  const [menuItems, setMenuItems] = useState([]);
  const [titleTypeMenuItems, setTitleTypeMenuItems] = useState('');

  const MFRoutes = useMemo(() => MFBootRoutes, []) as Record<string, MFRoutes>;

  useEffect(() => {
    if (IS_DEV) {
      // eslint-disable-next-line no-console
      console.log('Routes', { 'App': routes, 'MF routes': MFRoutes });
    }
  }, [MFRoutes]);

  const handleOpenCreateSubPage = () => {
    setLimitsOpen(false);
    setApprovOpen(false);
    setMyApplicationsIsOpen(false);
  };

  const handleOpenSupportPage = () => {
    setLimitsOpen(false);
    setApprovOpen(false);
    setMyApplicationsIsOpen(false);
  };

  const handleOpenApprovPage = () => {
    setLimitsOpen(false);
    setMyApplicationsIsOpen(false);
  };

  const handleOpenApplicationsPage = () => {
    setLimitsOpen(false);
    setApprovOpen(false);
    setMyApplicationsIsOpen(true);
  };

  const handleOpenLimitsPage = () => {
    setApprovOpen(false);
    setMyApplicationsIsOpen(false);
  };

  const handleExpanded = () => {
    expand(!isExpanded);
    setMyApplicationsIsOpen(false);
    setApprovOpen(false);
    setLimitsOpen(false);
  };

  const hasItemCounter = (name: string) => name === EmployeeAppLinksTitles['trips']
    || name === EmployeeAppLinksTitles['yandex']
    || name === EmployeeAppLinksTitles['cargos']
    || name === EmployeeAppLinksTitles['regularCargos'];

  const myApplications = [
    {
      to: `${match.path}/passengers/trips/list/planned`,
      name: EmployeeAppLinksTitles['trips'],
      nameTab: EmployeeAppLinksTitles['trips'],
      image: <Car />,
    },
    {
      to: `${match.path}/passengers/trips/yandex/planned`,
      name: EmployeeAppLinksTitles['yandexTrips'],
      nameTab: EmployeeAppLinksTitles['yandexTrips'],
      image: <Car />,
    },
    {
      to: `${match.path}/cargo/single/list/active`,
      name: EmployeeAppLinksTitles['cargos'],
      nameTab: EmployeeAppLinksTitles['cargos'],
      image: <Delivery />,
    },
    {
      to: `${match.path}/fleet/my-orders/repair`,
      name: EmployeeAppLinksTitles['carService'],
      nameTab: EmployeeAppLinksTitles['carService'],
      image: <CarService />,
    },
  ];

  const approv = [
    ...(MFRoutes.passengers && Object.keys(MFRoutes.passengers.data).length
      ? [
        {
          to: `${MFRoutes.passengers.data.APPROVEMENT_TRIPS}/active`,
          name: EmployeeAppLinksTitles['trips'],
          nameTab: `Согласование ${EmployeeAppLinksTitles['trips']}`,
          image: <BidTrip />,
        },
        {
          to: `${MFRoutes.passengers.data.APPROVEMENT_YANDEX}/active`,
          name: EmployeeAppLinksTitles['yandex'],
          nameTab: `Согласование ${EmployeeAppLinksTitles['yandex']}`,
          image: <BidTrip />,
        },
      ]
      : []),
    ...(MFRoutes.cargo && Object.keys(MFRoutes.cargo.data).length
      ? [
        {
          to: `${MFRoutes.cargo.data.APPROVEMENT_CARGOS}/active`,
          name: EmployeeAppLinksTitles['cargos'],
          nameTab: `Согласование ${EmployeeAppLinksTitles['cargos']}`,
          image: <BidCargo />,
        },
        {
          to: `${MFRoutes.cargo.data.APPROVEMENT_REGULAR_CARGOS}/active`,
          name: EmployeeAppLinksTitles['regularCargos'],
          nameTab: `Согласование ${EmployeeAppLinksTitles['regularCargos']}`,
          image: <BidCargo />,
        },
      ]
      : []),
    {
      to: `${match.path}/approvement/limits/active`,
      name: EmployeeAppLinksTitles['limitsInfo'],
      nameTab: `Согласование ${EmployeeAppLinksTitles['limitsInfo']}`,
      image: <Limits />,
    },
    ...(isDepartmentHead
      ? [
        {
          to: `${match.path}/approvement/delegates`,
          name: EmployeeAppLinksTitles['delegates'],
          nameTab: `Согласование ${EmployeeAppLinksTitles['delegates']}`,
          image: <Delegation />,
        },
      ]
      : []),
  ];

  const limits = [
    {
      to: `${match.path}/limits/limitsInfo`,
      name: EmployeeAppLinksTitles['limitsInfo'],
      nameTab: `Финансы ${EmployeeAppLinksTitles['limitsInfo']}`,
      image: <Limits />,
    },
    {
      to: `${match.path}/bonuses/account`,
      name: `${EmployeeAppLinksTitles['bonusesAccount']}`,
      nameTab: `Финансы ${EmployeeAppLinksTitles['bonusesAccount']}`,
      image: <Bonus />,
    },
    {
      to: `${match.path}/limits/limitRequests`,
      name: `${EmployeeAppLinksTitles['limits']}`,
      nameTab: `Финансы ${EmployeeAppLinksTitles['limits']}`,
      image: <LimitRequest />,
    },
  ];

  const menu = (
    <Menu>
      <>
        <TitleMenu>{titleTypeMenuItems}</TitleMenu>
        {menuItems.map((item, index) => (
          <Menu.Item key={index}>
            <NavLink to={item.to}>
              <WrapperInfoMenuItem>
                <IconContainer>{item.image}</IconContainer>
                <TitleMenuItems>{item.name}</TitleMenuItems>
              </WrapperInfoMenuItem>
            </NavLink>
          </Menu.Item>
        ))}
      </>
    </Menu>
  );

  return (
    <StyledMenu isExpanded={isExpanded} className={`side-menu ${className}`}>
      <Profile isExpanded={isExpanded} />
      <MenuItem
        onClick={handleOpenCreateSubPage}
        to={routes.HOME}
        icon={<House />}
        isActive={() => checkedActiveMainMenuItem('Главная')}
        $isExpanded={isExpanded}
        isOpen={myApplicationsIsOpen}
        open={myApplicationsIsOpen}
        setOpen={setMyApplicationsIsOpen}
        nonSpoiler
      >
        {isExpanded && 'Главная'}
      </MenuItem>

      <OnlySpoiler
        isActive={() => checkedActiveMainMenuItem('Мои заявки')}
        titleMenu="Мои заявки"
        typeMenu={myApplications}
        setTitleTypeMenuItems={setTitleTypeMenuItems}
        setMenuItems={setMenuItems}
        handleOpen={handleOpenApplicationsPage}
        open={myApplicationsIsOpen}
        setOpen={setMyApplicationsIsOpen}
        $isExpanded={isExpanded}
      >
        {isExpanded ? (
          <MenuItem
            to={`${match.path}/passengers/trips/list/planned`}
            icon={<Library />}
            isActive={() => checkedActiveMainMenuItem('Мои заявки')}
            $hasInnerChilds={true}
            isOpen={myApplicationsIsOpen}
            $isExpanded={isExpanded}
            handleOpen={handleOpenApplicationsPage}
            open={myApplicationsIsOpen}
            setOpen={setMyApplicationsIsOpen}
          >
            {isExpanded && 'Мои заявки'}
          </MenuItem>
        ) : (
          <Dropdown
            overlayClassName="DropdownSideMenu"
            placement="bottomLeft"
            overlay={menu}
            trigger={['click']}
          >
            <MenuItem
              to={`${match.path}/passengers/trips/list/planned`}
              icon={<Library />}
              isActive={() => checkedActiveMainMenuItem('Мои заявки')}
              $hasInnerChilds={true}
              isOpen={myApplicationsIsOpen}
              $isExpanded={isExpanded}
              handleOpen={handleOpenApplicationsPage}
              open={myApplicationsIsOpen}
              setOpen={setMyApplicationsIsOpen}
            >
              {isExpanded && 'Мои заявки'}
            </MenuItem>
          </Dropdown>
        )}

        {myApplicationsIsOpen && (
          <>
            {myApplications
              .filter(checkAvailability)
              .map(item => (
                <MenuItem
                  key={item.name}
                  isActive={() => checkedActiveMenuItem(item.nameTab)}
                  to={item.to}
                  icon={item.image}
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

      <OnlySpoiler
        isActive={() => checkedActiveMainMenuItem('Согласования')}
        titleMenu="Согласования"
        typeMenu={approv}
        setTitleTypeMenuItems={setTitleTypeMenuItems}
        setMenuItems={setMenuItems}
        handleOpen={handleOpenApprovPage}
        open={approvIsOpen}
        setOpen={setApprovOpen}
        $isExpanded={isExpanded}
      >
        {isExpanded ? (
          <MenuItem
            to={`${MFRoutes.passengers.data.APPROVEMENT_TRIPS}/active`}
            icon={<Check />}
            isActive={() => checkedActiveMainMenuItem('Согласования')}
            $hasInnerChilds={true}
            isOpen={approvIsOpen}
            $isExpanded={isExpanded}
            onClick={handleOpenApprovPage}
          >
            {isExpanded && 'Согласования'}
          </MenuItem>
        ) : (
          <Dropdown
            overlayClassName="DropdownSideMenu"
            placement="bottomLeft"
            overlay={menu}
            trigger={['click']}
          >
            <MenuItem
              to={`${MFRoutes.passengers.data.APPROVEMENT_TRIPS}/active`}
              icon={<Check />}
              isActive={() => checkedActiveMainMenuItem('Согласования')}
              $hasInnerChilds={true}
              isOpen={approvIsOpen}
              $isExpanded={isExpanded}
              onClick={handleOpenApprovPage}
            >
              {isExpanded && 'Согласования'}
            </MenuItem>
          </Dropdown>
        )}

        {approvIsOpen && (
          <>
            {approv.map(item => (
              <MenuItem
                key={item.name}
                isActive={() => checkedActiveMenuItem(item.nameTab)}
                to={item.to}
                icon={item.image}
                onClick={handleStop}
                $isExpanded={isExpanded}
                $isSubMenu={true}
                $noWhiteSpaceNormal={item.name === CargoApprovalMenuNames[CargoApprovalCounterType.regularCargos]}
              >
                {item.name}
                {hasItemCounter(item.name) && (
                  <>
                    {TaxiApprovalsCounterType[item.name] && (
                      <TaxiApprovalsCount type={TaxiApprovalsCounterType[item.name]} />
                    )}
                    {CargoApprovalsCounterType[item.name] && (
                      <CargoApprovalsCount type={CargoApprovalsCounterType[item.name]} />
                    )}
                  </>
                )}
              </MenuItem>
            ))}
          </>
        )}
      </OnlySpoiler>

      <OnlySpoiler
        isActive={() => checkedActiveMainMenuItem('Финансы')}
        titleMenu="Финансы"
        typeMenu={limits}
        setTitleTypeMenuItems={setTitleTypeMenuItems}
        setMenuItems={setMenuItems}
        handleOpen={handleOpenLimitsPage}
        open={limitsIsOpen}
        setOpen={setLimitsOpen}
        $isExpanded={isExpanded}
      >
        {isExpanded ? (
          <MenuItem
            to={`${match.path}/limits/limitsInfo`}
            icon={<Limit />}
            isActive={() => checkedActiveMainMenuItem('Финансы')}
            $hasInnerChilds={true}
            isOpen={limitsIsOpen}
            $isExpanded={isExpanded}
          >
            {isExpanded && 'Финансы'}
          </MenuItem>
        ) : (
          <Dropdown
            overlayClassName="DropdownSideMenu"
            placement="bottomLeft"
            overlay={menu}
            trigger={['click']}
          >
            <MenuItem
              to={`${match.path}/limits/limitsInfo`}
              icon={<Limit />}
              isActive={() => checkedActiveMainMenuItem('Финансы')}
              $hasInnerChilds={true}
              isOpen={limitsIsOpen}
              $isExpanded={isExpanded}
            >
              {isExpanded && 'Финансы'}
            </MenuItem>
          </Dropdown>
        )}

        {limitsIsOpen && (
          <>
            {limits.map(item => (
              <MenuItem
                key={item.name}
                isActive={() => checkedActiveMenuItem(item.nameTab)}
                to={item.to}
                icon={item.image}
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
      {/*
        Проверка нужна чтобы раздел не был виден внешним клиентам.
        Дублируется в HeaderProfile.tsx
        У основных тестовых пользователей тип структуры EXTERNAL.
        Для работы с разделом Биржа нужно заменить проверку на EXTERNAL.
      */}
      {orgStructureType === OrgStructureType.INTERNAL && (
        <AccessControl
          userPermissions={userRoles}
          allowedPermissions={[ROLE.DOMESTIC_COURIER]}
        >
          <MenuItem
            onClick={handleOpenSupportPage}
            to={`${match.path}/cargo/single/exchange/available`}
            icon={<Exchange />}
            isActive={isCreateSupportPage}
            $isExpanded={isExpanded}
            nonSpoiler
          >
            {isExpanded && 'Биржа заявок'}
          </MenuItem>
        </AccessControl>
      )}
      <Divider />

      <MenuItem
        onClick={handleOpenSupportPage}
        to={`${match.path}/support`}
        icon={<SupportMain />}
        isActive={isCreateSupportPage}
        $isExpanded={isExpanded}
        nonSpoiler
      >
        {isExpanded && 'Поддержка'}
      </MenuItem>
      <Spacer />
      <BottomMenu isExpanded={isExpanded} onClick={handleExpanded}>
        <Expander isRotate={isExpanded}>
          <ChevronRight />
        </Expander>
        {isExpanded && 'Свернуть меню'}
      </BottomMenu>
    </StyledMenu>
  );
});

export default SideMenu;
