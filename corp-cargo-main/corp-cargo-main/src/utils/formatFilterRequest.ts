/**
  * Форматирует объект фильтра заявок в строку с человеко-читаемыми названиями полей.
  * Фильтрует пустые значения и преобразует технические названия полей в понятные пользователю.
  * @returns {string} Строка с перечислением использованных полей фильтрации на русском языке, разделённых запятой.
  * Возвращает пустую строку, если фильтр пуст или не задан.
  *
  */
export const formatFilterRequest = <T>(
  fieldLabels: Record<keyof T, string>
) => {
  return (filterRequest: T): string => {
    if (!filterRequest) return '';

    const filters = filterRequest as Record<string, unknown>;
    const { empty, ...restFilters } = filters;

    return Object.entries(restFilters)
      .filter(([_, value]) => {
        if (value === undefined || value === null || value === '') return false;
        if (Array.isArray(value) && value.length === 0) return false;
        if (typeof value === 'object' && value !== null && Object.keys(value).length === 0) return false;
        return true;
      })
      .map(([key, _]) => fieldLabels[key as keyof T] || key)
      .join(', ');
  };
};
