import { LabeledValue } from 'types/Types';

/**
 * Сортирует LabeledValue по label в алфавитном порядке (без учета регистра)
 * @param prev - первый объект для сравнения
 * @param next - второй объект для сравнения
 * @returns -1, 0 или 1 для сортировки
 */
export const sortLabelValue = (prev: LabeledValue, next: LabeledValue): number => {
  if (prev.label && next.label) {
    if (`${prev.label}`.toLowerCase() < `${next.label}`.toLowerCase()) {
      return -1;
    }
    if (`${prev.label}`.toLowerCase() > `${next.label}`.toLowerCase()) {
      return 1;
    }
  }
  return 0;
};
