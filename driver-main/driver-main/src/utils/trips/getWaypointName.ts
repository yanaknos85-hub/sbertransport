export const getWaypointName = (index: number, length: number) => {
  switch (index) {
    case 0: return 'Начало поездки';
    case length - 1: return 'Конец поездки';
    default: return 'Промежуточная точка';
  }
};
