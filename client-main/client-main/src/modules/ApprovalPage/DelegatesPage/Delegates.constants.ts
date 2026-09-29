export enum DelegatesTexts {
  listTitle = 'listTitle',
  deleteConfirm = 'deleteConfirm',
  add = 'add',
  edit = 'edit',
  save = 'save',
  cancel = 'cancel',
  remove = 'remove',
}

export enum DelegatesModalTypes {
  add = 'add',
  edit = 'edit',
  delete = 'delete',
}

export const DelegatesTextsCyrillic = {
  [DelegatesTexts.listTitle]: 'Список делегатов',
  [DelegatesTexts.deleteConfirm]: 'Вы уверены, что хотите удалить делегата?',
  [DelegatesTexts.add]: 'Назначить делегата',
  [DelegatesTexts.edit]: 'Редактировать параметры делегирования',
  [DelegatesTexts.save]: 'Сохранить',
  [DelegatesTexts.remove]: 'Удалить',
  [DelegatesTexts.cancel]: 'Отменить',
};

export enum Delegates {
  endDateIncorrect = 'endDateIncorrect',
  startDateIncorrect = 'startDateIncorrect',
  delegateEmployee = 'delegateEmployee',
  startDate = 'startDate',
  endDate = 'endDate',
  transportTypeId = 'transportTypeId',
  noDelegateDates = 'noDelegateDates',
  employeeDep = 'employeeDep',
  id = 'id',
}

export const DelegatesCyrillic = {
  [Delegates.endDateIncorrect]: 'Дата окончания делегирования должна быть позднее даты начала',
  [Delegates.startDateIncorrect]: 'Дата начала делегирования должна быть не позднее даты окончания',
  [Delegates.delegateEmployee]: 'ФИО делегата',
  [Delegates.startDate]: 'Начало делегирования',
  [Delegates.endDate]: 'Окончание делегирования',
  [Delegates.transportTypeId]: 'Вид транспорта',
  [Delegates.noDelegateDates]: 'Период не задан',
  [Delegates.employeeDep]: 'Поиск делегата требует наличие выбранного вида транспорта и даты делегирования',
};
