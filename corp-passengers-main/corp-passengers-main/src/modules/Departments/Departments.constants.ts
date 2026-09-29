import { EmployeeStatus, EmployeeStatusTitle } from 'constants/constants.app';
import { LabeledValue } from 'utils/Types';

export enum ExpandedFiltersNames {
  humanReadableId = 'humanReadableId',
  status = 'status',
  organization = 'organization',
  code = 'code',
  departmentHead = 'departmentHead',
  departmentName = 'departmentName',
  location = 'location',
  fullStructurePath = 'fullStructurePath',
  parent = 'parent',
  children = 'children',
  employees = 'employees',
  easupId = 'easupId',
}

export const ExpandedFiltersTitles: Record<ExpandedFiltersNames, string> = {
  humanReadableId: 'ID подразделения',
  status: 'Статус',
  organization: 'Организация',
  code: 'Код подразделения',
  departmentHead: 'Руководитель',
  departmentName: 'Наименование подразделения',
  location: 'Территориальное местоположение',
  fullStructurePath: 'Путь подразделения',
  parent: 'Родительское подразделение',
  children: 'Дочерние подразделения',
  employees: 'Сотрудники',
  easupId: 'Id ЕАСУП',
};

export type ExpandedFiltersType = keyof typeof ExpandedFiltersNames;

export enum DepartmentsTexts {
  pageHeader = 'pageHeader',
  deleteConfirm = 'deleteConfirm',
  addNew = 'addNew',
  save = 'save',
  cancel = 'cancel',
  remove = 'remove',
}

export const DepartmentsTextsCyrillic: Record<DepartmentsTexts, string> = {
  pageHeader: 'К списку подразделений',
  deleteConfirm: 'Вы уверены что хотите удалить эту запись?',
  addNew: 'Добавить новое',
  save: 'Сохранить',
  remove: 'Удалить',
  cancel: 'Отменить',
};

export const EmployeeStatusOptions: LabeledValue<EmployeeStatus>[] = [
  {
    value: EmployeeStatus.ACTIVE,
    label: EmployeeStatusTitle.ACTIVE,
  },
  {
    value: EmployeeStatus.INACTIVE,
    label: EmployeeStatusTitle.INACTIVE,
  },
];

export interface IProfileConfig {
  name: ExpandedFiltersType;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  title?: string | any[];
  description?: string;
  isEditable?: boolean;
}
