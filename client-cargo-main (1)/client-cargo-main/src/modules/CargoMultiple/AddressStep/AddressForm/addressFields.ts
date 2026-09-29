import { StoreNames } from 'ioc/ioc.storeNames';
import { ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

export const useAddresses = () => {
  const {
    [StoreNames.geoStore]: geoStore,
  } = useAppStoreContext();

  const waypointsList = geoStore.waypoints.map(({ addressString }) => addressString);

  return [{
    address: {
      label: 'Адрес',
      name: 'address',
      type: FieldType.address,
      index: 0,
      rules: [ValidationRules.general.required, ValidationRules.general.includes([waypointsList[0]])],
    },
    name: {
      label: 'ФИО',
      name: 'name',
      type: FieldType.employee,
      index: 0,
      rules: [ValidationRules.general.required],
    },
    phone: {
      label: 'Телефон',
      name: 'phone',
      type: FieldType.phone,
      rules: [ValidationRules.general.required],
    },
    organization: {
      label: 'Организация',
      name: 'senderOrganization',
      rules: [ValidationRules.general.lattersAndDigits, ValidationRules.general.maxLength(128)],
    },
  },
  {
    address: {
      label: 'Адрес',
      name: 'address',
      type: FieldType.address,
      index: 0,
      rules: [ValidationRules.general.required, ValidationRules.general.includes([waypointsList[1]])],
    },
    name: {
      label: 'ФИО',
      name: 'name',
      type: FieldType.employee,
      index: 0,
      rules: [ValidationRules.general.required],
    },
    phone: {
      label: 'Телефон',
      name: 'phone',
      type: FieldType.phone,
      rules: [ValidationRules.general.required],
    },
    organization: {
      label: 'Организация',
      name: 'senderOrganization',
      rules: [ValidationRules.general.lattersAndDigits, ValidationRules.general.maxLength(128)],
    },
  }];
};
