export const FORMAT = 'DD.MM.YYYY, HH:mm';

export const VALUE_NOT_FOUND = '-';
export const DEADLINE_IS_VIOLATED = 'Нарушен';
export const DEADLINE_IS_NOT_VIOLATED = ' Не нарушен';

export enum TransportType {
  OFFICIAL = 'OFFICIAL',
  PRIVATE = 'PRIVATE',
  SPECIAL = 'SPECIAL',
}

export const CarTypeName: Record<TransportType, string> = {
  [TransportType.OFFICIAL]: 'Служебный',
  [TransportType.PRIVATE]: 'Личный',
  [TransportType.SPECIAL]: 'Специальный',
};

export enum latinCharsToCyrillicList {
  latin = 'ABEKMHOPCTYXS',
  rus = 'АВЕКМНОРСТУХС',
}

export const groupTransferChildSeatsLabels = {
  group1: 'Кресло от 9 мес. до 4 лет',
  group2: 'Кресло 3-7 лет',
  booster: 'Бустер 6-12 лет',
  newborn: 'Люлька до 1 года',
};

export enum OrderTitle {
  humanReadableId = 'ID заявки',
  tariffHumanReadableId = 'Номер тарифа',
  status = 'Статус заявки',

  creationTime = 'Дата и время создания заявки',
  pickupTime = 'Дата и время исполнения заявки',
  approvalTime = 'Дата и время согласования заявки',
  finalApprovalTime = 'Дата и время утверждения заявки',
  desiredTripTime = 'Желаемые дата и время поездки',
  transportType = 'Сервис',
  deadlineTime = 'Контрольный срок',

  car = 'Автомобиль',
  carNumber = 'Государственный номер',
  carBrand = 'Марка',
  carModel = 'Модель',

  person = 'Пассажир',
  passengersWithMe = 'Пассажиры со мной',
  passengersJoined = 'Присоединившиеся пассажиры',
  passengers = 'Пассажиры',
  personName = 'ФИО пассажира',
  personPhone = 'Телефон пассажира',

  creator = 'Заявитель',
  creatorName = 'ФИО',
  creatorPhone = 'Телефон',
  comment = 'Комментарий к заказу',
  driver = 'Водитель',

  agent = 'Исполнитель',
  contractor = 'Контрагент',
  tariffName = 'ID тарифа',
  trip = 'Поездка',
  personPositionName = 'Должность',
  personDepartmentName = 'Подразделение',
  approverName = 'ФИО согласующего',
  tripCost = 'Стоимость',
  tripDistance = 'Дистанция',
  tripTime = 'Время в пути',
  tripType = 'Тип поездки',
  relatedApplication = 'Связанная заявка',

  groupTransfer = 'Групповой трансфер',
  groupTransferAttributes = 'Дополнительные параметры',
  groupTransferClass = 'Вид сервиса',
  dateFlight = 'Дата и время рейса/поезда',
  numberFlight = 'Номер рейса/поезда',
  phoneHotel = 'Контакт принимающей гостиницы ',
  addContact = 'Дополнительный контакт',
  isVip = 'VIP',
  groupTransferChildSeats = 'Детское кресло',
  groupTransferAnimal = 'Перевозка животного',
  groupTransferOversizedLuggage = 'Негабаритный багаж',
  groupTransferLuggage = 'Количество багажа',
  groupTransferDesiredVehicle = 'Желаемый тип ТС',

  orderCreatorSectionTitle = 'Дополнительно',
  orderCreator = 'Инициатор/автор заявки',
  tripRating = 'Оценка поездки пользователем',
  tripRatingComment = 'Комментарий пользователя к оценке',

  creatorStructureTitle = 'Данные по структуре инициатора',
  creatorOrganization = 'Подразделение 1 уровня',
  creatorDepartment = 'Подразделение 2 уровня',

  planFactTitle = 'Плановые и фактические данные',
  territoryOrg = 'Наименование организации',

  primaryPassenger = 'ФИО пассажира (основного)',
  primaryPassengerPhone = 'Контактный номер (основного пассажира)',
  secondaryPassenger = 'ФИО дополнительного контактного лица',
  secondaryPassengerPhone = 'Контактный номер дополнительного контактного лица',
  passengersNumber = 'Количество пассажиров',
  tripPurpose = 'Цель поездки',

  groupTransferDepartureAddress = 'Адрес отправления',
  groupTransferArrivalAddress = 'Адрес прибытия',
  groupTransferIntermediateAddresses = 'Промежуточные адреса',
  groupTransferNumberOfPoints = 'Количество точек в маршруте',
  groupTransferPlannedRent = 'Плановая аренда, час',
  groupTransferArrivalTime = 'Факт. дата и время прибытия ТС',
  groupTransferOrderTime = 'Дата и время выполнения заявки',
  groupTransferOrderCloseTime = 'Дата закрытия обращения',
  groupTransferDeadline = 'Контроль. срок подача ТС (расчетный)',
  groupTransferDeadlineViolation = 'Нарушение КС',

  driverName = 'Имя, отчество водителя',
  vehicleData = 'Данные по транспортному средству',
  driverPhone = 'Контактный номер водителя ',
  preliminaryDistanceKm = 'Предварительный километраж в заявке, км',
  actualDistanceKm = 'Фактический километраж в поездке, км',
  prelimiaryAmount = 'Предварительная стоимость в заявке, руб',
  actualAmount = 'Фактическая стоимость в поездке, км',
  preliminaryRentTime = 'Предварительное время аренды, час/мин',
  actualTripTime = 'Фактическое время аренды в поездке, час/мин',
  actualWaitingTime = 'Суммарное фактическое время ожидания, мин',

}
