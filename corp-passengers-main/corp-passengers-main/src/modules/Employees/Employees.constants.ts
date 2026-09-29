import { UserModel } from 'stores/Employee/models/UserModel';

export enum EmployeesHandbookTexts {
  pageHeader = 'pageHeader',
  deleteConfirm = 'deleteConfirm',
  addNew = 'addNew',
  save = 'save',
  cancel = 'cancel',
  remove = 'remove',
  stayAtThePage = 'stayAtThePage',
  cancelConfirm = 'cancelConfirm',
  confirm = 'confirm',
  no = 'no',
}

export const EmployeesHandbookTextsCyrillic: Record<EmployeesHandbookTexts, string> = {
  pageHeader: 'К списку сотрудников',
  deleteConfirm: 'Вы уверены что хотите удалить эту запись?',
  addNew: 'Добавить нового',
  save: 'Сохранить',
  remove: 'Удалить',
  cancel: 'Отменить',
  stayAtThePage: 'Нет, остаться на странице',
  cancelConfirm: 'Вы хотите отменить изменения?',
  confirm: 'Да',
  no: 'Нет',
};

export enum EmployeeHandbooksNames {
  login = 'login',
  password = 'password',
  humanReadableId = 'humanReadableId',
  status = 'status',
  personnelNumber = 'personnelNumber',
  fullName = 'fullName',
  firstName = 'firstName',
  lastName = 'lastName',
  patronymic = 'patronymic',
  organization = 'organization',
  code = 'code',
  department = 'department',
  positionId = 'positionId',
  positionName = 'positionName',
  delegatedBy = 'delegatedBy',
  delegatedFrom = 'delegatedFrom',
  availableTransportTypes = 'availableTransportTypes',
  mobilePhone = 'mobilePhone',
  email = 'email',
  supervisor = 'supervisor',
  attributes = 'attributes',
}

export const EmployeeHandbookTitles: Record<EmployeeHandbooksNames, string> = {
  login: 'Логин',
  password: 'Пароль',
  humanReadableId: 'ID сотрудника',
  status: 'Статус',
  personnelNumber: 'Табельный номер',
  fullName: 'ФИО',
  firstName: 'Имя',
  lastName: 'Фамилия',
  patronymic: 'Отчество',
  organization: 'Организация',
  code: 'Код подразделения',
  department: 'Департамент',
  positionId: 'Позиция',
  positionName: 'Должность',
  delegatedBy: 'Делегирован',
  delegatedFrom: 'ФИО сотрудника, делегировавшего право согласования',
  availableTransportTypes: 'Доступные виды транспорта',
  mobilePhone: 'Мобильный телефон',
  email: 'Email',
  supervisor: 'Руководитель',
  attributes: 'Признаки сотрудника',
};

export type ExpandedFiltersType = keyof typeof EmployeeHandbooksNames;

export interface IProfileConfig {
  name: ExpandedFiltersType;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  title?: string | any[];
  description?: string;
  isEditable?: boolean;
  isRequired?: boolean;
}

export const employeesUserHandbookConfig = (user?: UserModel): IProfileConfig[] => [
  {
    name: EmployeeHandbooksNames.login,
    title: user?.login,
    description: EmployeeHandbookTitles.login,
    isEditable: true,
    isRequired: true,
  },
  {
    name: EmployeeHandbooksNames.password,
    title: user?.password,
    description: EmployeeHandbookTitles.password,
    isEditable: true,
    isRequired: true,
  },
  {
    name: EmployeeHandbooksNames.lastName,
    title: user?.lastName,
    description: EmployeeHandbookTitles.lastName,
    isEditable: true,
    isRequired: true,
  },
  {
    name: EmployeeHandbooksNames.firstName,
    title: user?.firstName,
    description: EmployeeHandbookTitles.firstName,
    isEditable: true,
    isRequired: true,
  },
  {
    name: EmployeeHandbooksNames.patronymic,
    title: user?.patronymic,
    description: EmployeeHandbookTitles.patronymic,
    isEditable: true,
    isRequired: true,
  },
];
