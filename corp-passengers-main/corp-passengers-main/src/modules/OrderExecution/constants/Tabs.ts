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

  // Passengers
  taxi = 'taxi',
  carsharing = 'carsharing',
  personal = 'personal',
  public = 'public',
  group_transfer = 'group_transfer',
  complaints = 'complaints',
  bus = 'bus',

  // Cargo
  courier = 'courier',
  dedicated = 'dedicated',
  interregional = 'interregional',
  domesticCourier = 'domestic_courier',
  individual = 'individual',
  routes = 'routes',

  // Car service
  repair = 'repair',
  evacuation = 'evacuation',
  tire = 'tire',
  tireFitting = 'tireFitting',
  wash = 'wash',
  maintenance = 'maintenance',
  parking = 'parking',
}

type TCategoryName<T> = {
  [key in Category]: T;
};

export const CategoryName: TCategoryName<string> = {
  [Category.all]: 'Все заявки',

  // Passengers
  [Category.taxi]: 'Такси',
  [Category.carsharing]: 'Каршеринг',
  [Category.personal]: 'Личный транспорт',
  [Category.public]: 'Общественный транспорт',
  [Category.group_transfer]: 'Трансфер',
  [Category.complaints]: 'Жалобы',
  [Category.bus]: 'Автобусные перевозки',

  // Cargo
  courier: 'Курьерская доставка',
  dedicated: 'Доставка сборного груза',
  interregional: 'Межрегиональная доставка',
  domestic_courier: 'Внутренний курьер',
  individual: 'Доставка выделенным транспортом',
  routes: 'Маршруты',

  // Car service
  [Category.repair]: 'Ремонт',
  [Category.evacuation]: 'Эвакуатор',
  [Category.tire]: 'Шиномонтаж',
  [Category.tireFitting]: 'Шиномонтаж',
  [Category.wash]: 'Мойка',
  [Category.maintenance]: 'Техническое обслуживание',
  [Category.parking]: 'Парковки',
};
