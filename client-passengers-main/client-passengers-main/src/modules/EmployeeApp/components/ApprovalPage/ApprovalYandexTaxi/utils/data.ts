/**
 * проверяет отклонение фактической цены от плановой
 * @param plannedCost - плановая цена
 * @param factCost - фактическя цена
 * @param treshhold - порог отклонения от 0 до 1, 1=100%
*/
export const isFactCostOverTreshold = (
  plannedCost: number | undefined,
  factCost: number | undefined | null,
  treshhold: number
): boolean => {
  if (!factCost || !plannedCost) return false;
  return ((factCost - plannedCost) / plannedCost > treshhold);
};
