import { CargoRequestStatusesType } from 'constants/CargoRequestStatuses.constants';

export enum CommonTitles {
  PLANNING = 'Планирование',
  PLANNING_FINISHED = 'Планирование завершено',
  AWAITING_APPROVAL = 'На согласовании',
  APPROVED = 'Согласовано',
  AWAITING_DATA = 'Отправлен контрагенту',
  AWAITING_TRANSFER = 'На сборе',
  TRANSFER_FINISHED = 'Доставка',
  SHIPMENT_FINISHED = 'Доставлено',
  CONFIRMATION_FINISHED = 'Завершено',
  CANCELED = 'Отменено',
}

export enum RoleFilters {
  all = 'all',
  authorId = 'authorId',
  senderId = 'senderId',
  recipientId = 'recipientId',
}

export const RoleFiltersTitles: Record<RoleFilters, string> = {
  [RoleFilters.all]: 'Все',
  [RoleFilters.authorId]: 'Инициатор',
  [RoleFilters.senderId]: 'Отправитель',
  [RoleFilters.recipientId]: 'Получатель',
};

export const CargoRequestStatusesTitles: Record<CargoRequestStatusesType, string> = {
  CARGO_PLANNING: CommonTitles.PLANNING,
  CARGO_PLANNING_FINISHED: CommonTitles.PLANNING_FINISHED,
  CARGO_AWAITING_APPROVAL: CommonTitles.AWAITING_APPROVAL,
  CARGO_APPROVED: CommonTitles.APPROVED,
  CARGO_AWAITING_DATA: CommonTitles.AWAITING_DATA,
  CARGO_DATA_RECEIVED: 'Передача данных перевозчику завершена', // deprecated
  CARGO_AWAITING_TRANSFER: CommonTitles.AWAITING_TRANSFER,
  CARGO_TRANSFER_FINISHED: CommonTitles.TRANSFER_FINISHED,
  CARGO_AWAITING_SHIPMENT: 'Доставка', // deprecated
  CARGO_SHIPMENT_FINISHED: CommonTitles.SHIPMENT_FINISHED,
  CARGO_AWAITING_DELIVERY_CONFIRMATION: 'Ожидайте подтверждение получения', // deprecated
  CARGO_DELIVERY_CONFIRMATION_FINISHED: CommonTitles.CONFIRMATION_FINISHED,
  CARGO_CANCELED: CommonTitles.CANCELED,
  CARGO_TRIAL: 'Разбирательство', // deprecated
  CARGO_LOST: 'Груз утерян', // deprecated
};
