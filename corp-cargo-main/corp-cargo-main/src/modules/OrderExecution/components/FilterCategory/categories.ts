import { Tab, Category, CategoryName } from '../../constants/Tabs';
import type { CategoryItem } from '../../interfaces/Orders.types';
import { Roles } from 'constants/constants.app';

export const allowedRoles = [
  Roles.ADMIN_DATA_MASTER,
  Roles.ENGINEER_CORP_CLIENT,
  Roles.ADMIN_CORP_CLIENT,
  Roles.DISPATCHER_SUPPORT_SERVICE,
];

const cargo: CategoryItem[] = [
  {
    type: Tab.cargo, category: Category.all, name: CategoryName.all, available: true,
  },
  {
    type: Tab.cargo, category: Category.courier, name: CategoryName.courier, available: true,
  },
  {
    type: Tab.cargo, category: Category.dedicated, name: CategoryName.dedicated, available: true,
  },
  {
    type: Tab.cargo, category: Category.interregional, name: CategoryName.interregional, available: true,
  },
  {
    type: Tab.cargo, category: Category.domesticCourier, name: CategoryName.domestic_courier, available: true,
  },
  {
    type: Tab.cargo, category: Category.individual, name: CategoryName.individual, available: true,
  },
  {
    type: Tab.cargo, category: Category.template, name: CategoryName.template, available: true,
  },
];

const categories = {
  [Tab.cargo]: cargo,
};

export default categories;
