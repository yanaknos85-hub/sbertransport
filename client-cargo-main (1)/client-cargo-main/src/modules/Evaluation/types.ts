import { ReactNode } from 'react';
import * as t from 'io-ts';

import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

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

export interface SVGProps {
  fillBackground: string;
  fillFont: string;
  stroke: string;
  fillOpacity?: number;
}

type IconsName = 'badPackage' | 'badCourier' | 'badPlace' | 'badTime';

export type Selected = Record<string, IconsName | boolean | JSX.Element>;

export interface RenderTitles {
  rateTitle: string;
  rateSubtitle: string;
}

export interface OpenNotification {
  placement: 'topLeft' | 'topRight' | 'bottomLeft' | 'bottomRight';
  message: string;
  description: string;
}

export const EvaluationRequest = t.intersection([
  t.type({
    requestId: t.string,
    reasons: t.array(ioTypeFromEnum<EvaluationReasonsEnum>('EvaluationReasonsEnum', EvaluationReasonsEnum)),
    rating: t.number,
  }),
  t.partial({ comment: t.string }),
]);

export type TEvaluationRequest = t.TypeOf<typeof EvaluationRequest>;

export type TransportType = 'DEDICATED' | 'INTERREGIONAL' | 'COURIER';

interface IconStyle {
  fillBackground: string;
  fillFont: string;
  stroke: string;
  fillOpacity: number;
}

export interface IconData {
  type: EvaluationReasonsEnum;
  name: EvaluationReasonsNameEnum;
  icon: (styles: IconStyle) => ReactNode;
}

export interface EvaluationIcons {
  negative: IconData[];
  positive: IconData[];
}

export type RenderedIcons = Record<string, EvaluationIcons>;
