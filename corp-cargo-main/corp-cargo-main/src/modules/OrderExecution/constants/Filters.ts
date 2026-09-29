import { Tab } from './Tabs';

export const AVAILABLE_FILTER_TABS = [Tab.passengers, Tab.cargo, Tab.carService];

export const DEFAULT_FILTER_VALUES = {
  page: 0,
  pageSize: 10,
};

export const PAGE_SETTINGS_DEFAULT = {
  page: 0,
  size: 10,
};

export enum FilterTab {
  all = 'all',
  myApplications = 'myApplications',
  passengers = 'passengers',
  cargo = 'cargo',
  carService = 'carService',
}

type TFilterTabName<T> = {
  [key in FilterTab]: T;
};

export const FilterTabName: TFilterTabName<string> = {
  [FilterTab.all]: 'Все',
  [FilterTab.myApplications]: 'Заявки на меня',
  [FilterTab.passengers]: 'Пассажирские перевозки',
  [FilterTab.cargo]: 'Грузовые перевозки',
  [FilterTab.carService]: 'Автосервис',
};
