import { availablePhoneCode } from 'shared/fieldValidationRules';

export enum ProfileLinks {
  avatar = 'avatar',
  fullName = 'fullName',
  position = 'position',
  organization = 'organization',
  department = 'department',
  departmentAddress = 'departmentAddress',
  email = 'email',
  mobilePhone = 'mobilePhone',
  personnellNumber = 'personnellNumber',
  mobilePhoneMessage = 'mobilePhoneMessage',
}

export const ProfileLinksTitles = {
  [ProfileLinks.avatar]: 'Аватар',
  [ProfileLinks.fullName]: 'ФИО',
  [ProfileLinks.position]: 'Роль',
  [ProfileLinks.organization]: 'Компания',
  [ProfileLinks.department]: 'Подразделение',
  [ProfileLinks.departmentAddress]: 'Адрес подразделения',
  [ProfileLinks.email]: 'Почта',
  [ProfileLinks.mobilePhone]: 'Телефон',
  [ProfileLinks.personnellNumber]: 'Табельный номер',
  [ProfileLinks.mobilePhoneMessage]: `Введите номер в формате +(код)123456789 (Пример: +79123456789). Допустимые коды: ${availablePhoneCode.join(
    ', '
  )}. `,
};

export type ProfileLinksType = keyof typeof ProfileLinks;
