import moment from 'moment/moment';

/**
 * Возвращает массив дат, пригодных для поискового запроса в реестрах поездок
 * @param value - значение с формы (У поля дат с чекбоксом)
 */
export function transformDate(
  value: [moment.Moment, moment.Moment] | undefined
): { start: number; end: number } | undefined {
  return (
    value && {
      start: value[0].startOf('day').valueOf(),
      end: value[1].endOf('day').valueOf(),
    }
  );
}
