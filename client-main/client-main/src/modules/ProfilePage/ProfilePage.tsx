import { Divider } from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useEffect } from 'react';

import { HeaderProfileNavigate, ProfileWrapper, TitleNavigate } from './styled';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import HeaderProfile from './components/HeaderProfile/HeaderProfile';
import MainInformation from './components/MainInformation/MainInformation';
import { VehiclesList } from './components/Vehicles/components/VehiclesList/VehiclesList';
import MyAddresses from './components/MyAddresses/MyAddresses';
import CorporateAddresses from './components/CorporateAddresses/CorporateAddresses';
import FrequentAddresses from './components/FrequentAddresses/FrequentAddresses';
import CargoList from './components/CargoList/CargoList';
import { ReactComponent as ArrowRight } from 'shared/components/Images/arrowRight.svg';
import { NavLink } from 'react-router-dom';
import * as routes from 'constants/constants.routes';
import Notifications from './components/Notifications/Notifications';

const ProfilePage: FC = observer(() => {
  const {
    [StoreNames.mappedStore]: mappedStore,
    [StoreNames.corporateStore]: corpStore,
    [StoreNames.addressStore]: addressStore,
    [StoreNames.configStore]: configStore,
  } = useAppStoreContext();
  const IS_PERSONAL_DEVICE = configStore.env.IS_PERSONAL_DEVICE;

  useEffect(() => {
    addressStore.initStore();
  }, [addressStore]);

  return (
    <>
      <HeaderProfileNavigate>
        <NavLink to={routes.HOME}>Главная</NavLink>
        <ArrowRight />
        <TitleNavigate>Личный кабинет</TitleNavigate>
      </HeaderProfileNavigate>
      <ProfileWrapper>
        <HeaderProfile selfDetailed={mappedStore.selfEmployeeDetailed} />
        <Divider />
        <MainInformation selfDetailed={mappedStore.selfEmployeeDetailed} selfDepartment={corpStore.department} />
        <Divider />
        {!IS_PERSONAL_DEVICE && (
        <>
          <VehiclesList />
          <Divider />
        </>
        )}
        <MyAddresses />
        <Divider />
        <CorporateAddresses />
        <Divider />
        <FrequentAddresses />
        <Divider />
        <Notifications />
        <Divider />
        <CargoList />
      </ProfileWrapper>
    </>
  );
});

export default ProfilePage;
