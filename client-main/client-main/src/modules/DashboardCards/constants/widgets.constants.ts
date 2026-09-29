export const defaultDataPaymentsTable = [
  {
    key: '1',
    name: 'в работе',
    count: 0,
    cost: 0,
  },
  {
    key: '2',
    name: 'Формирование приказа',
    count: 0,
    cost: 0,
  },
  {
    key: '3',
    name: 'Ожидание выплаты',
    count: 0,
    cost: 0,
  },
  {
    key: '4',
    name: 'Выплачено',
    count: 0,
    cost: 0,
  },
];

export enum ServiceWidgetsEnum {
  PASSENGER = 'PASSENGER',
  ALL = 'ALL',
}

export const ServiceWidgetsTitle = {
  [ServiceWidgetsEnum.ALL]: 'Все сервисы',
  [ServiceWidgetsEnum.PASSENGER]: 'Перевозка сотрудников',
};
