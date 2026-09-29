interface Vehicle {
  brand?: string | null;
  model?: string | null;
}

/**
 * Составляет полное название транспорта из бренда и модели
 * @param vehicle - объект с полями brand, model
 * @returns строка с полным названием транспорта
 */
export const getFullVehicle = <T extends Vehicle>(vehicle: T): string => (
  [vehicle.brand, vehicle.model].filter(Boolean).join(' ')
);
