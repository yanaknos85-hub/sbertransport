/**
 * Метод для склонения чисел
 *
 * @param number число для которого нужно получить числительное
 * @param words склонения. Пример: ['год', 'года', 'лет']
 */
export const declOfNum = (number: number, words: [string, string, string]): string => (
  words[number % 100 > 4 && number % 100 < 20 ? 2 : [2, 0, 1, 1, 1, 2][number % 10 < 5 ? Math.abs(number) % 10 : 5]]
);

export const declOfNumForSymbols = (number: number): string => declOfNum(number, ['символ', 'символа', 'символов']);

export const declOfNumForPlaces = (number: number): string => declOfNum(number, ['место', 'места', 'мест']);
