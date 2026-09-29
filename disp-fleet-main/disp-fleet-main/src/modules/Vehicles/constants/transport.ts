export enum Steps {
  Vehicle,
  Documents,
  General,
}

export const StepsNames: Record<Steps, string> = {
  [Steps.Vehicle]: 'Автомобиль',
  [Steps.Documents]: 'Документы',
  [Steps.General]: 'Общая информация',
};

export enum DirectoriesTypes {
  Vehicles = 'vehicles',
  Brands = 'brands',
  Models = 'models',
  UsingType = 'usingType',
  UsingSubType = 'usingSubType',
  Categories = 'categories',
  WheelDrive = 'wheelDrive',
  EngineType = 'engineType',
  FuelType = 'fuelType',
  Telematics = 'telematics',
  BodyType = 'bodyType',
  TransmissionType = 'transmissionType',
  WheelSize = 'wheelSize',
}
