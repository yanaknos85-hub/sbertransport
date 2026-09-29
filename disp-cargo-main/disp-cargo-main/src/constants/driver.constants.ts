export const DRIVER_EXPERIENCE_DESCRIPTIONS = {
  LESS_THEN_FIVE: 'Менее 5-ти лет',
  FIVE_TO_TEN: 'От 5-ти до 10-ти лет',
  MORE_THEN_TEN: 'Более 10-ти лет',
};

export const DRIVER_ACTIVE_DESCRIPTIONS = {
  ACTIVE: 'Активен',
  NOT_ACTIVE: 'Неактивен',
};

export const DRIVER_RATING = {
  MIN_RATING: 0,
  MAX_RATING: 5,
};

export enum DRIVER_LICENSES {
  A = 'A',
  A1 = 'A1',
  B = 'B',
  B1 = 'B1',
  C = 'C',
  C1 = 'C1',
  D = 'D',
  D1 = 'D1',
  BE = 'BE',
  CE = 'CE',
  C1E = 'C1E',
  DE = 'DE',
  D1E = 'D1E',
  M = 'M',
  TM = 'TM',
}

export enum DriverSpecialityTypes {
  Passenger = 'PASSENGER',
  Cargo = 'CARGO',
  Both = 'BOTH',
}

export const driverSpecialityTitles: Record<DriverSpecialityTypes, string> = {
  [DriverSpecialityTypes.Passenger]: 'Пассажирский',
  [DriverSpecialityTypes.Cargo]: 'Грузовой',
  [DriverSpecialityTypes.Both]: 'Такси и грузы',
};
