/**
 * Преобразует номер телефона для отправки на бэкенд
 * @param phone - номер телефона
 * @param pattern - тип преобразования: 'onlyNumbers' (+7\d{10}) или 'withBrackets' (+7(\d{3})\d{7})
 * @returns отформатированный номер телефона
 * @example
 * preparePhoneForBackend('+7 999 123-45-67', 'onlyNumbers') // "+79991234567"
 * preparePhoneForBackend('8 999 123-45-67', 'withBrackets') // "+7(999)1234567"
 */
export const preparePhoneForBackend = (
  phone: string,
  pattern: 'onlyNumbers' | 'withBrackets' = 'onlyNumbers'
): string => {
  if (!phone) {
    return '+';
  }

  const phoneWithoutNotNumbers = `+${phone.replace(/[^\d]/g, '')}`;

  if (pattern === 'onlyNumbers') {
    return phoneWithoutNotNumbers;
  }

  return phoneWithoutNotNumbers.replace(/(\+7)(\d{3})(\d{7})/, '$1($2)$3');
};
