/**
 * Форматирует рейтинг водителя из пятибальной системы (в формате 100 = 1.00)
 * @param rating - рейтинг в виде числа (от 0 до 100)
 * @param emptyValue - значение для отображения, если рейтинг отсутствует (по умолчанию '-')
 * @returns отформатированная строка с двумя знаками после запятой или значение emptyValue
 */
export const formatDriverRating = (rating: number | undefined | null, emptyValue = '-'): number | string => (
  rating ? (rating / 100).toFixed(2) : emptyValue
);
