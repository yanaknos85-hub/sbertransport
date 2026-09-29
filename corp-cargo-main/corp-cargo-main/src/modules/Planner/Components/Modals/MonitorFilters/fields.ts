import { FieldType } from 'shared/form/Field/Field';
import { Statuses, StatusNames } from '../../Monitor/constants';

export const monitorFields = {
  humanReadableId: {
    label: 'ID маршрута',
    name: 'humanReadableId',
    type: FieldType.input,
  },
  statusSet: {
    label: 'Статус',
    name: 'statusSet',
    type: FieldType.selectMultiple,
    allowClear: true,
    showSearch: true,
    optionFilterProp: 'label',
    params: {
      mode: 'multiple',
      allowClear: true,
      options: [
        { label: StatusNames[Statuses.CARGO_PLANNING], value: Statuses.CARGO_PLANNING },
        { label: StatusNames[Statuses.CARGO_PLANNING_FINISHED], value: Statuses.CARGO_PLANNING_FINISHED },
        { label: StatusNames[Statuses.CARGO_AWAITING_DATA], value: Statuses.CARGO_AWAITING_DATA },
        { label: StatusNames[Statuses.CARGO_AWAITING_TRANSFER], value: Statuses.CARGO_AWAITING_TRANSFER },
        { label: StatusNames[Statuses.CARGO_TRANSFER_FINISHED], value: Statuses.CARGO_TRANSFER_FINISHED },
        { label: StatusNames[Statuses.CARGO_SHIPMENT_FINISHED], value: Statuses.CARGO_SHIPMENT_FINISHED },
        { label: StatusNames[Statuses.CARGO_CANCELED], value: Statuses.CARGO_CANCELED },
      ],
    },
  },
  desiredDateRange: {
    label: 'Дата отправления',
    name: 'desiredDateRange',
    type: FieldType.dateRange,
  },
  creationDateRange: {
    label: 'Дата и время создания маршрута',
    name: 'creationDateRange',
    type: FieldType.dateRange,
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
  contractors: {
    label: "Контрагент",
    name: 'contractors',
    type: FieldType.selectMultiple,
  },
};
