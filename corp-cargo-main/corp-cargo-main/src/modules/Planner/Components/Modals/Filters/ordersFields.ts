import { FieldType } from 'shared/form/Field/Field';
import { ValidationRules } from 'shared/fieldValidationRules';
import { DeliveryUrgencyEnum } from '../../../types';

export const ordersFields = {
  humanReadableId: {
    label: 'ID заявки',
    name: 'humanReadableId',
    type: FieldType.input,
    index: 0,
  },
  express: {
    label: 'Срочность',
    name: 'express',
    type: FieldType.select,
    index: 0,
    params: {
      options: [
        { label: 'Стандарт', value: DeliveryUrgencyEnum.standard },
        { label: 'Экспресс', value: DeliveryUrgencyEnum.express },
      ],
    },
  },
  creationDateRange: {
    label: 'Дата создания',
    name: 'creationDateRange',
    type: FieldType.dateRange,
    index: 0,
  },
  desiredDateRange: {
    label: 'Дата отправления',
    name: 'desiredDateRange',
    type: FieldType.dateRange,
    index: 0,
  },
  departmentEmployeeId: {
    label: 'Подразделение заказчика',
    name: 'departmentEmployeeId',
    type: FieldType.department,
    index: 0,
  },
  departmentSenderId: {
    label: 'Подразделение отправителя',
    name: 'departmentSenderId',
    type: FieldType.department,
    index: 0,
  },
  departmentOrderId: {
    label: 'Подразделение получателя',
    name: 'departmentOrderId',
    type: FieldType.department,
    index: 0,
  },
  regionFrom: {
    label: 'Регион отправления',
    name: 'regionFrom',
    type: FieldType.select,
    index: 0,
  },
  regionTo: {
    label: 'Регион доставки',
    name: 'regionTo',
    type: FieldType.select,
    index: 0,
  },
  contractor: {
    label: 'Контрагент',
    name: 'contractor',
    type: FieldType.select,
    index: 0,
    params: {
      options: [
        {
          label: 'Исполнитель 1',
          value: 'contractor 1',
        },
        {
          label: 'Исполнитель 2',
          value: 'contractor 2',
        },
      ],
    },
  },
  transportType: {
    label: 'Тип автомобиля',
    name: 'transportType',
    type: FieldType.select,
    index: 0,
    params: {
      options: [
        { label: 'до 1.5 тонн', value: 'upTo 1.5' },
        { label: 'до 3 тонн', value: 'upTo 3.0' },
      ],
    },
  },
  authorEmployeeId: {
    label: 'ФИО заявителя',
    name: 'authorEmployeeId',
    type: FieldType.employee,
    params: {
      disabled: true,
    },
  },
  departmentId: {
    label: 'Подразделение',
    name: 'departmentId',
    type: FieldType.department,
    params: {
      disabled: true,
    },
  },
};
