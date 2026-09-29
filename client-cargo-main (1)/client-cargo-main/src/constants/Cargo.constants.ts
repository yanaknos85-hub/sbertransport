export const activeLabelName = 'Активные';
export const finalLabelName = 'Завершённые';
export const ordersLabelName = 'Заявки';
export const regularLabelName = 'Расписания';

export enum CargosTabsFilters {
  active = 'active',
  final = 'final',
  once = 'single/list/active',
  regular = 'regular/list/active',
  cargo = 'cargo',
  compensation = 'compensation',
}

export enum Filters {
  all = 'all',
  authorId = 'authorId',
  senderId = 'senderId',
  recipientId = 'recipientId',
}

export const filters = {
  [Filters.all]: 'Все',
  [Filters.authorId]: 'Инициатор',
  [Filters.senderId]: 'Отправитель',
  [Filters.recipientId]: 'Получатель',
};

