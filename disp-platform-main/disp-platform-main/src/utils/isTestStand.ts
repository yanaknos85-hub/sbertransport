/**
 * Проверяет, запущено ли приложение на тестовом стенде
 * @returns true если текущий домен является тестовым стендом (содержит .platform., .taxi., .cargo., .fleet. или localhost)
 */
export const isTestStand = () => {
  // Включаем в тестовые стенды только локалку и девы, а на остальных стендам даем
  // по умолчанию тестить, как пром
  const testStandPrefixes = ['.platform.', '.taxi.', '.cargo.', '.fleet.', 'localhost'];

  return testStandPrefixes.some(prefix => window.location.origin.includes(prefix));
};
