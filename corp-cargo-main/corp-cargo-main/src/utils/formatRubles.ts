export const formatRubles = (r: number | undefined | null, emptyValue = '-', withoutIcon?: boolean): string => Number.isFinite(r)
  ? new Intl.NumberFormat('ru-RU', withoutIcon ? {} : { style: 'currency', currency: 'RUB' }).format(r!)
  : emptyValue;

export const formatRublesWithoutPennies = (r: number, withoutIcon?: boolean): string => formatRubles(r, '', withoutIcon).replace(/,\d+/, '');
