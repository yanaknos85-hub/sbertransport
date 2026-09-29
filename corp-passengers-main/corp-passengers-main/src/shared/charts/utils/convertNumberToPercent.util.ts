/*
 * поскольку на графике визуально невозможно отобразить слишком маленькие значения - приводим значения
 * к минимально допустимому значению которое можно отобразить.
 */
export function convertNumberToPercent(totalValue: number, value: number): number {
  const result: number = (value / totalValue) * 100;
  if (result < 0.5 && result !== 0) {
    return 1.5;
  }
  return result;
}
