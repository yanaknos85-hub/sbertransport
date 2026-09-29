import { Value as DateFormValue } from 'shared/components/DateInput/types';
import { FilterValues } from '../types';

/**
 * Проверяет, есть ли значение даты
 * @param dateValue - значение даты (может быть объектом {mode, value} или простым значением)
 * @returns true - если значение даты присутствует и не пустое, false - если значение отсутствует или пустое
 */
export const hasDateValue = (dateValue: DateFormValue | undefined | null | string | boolean): boolean => {
  if (!dateValue) return false;
  // Проверяем, это объект {mode, value} или простое значение
  if (dateValue && typeof dateValue === 'object' && 'value' in dateValue) {
    return Array.isArray(dateValue.value) && dateValue.value.some((item) => item !== null);
  }
  return typeof dateValue === 'string' || typeof dateValue === 'boolean' || typeof dateValue === 'number';
};

/**
 * Проверяет, не пуст ли хотя бы один фильтр
 * @param values - объект со значениями фильтров
 * @returns true - если есть хотя бы один активный фильтр, false - если все фильтры пустые
 */
export const hasAnyActiveFilter = (values: FilterValues | undefined | null): boolean => {
  if (!values) return false;
  
  return !(
    !values.requestHumanId &&
    !values.cargoTransportType?.length &&
    !values.contractorSet?.length &&
    !values.authorFIO &&
    !values.requestStatusSet?.length &&
    !values.deadlineDate &&
    !values.requestTypeSet?.length &&
    !hasDateValue(values.desiredDate) &&
    !hasDateValue(values.creationDate) &&
    !hasDateValue(values.changeDate) &&
    !values.expectedCost
  );
};
