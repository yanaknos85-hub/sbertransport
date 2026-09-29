/* eslint-disable @stylistic/implicit-arrow-linebreak */
const fromRubles = (value: number): number => value * 100;

const toRubles = (value: number, withFraction = false): number => {
  const rubles = Math.round(value / 100);
  const fraction = value - rubles * 100;
  return rubles + (withFraction ? fraction / 100 : 0);
};

const formatRubles = (r: number | undefined | null, emptyValue = ''): string =>
  // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
  Number.isFinite(r)
    ? new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB' }).format(r || 0)
    : emptyValue;

const formatRublesWithoutRemainder = (r: number | undefined | null, emptyValue = ''): string =>
// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
  Number.isFinite(r)
    ? new Intl.NumberFormat('ru-RU', {
      style: 'currency', currency: 'RUB', minimumFractionDigits: 0,
    }).format(r || 0)
    : emptyValue;

const formatRublesfromRubles = (r: number | undefined | null, emptyValue = ''): string => Number.isFinite(r)
  ? new Intl.NumberFormat('ru', { style: 'currency', currency: 'RUB' }).format(fromRubles(r || 0))
  : emptyValue;

export {
  fromRubles, toRubles, formatRubles, formatRublesfromRubles, formatRublesWithoutRemainder
};
