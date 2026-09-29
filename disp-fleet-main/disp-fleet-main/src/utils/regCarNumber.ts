export enum latinCharsToCyrillicList {
  latin = 'ABEKMHOPCTYXS',
  rus = 'АВЕКМНОРСТУХС',
}

/**
 * Маска для валидации полного регистрационного номера автомобиля
 */
// @ts-ignore
export const FinalRegNumberMask = /^[авекмнорстух]\d{3}(?<!000)[авекмнорстух]{2}(0[1-9]|[1-9]\d{1,2})$/iu;

/**
 * Валидирует номер автомобиля на основе длины и формата (частичная проверка по ходу ввода)
 * @param input - номер автомобиля
 * @returns true если номер валиден для текущей длины
 */
const validateByLength = (input: string): boolean => {
  let isValid: boolean;

  switch (input.length) {
    case 0:
      isValid = true;
      break;
    case 1:
      // @ts-ignore
      isValid = !!input.match(/^[авекмнорстух]$/iu);
      break;
    case 2:
      // @ts-ignore
      isValid = !!input.match(/^[авекмнорстух][0-9]$/iu);
      break;
    case 3:
      // @ts-ignore
      isValid = !!input.match(/^[авекмнорстух]\d{2}$/iu);
      break;
    case 4:
      // @ts-ignore
      isValid = !!input.match(/^[авекмнорстух]\d{3}(?<!000)$/iu);
      break;
    case 5:
      // @ts-ignore
      isValid = !!input.match(/^[авекмнорстух]\d{3}(?<!000)[авекмнорстух]$/iu);
      break;
    case 6:
      // @ts-ignore
      isValid = !!input.match(/^[авекмнорстух]\d{3}(?<!000)[авекмнорстух]{2}$/iu);
      break;
    case 7:
      // @ts-ignore
      isValid = !!input.match(/^[авекмнорстух]\d{3}(?<!000)[авекмнорстух]{2}[0-9]$/iu);
      break;
    case 8:
    case 9:
      isValid = !!input.match(FinalRegNumberMask);
      break;
    default:
      isValid = false;
  }

  return isValid;
};

/**
 * Преобразует государственный номер автомобиля: убирает пробелы, переводит латиницу в кириллицу, приводит к верхнему регистру
 * @param value - исходный номер
 * @returns преобразованный номер
 */
export const transformStateNumber = (value: string) => value
  .trim()
  .toUpperCase()
  .split('')
  .map(char => latinCharsToCyrillicList.rus.includes(char) || /\d/.test(char)
    ? char
    : latinCharsToCyrillicList.rus[latinCharsToCyrillicList.latin.indexOf(char)] ?? ''
  )
  .join('')
  .slice(0, 9);

/**
 * Нормализует регистрационный номер автомобиля: трансформирует и валидирует, возвращает prevValue если невалиден
 * @param value - введенный номер
 * @param prevValue - предыдущее валидное значение
 * @returns нормализованный номер или prevValue если ввод невалиден
 */
export const normalizeRegCarNumber = (value: string, prevValue: string) => {
  const transformedValue = transformStateNumber(value);

  return validateByLength(transformedValue) ? transformedValue : prevValue;
};
