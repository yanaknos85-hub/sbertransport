/**
 * Форматирует сумму в рублях с использованием формата валюты (₽)
 * @param r - сумма в рублях
 * @param emptyValue - значение для отображения, если сумма отсутствует (по умолчанию '-')
 * @returns отформатированная строка валюты или значение emptyValue
 */
export const formatRubles = (r: number | undefined, emptyValue = '-'): string => (
  r !== undefined && Number.isFinite(r)
    ? new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB' }).format(r)
    : emptyValue
);
