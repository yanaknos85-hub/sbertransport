export enum CustomErrorCode {
  UNKNOWN = 1000,
  TYPES = 1001,
  TIMEOUT = 1002,
  CORS = 1003,
  UNDEFINED_FIELD = 1004,
}

export const errorText: Record<number, { title: string; subtitle: string }> = {
  400: {
    title: 'Упс... Ошибка заполнения данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  401: {
    title: 'Пожалуйста, авторизуйтесь',
    subtitle: '',
  },
  403: {
    title: 'Упс... У Вас недостаточно прав',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  408: {
    title: 'Упс... Слишком большой объем данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
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
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },

  // Все ошибки, для которых нет текста, выводятся как CustomErrorCode.UNKNOWN
  [CustomErrorCode.UNKNOWN]: {
    title: 'Упс... Ошибка системы',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  [CustomErrorCode.TYPES]: {
    title: 'Упс... Ошибка отображения данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  [CustomErrorCode.TIMEOUT]: {
    title: 'Упс... Слишком большой объем данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
};

export type SortDirection = 'DESC' | 'ASC';

export enum Contacts {
  outTel = '8(800) 707-48-82',
  linkOutTel = 'tel:88007074882',
}

export const ALPHABET = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

export const NEXT_ALLOW_CONFIRM_TIME = 'nextAllowConfirmTime';

export const EMPTY = '-';
