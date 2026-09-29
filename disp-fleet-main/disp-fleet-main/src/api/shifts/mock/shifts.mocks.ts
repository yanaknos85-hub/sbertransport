import { Shift } from '../shifts.types';
import { MassCreateFirstTitleItemMock } from '../shifts.types';
import { UUID } from 'utils/io-ts';

export const mockShifts: Shift[] = [
  {
    id: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
    driverName: 'Иванов Иван Иванович',
    vehicleBrand: 'ГАЗ',
    vehicleModel: 'ГАЗель-3302',
    vehicleStateNumber: 'А123БВ777',
    startDate: '2023-10-20',
    endDate: '2023-10-21',
  },
  {
    id: 'b2c3d4e5-f6g7-8901-h2i3-j4k5l6m7n8o9' as UUID,
    driverName: 'Петров Петр Петрович',
    vehicleBrand: 'КАВЗ',
    vehicleModel: 'КАВЗ-4238',
    vehicleStateNumber: 'К456ЛМ50',
    startDate: '2023-10-20',
    endDate: '2023-10-21',
  },
];

/** Мок для ответа массового создания первого титула - успешный случай */
export const mockMassCreateFirstTitleSuccess: MassCreateFirstTitleItemMock[] = [
  {
    shiftId: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
    ewbId: 'ewb1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p' as UUID,
    humanReadableId: 'EWB-20231020-001',
    fileName: 'shift_report_001.pdf',
    content: 'JVBERi0xLjQKJdPr6eE=',
    creationTime: '2023-10-20T10:30:00',
    errorText: '',
  },
  {
    shiftId: 'b2c3d4e5-f6g7-8901-h2i3-j4k5l6m7n8o9' as UUID,
    ewbId: 'ewb2b3c4d5e-6f7g-8h9i-0j1k-2l3m4n5o6p7q' as UUID,
    humanReadableId: 'EWB-20231021-002',
    fileName: 'shift_report_002.xlsx',
    content: 'UEsDBBQABgAIAAA=',
    creationTime: '2023-10-21T09:15:30',
    errorText: '',
  },
];

/** Мок для ответа массового создания первого титула - с ошибкой */
export const mockMassCreateFirstTitleWithError: MassCreateFirstTitleItemMock[] = [
  {
    shiftId: 'a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8' as UUID,
    ewbId: 'ewb1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p' as UUID,
    humanReadableId: 'EWB-20231020-001',
    fileName: 'shift_report_001.pdf',
    content: 'JVBERi0xLjQKJdPr6eE=',
    creationTime: '2023-10-20T10:30:00',
    errorText: '',
  },
  {
    shiftId: 'b2c3d4e5-f6g7-8901-h2i3-j4k5l6m7n8o9' as UUID,
    ewbId: 'ewb2b3c4d5e-6f7g-8h9i-0j1k-2l3m4n5o6p7q' as UUID,
    humanReadableId: 'EWB-20231021-002',
    fileName: 'shift_report_002.xlsx',
    content: 'UEsDBBQABgAIAAA=',
    creationTime: '2023-10-21T09:15:30',
    errorText: 'Ошибка подписи ЭЦП',
  },
];
