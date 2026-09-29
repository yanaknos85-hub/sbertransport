/**
 * Форматирует полное имя из имени, фамилии и отчества в формате "Фамилия И.О."
 * @param firstName - имя
 * @param lastName - фамилия
 * @param middleName - отчество
 * @returns отформатированная строка полного имени
 */
export const formatFullName = (
  firstName: string | undefined,
  lastName: string | undefined,
  middleName: string | undefined
): string => {
  const formattedFirstName = firstName?.trim() ? `${firstName.charAt(0)}.` : '';
  const formattedMiddleName = middleName?.trim() ? `${middleName.charAt(0)}.` : '';
  const formattedLastNameName = lastName?.trim() || '';
  const initial = `${formattedFirstName}${formattedMiddleName}`;
  const formattedFullName = initial ? `${formattedLastNameName} ${initial}` : formattedLastNameName;

  return formattedFullName;
};
