/* eslint-disable @typescript-eslint/no-explicit-any */
import { convertPurposesToOptions, prepareSubmitPurposes, getPurposesIds } from './purposesOptions';
import { TripPurpose } from 'stores/TripPurposes/TripPurpose.interface';

describe('purposesOptions', () => {
  describe('convertPurposesToOptions', () => {
    it('должен конвертировать массив purpose в options', () => {
      const purposes: TripPurpose[]  = [
        { id: '00000000-0000-0000-0000-000000000001' as any, label: 'Техника', purposeType: '', tripPurposeAttributes: [], tripPurposeDates: [], tripPurposeDepartments: [], tripPurposeTimes: [], tripPurposeWeekdays: [] },
        { id: '00000000-0000-0000-0000-000000000002' as any, label: 'Документы', purposeType: '', tripPurposeAttributes: [], tripPurposeDates: [], tripPurposeDepartments: [], tripPurposeTimes: [], tripPurposeWeekdays: [] },
        { id: '00000000-0000-0000-0000-000000000003' as any, label: 'Мебель', purposeType: '', tripPurposeAttributes: [], tripPurposeDates: [], tripPurposeDepartments: [], tripPurposeTimes: [], tripPurposeWeekdays: [] },
      ];

      const result = convertPurposesToOptions(purposes);

      expect(result).toEqual([
        { label: 'Техника', value: '00000000-0000-0000-0000-000000000001' },
        { label: 'Документы', value: '00000000-0000-0000-0000-000000000002' },
        { label: 'Мебель', value: '00000000-0000-0000-0000-000000000003' },
      ]);
    });

    it('должен группировать purpose с одинаковым label и объединять id', () => {
      const purposes: TripPurpose[]  = [
        { id: '00000000-0000-0000-0000-000000000001' as any, label: 'Техника', purposeType: '', tripPurposeAttributes: [], tripPurposeDates: [], tripPurposeDepartments: [], tripPurposeTimes: [], tripPurposeWeekdays: [] },
        { id: '00000000-0000-0000-0000-000000000002' as any, label: 'Техника', purposeType: '', tripPurposeAttributes: [], tripPurposeDates: [], tripPurposeDepartments: [], tripPurposeTimes: [], tripPurposeWeekdays: [] },
        { id: '00000000-0000-0000-0000-000000000003' as any, label: 'Документы', purposeType: '', tripPurposeAttributes: [], tripPurposeDates: [], tripPurposeDepartments: [], tripPurposeTimes: [], tripPurposeWeekdays: [] },
        { id: '00000000-0000-0000-0000-000000000004' as any, label: 'Техника', purposeType: '', tripPurposeAttributes: [], tripPurposeDates: [], tripPurposeDepartments: [], tripPurposeTimes: [], tripPurposeWeekdays: [] },
      ];

      const result = convertPurposesToOptions(purposes);

      expect(result).toEqual([
        { label: 'Техника', value: '00000000-0000-0000-0000-000000000001,00000000-0000-0000-0000-000000000002,00000000-0000-0000-0000-000000000004' },
        { label: 'Документы', value: '00000000-0000-0000-0000-000000000003' },
      ]);
    });

    it('должен возвращать пустой массив для пустого входа', () => {
      const result = convertPurposesToOptions([]);

      expect(result).toEqual([]);
    });
  });

  describe('prepareSubmitPurposes', () => {
    it('должен разбить строку значений на массив объектов', () => {
      const result = prepareSubmitPurposes(['1,2', '3']);

      expect(result).toEqual([
        { id: '1' },
        { id: '2' },
        { id: '3' },
      ]);
    });

    it('должен обрабатывать одну запись без запятых', () => {
      const result = prepareSubmitPurposes(['1']);

      expect(result).toEqual([{ id: '1' }]);
    });

    it('должен обрабатывать пустой массив', () => {
      const result = prepareSubmitPurposes([]);

      expect(result).toEqual([]);
    });
  });

  describe('getPurposesIds', () => {
    it('должен извлекать id из массива value', () => {
      const value = [
        { key: '00000000-0000-0000-0000-000000000001', label: 'Техника' },
        { key: '00000000-0000-0000-0000-000000000002', label: 'Документы' },
      ];

      const result = getPurposesIds(value as any);

      expect(result).toEqual([
        { id: '00000000-0000-0000-0000-000000000001' },
        { id: '00000000-0000-0000-0000-000000000002' },
      ]);
    });

    it('должен возвращать undefined для undefined', () => {
      const result = getPurposesIds(undefined);

      expect(result).toBeUndefined();
    });
  });
});
