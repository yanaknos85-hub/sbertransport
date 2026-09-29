export const formatRubles = (r: number | undefined, emptyValue = '-'): string => (
  r !== undefined && Number.isFinite(r)
    ? new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB' }).format(r)
    : emptyValue
);
