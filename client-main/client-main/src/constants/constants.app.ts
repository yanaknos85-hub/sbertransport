/* eslint-disable @typescript-eslint/no-duplicate-enum-values */
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export enum CORP_URL {
  ADMIN_SIGMA = 'https://corp.sbertransport.sigma.sbrf.ru',
  ADMIN_DELTA = 'https://corp.sbertransport.delta.sbrf.ru',
  ADMIN_ALFA = 'https://corp.sbertransport.ca.sbrf.ru',
  ADMIN_DEFAULT = 'https://corp.sbertransport.ru',
  SDO_DEFAULT = 'https://client-corp-ext.s-transport.ru',
}

// Ссылки на приложения в APPS
export const enum APPS_URL {
  IOS = 'sbapps://application?id=com.strans.cl',
  ANDROID = 'sberapps://open.sberapps/com.sbertransport',
}

export const HOST_NAME = window.location.hostname;

export enum DesignVersion {
  Narrow = 'Narrow',
  Middle = 'Middle',
  Wide = 'Wide',
}

// так как наш роутинг построен на том, что любая не авторизованная страница шлет на url '/',
// было невозможно положить в роутинг любую неавторизованную страницу, кроме адреса '/', поэтому я сделал эту функцию
// которая делает исключения в проверках на аторизацию.
export function isNotDirectEnter(url: string): boolean {
  return url !== '/first-enter' && url !== '/oauth/success' && url !== '/oauth/failure';
}

export enum AdminLinks {
  Tariffs = '/admin/tariffs',
  Departments = '/admin/departments',
}

export enum MESSAGES {
  notSpecified = 'Не указан',
  allPoints = 'Все точки',
  seat = 'Посадка',
  landing = 'Высадка',
  allTime = 'Время',
  deviationsByBoth = 'Отклонения по маршруту и времени',
  deviationsByRoute = 'Отклонения по маршруту',
  deviationsByTime = 'Отклонения по времени',
  car = 'Автомобиль',
  motorcycle = 'Мотоцикл',
  personalCarRequired = 'Выберите личный транспорт',
  noPersonalCars = 'У сотрудника не заведен личный транспорт',
  taxiTrip = 'Поездка на такси',
  personalTrip = 'Поездка на личном транспорте',
  cost = 'Стоимость: ',
}

export enum SYSTEM_MESSAGES {
  welcome = 'Добро пожаловать!',
  goodbye = 'До скорых встреч!',
  sessionExpired = 'Ваша сессия истекла. Пожалуйста, войдите в приложение снова.',
  generalSaveError = 'Ошибка при сохранении данных',
  tripRequestSavedSuccessfully = 'Заявка успешно сохранена',
  tripRequestSavedUnsuccessfullyBL = 'Произошла ошибка при создании заявки',
  tripRequestUpdatedUnsuccessfully = 'Произошла ошибка при обновлении заявки',
  tripRequestCancel = 'Заявка отменена',
  tripRequestCancelError = 'При отмене заявки произошла ошибка',
  tripRequestFinish = 'Заявка успешно завершена',
  tripRequestRated = 'Спасибо за Ваш отзыв!',
  tripRequestLoadFailed = 'Не удалось загрузить заявку с таким ID',

  avatarEditSuccess = 'Информация изменена',

  loginFailed = 'Неверно введен пароль. Попробуйте снова. Проверьте правильность написания на отсутствие пробелов до или после ввода логина.',
  authFailed = 'Ошибка аутентификации',

  suitableTripRequestAddSuccess = 'Вы успешно подали заявку на совместную поездку',

  vechicleAddSuccess = 'Новый транспорт успешно создан',
  vechicleAddFailed = 'При добавлении транспорта произошла ошибка',
  vechicleEditSuccess = 'Информация о транспорте изменена',
  vechicleEditFailed = 'Ошибка при сохранении данных',
  vechicleDeleteSuccess = 'Информация о транспорте удалена',
  vechicleDeleteFailed = 'Ошибка при удалении данных',

  personalCarRequestAddFailed = 'При добавлении транспорта произошла ошибка',
  personalCarRequestEditFailed = 'Ошибка при сохранении данных',
  personalCarRequestDeleteFailed = 'Ошибка при удалении данных',
  personalCarRequestAddSuccess = 'Новый транспорт успешно создан',
  personalCarRequestEditSuccess = 'Информация о транспорте изменена',
  personalCarRequestDeleteSuccess = 'Информация о транспорте удалена',

  uploadOsagoFileSuccess = 'Ваши данные успешно заполнены',
  uploadOsagoFileError = 'Произошла ошибка. Попробуйте еще раз.',
  osagoIsExpired = 'Срок действия Вашего полиса истек. Пожалуйста, обновите полис.',
  uploadOsagoFileErrorServer = 'Сервис временно не работает. Попробуйте чуть позже.',

  personalCarRequestSaveSuccess = 'ТС успешно сохранено',

  employeeEditSuccess = 'Информация Вашего профиля изменена',

  departmentAddSuccess = 'Подразделение успешно создано',
  departmentDeleteSuccess = 'Подразделение успешно удалено',
  departmentEditSuccess = 'Информация о подразделении успешно сохранена',

  approveSuccessfull = 'Спасибо, заявка согласована',
  approveFailure = 'Ошибка согласования заявки',
  routeIsNotApproved = 'Маршрут не согласован',
  errorOccurred = 'Произошла ошибка',

  declineSuccessfull = 'Заявка отклонена',
  declineFailure = 'Ошибка отклонения заявки',

  addressLableIsNotUniq = 'Адрес с таким названием уже существует',
  addressIsNotUniq = 'Указанный адрес уже существует',
  addressDeleteSuccess = 'Адрес успешно удалён',
  addressAddSuccess = 'Адрес успешно добавлен',

  delegateSuccessfull = 'Делегат успешно назначен',
  delegatePeriodError = 'Ошибка при сохранении данных. Попробуйте выбрать другой период делегирования',
  delegateDeleteSuccess = 'Делегат успешно удален',
  delegateUpdateSuccess = 'Делегат успешно обновлен',
  delegateDeleteError = 'Ошибка при удалении данных',

  fileUploadSuccess = 'Файл успешно загружен',
  simulationSuccess = 'Симуляция импорта успешно завершена',

  limitRequestCreateSuccess = 'Заявка на лимит успешно создана',
  limitRequestEditSuccess = 'Заявка на лимит успешно изменена',
  limitRequestCancelSuccess = 'Заявка на лимит успешно отменена',
  limitIsApproved = 'Заявка на лимит одобрена',

  // eslint-disable-next-line max-len
  savingWithAddressDuplicates = 'Пункт отправления и назначения не должны совпадать. Добавьте промежуточный адрес или измените пункт назначения!',
  savingWithWrongSum = 'К сожалению, на текущий момент, резервирование для сотрудника на общественный вид транспорта невозможно на указанную сумму',
  // eslint-disable-next-line @typescript-eslint/no-duplicate-enum-values
  finalTripApproveSuccess = 'Спасибо, заявка согласована',
  finalTripDeclineSuccess = 'Заявка в статусе: Не согласована',

  compensationRequestCreateSuccess = 'Заявка на компенсацию успешно создана',
  errorCompensationRequest = 'Не удалось создать заявку на компенсацию',

  phoneConfirmationSuccess = 'Номер телефона успешно подтвержден',
  phoneConfirmationError = 'Не удалось подтвердить номер телефона',
  blockPhoneConfirmationError = 'Не удалось получить код подтверждения',
}

export const RUBLE_SIGN = '₽';

export const DATE_FORMAT = {
  BASE: 'YYYY-MM-DD',
  BASE_REVERTED: 'DD-MM-YYYY',
  BASE_REVERTED_DOTS: 'DD.MM.YYYY',
  BASE_REVERTED_DATE_DOTS: 'DD.MM.YYYY HH:mm',
  MONTH_NAME: 'D MMM YYYY',
  MONTH_NAME_WITH_TIME: 'D MMM YYYY H:mm',
  TIME_SHORT: 'H:mm',
  TIME_FULL: 'HH:mm',
  DATE_WITH_TIME: 'DD-MM-YYYY H:mm',
  YEAR: 'YYYY',
  MONTH_AND_YEAR: 'MM.YYYY',
  MONTH_NAME_NOT_YEAR_WITH_TIME: 'D MMMM YYYY в H:mm',
};

export const CLEAR_QUERY_CONFIG = {
  // Не храним кэш
  cacheTime: 0,
  staleTime: 0,
  // Не будет отображать fallback из Suspense
  suspense: false,
  // Не делаем refetch на фокус окна
  refetchOnWindowFocus: false,
  // В случае ошибки, не будет повторно вызывать запрос
  retry: false,
};

export const DOWNLOAD_APPS_URL = 'https://apps.sbertransport.ru';

export const ROLE = {
  DRIVER: 'ROLE_DRIVER',
  PARKING_ADMIN: 'ROLE_PARKING_ADMIN',
  PARKING_COORDINATOR: 'ROLE_PARKING_COORDINATOR',
  EMPLOYEE_CORP_CLIENT: 'ROLE_EMPLOYEE_CORP_CLIENT',
  DOMESTIC_COURIER: 'ROLE_COURIER',
  ADMIN_DATA_MASTER: 'ROLE_ADMIN_DATA_MASTER',
};
export enum ServiceTypeEnum {
  EMPLOYEE_TRANSPORTATION = 'EMPLOYEE_TRANSPORTATION',
  CARGO_TRANSPORTATION = 'CARGO_TRANSPORTATION',
  REPAIR = 'REPAIR',
  PARKING = 'PARKING',
}

export const ServiceTypeEnumTitles = {
  [ServiceTypeEnum.EMPLOYEE_TRANSPORTATION]: 'Перевозка сотрудников',
  [ServiceTypeEnum.CARGO_TRANSPORTATION]: 'Грузоперевозки',
  [ServiceTypeEnum.REPAIR]: 'Ремонт',
};

export enum ServiceEnum {
  employeeTransportation = 'employeeTransportation',
  cargo = 'cargo',
  // todo удалить после тестирования и обкатки в проме !!!
  // cargoMulti = 'cargoMulti',
  // regularCargo = 'regularCargo',
  maintenance = 'maintenance',
  parking = 'parking',
}

export const ServiceEnumTitles = {
  [ServiceEnum.employeeTransportation]: ' Перевозка сотрудников',
  [ServiceEnum.cargo]: ' Доставка',
  // todo удалить после тестирования и обкатки в проме !!!
  // [ServiceEnum.cargoMulti]: ' Доставка на множество адресов',
  // [ServiceEnum.regularCargo]: 'Регулярная доставка',
  [ServiceEnum.maintenance]: 'Автосервис',
  [ServiceEnum.parking]: 'Парковки',
};

export const EmployeeAppLinksTitles = {
  create: 'Оформить поездку',
  trips: 'Поездки',
  yandexTrips: 'Поездки на Яндекс Go',
  cargos: 'Доставки',
  regularCargos: 'Регулярная доставка',
  carService: 'Автосервис',
  multipleCargos: 'Многоточечные доставки',
  approvement: 'Мои согласования',
  limits: 'Мои заявки на лимиты',
  personalCars: 'Мой транспорт',
  favorite: 'Мои адреса',
  support: 'Поддержка',
  profile: 'Перейти в личный кабинет',
  admin: 'Перейти в корпоративный кабинет',
  delegates: 'Делегаты',
  approvalRequestList: 'Заявки на поездки',
  approvementLimit: 'Заявки на лимиты',
  limitsInfo: 'Мои лимиты',
  limitRequests: 'Заявки на лимиты',
  bonuses: 'Бонусы',
  bonusesAccount: 'Бонусный счет',

  taxi: 'Такси',
  yandex: 'Яндекс Go',
  personal: 'Личный транспорт',
  cargo: 'Доставка',
  cargoMulti: 'Доставка на множество адресов',
  cargoMass: 'Экспорт из Excel',
  public: 'Общественный транспорт',
  carsharing: 'Каршеринг',
  cooperative: 'Групповые поездки',
  exit: 'Выйти из аккаунта',
};

export enum EmployeeStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export enum EmployeeStatusTitle {
  ACTIVE = 'Активный',
  INACTIVE = 'Неактивный',
}

export type EmployeeStatusType = keyof typeof EmployeeStatus;

export const IOEmployeeStatusType = ioTypeFromEnum<EmployeeStatusType>('employeeStatusType', EmployeeStatus);

export enum TripsTabsFilters {
  planned = 'planned',
  final = 'final',
}

export type TTripsTabsFilters = keyof typeof TripsTabsFilters;

export enum LimitRequestTabsFilters {
  active = 'active',
  closed = 'closed',
}

export enum CargosTabsFilters {
  active = 'active',
  final = 'final',
}

export enum CustomErrorCode {
  // MF - Module Federation
  MF_LOAD = 'MF-001', // Модуль не загружен

  // F - Frontend
  UNKNOWN = 'F-002', // Неизвестная ошибка
  TYPES = 'F-003', // С бэка пришел не тот тип, который ожидается
  UNDEFINED_FIELD = 'F-006', // Пытается прочитать поле у null или undefined

  // A - API
  TIMEOUT = 'A-004', // Таймаут
  CORS = 'A-005', // CORS
}

export const errorText: Record<string, { title: string; subtitle: string }> = {
  400: {
    title: 'Упс... Ошибка заполнения данных',
    subtitle: 'Проверьте корреткность данных. Если ошибка не исчезнет, обратитесь в поддержку.',
  },
  401: {
    title: 'Пожалуйста, авторизуйтесь',
    subtitle: '',
  },
  403: {
    title: 'Упс... У Вас недостаточно прав',
    subtitle: 'Если Вам необходим доступ, обратитесь в поддержку',
  },
  408: {
    title: 'Упс... Превышено время ожидания данных',
    subtitle: 'Попробуйте перезагрузить страницу. Если ошибка не исчезнет, обратитесь в поддержку.',
  },
  409: {
    title: 'Упс... Конфликт данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  417: {
    title: 'Упс... Ошибка данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },

  500: {
    title: 'Упс... Ошибка сервера',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  503: {
    title: 'Упс... Проводятся работы на сервере',
    subtitle: 'Попробуйте позже. Если ошибка сохранится, обратитесь в поддержку.',
  },

  // Все ошибки, для которых нет текста, выводятся как CustomErrorCode.UNKNOWN
  [CustomErrorCode.UNKNOWN]: {
    title: 'Упс... Ошибка системы',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  [CustomErrorCode.TYPES]: {
    title: 'Упс... Ошибка отображения данных',
    subtitle: '',
  },
  [CustomErrorCode.TIMEOUT]: {
    title: 'Упс... Превышено время ожидания данных',
    subtitle: 'Попробуйте перезагрузить страницу. Если ошибка не исчезнет, обратитесь в поддержку.',
  },
  [CustomErrorCode.MF_LOAD]: {
    title: 'Ошибка загрузки модуля',
    subtitle: 'Попробуйте почистить кэш. Если ошибка не исчезнет, обратитесь в поддержку.',
  },
  [CustomErrorCode.UNDEFINED_FIELD]: {
    title: 'Упс... Ошибка чтения данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  [CustomErrorCode.CORS]: {
    title: 'Упс... Ошибка CORS',
    subtitle: 'Попробуйте перезагрузить страницу. Если ошибка не исчезнет, обратитесь в поддержку.',
  },
};

export enum Contacts {
  outTel = '8(800) 707-48-82',
  linkOutTel = 'tel:88007074882',
  insideTel = '8(559) 90-047',
  mailSbertransport = 'catransport@sberbank.ru',
  linkInsideTel = 'tel:855990047',
  chatSbertransport = 'sberchat.sberbank.ru/@SBERTRANSPORT',
  linkChatSbertransport = 'https://sberchat.sberbank.ru/@SBERTRANSPORT',
  linkMailSbertransport = 'mailto:catransport@sberbank.ru',
}

export enum SDOContacts {
  tel = '8(800) 100-03-02',
  linkTel = 'tel:88001000302',
  mailSbertransport = 'support@sbertransport.ru',
  linkMailSbertransport = 'mailto:support@sbertransport.ru',
}

export const SUPPORT_PHONE = {
  code: '88007074882',
  title: '8-800-707-48-82',
};

export const SDO_SUPPORT_PHONE = {
  code: '88001000302',
  title: '8(800) 100-03-02',
};

export const TEST_MODE = 'TEST_MODE';

export const API_2GIS_KEY = '731c6739-3e33-4939-bdcb-555c7a77bc46';
export const API_2GIS_SDO_KEY = '1ba2da76-5f44-400b-a9e6-5e2197ea2ccf';

export const APP_VERSION = '1.0';
