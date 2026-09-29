/**
 * @function preparePhoneForBackend
 * @param {string} phone
 * @param {'onlyNumbers' | 'withBrackets'} pattern - тип преобразования
 * @returns в базе телефон храниться ввиде строки +7\d{10} или +7(\d{3})\d{7}
 */
export const preparePhoneForBackend = (
  phone: string,
  pattern: 'onlyNumbers' | 'withBrackets' = 'onlyNumbers'
): string => {
  const phoneWithoutNotNumbers = `+${phone.replace(/[^\d]/g, '')}`;

  if (pattern === 'onlyNumbers') {
    return phoneWithoutNotNumbers;
  }

  return phoneWithoutNotNumbers.replace(/(\+7)(\d{3})(\d{7})/, '$1($2)$3');
};
