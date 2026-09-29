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
