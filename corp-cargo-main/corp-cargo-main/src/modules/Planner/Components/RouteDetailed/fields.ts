import { FieldType } from 'shared/form/Field/Field';
import { ValidationRules } from 'shared/fieldValidationRules';

export const fields = {
  desiredDate: {
    label: 'Дата',
    name: 'desiredDate',
    type: FieldType.date,
    index: 0,
  },
  contractor: {
    label: 'Контрагент',
    name: 'contractor',
    type: FieldType.select,
    index: 0,
    rules: [ValidationRules.general.required],
  },
  transportType: {
    label: 'Вид транспорта',
    name: 'transportType',
    type: FieldType.select,
    index: 0,
    rules: [ValidationRules.general.required],
  },
};
