import { observer } from 'mobx-react';
import * as React from 'react';
import { DashboardLinks, MFServiceLinks } from 'modules/DashboardCards/constants/general.constants';
import { ROLE } from 'constants/constants.app';
import { ServiceEnum, ServiceTypeEnum, ServiceEnumTitles } from 'constants/constants.app';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import cargoImg from 'shared/components/Images/orders/cargo.png';
import taxiImg from 'shared/components/Images/orders/taxi.png';
import parkingImg from 'shared/components/Images/orders/parking.png';
import wrench from 'shared/components/Images/orders/wrench.png';

import { useRole } from 'utils/useRole';
import CardDesktop from './Card';
import CardMobile from './CardMobile';
import {
  Cards, CargoIcon, ParkingIcon, RepairIcon, StyledTitle, TaxiIcon
} from '../../styled';
import { TServices } from './types';
import useColors from './useColors';
import useLimits from './useLimits';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import OnTheLineSwitch from 'modules/OnTheLineSwitch/OnTheLineSwitch';

const { origin } = window.location;
export const IS_TEST_STAND
  = origin.includes('localhost')
  || origin.includes('.cargo.')
  || origin.includes('.ift.')
  || origin.includes('psi.')
  || origin.includes('.psi-gen.')
  || origin.includes('.psi-gen2.')
  || origin.includes('.st.')
  || origin.includes('.nt.');

const Services: React.FC = observer(() => {
  const { employeeLimitsPercentsByType, departmentLimitsPercentsByType } = useLimits();
  const { colorByType } = useColors(employeeLimitsPercentsByType);
  const roles = useRole();

  // todo !!! удалить условие, когда будет готова многоточка !!!

  // Порядок вывода карточек определяется в этом объекте
  // Некоторые ключи такие как например title проставляются автоматически ниже, лучше не прописывать их тут
  const services: TServices = {
    [ServiceEnum.employeeTransportation]: {
      serviceType: ServiceTypeEnum.EMPLOYEE_TRANSPORTATION,
      transportTypeForLimitSelect: TransportTypeEnum.TAXI,
      price: 40,
      buttonText: 'Заказать',
      link: DashboardLinks[MFServiceLinks.transportCreate],
      icon: <TaxiIcon src={taxiImg} />,
      content: <p>Перевозка сотрудников в производственных целях</p>,
    },
    // todo удалить после тестирования и обкатки в проме !!!
    // [ServiceEnum.cargo]: {
    //   serviceType: ServiceTypeEnum.CARGO_TRANSPORTATION,
    //   transportTypeForLimitSelect: TransportTypeEnum.DEDICATED,
    //   price: 400,
    //   buttonText: 'Заказать',
    //   // buttonIsDisabled: !departmentLimitsPercentsByType[ServiceEnum.cargo], // убираем блокировку поока
    //   link: DashboardLinks[MFServiceEnum.cargoCreate],
    //   icon: <CargoIcon src={cargoImg} />,
    //   content: <p>Доставка груза, документов, массовая заявка загружаемая через Excel</p>,
    // },
    [ServiceEnum.cargo]: {
      serviceType: ServiceTypeEnum.CARGO_TRANSPORTATION,
      transportTypeForLimitSelect: TransportTypeEnum.DEDICATED,
      price: 450,
      buttonText: 'Заказать',
      link: DashboardLinks[MFServiceLinks.cargoCreate],
      icon: <CargoIcon src={cargoImg} />,
      content: <p>Заказывайте доставку документов, техники. Создавайте сложные заявки через Excel*</p>,
    },
    // todo удалить после тестирования и обкатки в проме !!!
    // [ServiceEnum.regularCargo]: {
    //   serviceType: ServiceTypeEnum.CARGO_TRANSPORTATION,
    //   transportTypeForLimitSelect: TransportTypeEnum.DEDICATED,
    //   price: 4000,
    //   buttonText: 'Заказать',
    //   // buttonIsDisabled: !departmentLimitsPercentsByType[ServiceEnum.regularCargo],  // убираем блокировку поока
    //   link: DashboardLinks[MFServiceEnum.regularCargoCreate],
    //   icon: <TruckIcon src={regCargoImg} />,
    //   content: <p>Заказывайте регулярную доставку документов, техники. Создавайте сложные заявки через Excel*</p>,
    // },
    [ServiceEnum.parking]: {
      serviceType: ServiceTypeEnum.PARKING,
      price: 40,
      buttonText: 'Заказать',
      link: DashboardLinks[MFServiceLinks.parking],
      icon: <ParkingIcon src={parkingImg} />,
      content: <p>Выбор парковочных мест в корпоративных и городских локациях в производственных и в личных целях</p>,
      hiddenLimit: true,
      userPermissions: roles,
      allowedPermissions: [ROLE.DRIVER, ROLE.PARKING_ADMIN, ROLE.PARKING_COORDINATOR, ROLE.EMPLOYEE_CORP_CLIENT],
    },
    [ServiceEnum.maintenance]: {
      serviceType: ServiceTypeEnum.REPAIR,
      price: 100,
      buttonText: 'Заказать',
      link: DashboardLinks[MFServiceLinks.maintenance],
      icon: <RepairIcon src={wrench} />,
      content: <p>Техническое обслуживание и текущий ремонт служебного транспорта</p>,
      hiddenLimit: true,
    },
  };

  const { isMobile } = usePlatformDetect();

  const checkPermissions = (serviceName: ServiceEnum) => {
    const { allowedPermissions, userPermissions } = services[serviceName];

    if (serviceName === ServiceEnum.maintenance && isMobile) return false; // временно скрыть заявки Укап в моб версии

    if (
      allowedPermissions
      && allowedPermissions.length
      && !userPermissions.some(permission => allowedPermissions.includes(permission))
    ) {
      return false;
    }
    return true;
  };

  const isDriver = roles.includes(ROLE.DRIVER);

  const Card = isMobile ? CardMobile : CardDesktop;

  return (
    <>
      <StyledTitle margin="24px 0px 8px 8px">
        Услуги
        {isMobile && isDriver && <OnTheLineSwitch />}
      </StyledTitle>
      <Cards direction={isMobile ? 'column' : 'row'}>
        {Object.keys(services)
          .filter(checkPermissions)
          .map((serviceName, i) => (
            <Card
              title={ServiceEnumTitles[serviceName as ServiceEnum]}
              color={colorByType[serviceName as ServiceEnum]}
              {...services[serviceName as ServiceEnum]}
              employeeLimitPercent={employeeLimitsPercentsByType[serviceName as ServiceEnum]}
              departmentLimitPercent={departmentLimitsPercentsByType[serviceName as ServiceEnum]}
              key={serviceName as ServiceEnum}
              index={i}
            />
          ))}
      </Cards>
    </>
  );
});

export default Services;
