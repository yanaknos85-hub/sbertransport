export const FIELD_LABELS = {
  corpClient: 'Корпоративный клиент',
  department: 'Подразделение',
  id: 'ID заявки',
  status: 'Статус заявки',
  creationTime: 'Дата и время создания заявки',
  deadline: 'Контрольный срок',
  contractor: 'Перевозчик',
  executorPhone: 'Телефон',
  carClass: 'Класс автомобиля',
  declarantFIO: 'ФИО заявителя',
  declarantPhone: 'Телефон',
  declarantPositionName: 'Должность',
  declarantAddressType: 'Тип адреса',
  declarantAddressFrom: 'Откуда',
  declarantAddressTo: 'Куда',
  addContactPhone: 'Телефон доп. контактного лица',
  addContractFIO: 'ФИО доп. контактного лица',
};

export const TAXI_CLASSES_OPTIONS = [
  { label: 'Эконом', value: 'ECONOMY' },
  { label: 'Комфорт', value: 'COMFORT' },
  { label: 'Комфорт+', value: 'COMFORT_PLUS' },
  { label: 'Бизнес', value: 'BUSINESS' },
  { label: 'Служебный', value: 'OFFICIAL' },
];

export const BUS_CLASSES_OPTIONS = [
  { label: 'Автобус до 9 мест', value: 'VIP_BUS' },
  { label: 'Автобус от 10 до 21 места', value: 'SMALL_BUS' },
  { label: 'Автобус от 22 до 41 места', value: 'MIDDLE_BUS' },
  { label: 'Автобус от 42 до 55 места', value: 'LARGE_BUS' },
];

export const COMMON_CLASSES_OPTIONS = [
  ...TAXI_CLASSES_OPTIONS,
  ...BUS_CLASSES_OPTIONS,
];

export enum TransportTypesFilter {
  TAXI = 'taxi',
  PERSONAL = 'personal',
  PUBLIC = 'public',
  CARSHARING = 'carsharing',
  GROUP_TRANSFER = 'group_transfer',
}
