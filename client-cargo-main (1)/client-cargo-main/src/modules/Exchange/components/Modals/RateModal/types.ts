export interface SVGProps {
  fillBackground: string;
  fillFont: string;
  stroke: string;
  fillOpacity?: number;
}

export const enum EvaluationReasonsNameEnum {
  INCONVENIENT_PICKUP_LOCATION = 'Неудобное место выдачи',
  WRINKLED_OR_TORN_PACKAGING = 'Мятая упаковка',
  RUDENESS = 'Грубый курьер',
  LATE = 'Курьер опоздал',
  FAST_SHIPPING = 'Быстрая доставка',
  POLITENESS = 'Вежливый курьер',
}

export enum EvaluationReasonsEnum {
  INCONVENIENT_PICKUP_LOCATION = 'INCONVENIENT_PICKUP_LOCATION',
  WRINKLED_OR_TORN_PACKAGING = 'WRINKLED_OR_TORN_PACKAGING',
  RUDENESS = 'RUDENESS',
  LATE = 'LATE',
  FAST_SHIPPING = 'FAST_SHIPPING',
  POLITENESS = 'POLITENESS',
}

export interface RenderTitle {
  rateTitle: string;
  rateSubtitle: string;
}
