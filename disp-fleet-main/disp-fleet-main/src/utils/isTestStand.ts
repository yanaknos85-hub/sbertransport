/**
 * Определяет, запущено ли приложение на тестовом стенде
 * @returns true если текущий стенд является тестовым (локалка, девы, определенные домены)
 */
export const isTestStand = () => {
  // Включаем в тестовые стенды только локалку и девы, а на остальных стендам даем
  // по умолчанию тестить, как пром
  const testStandPrefixes = ['.platform.', '.taxi.', '.cargo.', '.fleet.', 'localhost'];

  return testStandPrefixes.some(prefix => window.location.origin.includes(prefix));
};
