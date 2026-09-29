import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

/**
 * Смена (Shift) - информация о смене водителя
 * @remarks
 * Представляет собой объект с деталями смены, включая данные водителя, транспорта и временные рамки
 */
export const Shift = t.type({
  /** Уникальный идентификатор смены */
  id: tt.uuid,
  /** Полное имя водителя */
  driverName: t.string,
  /** Марка транспортного средства */
  vehicleBrand: t.string,
  /** Модель транспортного средства */
  vehicleModel: t.string,
  /** Государственный номер транспортного средства */
  vehicleStateNumber: t.string,
  /** Дата начала смены (ISO строка) */
  startDate: t.string,
  /** Дата окончания смены (ISO строка) */
  endDate: t.string,
});
/** Тип данных смены */
export type Shift = t.TypeOf<typeof Shift>;

/**
 * Фильтры для получения списка смен
 * @remarks
 * Используется для фильтрации смен по дате начала
 */
export interface ShiftsFilters {
  /** Дата начала смены (опциональное поле) */
  startDate?: string;
}

/**
 * Массив смен
 * @remarks
 * Валидационный тип для массива смен, используется при декодировании API ответа
 */
export const Shifts = t.array(Shift);
/** Тип массива смен */
export type TShiftsResponse = t.TypeOf<typeof Shifts>;

/**
 * Запрос на массовое создание первого титула для смен
 * @remarks
 * Содержит массив идентификаторов смен, для которых нужно создать first-title
 */
export const MassCreateFirstTitleRequest = t.type({
  shiftIds: t.array(tt.uuid),
  timeZone: t.string,
});
export type TMassCreateFirstTitleRequest = t.TypeOf<typeof MassCreateFirstTitleRequest>;

/**
 * Ответ на массовое создание первого титула - отдельная запись
 * @remarks
 * Содержит информацию о сгенерированном файле, его содержимом и статусе ошибки
 */
export const MassCreateFirstTitleItem = t.type({
  /** Уникальный идентификатор смены */
  shiftId: tt.uuid,
  /** Уникальный идентификатор EWB */
  ewbId: tt.uuid,
  /** Читаемый идентификатор (формат: EWB-YYYYMMDD-NNN) */
  humanReadableId: t.string,
  /** Имя файла с расширением */
  fileName: t.string,
  /** Содержимое файла в base64 */
  content: t.string,
  /** Время создания */
  creationTime: t.string,
  /** Текст ошибки, если была ошибка, иначе пустая строка */
  errorText: t.string,
});
export type TMassCreateFirstTitleItem = t.TypeOf<typeof MassCreateFirstTitleItem>;

/**
 * Массив ответов на массовое создание первого титула
 * @remarks
 * Валидационный тип для массива результатов, используется при декодировании API ответа
 */
export const MassCreateFirstTitleResponse = t.array(MassCreateFirstTitleItem);
/** Тип массива результатов */
export type TMassCreateFirstTitleResponse = t.TypeOf<typeof MassCreateFirstTitleResponse>;

/** Тип для моков ответа массового создания первого титула */
export type MassCreateFirstTitleItemMock = TMassCreateFirstTitleItem;

/**
 * Запрос на отправку первого титула
 * @remarks
 * Содержит данные первого титула для отправки
 */
export const SendFirstTitleRequest = t.type({
  id: tt.uuid,
  content: t.string,
  fileName: t.string,
  ewbUuid: tt.uuid,
  signature: t.string,
  creationTime: t.string,
  humanReadableId: t.string,
  timeZone: t.string,
});
export type TSendFirstTitleRequest = t.TypeOf<typeof SendFirstTitleRequest>;

/**
 * Запрос на отправку массива первых титулов
 * @remarks
 * Содержит массив данных титулов для отправки
 */
export const SendFirstTitleBatchRequest = t.array(SendFirstTitleRequest);
export type TSendFirstTitleBatchRequest = t.TypeOf<typeof SendFirstTitleBatchRequest>;

/**
 * Ответ на отправку первого титула - пустой объект
 * @remarks
 * API возвращает пустой ответ 200 OK при успешной отправке
 */
export const SendFirstTitleResponse = t.record(t.string, t.unknown);
export type TSendFirstTitleResponse = t.TypeOf<typeof SendFirstTitleResponse>;

/**
 * Ответ WebSocket по сменам EWB
 * @remarks
 * Содержит результат обработки смены
 */
export const EwbShiftResponse = t.type({
  /** Уникальный идентификатор смены */
  shiftId: tt.uuid,
  /** Флаг успешности операции */
  success: t.boolean,
  /** Текст ошибки, если была ошибка, иначе пустая строка */
  errorText: t.string,
});
export type TEwbShiftResponse = t.TypeOf<typeof EwbShiftResponse>;
