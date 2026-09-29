const fromRubles = (value: number): number => Math.round(value * 100);

const toRubles = (value: number, withFraction = false): number => {
  const rubles = Math.round(value / 100);
  const fraction = value - rubles * 100;
  return rubles + (withFraction ? fraction / 100 : 0);
};

const formatRubles = (r: number | undefined | null, emptyValue = ''): string => (
  // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
  Number.isFinite(r) ? new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB' }).format(r!) : emptyValue
);

const formatRublesWithoutPennies = (r: number): string => formatRubles(r, '').replace(/,\d+/, '');

export {
  fromRubles, toRubles, formatRubles, formatRublesWithoutPennies
};
