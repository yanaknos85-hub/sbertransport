export interface Package {
  id: string;
  name: string;
  unit: string;
  count?: number;
}

export interface ServiceItems {
  title: string;
  name: string;
  description?: string;
  price: string;
  icon?: JSX.Element;
  infoDescription?: string;
  availableWhenLoadersOrdered?: boolean;
  value: boolean;
  button?: string;
}

export enum ServiceItemsNameEnum {
  LOADERS = 'LOADERS',
  LIFT = 'LIFT',
  CAR = 'CAR',
}

export const ServiceItemsName = {
  [ServiceItemsNameEnum.LOADERS]: 'loaders',
  [ServiceItemsNameEnum.LIFT]: 'lift',
  [ServiceItemsNameEnum.CAR]: 'car',
};
