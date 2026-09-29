export enum KeyFilterState {
  regular = 'regular',
  once = 'once',
}

export enum KeyFilterStateMain {
  once = 'single/list/active',
  regular = 'regular/list/active',
}

export enum KeyFilterStateMainApproval {
  delivery = 'cargo',
  compensation = 'compensation',
}

export enum activeTabFilter {
  all = 'all',
  authorIdRegular = 'authorIdRegular',
}

export enum Filters {
  all = 'all',
  authorId = 'authorId',
  senderId = 'senderId',
  recipientId = 'recipientId',
}

export enum FiltersTabs {
  all = 'all',
  author = 'authorId',
  sender = 'senderId',
  recipient = 'recipientId',
  regular = 'regular',
  once = 'once',
}

export enum FiltersTabsRegular {
  allRegular = 'allRegular',
  authorIdRegular = 'authorIdRegular',
  senderIdRegular = 'senderIdRegular',
  recipientIdRegular = 'recipientIdRegular',
}

export interface JournalFilter {
  [KeyFilterState.once]: Record<Filters, string>;
  [KeyFilterState.regular]: Record<Filters, string>;
}

export interface JournalFilterState {
  [KeyFilterState.once]: Record<Filters, boolean>;
  [KeyFilterState.regular]: Record<Filters, boolean>;
}

export const filters = {
  [Filters.all]: 'Все',
  [Filters.authorId]: 'Инициатор',
  [Filters.senderId]: 'Отправитель',
  [Filters.recipientId]: 'Получатель',
};

export const filtersTabs = {
  [KeyFilterState.once]: {},
  [KeyFilterState.regular]: {},
};

