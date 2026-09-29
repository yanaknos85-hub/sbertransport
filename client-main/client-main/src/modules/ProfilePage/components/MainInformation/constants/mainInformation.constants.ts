export enum Documents {
  TELEPHONE = 'TELEPHONE',
  ROLE = 'ROLE',
  DIVISION = 'DIVISION',
  MAIL = 'MAIL',
  COMPANY = 'COMPANY',
  ADDRESS_DEPARTMENT = 'ADDRESS_DEPARTMENT',
}

export const documentTitles = {
  [Documents.TELEPHONE]: 'Телефон',
  [Documents.ROLE]: 'Роль',
  [Documents.DIVISION]: 'Подразделение',
  [Documents.MAIL]: 'Почта',
  [Documents.COMPANY]: 'Компания',
  [Documents.ADDRESS_DEPARTMENT]: 'Адрес подразделения',
};

export const defaultStateConfirm = { resetCount: false, editPhone: false };

export const SEND_CODE_DELAY = 60;
