export enum AuthSteps {
  password,
  code,
}

export enum SYSTEM_MESSAGES {
  welcome = 'Добро пожаловать!',
  loginFailed = 'Неверно введен пароль. Попробуйте снова. Проверьте правильность написания на отсутствие пробелов до или после ввода логина.',
  authFailed = 'Ошибка аутентификации',
  codeFailed = 'Неверно введен код или истекло время его жизни',
  refreshExpired = 'Пожалуйста, авторизуйтесь',
  authISOError = 'Поле логин или пароль содержит недопустимые символы',
  incorrectLogin = 'Введенный логин не найден в базе пользователей',
  incorrectCode = 'Неверно введен код',
  avatarEditSuccess = 'Аватарка изменена',
  generalSaveError = 'Ошибка при сохранении данных',
  selfEditSuccess = 'Данные успешно обновлены',
  userNotFoundFailed = 'Пользователь с таким логином не найден. Проверьте правильность введенных данных и попробуйте снова',
  connectionFailed = 'Отсутствует канал связи для отправки кода. Проверьте настройки профиля или запросите помощь у администратора',
  resetFailed = 'Сброс пароля невозможен, ваш номер не подтвержден. Пожалуйста, обратитесь в поддержку',
  serviceFailed = 'Сервис временно недоступен из-за технических неполадок. Мы уже знаем о проблеме и исправим её в ближайшее время. Попробуйте повторить действие через некоторое время.',
  blockPhoneConfirmationError = 'Не удалось получить код подтверждения',
  phoneConfirmationError = 'Не удалось подтвердить номер телефона',
}

export enum AuthErrorTypes {
  WRONG_CREDENTIALS = 'WRONG_CREDENTIALS',
  TOO_MANY_LOGIN_TRIES = 'TOO_MANY_LOGIN_TRIES',
  REFRESH_EXPIRED = 'REFRESH_EXPIRED',
}

export const AUTH = 'auth';
export const UPDATE_USER_PASS = `/${AUTH}/changePassword`;

export const SUDIR = 'sudir';
export const SUDIR_LOGIN = `/${SUDIR}/login`;
export const SUDIR_LOGOUT = `/${SUDIR}/logout`;
