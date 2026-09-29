import { IS_DEV } from 'constants/constants.env';

export const FILL_FORM = IS_DEV && /fill/.test(window.location.search);

export enum Ownership {
  USER = 'USER', // в собственности пользователя"
  SPOUSE = 'SPOUSE', // в собственности супруга/супруги пользователя
  THIRD_PARTY = 'THIRD_PARTY', // в собственности третьих лиц
}

export const OwnershipLabels = {
  [Ownership.USER]: 'Я собственник',
  [Ownership.SPOUSE]: 'Супруг/супруга',
  [Ownership.THIRD_PARTY]: 'Третье лицо',
};

export enum Documents {
  DRIVER_LIC = 'DRIVER_LIC',
  OSAGO = 'OSAGO',
  PASSPORT_TS = 'PASSPORT_TS',
  MARRIAGE_CERTIFICATE = 'MARRIAGE_CERTIFICATE',
  AGREEMENT_PDN = 'AGREEMENT_PDN',
  REGISTRATION_CERTIFICATE_TS = 'REGISTRATION_CERTIFICATE_TS',
}

export const documentTitles = {
  [Documents.DRIVER_LIC]: 'Водительское удостоверение',
  [Documents.OSAGO]: 'Полис ОСАГО',
  [Documents.PASSPORT_TS]: 'ПТС',
  [Documents.MARRIAGE_CERTIFICATE]: 'Свидетельство о браке',
  [Documents.AGREEMENT_PDN]: 'Согласие на обработку ПДН',
  [Documents.REGISTRATION_CERTIFICATE_TS]: 'Свидетельство о регистрации',
};

export const validFormatFile = [
  'image/jpg',
  'image/jpeg',
  'image/png',
  'image/tiff',
  'application/pdf',
  'heif',
  'application/pdf',
  'jpg',
  'jpeg',
  'png',
  'tiff',
  'pdf',
];
