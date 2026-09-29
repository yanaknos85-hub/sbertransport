import { Shift } from 'api/shifts/shifts.types';
import { UUID } from 'utils/io-ts';
import { TMassCreateFirstTitleItem, TEwbShiftResponse } from 'api/shifts/shifts.types';

export const mockShifts: Shift[] = [
  {
    id: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
    driverName: 'Иванов Иван Иванович',
    vehicleBrand: 'ГАЗ',
    vehicleModel: 'ГАЗель-3302',
    vehicleStateNumber: 'А123БВ777',
    startDate: '2023-10-20T00:00:00Z',
    endDate: '2023-10-21T00:00:00Z',
  },
  {
    id: 'b2c3d4e5-f6g7-8901-h2i3-j4k5l6m7n8o9' as UUID,
    driverName: 'Петров Петр Петрович',
    vehicleBrand: 'КАВЗ',
    vehicleModel: 'КАВЗ-4238',
    vehicleStateNumber: 'К456ЛМ50',
    startDate: '2023-10-20T00:00:00Z',
    endDate: '2023-10-21T00:00:00Z',
  },
  {
    id: 'c3d4e5f6-g7h8-9012-i3j4-k5l6m7n8o9p0' as UUID,
    driverName: 'Сидоров Сидор Сидорович',
    vehicleBrand: 'УАЗ',
    vehicleModel: 'УАЗ-3909',
    vehicleStateNumber: 'М789НТ199',
    startDate: '2023-10-20T00:00:00Z',
    endDate: '2023-10-21T00:00:00Z',
  },
];

export const mockEmptyShifts: Shift[] = [];

/**
 * Массив из 150 смен для тестирования ограничения выбора
 */
export const mock150Shifts: Shift[] = Array.from({ length: 150 }, (_, i) => ({
  id: `shift-${i}` as UUID,
  driverName: `Водитель ${i + 1}`,
  vehicleBrand: 'ГАЗ',
  vehicleModel: 'ГАЗель',
  vehicleStateNumber: `А${String(i).padStart(3, '0')}БВ${String(i).padStart(3, '0')}`,
  startDate: '2023-10-20T00:00:00Z',
  endDate: '2023-10-21T00:00:00Z',
}));

export const useShiftsMock = jest.fn();

/**
 * Моковые данные для массового создания первого титула
 * @remarks
 * Содержит успешные и ошибочные результаты для тестирования компонента Results
 */
export const mockMassCreateFirstTitle: TMassCreateFirstTitleItem[] = [
  {
    shiftId: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
    ewbId: 'ewb1-2345-6789-abcd-ef0123456789' as UUID,
    humanReadableId: 'EWB-20231020-001',
    fileName: 'first_title_001.xml',
    content: 'base64-content-1',
    creationTime: '2023-10-20T10:00:00Z',
    errorText: '',
  },
  {
    shiftId: 'b2c3d4e5-f6g7-8901-h2i3-j4k5l6m7n8o9' as UUID,
    ewbId: 'ewb2-2345-6789-abcd-ef0123456790' as UUID,
    humanReadableId: 'EWB-20231020-002',
    fileName: 'first_title_002.xml',
    content: 'base64-content-2',
    creationTime: '2023-10-20T10:01:00Z',
    errorText: '',
  },
  {
    shiftId: 'c3d4e5f6-g7h8-9012-i3j4-k5l6m7n8o9p0' as UUID,
    ewbId: 'ewb3-2345-6789-abcd-ef0123456791' as UUID,
    humanReadableId: 'EWB-20231020-003',
    fileName: 'first_title_003.xml',
    content: 'base64-content-3',
    creationTime: '2023-10-20T10:02:00Z',
    errorText: 'Ошибка подписания документа',
  },
  {
    shiftId: 'd4e5f6g7-h8i9-0123-j4k5-l6m7n8o9p0q1' as UUID,
    ewbId: 'ewb4-2345-6789-abcd-ef0123456792' as UUID,
    humanReadableId: 'EWB-20231020-004',
    fileName: 'first_title_004.xml',
    content: 'base64-content-4',
    creationTime: '2023-10-20T10:03:00Z',
    errorText: 'Неверный формат данных',
  },
];

/**
 * Моковые данные WebSocket-ответов для компонента Results
 * @remarks
 * Содержит успешные и ошибочные ответы для имитации процесса создания ЭПЛ
 */
export const mockEwbShiftResponses: TEwbShiftResponse[] = [
  {
    shiftId: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
    success: true,
    errorText: '',
  },
  {
    shiftId: 'b2c3d4e5-f6g7-8901-h2i3-j4k5l6m7n8o9' as UUID,
    success: true,
    errorText: '',
  },
  {
    shiftId: 'c3d4e5f6-g7h8-9012-i3j4-k5l6m7n8o9p0' as UUID,
    success: false,
    errorText: 'Ошибка подписания документа',
  },
  {
    shiftId: 'd4e5f6g7-h8i9-0123-j4k5-l6m7n8o9p0q1' as UUID,
    success: false,
    errorText: 'Неверный формат данных',
  },
];

/**
 * Мок с результатами создания ЭПЛ (для отображения в Results)
 * @remarks
 * Содержит как успешные, так и ошибочные ответы для демонстрации работы компонента Results
 * Длина массива соответствует первым 2 элементам mockMassCreateFirstTitle для корректной работы isComplete
 */
export const mockEwbShiftResponsesIncomplete: TEwbShiftResponse[] = [
  {
    shiftId: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
    success: true,
    errorText: '',
  },
  {
    shiftId: 'b2c3d4e5-f6g7-8901-h2i3-j4k5l6m7n8o9' as UUID,
    success: false,
    errorText: 'Ошибка подписания документа',
  },
];
