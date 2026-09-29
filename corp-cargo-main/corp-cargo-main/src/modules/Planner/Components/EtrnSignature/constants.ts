export enum EtrnStatus {
  IDENTIFIED = 'IDENTIFIED',
  WAIT_KORUS_DATA = 'WAIT_KORUS_DATA',
  WAIT_CONDITIONS = 'WAIT_CONDITIONS',
  READY_FOR_BANK_ACTION = 'READY_FOR_BANK_ACTION',
  WAIT_KORUS_CONFIRMATION = 'WAIT_KORUS_CONFIRMATION',
  PROCESS_COMPLETED = 'PROCESS_COMPLETED',
}

export const EtrnStatusNames: Record<string, string> = {
  [EtrnStatus.IDENTIFIED]: 'Идентификация',
  [EtrnStatus.WAIT_KORUS_DATA]: 'Ожидание данных от КОРУС',
  [EtrnStatus.WAIT_CONDITIONS]: 'Ожидание проверки условий',
  [EtrnStatus.READY_FOR_BANK_ACTION]: 'Готов к подписанию',
  [EtrnStatus.WAIT_KORUS_CONFIRMATION]: 'Подтверждение от КОРУС',
  [EtrnStatus.PROCESS_COMPLETED]: 'Процесс завершён',
};

/**
 * Титулы участников ЭТрН.
 * Значения совпадают с тем, что бэкенд возвращает в поле currentTitle карточки.
 *
 * - T1: грузоотправитель
 * - T2: перевозчик (водитель) — на этом титуле доступна подпись УКЭП
 * - T3: грузополучатель - само подписание УКЭП это титул 3
 * - T4: перевозчик (приёмка)
 *
 * DTO `currentTitle` остаётся `t.union([t.null, t.string])` — runtime-валидация
 * не меняется. Enum нужен для UI-сравнений и автокомплита в IDE.
 */
export enum TitleType {
  T1 = 'T1',
  T2 = 'T2',
  T3 = 'T3',
  T4 = 'T4',
}

export const getStatusTone = (status: string): 'green' | 'gray' => {
  const greenStatuses = ['READY_FOR_BANK_ACTION', 'WAIT_KORUS_CONFIRMATION', 'PROCESS_COMPLETED'];
  return greenStatuses.includes(status) ? 'green' : 'gray';
};

export const ETRN_TOOLTIPS = {
  number: [
    'Идентификатор электронной',
    'транспортной накладной'
  ],
  sla: [
    'Оставшееся время или факт',
    'нарушения установленного срока'
  ],
  currentTitle: [
    'Титул 1. Отправление груза — Сведения о грузе и его передаче для перевозки',
    'Титул 2. Приём груза перевозчиком — Подтверждение приёма груза для перевозки',
    'Титул 3. Приёмка груза получателем — Сведения о фактической приёмке груза',
    'Титул 4. Завершение перевозки — Завершающие сведения о выполнении перевозки',
  ],
};

/**
 * Маппинг reasonCodes eligibility API → русские тексты.
 * Полностью покрывает 13 кодов из контракта.
 * https://jira.sberbank.ru/browse/TRANSPORT-44818
 */
export const ETRN_REASON_CODES_MAP: Record<string, string> = {
  NO_SIGNER_ROLE: 'Нет роли «Подписант УКЭП»',
  USER_INACTIVE: 'Пользователь неактивен',
  NOT_BANK_EMPLOYEE: 'Не является сотрудником Банка',
  DISPATCHER_NOT_FOUND: 'Диспетчер не найден',
  DISPATCHER_INACTIVE: 'Диспетчер неактивен',
  ETRN_SIGNING_DISABLED: 'Подписание ЭТрН отключено',
  ATTORNEY_NUMBER_MISSING: 'Не указан номер МЧД',
  ATTORNEY_NOT_YET_VALID: 'МЧД ещё не действует',
  ATTORNEY_EXPIRED: 'Истёк срок действия МЧД',
  ATTORNEY_DATES_INVALID: 'Неверные даты МЧД',
  ORGANIZATION_MISMATCH: 'Не совпадает организация',
  AUTHORITY_RECORD_AMBIGUOUS: 'Запись полномочий неоднозначна',
  AUTHORITY_SOURCE_UNAVAILABLE: 'Источник данных недоступен',
};