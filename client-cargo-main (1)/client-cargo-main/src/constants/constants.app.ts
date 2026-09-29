import { IS_DEV } from 'constants/constants.env';

export const MAIN_ROUTE_PATH = 'client/create';
export const AUTH_ROUTE_PATH = 'oauth';
export const DEFAULT_ERROR = 'Необходимо выбрать груз';

export enum DesignVersion {
  Narrow = 'Narrow',
  Middle = 'Middle',
  Wide = 'Wide',
}

export enum AppLinksStartPage {
  EmployeesApp = '/client',
  Logout = '/oauth/logout',
  FirstEnter = '/first-enter',

  AuthApp = '/oauth',
  Success = '/oauth/success',
  Failure = '/oauth/failure',
  SudirApi = '/api/sudir/oauth2/authorization/sudir',
}

// так как наш роутинг построен на том, что любая не авторизованная страница шлет на url '/',
// было невозможно положить в роутинг любую неавторизованную страницу, кроме адреса '/', поэтому я сделал эту функцию
// которая делает исключения в проверках на аторизацию.
export function isNotDirectEnter(url: string): boolean {
  return url !== AppLinksStartPage.Success && url !== AppLinksStartPage.Failure;
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

  loginFailed = 'Неверно введен пароль. Попробуйте снова. Проверьте правильность написания на отсутствие пробелов до или после ввода логина.',
  authFailed = 'Ошибка аутентификации',

  suitableTripRequestAddSuccess = 'Вы успешно подали заявку на совместную поездку',

  personalCarRequestAddFailed = 'При добавлении транспорта произошла ошибка',
  // eslint-disable-next-line @typescript-eslint/no-duplicate-enum-values
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
  delegateDeleteSuccess = 'Делегат успешно удален',

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

  // Exchange
  exchangeDeclineSuccess = 'Вы отказались от заявки',
  exchangeTakeToWorkSuccess = 'Заявка взята в работу',
}

export const RUBLE_SIGN = '₽';

export const DATE_FORMAT = {
  BASE: 'YYYY-MM-DD',
  BASE_REVERTED: 'DD-MM-YYYY',
  BASE_REVERTED_DOTS: 'DD.MM.YYYY',
  MONTH_NAME: 'D MMM YYYY',
  MONTH_NAME_WITH_TIME: 'D MMM YYYY H:mm',
  MONTH_NAME_WITH_TIME_SECONDS: 'DD-MM-YYYY H:mm:ss',
  MONTH_NAME_WITH_TIME_SECONDS_REVERTED: 'YYYY-MM-DD HH:mm:ss',
  TIME_SHORT: 'H:mm',
  TIME_FULL: 'HH:mm',
  DATE_WITH_TIME: 'DD-MM-YYYY H:mm',
  DATE_WITH_TIME_DOUBLE_HOURS: 'DD-MM-YYYY HH:mm',
  DATE_WITH_TIME_DOTS: 'DD.MM.YYYY HH:mm',
  DATE_WITH_TIME_DOTS_REVERTED: 'HH:mm DD.MM.YYYY',
  DATE_WITH_TIME_SPACES: 'DD MM YYYY HH:mm',
  YEAR: 'YYYY',
  MONTH_AND_YEAR: 'MM.YYYY',
  DAY_MONTH_YEAR: 'D MMMM YYYY',
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

export enum CustomErrorCode {
  MF_LOAD = 666,
  UNKNOWN = 1000,
  TYPES = 1001,
  TIMEOUT = 1002,
  CORS = 1003,
  UNDEFINED_FIELD = 1004,
}

const defaultSubTitle = 'Пожалуйста, обратитесь в поддержку';

export const errorText: Record<number, { title: string; subtitle: string }> = {
  400: {
    title: 'Ошибка заполнения данных',
    subtitle: defaultSubTitle,
  },
  401: {
    title: 'Пожалуйста, авторизуйтесь',
    subtitle: '',
  },
  403: {
    title: 'У Вас недостаточно прав',
    subtitle: defaultSubTitle,
  },
  408: {
    title: 'Слишком большой объем данных',
    subtitle: defaultSubTitle,
  },
  409: {
    title: 'Конфликт данных',
    subtitle: defaultSubTitle,
  },
  417: {
    title: 'Ошибка данных',
    subtitle: defaultSubTitle,
  },

  500: {
    title: 'Ошибка сервера',
    subtitle: defaultSubTitle,
  },
  503: {
    title: 'Проводятся работы на сервере',
    subtitle: defaultSubTitle,
  },

  // Все ошибки, для которых нет текста, выводятся как CustomErrorCode.UNKNOWN
  [CustomErrorCode.MF_LOAD]: {
    title: `Ошибка загрузки ${IS_DEV && 'микрофронта'}`,
    subtitle: IS_DEV ? '' : defaultSubTitle,
  },
  [CustomErrorCode.UNKNOWN]: {
    title: 'Ошибка системы',
    subtitle: defaultSubTitle,
  },
  [CustomErrorCode.TYPES]: {
    title: 'Ошибка отображения данных',
    subtitle: defaultSubTitle,
  },
  [CustomErrorCode.TIMEOUT]: {
    title: 'Слишком большой объем данных',
    subtitle: defaultSubTitle,
  },
};

export const SUPPORT_PHONE = {
  code: '88007074882',
  title: '8-800-707-48-82',
};

export const TEST_MODE = 'TEST_MODE';

export const API_2GIS_KEY = '731c6739-3e33-4939-bdcb-555c7a77bc46';
export const API_2GIS_SDO_KEY = '1ba2da76-5f44-400b-a9e6-5e2197ea2ccf';

export enum MapComponentType {
  webGL2GIS = 'webGL2GIS',
  leaflet = 'leaflet',
  openlayers = 'openlayers',
}
