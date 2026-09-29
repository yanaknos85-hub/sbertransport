import {
  RoleFilters,
  RoleFiltersTitles
} from 'shared/components/Cargo/HistoryTimeline/HistoryTimeline.constants';
import { FieldType } from 'shared/form/Field/Field';

import { TransportTypeEnum, TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import {
  CargoRequestStatusesEnum,
  CargoRequestStatusesTitles
} from 'constants/CargoRequestStatuses.constants';

export const fields = {
  requestHumanId: {
    label: 'ID заявки',
    name: 'requestHumanId',
    type: FieldType.input,
  },
  desiredDate: {
    label: 'Дата отправления',
    name: 'desiredDate',
    type: FieldType.dateRange,
  },
  shipmentTime: {
    label: 'Дата получения',
    name: 'shipmentTime', // найти название поля
    type: FieldType.dateRange,
  },
  requestStatusSet: {
    label: 'Статус',
    name: 'requestStatusSet',
    type: FieldType.selectMultiple,
    params: {
      allowClear: true,
      showSearch: true,
      optionFilterProp: 'label',
      mode: 'multiple',
      options: [
        { label: CargoRequestStatusesTitles[CargoRequestStatusesEnum.CARGO_AWAITING_APPROVAL], value: CargoRequestStatusesEnum.CARGO_AWAITING_APPROVAL },
        { label: CargoRequestStatusesTitles[CargoRequestStatusesEnum.CARGO_APPROVED], value: CargoRequestStatusesEnum.CARGO_APPROVED },
        { label: CargoRequestStatusesTitles[CargoRequestStatusesEnum.CARGO_AWAITING_DATA], value: CargoRequestStatusesEnum.CARGO_AWAITING_DATA },
        { label: CargoRequestStatusesTitles[CargoRequestStatusesEnum.CARGO_AWAITING_TRANSFER], value: CargoRequestStatusesEnum.CARGO_AWAITING_TRANSFER },
        { label: CargoRequestStatusesTitles[CargoRequestStatusesEnum.CARGO_TRANSFER_FINISHED], value: CargoRequestStatusesEnum.CARGO_TRANSFER_FINISHED },
        { label: CargoRequestStatusesTitles[CargoRequestStatusesEnum.CARGO_SHIPMENT_FINISHED], value: CargoRequestStatusesEnum.CARGO_SHIPMENT_FINISHED },
        { label: CargoRequestStatusesTitles[CargoRequestStatusesEnum.CARGO_DELIVERY_CONFIRMATION_FINISHED], value: CargoRequestStatusesEnum.CARGO_DELIVERY_CONFIRMATION_FINISHED },
        { label: CargoRequestStatusesTitles[CargoRequestStatusesEnum.CARGO_CANCELED], value: CargoRequestStatusesEnum.CARGO_CANCELED },

      ],
    },
  },
  transportTypeEnum: {
    label: 'Тип доставки',
    name: 'transportTypeEnum', // найти название поля
    type: FieldType.selectMultiple,
    params: {
      allowClear: true,
      showSearch: true,
      optionFilterProp: 'label',
      mode: 'multiple',
      options: [
        { label: TransportTypeTitlesEnum[TransportTypeEnum.COURIER], value: TransportTypeEnum.COURIER },
        { label: TransportTypeTitlesEnum[TransportTypeEnum.DEDICATED], value: TransportTypeEnum.DEDICATED },
        { label: TransportTypeTitlesEnum[TransportTypeEnum.INTERREGIONAL], value: TransportTypeEnum.INTERREGIONAL },
        { label: TransportTypeTitlesEnum[TransportTypeEnum.DOMESTIC_COURIER], value: TransportTypeEnum.DOMESTIC_COURIER },
        { label: TransportTypeTitlesEnum[TransportTypeEnum.INDIVIDUAL], value: TransportTypeEnum.INDIVIDUAL },
      ],
    },
  },
  role: {
    label: 'Роль в маршруте',
    name: 'role', // найти название поля
    type: FieldType.select,
    params: {
      allowClear: true,
      showSearch: true,
      optionFilterProp: 'label',
      options: [
        { label: RoleFiltersTitles[RoleFilters.all], value: RoleFilters.all },
        { label: RoleFiltersTitles[RoleFilters.authorId], value: RoleFilters.authorId },
        { label: RoleFiltersTitles[RoleFilters.senderId], value: RoleFilters.senderId },
        { label: RoleFiltersTitles[RoleFilters.recipientId], value: RoleFilters.recipientId },
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
};
