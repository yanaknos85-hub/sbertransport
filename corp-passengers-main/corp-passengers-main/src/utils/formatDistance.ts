export const formatDistance = (d: number | undefined | null, emptyValue = '-'): string => Number.isFinite(d) ? new Intl.NumberFormat('ru-RU').format(d!) : emptyValue;

export const formatDistanceValue = (num: number): string => {
  const truncatedNumber: number = Math.round(num * 10) / 10;
  const formattedNumber: string = truncatedNumber.toString().replace('.', ',');
  return formattedNumber;
};
