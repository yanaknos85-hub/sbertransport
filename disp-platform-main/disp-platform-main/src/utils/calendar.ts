/**
 * Названия месяцев на русском языке
 */
export const monthNames = [
  'Январь',
  'Февраль',
  'Март',
  'Апрель',
  'Май',
  'Июнь',
  'Июль',
  'Август',
  'Сентябрь',
  'Октябрь',
  'Ноябрь',
  'Декабрь',
];

/**
 * Названия месяцев на английском языке (верхний регистр)
 */
export const monthNamesEng = [
  'JANUARY',
  'FEBRUARY',
  'MARCH',
  'APRIL',
  'MAY',
  'JUNE',
  'JULY',
  'AUGUST',
  'SEPTEMBER',
  'OCTOBER',
  'NOVEMBER',
  'DECEMBER',
];

/**
 * Сокращенные названия месяцев на русском языке
 */
export const monthNamesShort = ['Янв', 'Фев', 'Март', 'Апр', 'Май', 'Июнь', 'Июль', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'];

/**
 * Сокращенные названия месяцев на английском языке
 */
export const monthNamesEngShort = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

/**
 * Делает первую найденную букву в строке заглавной
 * @param str - входная строка
 * @returns строка с заглавной первой буквой
 */
export const makeFirstFoundCharUppercase = (str: string): string => {
  const firstLetterMatch = str.match(/[a-zA-Zа-яёА-ЯЁ]/);
  if (!firstLetterMatch) return str;

  const pos = firstLetterMatch.index;

  return str.slice(0, pos!) + str[pos!].toUpperCase() + str.slice(pos! + 1);
};
