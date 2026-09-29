export enum DelegatesTexts {
  pageHeader = 'pageHeader',
  deleteConfirm = 'deleteConfirm',
  addNew = 'addNew',
  save = 'save',
  cancel = 'cancel',
  remove = 'remove',
}

export const DelegatesTextsCyrillic = {
  [DelegatesTexts.pageHeader]: 'Карточка делегата',
  [DelegatesTexts.deleteConfirm]: 'Вы уверены что хотите удалить этого делагата?',
  [DelegatesTexts.addNew]: 'Назначить делегата',
  [DelegatesTexts.save]: 'Сохранить',
  [DelegatesTexts.remove]: 'Удалить',
  [DelegatesTexts.cancel]: 'Отменить',
};

export enum Delegates {
  endDateIncorrect = 'endDateIncorrect',
  startDateIncorrect = 'startDateIncorrect',
  delegateId = 'delegateId',
  startDate = 'startDate',
  endDate = 'endDate',
  transportTypeId = 'transportTypeId',
  noDelegateDates = 'noDelegateDates',
}

export const DelegatesCyrillic = {
  [Delegates.endDateIncorrect]: 'Дата окончания делегирования должна быть позднее даты начала',
  [Delegates.startDateIncorrect]: 'Дата начала делегирования должна быть позднее даты окончания',
  [Delegates.delegateId]: 'ФИО делегата',
  [Delegates.startDate]: 'Начало делегирования',
  [Delegates.endDate]: 'Окончание делегирования',
  [Delegates.transportTypeId]: 'Вид транспорта',
  [Delegates.noDelegateDates]: 'Период не задан',
};
