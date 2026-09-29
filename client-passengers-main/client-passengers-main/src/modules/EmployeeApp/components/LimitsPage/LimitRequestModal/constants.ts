import { TransportTypeEnum, TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { LabeledValue } from 'utils';

export enum LimitRequestPeriods {
  январь,
  февраль,
  март,
  апрель,
  май,
  июнь,
  июль,
  август,
  сентябрь,
  октябрь,
  ноябрь,
  декабрь,
}

export enum LimitRequestAskTarget {
  NEIGHBOR = 'NEIGHBOR',
  PARENT = 'PARENT',
}

export const AskTargetOptions: LabeledValue[] = [
  {
    value: LimitRequestAskTarget.NEIGHBOR,
    label: 'смежные подразделения',
  },
  {
    value: LimitRequestAskTarget.PARENT,
    label: 'вышестоящее подразделение',
  },
];

export const PeriodOptions: LabeledValue[] = Array.from({ length: 12 }).map((x, i) => ({
  value: i,
  label: LimitRequestPeriods[i],
}));

export enum LimitRequestLevel {
  SIBLINGS = 'siblings',
  PARENT = 'parent',
}

export type LimitRequestLevelType = 'siblings' | 'parent';

// при этом проценте показывается кнопка "Уведомить"
export const minLimitPercent = 15;
// такой процент от первоначальной суммы должен остаться у смежных подразделений, если они одобрят полную сумму
export const siblingsLimitMoneyPercent = 20;

const isLastMonth = new Date().getMonth() === 11;
const currentMonth = new Date().getMonth();
const nextMonth = new Date().getMonth() + 1;

export const monthArray = [
  {
    value: currentMonth,
    label: LimitRequestPeriods[currentMonth],
  },
  {
    value: isLastMonth ? 0 : nextMonth,
    label: isLastMonth ? LimitRequestPeriods[0] : LimitRequestPeriods[nextMonth],
  },
];

export const getTransportTypeOptions = (transportTypes: string[]): LabeledValue[] => Object.values(transportTypes).map(x => ({
  value: x,
  label: TransportTypeTitlesEnum[x as TransportTypeEnum],
}));

export const periodTitle = 'Период';
export const sumTitle = 'Сумма';
export const reasonTitle = 'Обоснование';
