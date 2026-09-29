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

export const monthNamesShort = ['Янв', 'Фев', 'Март', 'Апр', 'Май', 'Июнь', 'Июль', 'Авг', 'Сент', 'Окт', 'Ноя', 'Дек'];

export const monthNamesEngShort = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

/** Конвертация для числа месяца в стрингу */
export const convertMonthNumToString = (
  num: number,
  options: { lowercase?: boolean; short?: boolean; eng?: boolean } = {}
): string => {
  const {
    lowercase, short, eng,
  } = options;

  const monthArr = short ? (eng ? monthNamesEngShort : monthNamesShort) : (eng ? monthNamesEng : monthNames);

  const month = monthArr[num];

  return lowercase ? month.toLocaleLowerCase() : month;
};

/** Конвертация для отправки на бек */
export const normalizeMonth = (num: number): string => convertMonthNumToString(num, { lowercase: false, eng: true });
