import { NotificationInfo } from '../notifications';

export const labelInterpolation = {
  employee: {
    label: '<ФИО пассажира>',
    property: '{request.passenger.firstName} {request.passenger.lastName} {request.passenger.patronymic}[0].',
  },
  dateOnly: {
    label: '<желаемая дата>',
    property: '{request.desiredDate}', // Дата
  },
  date: {
    label: '<желаемая дата',
    property: '',
  },
  tripId: {
    label: '<ID заявки>',
    property: '{request.humanReadableId}',
  },
  time: {
    label: 'и время поездки>',
    property: '{request.desiredDate}', // Время
  },
  status: {
    label: '<статус>',
    property: '{status}',
  },
  color: {
    label: '<цвет>',
    property: '', // не реализовано
  },
  model: {
    label: '<марка>',
    property: '', // не реализовано
  },
  number: {
    label: '<номер>',
    property: '', // не реализовано
  },
  waitCostPerMin: {
    label: '<Стоимость за минуту ожидания при подаче, руб.>',
    property: '{request.tariff.}', // уточнить
  },
  waitCostPerMinIntermediate: {
    label: '<Стоимость за минуту ожидания в промежуточной точке, руб.>',
    property: '{request.tariff}', // уточнить
  },
  timeDeviationMin: {
    label: '<Предварительное время поездки>',
    property: '{request.expected.duration}',
  },
  excessWaitingInIntermediate: {
    label: '<Превышение времени ожидания в промежуточном пункте. Время>',
    property: '', // Не реализовано
  },
  attachPassengerName: {
    label: '<ФИО пассажира, присоединяющегося к поездке>',
    property: '{request.passenger.firstName} {request.passenger.lastName} {request.passenger.patronymic}',
  },
  declinePassengerName: {
    label: '<ФИО пассажира, исключенного из СП>',
    property: '{request.passenger.firstName} {request.passenger.lastName} {request.passenger.patronymic}',
  },
  suitableTripId: {
    label: '<ID_СП>',
    property: '', // уточнить
  },
  somePeriod: {
    label: '<какой-то промежуток времени>',
    property: '',
  },
  percent: {
    label: '<процент остатка>',
    property: '', // уточнить
  },
  transportType: {
    label: '<вид транспорта>',
    property: '{transportType}',
  },
  department: {
    label: '<Наименование подразделения>',
    property: '{department.humanReadableId}', // уточнить
  },
  requestLimitPersonId: {
    label: '<ID заявки на пополнение личного лимита>',
    property: '{humanReadableId}', // не реализоано
  },
  requestLimitDepartmentId: {
    label: '<ID заявки на пополнение лимита подразделения>',
    property: '{humanReadableId}', // не реализоано
  },
  limitDepartmentId: {
    label: '<ID лимита>',
    property: '{humanReadableId}', // уточнить
  },
  sum: {
    label: '<сумма изменения>',
    property: '{sum}', // уточнить
  },
  changeInitiator: {
    label: '<ФИО инициатора изменений>',
    property: '', // уточнить
  },
  difference: {
    label: '<сумма изменения>',
    property: '{sum}', // уточнить
  },
  limitDepartmentStatus: {
    label: '<статус лимита подразделения>',
    property: '{status}', // уточнить
  },
  limitPersonStatus: {
    label: '<статус личного лимита>',
    property: '{status}', // уточнить
  },
  personLimitSum: {
    label: '<Общая сумма выделенного лимита на все виды транспорта>',
    property: '', // уточнить
  },
  transportTypeLimit: {
    label: '<сумма на вид транспорта>',
    property: '{sum}', // уточнить
  },
  delegatePeriod: {
    label: '<период делегирования>',
    property: '',
  },
  newDelegatePeriod: {
    label: '<новый период делегирования>',
    property: '{startDate} - {endDate}', // уточнить
  },
  oldDelegatePeriod: {
    label: '<старый период делегирования>',
    property: '{old.startDate} - {old.endDate}', // уточнить
  },
  timeToTarget: {
    label: '<timeToTarget>',
    property: '', // уточнить
  },
  driver: {
    label: '<driver>',
    property: '', // уточнить
  },
  onlyDate: {
    label: '<onlyDate>',
    property: '', // уточнить
  },
  id: {
    label: '<id>',
    property: '', // уточнить
  },
};

export type LabelInterpolation = keyof typeof labelInterpolation;

const interpolate = (parts: TemplateStringsArray, ...keys: LabelInterpolation[]): string => parts.map((part, index) => part + (labelInterpolation[keys[index]]?.label || '')).join('');

export const Notifications = {
  auth: {
    sessionExpired: 'Ваша сессия истекла. Пожалуйста, войдите в приложение снова.',
  },
  tableHeaders: {
    notification: 'Уведомление',
    pushNotification: 'Текст уведомления Push',
    smsNotification: 'Текст уведомления SMS',
    outlookNotification: 'Текст уведомления Outlook',
    meanings: 'Способ уведомления',
    restrictions: 'Ограничения по ролям',
    approvementFrequency: 'Частота уведомлений о необходимости согласовать заявку',
    statusFrequency: 'Частота уведомлений о статусе согласования заявки',
    excessWaitingInIntermediate: 'Превышение времени ожидания в промежуточном пункте',
    lowLimitNotification: 'Уведомление о низком остатке лимита',
    enableHeaders: 'Способ уведомления',
  },
  REQUEST_TAXI: {
    requestApprovement: {
      name: 'Согласование заявки',
      text: interpolate`Согласование поездки на такси ${'employee'} ${'date'} ${'time'}.`,
    },
    approvementStatus: {
      name: 'Статус согласования заявки',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    waitAssignment: {
      name: 'Ожидайте назначение водителя',
      text: interpolate`Ожидайте назначения водителя. Мы ищем для Вас лучшее решение.`,
    },
    driverAssignment: {
      name: 'Назначение водителя',
      text: interpolate`Автомобиль ${'color'} ${'model'} г.н. ${'number'} прибудет через ${'timeToTarget'} минут.`,
    },
    driverWaitesInTarget: {
      name: 'Водитель ожидает в пункте назначения',
      text: interpolate`Водитель ${'driver'} ожидает в пункте назначения. Автомобиль ${'color'} ${'model'} г.н. ${'number'}.`,
    },
    freeTimeOver: {
      name: 'О начале платного ожидания в точке назначения',
      text: interpolate`Время бесплатного ожидания истекло, каждая последующая минута ожидания составит ${'waitCostPerMin'} руб.`,
    },
    rideStarted: {
      name: 'Поездка началась',
      text: interpolate`Поездка началась, время в пути составит ${'timeDeviationMin'}`,
    },
    arrivalToIntermediate: {
      name: 'Прибытие в промежуточный пункт',
      text: interpolate`Вы прибыли в промежуточную точку. Помните, что платная стоимость ожидания составляет ${'waitCostPerMinIntermediate'} руб./мин.`,
    },
    excessWaitingInIntermediate: {
      name: 'Превышение времени ожидания в промежуточном пункте',
      text: interpolate`Вы превысили нормативное время ожидания в ${'excessWaitingInIntermediate'} минут.`,
    },
    rideFinished: {
      name: 'Поездка завершена',
      text: interpolate`Поездка завершена.`,
    },
    attachementToSuitableRide: {
      name: 'Присоединение к совместной поездке',
      text: interpolate`К вашей поезде присоединился ${'attachPassengerName'}.`,
    },
    decliningFromSuitableRide: {
      name: 'Изменение в совместной поездке',
      text: interpolate`Пассажир ${'declinePassengerName'} отказался от совместной поездки с вами № ${'suitableTripId'}.`,
    },
  } as Record<string, NotificationInfo>,
  REQUEST_PUBLIC: {
    requestApprovement: {
      name: 'Согласование заявки',
      text: interpolate`Согласование поездки на общественном транспорте ${'employee'} ${'date'}.`,
    },
    approvementStatus: {
      name: 'Статус согласования заявки',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    supportCosts: {
      name: 'Подтверждение поездки',
      text: interpolate`Необходимо подтвердить расходы на поездку по заявке ${'tripId'} ${'date'} ${'time'}`,
    },
    tripImplementation: {
      name: 'Утверждение',
      text: interpolate`Утверждение поездки на общественном транспорте ${'employee'} ${'dateOnly'}.`,
    },
    tripImplementationStatus: {
      name: 'Статус утверждения',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап 'Утверждение' со статусом '${'status'}'.`,
    },
    paymentStatus: {
      name: 'Статус оплаты',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап 'Ожидание выплаты' со статусом '${'status'}'.`,
    },
  } as Record<string, NotificationInfo>,
  REQUEST_PERSONAL: {
    requestApprovement: {
      name: 'Согласование заявки',
      text: interpolate`Согласование поездки на личном транспорте ${'employee'} ${'date'}.`,
    },
    approvementStatus: {
      name: 'Статус согласования заявки',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    arrivalToIntermediate: {
      name: 'Прибытие в промежуточный пункт',
      text: 'Вы прибыли в промежуточный пункт, не забудьте отменить свое пребывание в нем для выплаты компенсации.',
    },
    rideFinished: {
      name: 'Поездка завершена',
      text: 'Поездка завершена.',
    },
    tripImplementation: {
      name: 'Утверждение финального маршрута',
      text: 'Утверждение поездки на личном транспорте <ФИО пассажира> <желаемая дата>',
    },
    tripImplementationStatus: {
      name: 'Статус утверждения финального маршрута',
      text: interpolate`По вашей поездке ${'tripId'} <желаемая дата и время поездки> завершен этап 'Утверждение маршрута' со статусом '${'status'}'.`,
    },
    paymentDone: {
      name: 'Ожидание выплаты',
      text: interpolate`Ваша заявка ${'tripId'} ${'date'} ${'time'} была отправлена на выплату.`,
    },
    paymentStatusWaiting: {
      name: 'Статус ожидания выплаты',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап 'Ожидание выплаты' со статусом '${'status'}'.`,
    },
    attachementToSuitableRide: {
      name: 'Присоединение к совместной поездке',
      text: interpolate`К вашей поезде присоединился ${'attachPassengerName'}.`,
    },
    decliningFromSuitableRide: {
      name: 'Изменения в совместной поездке',
      text: interpolate`Пассажир ${'declinePassengerName'} отказался от совместной поездки с вами № ${'suitableTripId'}.`,
    },
    pointsApprovement: {
      name: 'Согласование доп. точек в маршруте',
      text: interpolate`Необходимо согласовать дополнительные точки в маршруте поездки на личном транспорте ${'employee'} ${'onlyDate'}.`,
    },
    pointsApprovementDone: {
      name: 'Добавление дополнительных точек в маршрут было согласовано.',
      text: interpolate`Добавление дополнительных точек по вашей поездке ${'tripId'} ${'date'} ${'time'} было согласовано.`,
    },
    pointsApprovementDeclined: {
      name: 'Добавление дополнительных точек в маршрут было отклонено.',
      text: interpolate`Добавление дополнительных точек по вашей поездке ${'tripId'} ${'date'} ${'time'} было отклонено.`,
    },
  } as Record<string, NotificationInfo>,
  REQUEST_CAR_SHARING: {
    requestApprovement: {
      name: 'Согласование заявки',
      text: interpolate`Согласование поездки на каршеринге ${'employee'} ${'date'}.`,
    },
    approvementStatus: {
      name: 'Статус согласования заявки',
      text: interpolate`По вашей поездке ${'id'} ${'date'} ${'time'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    vehicleReservation: {
      name: 'Бронирование автомобиля',
      text: interpolate`Вы забронировали автомобиль. У Вас есть ${'somePeriod'} минут для того, чтобы добраться до него.`,
    },
    vehicleInspection: {
      name: 'Осмотр автомобиля',
      text: 'Осмотрите авто на наличие дефектов. Сделайте фото с разных ракурсов и загрузите в приложение.',
    },
    rentStarted: {
      name: 'Старт аренды. Поездка началась',
      text: 'Будьте вдвойне внимательны. Не забывайте, что Вы едете на арендованном автомобиле. Поехали…',
    },
    arrivalToIntermediate: {
      name: 'Прибытие в промежуточный пункт',
      text: interpolate`Вы прибыли в промежуточную точку. Помните, что платная стоимость ожидания составляет ${'waitCostPerMinIntermediate'} руб./мин`,
    },
    rentFinished: {
      name: 'Завершение аренды',
      text: 'Припаркуйтесь в разрешенном месте. Сфотографируйте автомобиль со всех сторон. Завершите аренду в приложении.',
    },
    rideFinished: {
      name: 'Поездка завершена',
      text: 'Поездка завершена. Не забудьте оценить качество сервиса.',
    },
    attachementToSuitableRide: {
      name: 'Присоединение к совместной поездке',
      text: interpolate`К вашей поезде присоединился ${'attachPassengerName'}.`,
    },
    decliningFromSuitableRide: {
      name: 'Изменения в совместной поездке',
      text: interpolate`Пассажир ${'declinePassengerName'} отказался от совместной поездки с вами № ${'suitableTripId'}.`,
    },
  } as Record<string, NotificationInfo>,
  REQUEST_BICYCLE: {
    requestApprovement: {
      name: 'Согласование заявки',
      text: interpolate`Согласование поездки на каршеринге ${'employee'} ${'date'}.`,
    },
    approvementStatus: {
      name: 'Статус согласования заявки',
      text: interpolate`По вашей поездке ${'id'} ${'date'} ${'time'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    vehicleReservation: {
      name: 'Бронирование велосипеда',
      text: interpolate`Вы забронировали велосипед. У Вас есть ${'somePeriod'} минут для того, чтобы добраться до него.`,
    },
    vehicleInspection: {
      name: 'Осмотр велосипеда',
      text: 'Осмотрите велосипед на наличие дефектов. Сделайте фото с разных ракурсов и загрузите в приложение.',
    },
    rentStarted: {
      name: 'Старт аренды. Поездка началась',
      text: 'Будьте вдвойне внимательны. Не забывайте, что Вы едете на арендованном велосипеде. Поехали…',
    },
    arrivalToIntermediate: {
      name: 'Прибытие в промежуточный пункт',
      text: interpolate`Вы прибыли в промежуточную точку. Помните, что платная стоимость ожидания составляет ${'waitCostPerMinIntermediate'} руб./мин`,
    },
    rentFinished: {
      name: 'Завершение аренды',
      text: 'Припаркуйтесь в разрешенном месте. Сфотографируйте велосипед со всех сторон. Завершите аренду в приложении.',
    },
    rideFinished: {
      name: 'Поездка завершена',
      text: 'Поездка завершена. Не забудьте оценить качество сервиса.',
    },
  } as Record<string, NotificationInfo>,
  REQUEST_SCOOTER: {
    requestApprovement: {
      name: 'Согласование заявки',
      text: interpolate`Согласование поездки на самокате ${'employee'} ${'date'}.`,
    },
    approvementStatus: {
      name: 'Статус согласования заявки',
      text: interpolate`По вашей поездке ${'id'} ${'date'} ${'time'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    vehicleReservation: {
      name: 'Бронирование самоката',
      text: interpolate`Вы забронировали самокат. У Вас есть ${'somePeriod'} минут для того, чтобы добраться до него.`,
    },
    vehicleInspection: {
      name: 'Осмотр самоката',
      text: 'Осмотрите самокат на наличие дефектов. Сделайте фото с разных ракурсов и загрузите в приложение.',
    },
    rentStarted: {
      name: 'Старт аренды. Поездка началась',
      text: 'Будьте вдвойне внимательны. Не забывайте, что Вы едете на арендованном самокате. Поехали…',
    },
    arrivalToIntermediate: {
      name: 'Старт аренды. Поездка началась',
      text: interpolate`Вы прибыли в промежуточную точку. Помните, что платная стоимость ожидания составляет ${'waitCostPerMinIntermediate'} руб./мин`,
    },
    rentFinished: {
      name: 'Завершение аренды',
      text: 'Припаркуйтесь в разрешенном месте. Сфотографируйте самокат со всех сторон. Завершите аренду в приложении.',
    },
    rideFinished: {
      name: 'Поездка завершена',
      text: 'Поездка завершена. Не забудьте оценить качество сервиса.',
    },
  } as Record<string, NotificationInfo>,
  REQUEST_LIMIT_PERSON: {
    requestApprovement: {
      name: 'Согласование заявки',
      text: interpolate`Согласование заявки ${'employee'} на пополнение личного лимита на ${'transportType'}.`,
    },
    approvementStatus: {
      name: 'Статус согласования ',
      text: interpolate`По вашей заявке ${'requestLimitPersonId'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    limitFilled: {
      name: 'Исполнение заявки',
      text: 'Личный лимит был пополнен.',
    },
  } as Record<string, NotificationInfo>,
  REQUEST_LIMIT_DEPARTMENT: {
    requestApprovement: {
      name: 'Согласование заявки',
      text: interpolate`Согласование лимита подразделения ${'department'}`,
    },
    approvementStatus: {
      name: 'Статус согласования',
      text: interpolate`По вашей заявке ${'requestLimitDepartmentId'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    limitFilled: {
      name: 'Исполнение заявки',
      text: 'Лимит подразделения был пополнен.',
    },
  } as Record<string, NotificationInfo>,
  LIMIT_DEPARTMENT: {
    requestApprovement: {
      name: 'Выделение лимита подразделения',
      text: interpolate`Вашему подразделению назначен лимит ${'id'} на период ${'somePeriod'}`,
    },
    approvementStatus: {
      name: 'Изменение лимита подразделения',
      text: interpolate`По решению ${'changeInitiator'} ваш лимит подразделения на транспортное обеспечение изменился. Снизился/увеличился на ${'difference'} руб.`,
    },
    limitStatus: {
      name: 'Статус лимита подразделения',
      text: interpolate`Статус лимита изменен на ${'limitDepartmentStatus'}`,
    },
    lowLimit: {
      name: 'Уведомление о низком остатке лимита',
      text: interpolate`Лимит подразделения израсходован на ${'percent'} %. Обратите внимание на статистику поездок ваших сотрудников.`,
    },
    lowLimitFromEmployee: {
      name: 'Уведомление от сотрудника о низком остатке лимита',
      text: interpolate`Лимит подразделения израсходован на ${'percent'} %.`,
    },
  } as Record<string, NotificationInfo>,
  LIMIT_PERSON: {
    requestApprovement: {
      text: 'Выделение личного лимита',
      name: interpolate`Вам распределен личный лимит на транспортное обеспечение ${'personLimitSum'} руб. на период ${'somePeriod'}.`,
    },
    approvementStatus: {
      text: 'Изменение личного лимита',
      name: interpolate`Ваш личный лимит на ${'transportType'} ${'sum'} руб. на период ${'somePeriod'} изменился на ${'difference'} руб. на период ${'somePeriod'}.`,
    },
    limitStatus: {
      text: 'Статус личного лимита',
      name: interpolate`Статус лимита изменен на ${'limitPersonStatus'}`,
    },
    lowLimit: {
      text: 'Уведомление о низком остатке лимита',
      name: interpolate`Ваш личный лимит на ${'transportType'} израсходован на ${'percent'} %. Обратите внимание на статистику поездок ваших сотрудников.`,
    },
  } as Record<string, NotificationInfo>,
  USER_DELEGATE: {
    credentialsAssignment: {
      name: 'Назначение',
      text: interpolate`Вам делегированы полномочия на согласование обращений по ${'transportType'} на период ${'delegatePeriod'}`,
    },
    credentialsChanged: {
      name: 'Изменение полномочий - Срок полномочий',
      text: interpolate`Период действия ваших полномочий по согласованию заявок на ${'transportType'} изменен с ${'oldDelegatePeriod'} по ${'newDelegatePeriod'}. По истечении периода право согласования будет отозвано.`,
    },
    limitStatus: {
      name: 'Изменение полномочий - Вид транспорта',
      text: interpolate`Право согласования для ${'transportType'} на период ${'delegatePeriod'} было отозвано ${'changeInitiator'}`,
    },
  } as Record<string, NotificationInfo>,
  USER_OWNER_LIMIT: {
    limitOwnerAssignment: {
      name: 'Назначение',
      text: 'Вы назначены владельцем лимита на транспортное обеспечение.',
    },
    credentialsChanged: {
      name: 'Изменение полномочий',
      text: interpolate`Период действия полномочий Владельца лимита изменен с ${'oldDelegatePeriod'} по ${'newDelegatePeriod'}. После истечения периода полномочия будут отозваны.`,
    },
  } as Record<string, NotificationInfo>,
  USER_ASSIGNMENT: {
    supervisorAssignment: {
      name: 'Назначение руководителем подразделения',
      text: 'Поздравляем! Вы назначены руководителем подразделения!',
    },
  } as Record<string, NotificationInfo>,
};
