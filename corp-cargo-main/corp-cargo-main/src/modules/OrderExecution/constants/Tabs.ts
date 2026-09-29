export enum Tab {
  passengers = 'passengers',
  cargo = 'cargo',
  carService = 'carService',
  parking = 'parking',
}

type TTabName<T> = {
  [key in Tab]: T;
};

export const TabName: TTabName<string> = {
  [Tab.passengers]: 'Пассажирские перевозки',
  [Tab.cargo]: 'Грузовые перевозки',
  [Tab.carService]: 'Автосервис',
  [Tab.parking]: 'Парковки',
};

export enum Category {
  all = 'all',
  courier = 'courier',
  dedicated = 'dedicated',
  interregional = 'interregional',
  domesticCourier = 'domestic_courier',
  individual = 'individual',
  template = 'template',
  routes = 'routes',
}

type TCategoryName<T> = {
  [key in Category]: T;
};

export const CategoryName: TCategoryName<string> = { /* TODO Доделать, разобраться с domestic_courier */
  [Category.all]: 'Все заявки',

  courier: 'Курьерская доставка',
  dedicated: 'Доставка сборного груза',
  interregional: 'Межрегиональная доставка',
  domestic_courier: 'Внутренний курьер',
  individual: 'Доставка выделенным транспортом',
  template: 'Расписание',
  routes: 'Маршруты',
};
