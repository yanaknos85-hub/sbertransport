import { validationPatterns, ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';

import { RequestListItem } from 'stores/CargoMassMultiple/CargoMassMultiple.interface';

const { required } = ValidationRules.general;

export const useFields = (data: RequestListItem) => {
  const {
    sender,
    recipient,
    sourceLoaders,
    destinationLoaders,
    expected,
    senderOrganization,
    recipientOrganization,
  } = data;

  return {
    senderAddress: {
      label: 'Адрес отправителя',
      name: 'senderAddress',
      initialValue: expected.waypoints[0].addressStringRepresentation,
      type: FieldType.address,
      rules: [
        required,
        {
          pattern: validationPatterns.emptyStringValidation,
          message: 'Введите адрес отправителя',
        },
      ],
      params: {
        dropdownMatchSelectWidth: false,
        dropdownStyle: {
          borderRadius: '12px',
        },
      },
      index: 0,
    },
    senderName: {
      label: 'ФИО отправителя',
      name: 'senderName',
      initialValue: sender.fullName,
      type: FieldType.employee,
      index: 0,
      rules: [
        required,
        {
          pattern: validationPatterns.emptyStringValidation,
          message: 'Введите ФИО отправителя',
        },
      ],
    },
    senderPhone: {
      label: 'Телефон',
      name: 'senderPhone',
      initialValue: sender.mobilePhone,
      type: FieldType.phone,
      rules: [required],
    },
    senderOrganization: {
      label: 'Организация',
      name: 'senderOrganization',
      initialValue: senderOrganization,
    },
    sourceLoaders: {
      label: 'Для маршрута требуется грузчик',
      name: 'sourceLoaders',
      initialValue: sourceLoaders,
      type: FieldType.checkbox,
    },
    recipientAddress: {
      label: 'Адрес получателя',
      name: 'recipientAddress',
      initialValue: expected.waypoints[1].addressStringRepresentation,
      type: FieldType.address,
      rules: [
        required,
        {
          pattern: validationPatterns.emptyStringValidation,
          message: 'Введите адрес получателя',
        },
      ],
      params: {
        dropdownMatchSelectWidth: false,
        dropdownStyle: {
          borderRadius: '12px',
        },
      },
      index: 1,
    },
    recipientName: {
      label: 'ФИО получателя',
      name: 'recipientName',
      initialValue: recipient.fullName,
      type: FieldType.employee,
      index: 1,
      rules: [
        required,
        {
          pattern: validationPatterns.emptyStringValidation,
          message: 'Введите ФИО получателя',
        },
      ],
    },
    recipientPhone: {
      label: 'Телефон',
      name: 'recipientPhone',
      initialValue: recipient.mobilePhone,
      type: FieldType.phone,
      rules: [required],
    },
    recipientOrganization: {
      label: 'Организация',
      name: 'recipientOrganization',
      initialValue: recipientOrganization,
    },
    destinationLoaders: {
      label: 'Требуется грузчик в точке получения',
      name: 'destinationLoaders',
      initialValue: destinationLoaders,
      type: FieldType.checkbox,
    },
  };
};
