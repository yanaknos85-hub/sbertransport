import { FieldType } from 'shared/form/Field/Field';

export const routesFields = {
  humanReadableId: {
    label: 'ID маршрута',
    name: 'humanReadableId',
    type: FieldType.input,
  },
  statusSet: {
    label: 'Статус',
    name: 'statusSet',
    type: FieldType.select,
    params: {
      options: [
        { label: 'Планируется', value: 'CARGO_PLANNING' },
        { label: 'Запланировано', value: 'CARGO_PLANNING_FINISHED' },
      ],
    },
  },
  regionFrom: {
    label: 'Территория отправления',
    name: 'regionFrom',
    type: FieldType.selectMultiple,
  },
  regionTo: {
    label: 'Территория доставки',
    name: 'regionTo',
    type: FieldType.selectMultiple,
  },
  creationDateRange: {
    label: 'Дата и время создания маршрута',
    name: 'creationDateRange',
    type: FieldType.dateRange,
  },
  desiredDateRange: {
    label: 'Дата отправления',
    name: 'desiredDateRange',
    type: FieldType.dateRange,
  },
  contractor: {
    label: 'Контрагент',
    name: 'contractor',
    type: FieldType.select,
  },
  tonnage: {
    label: 'Грузоподъёмность',
    name: 'tonnage',
    type: FieldType.select,
    params: {
      options: [
        { label: 'до 1.5 тонн', value: 'upTo 1.5' },
        { label: 'до 3 тонн', value: 'upTo 3.0' },
      ],
    },
  },
};
