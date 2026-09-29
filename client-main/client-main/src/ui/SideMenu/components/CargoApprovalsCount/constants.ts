export type TKeys<T> = {
  [key in string]: T;
};

export enum CargoApprovalCounterType {
  cargos = 'cargos',
  regularCargos = 'regularCargos',
}

export const CargoApprovalMenuNames: TKeys<string> = {
  [CargoApprovalCounterType.cargos]: 'Доставки',
  [CargoApprovalCounterType.regularCargos]: 'Регулярная доставка',
};
