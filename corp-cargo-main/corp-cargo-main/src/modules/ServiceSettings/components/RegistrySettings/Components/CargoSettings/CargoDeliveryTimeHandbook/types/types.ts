export enum DeliveryUrgency {
  STANDART = 'STANDART',
  EXPRESS = 'EXPRESS',
}

export enum DeliveryRange {
  RANGE0to100 = 0,
  RANGE101to500 = 101,
  RANGE501to1000 = 501,
  RANGE1001 = 1001,
}

export type DeliveryTimeRow = {
  [key in DeliveryRange]?: number;
} & { urgency: DeliveryUrgency };
