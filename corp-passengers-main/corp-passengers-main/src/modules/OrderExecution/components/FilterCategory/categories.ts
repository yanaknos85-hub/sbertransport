import { Tab, Category, CategoryName } from '../../constants/Tabs';
import type { CategoryItem } from '../../interfaces/Orders.types';
import { Roles } from 'constants/constants.app';

export const allowedRoles = [
  Roles.ADMIN_DATA_MASTER,
  Roles.ENGINEER_CORP_CLIENT,
  Roles.ADMIN_CORP_CLIENT,
  Roles.DISPATCHER_SUPPORT_SERVICE,
];

export const passengers: CategoryItem[] = [
  // Скрываем все заявки перед показом
  // { type: Tab.passengers, category: Category.all, name: CategoryName.all, available: true },
  {
    type: Tab.passengers, category: Category.taxi, name: CategoryName.taxi, available: true,
  },
  {
    type: Tab.passengers, category: Category.carsharing, name: CategoryName.carsharing, available: true,
  },
  {
    type: Tab.passengers, category: Category.personal, name: CategoryName.personal, available: true,
  },
  {
    type: Tab.passengers, category: Category.public, name: CategoryName.public, available: true,
  },
  {
    type: Tab.passengers, category: Category.group_transfer, name: CategoryName.group_transfer, available: true,
  },
  {
    type: Tab.passengers, category: Category.bus, name: CategoryName.bus, available: true,
  },
];

const categories = {
  [Tab.passengers]: passengers,
};

export default categories;
