/* eslint-disable @typescript-eslint/no-duplicate-enum-values */
export const MAIN_ROUTE_PATH = 'client';
export const AUTH_ROUTE_PATH = 'oauth';

export enum DesignVersion {
  Narrow = 'Narrow',
  Middle = 'Middle',
  Wide = 'Wide',
}

export enum AppLinksStartPage {
  EmployeesApp = '/client',
  FirstEnter = '/first-enter',

  AuthApp = '/oauth',
  Logout = '/oauth/logout',
  Success = '/oauth/success',
  Failure = '/oauth/failure',
  SudirApi = '/api/sudir/oauth2/authorization/sudir',
}

// так как наш роутинг построен на том, что любая не авторизованная страница шлет на url '/',
// было невозможно положить в роутинг любую неавторизованную страницу, кроме адреса '/', поэтому я сделал эту функцию
// которая делает исключения в проверках на аторизацию.
export function isNotDirectEnter(url: string): boolean {
  return url !== AppLinksStartPage.FirstEnter && url !== AppLinksStartPage.Success && url !== AppLinksStartPage.Failure;
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
  finalTripApproveSuccess = 'Спасибо, заявка согласована',
  finalTripDeclineSuccess = 'Заявка в статусе: Не согласована',

  compensationRequestCreateSuccess = 'Заявка на компенсацию успешно создана',
  errorCompensationRequest = 'Не удалось создать заявку на компенсацию',
}

export const RUBLE_SIGN = '₽';

export const DATE_FORMAT = {
  BASE: 'YYYY-MM-DD',
  BASE_REVERTED: 'DD-MM-YYYY',
  BASE_REVERTED_DOTS: 'DD.MM.YYYY',
  MONTH_NAME: 'D MMM YYYY',
  MONTH_NAME_WITH_TIME: 'D MMM YYYY H:mm',
  TIME_SHORT: 'H:mm',
  TIME_FULL: 'HH:mm',
  DATE_WITH_TIME: 'DD-MM-YYYY H:mm',
  YEAR: 'YYYY',
  MONTH_AND_YEAR: 'MM.YYYY',
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
