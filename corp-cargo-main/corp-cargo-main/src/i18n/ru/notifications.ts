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
    enableHeaders: 'Способ уведомления',
    restrictions: 'Ограничения по ролям',
    approvementFrequency: 'Частота уведомлений о необходимости согласовать заявку',
    statusFrequency: 'Частота уведомлений о статусе согласования заявки',
    excessWaitingInIntermediate: 'Превышение времени ожидания в промежуточном пункте',
    lowLimitNotification: 'Уведомление о низком остатке лимита',
    eventCondition: 'Условие наступления события',
  },
  REQUEST_TAXI: {
    requestApprovement: {
      description: 'notice_101',
      name: 'Согласование заявки на такси',
      text: interpolate`Согласование поездки на такси ${'employee'} ${'date'} ${'time'}.`,
    },
    approvementStatus: {
      description: 'notice_102',
      name: 'Статус согласования заявки на такси',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    waitAssignment: {
      description: 'notice_103',
      name: 'Ожидайте назначение водителя',
      text: interpolate`Ожидайте назначения водителя. Мы ищем для Вас лучшее решение.`,
    },
    driverAssignment: {
      description: 'notice_104',
      name: 'Назначение водителя',
      text: interpolate`Автомобиль ${'color'} ${'model'} г.н. ${'number'} прибудет через ${'timeToTarget'} минут.`,
    },
    driverWaitesInTarget: {
      description: 'notice_105',
      name: 'Водитель ожидает в пункте назначения',
      text: interpolate`Водитель ${'driver'} ожидает в пункте назначения. Автомобиль ${'color'} ${'model'} г.н. ${'number'}.`,
    },
    freeTimeOver: {
      description: 'notice_106',
      name: 'О начале платного ожидания в точке назначения',
      text: interpolate`Время бесплатного ожидания истекло, каждая последующая минута ожидания составит ${'waitCostPerMin'} руб.`,
    },
    rideStarted: {
      description: 'notice_107',
      name: 'Поездка на такси началась',
      text: interpolate`Поездка началась, время в пути составит ${'timeDeviationMin'}`,
    },
    arrivalToIntermediate: {
      description: 'notice_108',
      name: 'Прибытие на такси в промежуточный пункт',
      text: interpolate`Вы прибыли в промежуточную точку. Помните, что платная стоимость ожидания составляет ${'waitCostPerMinIntermediate'} руб./мин.`,
    },
    excessWaitingInIntermediate: {
      description: 'notice_109',
      name: 'Превышение времени ожидания такси в промежуточном пункте',
      text: interpolate`Вы превысили нормативное время ожидания в ${'excessWaitingInIntermediate'} минут.`,
    },
    rideFinished: {
      description: 'notice_110',
      name: 'Поездка на такси завершена',
      text: interpolate`Поездка завершена.`,
    },
    attachementToSuitableRide: {
      description: 'notice_111',
      name: 'Присоединение к совместной поездке на такси',
      text: interpolate`К вашей поезде присоединился ${'attachPassengerName'}.`,
    },
    decliningFromSuitableRide: {
      description: 'notice_112',
      name: 'Изменение в совместной поездке на такси',
      text: interpolate`Пассажир ${'declinePassengerName'} отказался от совместной поездки с вами № ${'suitableTripId'}.`,
    },
  } as Record<string, NotificationInfo>,
  REQUEST_PUBLIC: {
    requestApprovement: {
      description: 'notice_201',
      name: 'Согласование заявки на общественный транспорт',
      text: interpolate`Согласование поездки на общественном транспорте ${'employee'} ${'date'}.`,
    },
    approvementStatus: {
      description: 'notice_202',
      name: 'Статус согласования заявки на общественный транспорт',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    supportCosts: {
      description: 'notice_203',
      name: 'Подтверждение поездки на общественном транспорте',
      text: interpolate`Необходимо подтвердить расходы на поездку по заявке ${'tripId'} ${'date'} ${'time'}`,
    },
    tripImplementation: {
      description: 'notice_204',
      name: 'Утверждение поездки на общественном транспорте',
      text: interpolate`Утверждение поездки на общественном транспорте ${'employee'} ${'dateOnly'}.`,
    },
    tripImplementationStatus: {
      description: 'notice_205',
      name: 'Статус утверждения поездки на общественном транспорте',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап 'Утверждение' со статусом '${'status'}'.`,
    },
    paymentStatus: {
      description: 'notice_206',
      name: 'Статус оплаты поездки на общественном транспорте',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап 'Ожидание выплаты' со статусом '${'status'}'.`,
    },
  } as Record<string, NotificationInfo>,
  REQUEST_PERSONAL: {
    requestApprovement: {
      description: 'notice_301',
      name: 'Согласование заявки на личный транспорт',
      text: interpolate`Согласование поездки на личном транспорте ${'employee'} ${'date'}.`,
    },
    approvementStatus: {
      description: 'notice_302',
      name: 'Статус согласования заявки на личный транспорт',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    coopTripAttachmentApprove: {
      description: 'notice_303',
      name: 'Согласование присоединения к СП на личном транспорте',
      text: interpolate`К вашей поездке присоединился ${'attachPassengerName'}.`,
    },
    coopTripAttachmentApproveStatus: {
      description: 'notice_304',
      name: 'Статус присоединения к СП на личном транспорте',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап "Согласование присоединения"  со статусом '${'status'}'.`,
    },
    tripStartRemind: {
      description: 'notice_305',
      name: 'Напоминание о начале поездки',
      text: interpolate`Ваша поездка скоро начнется. Не забудьте отметиться в приложении.`,
    },
    arrivalToIntermediate: {
      description: 'notice_306',
      name: 'Прибытие в промежуточный пункт на личном транспорте',
      text: 'Вы прибыли в промежуточный пункт, не забудьте отменить свое пребывание в нем для выплаты компенсации.',
    },
    rideFinished: {
      description: 'notice_307',
      name: 'Поездка на личном транспорте завершена',
      text: 'Поездка завершена.',
    },
    tripImplementation: {
      description: 'notice_308',
      name: 'Утверждение финального маршрута на личном транспорте',
      text: 'Утверждение поездки на личном транспорте <ФИО пассажира> <желаемая дата>',
    },
    tripImplementationStatus: {
      description: 'notice_309',
      name: 'Статус утверждения финального маршрута на личном транспорте',
      text: interpolate`По вашей поездке ${'tripId'} <желаемая дата и время поездки> завершен этап 'Утверждение маршрута' со статусом '${'status'}'.`,
    },
    paymentDone: {
      description: 'notice_310',
      name: 'Ожидание выплаты за поездку на личном транспорте',
      text: interpolate`Ваша заявка ${'tripId'} ${'date'} ${'time'} была отправлена на выплату.`,
    },
    paymentStatusWaiting: {
      description: 'notice_311',
      name: 'Статус ожидания выплаты за поездку на личном транспорте',
      text: interpolate`По вашей поездке ${'tripId'} ${'date'} ${'time'} завершен этап 'Ожидание выплаты' со статусом '${'status'}'.`,
    },
    attachementToSuitableRide: {
      description: 'notice_312',
      name: 'Присоединение к совместной поездке на личном транспорте',
      text: interpolate`К вашей поезде присоединился ${'attachPassengerName'}.`,
    },
    decliningFromSuitableRide: {
      description: 'notice_313',
      name: 'Изменения в совместной поездке на личном транспорте',
      text: interpolate`Пассажир ${'declinePassengerName'} отказался от совместной поездки с вами № ${'suitableTripId'}.`,
    },
    pointsApprovement: {
      description: 'notice_314',
      name: 'Согласование доп. точек в маршруте на личном транспорте',
      text: interpolate`Необходимо согласовать дополнительные точки в маршруте поездки на личном транспорте ${'employee'} ${'onlyDate'}.`,
    },
    pointsApprovementDone: {
      description: 'notice_315',
      name: 'Добавление дополнительных точек в маршрут было согласовано',
      text: interpolate`Добавление дополнительных точек по вашей поездке ${'tripId'} ${'date'} ${'time'} было согласовано.`,
    },
    /** хук useDefaultNotificationsByClasses создаёт настройки по умолчанию основываясь на NotificationType из
     * Notifications.interface - на беке в enum notificationType нет записи для
     * 'Добавление дополнительных точек в маршрут было отклонено.'. Без него при попытке изменить или сохранить
     * настройки будет падать ошибка - параметр обязательный. Как бэк обновит данные - раскоменитровать */
    // pointsApprovementDeclined: {
    //   description: 'notice_316',
    //   name: 'Добавление дополнительных точек в маршрут было отклонено.',
    //   text: interpolate`Добавление дополнительных точек по вашей поездке ${'tripId'} ${'date'} ${'time'} было отклонено.`,
    // },
  } as Record<string, NotificationInfo>,
  REQUEST_CAR_SHARING: {
    // requestAttachment: {
    //   description: 'notice_401',
    //   name: 'Обработка заявки на присоединение к корп. каршерингу',
    //   text: interpolate`Поступила новая заявка на подключение к корп. каршерингу ${'id'}. Обработайте заявку до <Время перехода в статус "На рассмотрении" + КС" >`
    // },
    // requestAttachmentStatus: {
    //   description: 'notice_402',
    //   name: 'Статус обработки заявки на каршеринг',
    //   text: interpolate`По вашей поездке <ID заявки>  получено решение. Пожалуйста, ознакомьтесь!`
    // },
    requestApprovement: {
      description: 'notice_403',
      name: 'Согласование заявки на каршеринг',
      text: interpolate`Согласование поездки на каршеринге ${'employee'} ${'date'}. Согласуйте заявку до <Время перехода в статус "На согласовании" + КС">`,
    },
    approvementStatus: {
      description: 'notice_404',
      name: 'Статус согласования заявки на каршеринг',
      text: interpolate`По вашей поездке ${'id'} ${'date'} ${'time'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    vehicleReservation: {
      description: 'notice_405',
      name: 'Бронирование автомобиля',
      text: interpolate`Вы забронировали автомобиль. У Вас есть ${'somePeriod'} минут для того, чтобы добраться до него.`,
    },
    vehicleInspection: {
      description: 'notice_406',
      name: 'Осмотр автомобиля',
      text: 'Осмотрите авто на наличие дефектов. Сделайте фото с разных ракурсов и загрузите в приложение.',
    },
    rentStarted: {
      description: 'notice_407',
      name: 'Старт аренды. Поездка  на каршеринге началась',
      text: 'Будьте вдвойне внимательны. Не забывайте, что Вы едете на арендованном автомобиле. Поехали…',
    },
    arrivalToIntermediate: {
      description: 'notice_408',
      name: 'Прибытие в промежуточный пункт на каршеринге',
      text: interpolate`Вы прибыли в промежуточную точку. Помните, что платная стоимость ожидания составляет ${'waitCostPerMinIntermediate'} руб./мин`,
    },
    rentFinished: {
      description: 'notice_409',
      name: 'Завершение аренды',
      text:
        'Припаркуйтесь в разрешенном месте. Сфотографируйте автомобиль со всех сторон. Завершите аренду в приложении.',
    },
    rideFinished: {
      description: 'notice_410',
      name: 'Поездка на каршеринге завершена ',
      text: 'Поездка завершена. Не забудьте оценить качество сервиса.',
    },
    attachementToSuitableRide: {
      description: 'notice_411',
      name: 'Присоединение к совместной поездке на каршеринге',
      text: interpolate`К вашей поезде присоединился ${'attachPassengerName'}.`,
    },
    decliningFromSuitableRide: {
      description: 'notice_412',
      name: 'Изменения в совместной поездке на каршеринге',
      text: interpolate`Пассажир ${'declinePassengerName'} отказался от совместной поездки с вами № ${'suitableTripId'}.`,
    },
  } as Record<string, NotificationInfo>,
  REQUEST_BICYCLE: {
    requestApprovement: {
      description: 'notice_501',
      name: 'Согласование заявки на велосипед',
      text: interpolate`Согласование поездки на каршеринге ${'employee'} ${'date'}.`,
    },
    approvementStatus: {
      description: 'notice_502',
      name: 'Статус согласования заявки на велосипед',
      text: interpolate`По вашей поездке ${'id'} ${'date'} ${'time'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    vehicleReservation: {
      description: 'notice_503',
      name: 'Бронирование велосипеда',
      text: interpolate`Вы забронировали велосипед. У Вас есть ${'somePeriod'} минут для того, чтобы добраться до него.`,
    },
    vehicleInspection: {
      description: 'notice_504',
      name: 'Осмотр велосипеда',
      text: 'Осмотрите велосипед на наличие дефектов. Сделайте фото с разных ракурсов и загрузите в приложение.',
    },
    rentStarted: {
      description: 'notice_505',
      name: 'Старт аренды. Поездка на велосипеде началась',
      text: 'Будьте вдвойне внимательны. Не забывайте, что Вы едете на арендованном велосипеде. Поехали…',
    },
    arrivalToIntermediate: {
      description: 'notice_506',
      name: 'Прибытие в промежуточный пункт на велосипеде',
      text: interpolate`Вы прибыли в промежуточную точку. Помните, что платная стоимость ожидания составляет ${'waitCostPerMinIntermediate'} руб./мин`,
    },
    rentFinished: {
      description: 'notice_507',
      name: 'Завершение аренды велосипеда',
      text:
        'Припаркуйтесь в разрешенном месте. Сфотографируйте велосипед со всех сторон. Завершите аренду в приложении.',
    },
    rideFinished: {
      description: 'notice_508',
      name: 'Поездка на велосипеде завершена',
      text: 'Поездка завершена. Не забудьте оценить качество сервиса.',
    },
  } as Record<string, NotificationInfo>,
  REQUEST_SCOOTER: {
    requestApprovement: {
      description: 'notice_601',
      name: 'Согласование заявки на самокат',
      text: interpolate`Согласование поездки на самокате ${'employee'} ${'date'}.`,
    },
    approvementStatus: {
      description: 'notice_602',
      name: 'Статус согласования заявки на самокат',
      text: interpolate`По вашей поездке ${'id'} ${'date'} ${'time'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    vehicleReservation: {
      description: 'notice_603',
      name: 'Бронирование самоката',
      text: interpolate`Вы забронировали самокат. У Вас есть ${'somePeriod'} минут для того, чтобы добраться до него.`,
    },
    vehicleInspection: {
      description: 'notice_604',
      name: 'Осмотр самоката',
      text: 'Осмотрите самокат на наличие дефектов. Сделайте фото с разных ракурсов и загрузите в приложение.',
    },
    rentStarted: {
      description: 'notice_605',
      name: 'Старт аренды. Поездка на самокате началась',
      text: 'Будьте вдвойне внимательны. Не забывайте, что Вы едете на арендованном самокате. Поехали…',
    },
    arrivalToIntermediate: {
      description: 'notice_605',
      name: 'Прибытие в промежуточный пункт на самокате',
      text: interpolate`Вы прибыли в промежуточную точку. Помните, что платная стоимость ожидания составляет ${'waitCostPerMinIntermediate'} руб./мин`,
    },
    rentFinished: {
      description: 'notice_607',
      name: 'Завершение аренды самоката',
      text: 'Припаркуйтесь в разрешенном месте. Сфотографируйте самокат со всех сторон. Завершите аренду в приложении.',
    },
    rideFinished: {
      description: 'notice_608',
      name: 'Поездка на самокате завершена',
      text: 'Поездка завершена. Не забудьте оценить качество сервиса.',
    },
  } as Record<string, NotificationInfo>,
  REQUEST_LIMIT_PERSON: {
    requestApprovement: {
      description: 'notice_701',
      name: 'Согласование заявки на личный лимит',
      text: interpolate`Согласование заявки ${'employee'} на пополнение личного лимита на ${'transportType'}.`,
    },
    approvementStatus: {
      description: 'notice_702',
      name: 'Статус согласования на личный лимит',
      text: interpolate`По вашей заявке ${'requestLimitPersonId'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    limitFilled: {
      description: 'notice_703',
      name: 'Исполнение заявки на личный лимит',
      text: 'Личный лимит был пополнен.',
    },
  } as Record<string, NotificationInfo>,
  REQUEST_LIMIT_DEPARTMENT: {
    requestApprovement: {
      description: 'notice_801',
      name: 'Согласование заявки на лимит подразделения',
      text: interpolate`Согласование лимита подразделения ${'department'}`,
    },
    approvementStatus: {
      description: 'notice_802',
      name: 'Статус согласования заявки на лимит подразделения',
      text: interpolate`По вашей заявке ${'requestLimitDepartmentId'} завершен этап 'Согласование' со статусом '${'status'}'.`,
    },
    limitFilled: {
      description: 'notice_803',
      name: 'Исполнение заявки на лимит подразделения',
      text: 'Лимит подразделения был пополнен.',
    },
  } as Record<string, NotificationInfo>,
  LIMIT_DEPARTMENT: {
    requestApprovement: {
      description: 'notice_901',
      name: 'Выделение лимита подразделения',
      text: interpolate`Вашему подразделению назначен лимит ${'id'} на период ${'somePeriod'}`,
    },
    approvementStatus: {
      description: 'notice_902',
      name: 'Изменение лимита подразделения',
      text: interpolate`По решению ${'changeInitiator'} ваш лимит подразделения на транспортное обеспечение изменился. Снизился/увеличился на ${'difference'} руб.`,
    },
    limitStatus: {
      description: 'notice_903',
      name: 'Статус лимита подразделения',
      text: interpolate`Статус лимита изменен на ${'limitDepartmentStatus'}`,
    },
    lowLimit: {
      description: 'notice_904',
      name: 'Уведомление о низком остатке лимита',
      text: interpolate`Лимит подразделения израсходован на ${'percent'} %. Обратите внимание на статистику поездок ваших сотрудников.`,
    },
    lowLimitFromEmployee: {
      description: 'notice_905',
      name: 'Уведомление о низком остатке лимита подразделения',
      text: interpolate`Лимит подразделения израсходован на ${'percent'} %.`,
    },
  } as Record<string, NotificationInfo>,
  LIMIT_PERSON: {
    requestApprovement: {
      description: 'notice_1001',
      name: 'Выделение личного лимита',
      text: interpolate`Вам распределен личный лимит на транспортное обеспечение ${'personLimitSum'} руб. на период ${'somePeriod'}.`,
    },
    approvementStatus: {
      description: 'notice_1002',
      name: 'Изменение личного лимита',
      text: interpolate`Ваш личный лимит на ${'transportType'} ${'sum'} руб. на период ${'somePeriod'} изменился на ${'difference'} руб. на период ${'somePeriod'}.`,
    },
    limitStatus: {
      description: 'notice_1003',
      name: 'Статус личного лимита',
      text: interpolate`Статус лимита изменен на ${'limitPersonStatus'}`,
    },
    lowLimit: {
      description: 'notice_1004',
      name: 'Уведомление о низком остатке личного лимита',
      text: interpolate`Ваш личный лимит на ${'transportType'} израсходован на ${'percent'} %. Обратите внимание на статистику поездок ваших сотрудников.`,
    },
  } as Record<string, NotificationInfo>,
  USER_DELEGATE: {
    credentialsAssignment: {
      description: 'notice_1101',
      name: 'Назначение делегатом',
      text: interpolate`Вам делегированы полномочия на согласование обращений по ${'transportType'} на период ${'delegatePeriod'}`,
    },
    credentialsChanged: {
      description: 'notice_1102',
      name: 'Изменение полномочий - Срок полномочий',
      text: interpolate`Период действия ваших полномочий по согласованию заявок на ${'transportType'} изменен с ${'oldDelegatePeriod'} по ${'newDelegatePeriod'}. По истечении периода право согласования будет отозвано.`,
    },
    limitStatus: {
      description: 'notice_1103',
      name: 'Изменение полномочий - Вид транспорта',
      text: interpolate`Право согласования для ${'transportType'} на период ${'delegatePeriod'} было отозвано ${'changeInitiator'}`,
    },
  } as Record<string, NotificationInfo>,
  USER_OWNER_LIMIT: {
    limitOwnerAssignment: {
      description: 'notice_1201',
      name: 'Назначение владельцем лимита подразделения',
      text: 'Вы назначены владельцем лимита на транспортное обеспечение.',
    },
    credentialsChanged: {
      description: 'notice_1202',
      name: 'Изменение полномочий',
      text: interpolate`Период действия полномочий Владельца лимита изменен с ${'oldDelegatePeriod'} по ${'newDelegatePeriod'}. После истечения периода полномочия будут отозваны.`,
    },
  } as Record<string, NotificationInfo>,
  USER_ASSIGNMENT: {
    supervisorAssignment: {
      description: 'notice_1301',
      name: 'Назначение руководителем подразделения',
      text: 'Поздравляем! Вы назначены руководителем подразделения!',
    },
  } as Record<string, NotificationInfo>,
};
