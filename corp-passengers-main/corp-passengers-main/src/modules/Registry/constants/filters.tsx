import { LabeledValue } from 'utils/Types';

export const balanceUnitOptions: LabeledValue[] = [
  { label: '1300 - ЦЧБ', value: 1300 },
  { label: '1600 – УБ', value: 1600 },
  { label: '1800 – ББ', value: 1800 },
  { label: '3800 - МБ', value: 3800 },
  { label: '4000 – СРБ', value: 4000 },
  { label: '4200 – ВВБ', value: 4200 },
  { label: '4400 – СИБ', value: 4400 },
  { label: '5200 – ЮЗБ', value: 5200 },
  { label: '5400 – ПБ', value: 5400 },
  { label: '5500 – СЗБ', value: 5500 },
  { label: '7000 – ДВБ', value: 7000 },
  { label: '9900 – ЦА', value: 9900 },
];

export const paymentPeriodOptions: LabeledValue[] = [
  { value: 1, label: 'Первый период (1–7 число)' },
  { value: 2, label: 'Второй период (8–15 число)' },
  { value: 3, label: 'Третий период (16–23 число)' },
  { value: 4, label: 'Четвертый период (24-конец месяца)' },
];

export const ratingOptions: LabeledValue[] = [
  { label: 1, value: 1 },
  { label: 2, value: 2 },
  { label: 3, value: 3 },
  { label: 4, value: 4 },
  { label: 5, value: 5 },
  { label: 'Без оценки', value: 0 },
];

