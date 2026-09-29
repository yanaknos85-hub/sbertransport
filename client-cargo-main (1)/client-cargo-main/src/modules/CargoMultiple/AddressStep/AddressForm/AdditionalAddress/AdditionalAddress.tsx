import React, { FC, useState } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import { EmployeeModel } from '@sber-sbertransport/mf-core';
import {
  Form, Radio as RadioAnt, RadioChangeEvent, Switch
} from 'antd';
import { FormInstance } from 'antd/es/form/Form';
import { StoreNames } from 'ioc/ioc.storeNames';
import { ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { ExternalEmployee } from 'stores/Cargo/Cargo.interface';
import { AddressLoadType } from 'stores/Cargos/typesMulti';

import { ReactComponent as TrashBucket } from '../images/trash.svg';
import * as S from './AdditionalAddress.styles';

interface Props {
  handleRadioButton: (value: string, name: number) => void;
  handleOnSelect: (param: string, name: number) => void;
  handleOnClear: (fieldName: string, fieldPhone: string, name: number) => void;
  handleOnChange: (fieldName: string, fieldPhone: string) => void;
  idx: number;
  keyProp: number;
  name: number;
  remove: (index: number | number[]) => void;
  waypointsLength: number;
  form: FormInstance;
  isCustomerReceiving: boolean;
  onCustomerReceivingChange: (name: number, checked: boolean) => void;
}

export const AdditionalAddress: FC<Props> = props => {
  const {
    idx,
    name,
    remove,
    keyProp,
    handleOnClear,
    handleOnSelect,
    handleOnChange,
    waypointsLength,
    handleRadioButton,
    form,
    isCustomerReceiving,
    onCustomerReceivingChange,
  } = props;

  const {
    [StoreNames.geoStore]: geoStore,
    [StoreNames.cargoStore]: cargoStore,
  } = useAppStoreContext();
  const [isOpenContact, setIsOpenContact] = useState(false);
  const ADDRESS_TYPE = name === 0 ? AddressLoadType.LOAD : AddressLoadType.UNLOAD;
  const sender: EmployeeModel | ExternalEmployee | null = cargoStore.stepAddressValues.sender;

  const getNameRules = () => {
    if (idx > 0 && cargoStore.isRelocation && isCustomerReceiving) {
      return [];
    }

    return [ValidationRules.general.required];
  };

  const getPhoneRules = () => {
    if (idx > 0 && cargoStore.isRelocation && isCustomerReceiving) {
      return [];
    }

    return [
      ValidationRules.general.required.required,
      ValidationRules.general.phone,
    ];
  };

  const addressFields = {
    type: {
      label: '',
      name: [name, 'type'],
      type: FieldType.radio,
      validateTrigger: ['onChange'],
      fieldKey: [name, 'type'],
      initialValue: ADDRESS_TYPE,
      rules: [
        ValidationRules.general.required,
        ValidationRules.general.addressType(waypointsLength, name),
      ],
    },
    address: {
      label: 'Адрес',
      name: [name, 'address'],
      type: FieldType.address,
      validateTrigger: ['onChange', 'onBlur'],
      index: idx,
      rules: [
        ValidationRules.general.required.required,
        ValidationRules.general.empty,
      ],
      params: {
        dropdownMatchSelectWidth: false,
        dropdownStyle: {
          borderRadius: '12px',
        },
      },
    },
    entrance: {
      label: 'Подъезд',
      name: [name, 'entrance'],
      type: FieldType.input,
      validateTrigger: ['onChange', 'onBlur'],
      index: idx,
    },
    floor: {
      label: 'Этаж',
      name: [name, 'floor'],
      type: FieldType.input,
      validateTrigger: ['onChange', 'onBlur'],
      index: idx,

    },
    flat: {
      label: 'Квартира',
      name: [name, 'flat'],
      type: FieldType.input,
      validateTrigger: ['onChange', 'onBlur'],
      index: idx,
    },
    name: {
      label: 'ФИО',
      name: [name, 'name'] as const,
      type: FieldType.employee,
      validateTrigger: ['onChange', 'onBlur'],
      index: idx,
      rules: getNameRules(),

    },
    phone: {
      label: 'Телефон',
      name: [name, 'phone'] as const,
      type: FieldType.phone,
      validateTrigger: ['onChange', 'onBlur'],
      rules: getPhoneRules(),
    },

    extraContactName: {
      label: 'Имя',
      name: [name, 'extraContactName'],
      type: FieldType.input,
      validateTrigger: ['onChange', 'onBlur'],
      index: idx,
    },
    extraContactPhone: {
      label: 'Телефон',
      name: [name, 'extraContactPhone'] as const,
      type: FieldType.phone,
      validateTrigger: ['onChange', 'onBlur'],
      rules: [
        ValidationRules.general.phone,
      ],
    },
    organization: {
      label: 'Организация',
      name: [name, 'organization'],
      validateTrigger: ['onChange', 'onBlur'],
      rules: [
        ValidationRules.general.lattersAndDigits,
        ValidationRules.general.maxLength(128),
      ],
    },
  };

  const onChangeCheckBox = (e: RadioChangeEvent) => {
    handleRadioButton(e.target.value, name);
  };

  const handleDeleteAddress = (addressItemId: number) => {
    remove(addressItemId);
    geoStore.removeWaypoint(addressItemId);
  };

  const handleCustomerReceivingChange = (checked: boolean) => {
    onCustomerReceivingChange(name, checked);

    if (checked) {
      // Заполняем поля значениями отправителя
      form.setFields([
        {
          name: ['waypoints', name, 'name'],
          value: sender?.fullNameWithCode || '',
          touched: false,
        },
        {
          name: ['waypoints', name, 'phone'],
          value: sender?.mobilePhone || '',
          touched: false,
        },
      ]);
    } else {
      form.setFields([
        {
          name: ['waypoints', name, 'name'], value: '', touched: false,
        },
        {
          name: ['waypoints', name, 'phone'], value: '', touched: false,
        },
      ]);
    }
  };

  return (
    <S.Section key={keyProp}>
      <S.Header>
        {!cargoStore.isRelocation ? (
          <p>
            Адрес №
            {idx + 1}
          </p>
        ) : (idx > 0 ? 'Куда' : 'Откуда')}
        {name > 0 && !(cargoStore.isRelocation && idx > 0) && (
          <S.Trash>
            <TrashBucket onClick={() => handleDeleteAddress(name)} />
          </S.Trash>
        )}
      </S.Header>
      {!cargoStore.isRelocation && (
        <Form.Item
          {...addressFields.type}
        >
          <RadioAnt.Group onChange={onChangeCheckBox}>
            <RadioAnt value={AddressLoadType.LOAD}>Сбор</RadioAnt>
            <RadioAnt value={AddressLoadType.UNLOAD}>Доставка</RadioAnt>
          </RadioAnt.Group>
        </Form.Item>
      )}
      <FormField
        {...addressFields.address}
      />
      {cargoStore.isRelocation && (
        <S.AddressInfo>
          <FormField
            {...addressFields.entrance
            }
          />
          <FormField
            {...addressFields.floor
            }
          />
          <FormField
            {...addressFields.flat}
          />
        </S.AddressInfo>
      )}

      {/* Switch для адреса "Куда" */}
      {idx > 0 && cargoStore.isRelocation && (
        <Form.Item>
          <Switch
            checked={isCustomerReceiving}
            onChange={handleCustomerReceivingChange}
          />
          <S.RelocationSwitch>Заказчик получит груз</S.RelocationSwitch>
        </Form.Item>
      )}

      {/* Поля ФИО и Телефон - скрываются при включенном Switch для адреса "Куда" */}
      {!(idx > 0 && cargoStore.isRelocation && isCustomerReceiving) && (
        <>
          <FormField
            {...addressFields.name}
            params={{
              onBlur: () => handleOnSelect(addressFields.name.name[1], name),
              onSelect: () => handleOnSelect(addressFields.name.name[1], name),
              onClear: () => handleOnClear(addressFields.name.name[1], addressFields.phone.name[1], name),
              onChange: () => handleOnChange(addressFields.name.name[1], addressFields.phone.name[1]),
            }}
          />
          <FormField
            {...addressFields.phone}
          />
        </>
      )}

      {cargoStore.isRelocation && (
        isOpenContact
          ? (
            <div>
              <S.ContactHeader>
                <p>
                  Контактное лицо при
                  {' '}
                  {idx === 0 ? 'отправлении' : 'получении'}
                </p>
                <TrashBucket onClick={() => setIsOpenContact(false)} />
              </S.ContactHeader>
              <FormField
                {...addressFields.extraContactName}
              />
              <FormField
                {...addressFields.extraContactPhone}
              />
            </div>
          )
          : (
            <S.AddContactButton onClick={() => setIsOpenContact(true)}>
              <PlusOutlined />
              <span>
                Добавить контактное лицо при
                {' '}
                {idx === 0 ? 'отправлении' : 'получении'}
              </span>
            </S.AddContactButton>
          )
      )}

      {!cargoStore.isRelocation && (
        <FormField
          {...addressFields.organization}
        />
      )}
    </S.Section>
  );
};
