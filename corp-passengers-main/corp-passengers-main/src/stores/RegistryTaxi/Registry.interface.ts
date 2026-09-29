export const coopTrip = [
  { name: 'Совместная', isCoop: Number(true) },
  { name: 'Индивидуальная', isCoop: Number(false) },
];

export const listOfRating = [
  { name: 1, rating: 1 },
  { name: 2, rating: 2 },
  { name: 3, rating: 3 },
  { name: 4, rating: 4 },
  { name: 5, rating: 5 },
];
// TODO:Сделал пока заглушку для отображения ждем бэк
export const employeeItinerantType = [
  { name: 'Да', listItinerant: ['FULL'] },
  { name: 'Нет', listItinerant: ['PARTIAL'] },
];

export enum TransportTypesPassenger {
  TAXI = 'TAXI',
  PERSONAL = 'PERSONAL',
  PUBLIC = 'PUBLIC',
  CARSHARING = 'CARSHARING',
  GROUP_TRANSFER = 'GROUP_TRANSFER',
}
