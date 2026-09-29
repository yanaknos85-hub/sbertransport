import React, { FC, useEffect, useState } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import { EmployeeModel } from '@sber-sbertransport/mf-core';
import { Form } from 'antd';
import Button from 'antd/es/button/button';
import { FormInstance } from 'antd/es/form/Form';
import { StoreNames } from 'ioc/ioc.storeNames';
import { observer } from 'mobx-react';
import { DateFieldComponent } from 'shared/components/DateFieldComponent/DateFieldComponent';
import { ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import Input from 'shared/form/Input/Input';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { ExternalEmployee } from 'stores/Cargo/Cargo.interface';

import { AdditionalAddress } from './AdditionalAddress/AdditionalAddress';
import * as S from './AddressForm.styles';

interface Props {
  form: FormInstance;
  validateFields: (params: any) => void;
}

export const AddressForm: FC<Props> = observer(props => {
  const { form, validateFields } = props;

  const {
    [StoreNames.cargoStore]: cargoStore,
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.addressStore]: addressStore,
  } = useAppStoreContext();

  const [waypointsLength, setWaypointsLength] = useState(0);
  const [customerReceivingStates, setCustomerReceivingStates] = useState<Record<number, boolean>>({});

  const { sender: senderForm } = cargoStore.stepAddressValuesMulti;
  const sender: EmployeeModel | ExternalEmployee | null = cargoStore.stepAddressValues.sender;

  const initialWaypoints = [
    {
      address: '',
      name: sender?.fullNameWithCode,
      phone: sender?.mobilePhone,
      organization: '',
    }, {
      address: '',
      name: '',
      phone: '',
      organization: '',
    },
  ];

  const field = {
    name: {
      label: 'ФИО',
      name: 'name',
      type: FieldType.employee,
      index: 1,
      rules: [ValidationRules.general.required],
    },
  };

  const fieldNameList = Object.keys(field);

  const handleChangeForm = () => {
    validateFields(fieldNameList);
    setWaypointsLength(form.getFieldValue('waypoints').length);
  };

  const handleOnChange = (fieldName: string, fieldPhone: string) => {
    if (form.getFieldValue(fieldName)?.length === 0) {
      form.setFields([{ name: fieldPhone, value: '' }]);
    }
  };

  const handleOnClear = (fieldName: string, fieldPhone: string, name: number) => {
    form.setFields([{ name: ['waypoints', name, 'phone'], value: '' }]);
  };

  const handleOnSelect = (fieldName: string, name: number) => {
    if (fieldName === field.name.name) {
      setTimeout(() => {
        const employee = employeeStore.employeeAutocompleteSelected[name];
        if (employee) {
          if (form.getFieldValue(['waypoints', name, 'name']) === employee.fullNameWithCode) {
            form.setFields([{ name: ['waypoints', name, 'phone'], value: employee.mobilePhone }]);
          } else {
            form.setFields([{ name: field.name.name, value: '' }]);
          }
          validateFields(['waypoints', name]);
        }
      }, 0);
    }
  };

  const handleRadioButton = (value: string, name: number) => {
    form.setFields([{ name: ['waypoints', name, 'type'], value }]);
  };

  const handleCustomerReceivingChange = (name: number, checked: boolean) => {
    setCustomerReceivingStates(prev => ({
      ...prev,
      [name]: checked,
    }));
  };

  useEffect(() => {
    validateFields(() => {
      // eslint-disable-next-line @typescript-eslint/no-empty-function
    });
  }, [customerReceivingStates]);

  useEffect(() => {
    addressStore.initStore();
  }, []);

  return (
    <S.SideSections>
      <Form
        form={form}
        name="addressForm"
        onValuesChange={handleChangeForm}
        initialValues={{
          sender: senderForm,
          waypoints: initialWaypoints,
        }}
      >
        {cargoStore.isRelocation && (
          <Form.Item
            label="Номер служебной записки"
            name="internalNote"
            rules={[
              {
                required: true,
                message: 'Пожалуйста, введите номер служебной записки!',
              },
            ]}
            labelCol={{ span: 24 }}
            wrapperCol={{ span: 24 }}
          >
            <Input placeholder="Например: ЦА-440-вн/677" />
          </Form.Item>
        )}
        {!cargoStore.isRegular && <DateFieldComponent form={form} />}
        <Form.List
          name="waypoints"
          rules={[
            {
              validator: async (_, names) => {
                if (!names || names.length < 2) {
                  return Promise.reject(new Error('Необходимо указать минимум два адреса'));
                }
              },
            },
          ]}
        >
          {(waypoints, { add, remove }, { errors }) => {
            return (
              <>
                {waypoints.map(({ key, name }, idx) => {
                  return (
                    <div key={key}>
                      <AdditionalAddress
                        handleRadioButton={handleRadioButton}
                        handleOnSelect={handleOnSelect}
                        handleOnClear={handleOnClear}
                        handleOnChange={handleOnChange}
                        idx={idx}
                        keyProp={key}
                        name={name}
                        remove={remove}
                        waypointsLength={waypointsLength}
                        form={form}
                        isCustomerReceiving={customerReceivingStates[name] || false}
                        onCustomerReceivingChange={handleCustomerReceivingChange}
                      />
                    </div>
                  );
                })}
                <S.ErrorsWrapper>
                  <Form.ErrorList errors={errors} />
                </S.ErrorsWrapper>
                {!cargoStore.isRelocation && (
                  <Form.Item>
                    <Button
                      onClick={() => add()}
                      block
                      icon={<PlusOutlined />}
                    >
                      Добавить адрес
                    </Button>
                  </Form.Item>
                )}
              </>
            );
          }}
        </Form.List>
      </Form>
    </S.SideSections>
  );
});
