import { routes } from 'constants/routes.constants';
import { Contacts } from 'constants/app.constants';

import { ReactComponent as CarIconIcon } from 'assets/icons/car.svg';
import { ReactComponent as OperatorIcon } from 'assets/icons/operator.svg';
import { ReactComponent as SupportIcon } from 'assets/icons/phone.svg';

interface MenuItem {
  title: string;
  icon: React.ReactNode;
  link: string;
  arrow: boolean;
  disabled?: boolean;
  children?: React.ReactNode;
}

interface MenuSection {
  sectionTitle: string;
  items: MenuItem[];
}

export const menuItems = (dispatcherPhone?: string): MenuSection[] => ([
  {
    sectionTitle: 'Поездки',
    items: [
      {
        title: 'Мои заказы',
        icon: <CarIconIcon />,
        link: routes.MyTrips,
        arrow: true,
      },
    ],
  },
  {
    sectionTitle: 'Поддержка',
    items: [
      {
        title: '',
        icon: <OperatorIcon />,
        link: '',
        arrow: false,
        disabled: !dispatcherPhone,
        children: <a href={`tel:${dispatcherPhone}`}>Позвонить диспетчеру</a>,
      },
      {
        title: '',
        icon: <SupportIcon />,
        link: '',
        arrow: false,
        children: <a href={`tel:${Contacts.outTel}`}>Позвонить в поддержку</a>,
      },
    ],
  },
]);
