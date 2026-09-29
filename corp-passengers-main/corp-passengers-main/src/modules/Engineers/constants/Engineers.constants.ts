export const FORMAT = 'DD.MM.YYYY, HH:mm';

export const VALUE_NOT_FOUND = '-';

export enum EngineerHandbooksNames {
  login = 'login',
  password = 'password',
  humanReadableId = 'humanReadableId',
  statusTitle = 'statusTitle',
  personnelNumber = 'personnelNumber',
  nameWithInitials = 'nameWithInitials',
  firstName = 'firstName',
  lastName = 'lastName',
  patronymic = 'patronymic',
  organization = 'organization',
  code = 'code',
  department = 'department',
  positionId = 'positionId',
  delegatedBy = 'delegatedBy',
  availableTransportTypes = 'availableTransportTypes',
  mobilePhone = 'mobilePhone',
  email = 'email',
  supervisor = 'supervisor',
}

export const EngineerHandbookTitles: Record<EngineerHandbooksNames, string> = {
  login: 'Логин',
  password: 'Пароль',
  humanReadableId: 'Id пользователя',
  statusTitle: 'Статус подразделения',
  personnelNumber: 'Табельный номер',
  nameWithInitials: 'ФИО',
  firstName: 'Имя',
  lastName: 'Фамилия',
  patronymic: 'Отчество',
  organization: 'Организация',
  code: 'Код подразделения',
  department: 'Департамент',
  positionId: 'Позиция',
  delegatedBy: 'Делегирован',
  availableTransportTypes: 'Доступные виды транспорта',
  mobilePhone: 'Мобильный телефон',
  email: 'Email',
  supervisor: 'Руководитель',
};

export type ExpandedFiltersType = keyof typeof EngineerHandbooksNames;

export interface IProfileConfig {
  name: ExpandedFiltersType;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  title?: string | any[];
  description?: string;
  isEditable?: boolean;
  isRequired?: boolean;
}

export enum DeadlineState {
  NONE = 'NONE',
  YELLOW = 'YELLOW',
  RED = 'RED',
}
