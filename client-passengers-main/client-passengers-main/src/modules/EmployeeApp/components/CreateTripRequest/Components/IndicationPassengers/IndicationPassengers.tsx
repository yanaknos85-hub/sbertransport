import React, { FC, useEffect } from 'react';
import classNames from 'classnames';
import { Form } from 'antd';
import { FormInstance } from 'antd/es/form/Form';

import { DepartmentEmployeeAutoComplete } from 'shared/components/EmployeeDynamicAutoComplete';
import { CreateRequestLinksTitles } from '../../constants/CreateRequest.constants';
import { ValidationRules } from 'shared/fieldValidationRules';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores';

import styles from '../../styles/create.module.scss';

interface IndicationPassengersProps {
  passengerCount: number;
  form: FormInstance;
  productTransportType: TransportTypeEnum;
}

export const IndicationPassengers: FC<IndicationPassengersProps> = ({
  passengerCount, form, productTransportType,
}) => {
  const divsArray = Array.from({ length: passengerCount });
  const {
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();
  const { employee, passenger } = form.getFieldsValue();
  const formPassenger = passenger === 'me'
    ? selfStore.selfEmployee
    : employee;
  const employees = form.getFieldValue('listEmployees');

  useEffect(() => {
    form.setFieldsValue({ listEmployees: { employee0: formPassenger } });
  }, []);

  useEffect(() => {
    if (employees) {
      const keys = Object.keys(employees);
      const truncatedKeys = keys.slice(0, passengerCount);
      const truncatedObject = {};

      for (const key of truncatedKeys) {
        if (key === 'employee0') {
          truncatedObject[key] = formPassenger;
        } else {
          truncatedObject[key] = employees[key];
        }
      }

      form.resetFields(['listEmployees']);
      form.setFieldsValue({ listEmployees: { ...truncatedObject } });
    }
  }, [passengerCount]);

  return (
    <div className={styles.employees_wrapper}>
      <Form.List name="listEmployees">
        {() => (
          <>
            {divsArray.map((_, index) => (
              <div key={index}>
                <span className={classNames(styles.employees_title)}>
                  {(productTransportType === TransportTypeEnum.PERSONAL || productTransportType === TransportTypeEnum.CARSHARING)
                  && index === 0 ? 'Водитель' : `Пассажир ${productTransportType === TransportTypeEnum.TAXI
                      ? index + 1 : index}`}
                </span>
                <Form.Item
                  shouldUpdate={true}
                >
                  {({ getFieldValue }) => {
                    return (
                      <Form.Item
                        shouldUpdate={true}
                        name={`employee${index}`}
                        rules={[ValidationRules.general.isValidIndicationPassangers(getFieldValue('listEmployees'))]}
                      >
                        <DepartmentEmployeeAutoComplete
                          disabled={index === 0}
                          placeholder={CreateRequestLinksTitles.createForAnotherPlaceholder}
                        />
                      </Form.Item>
                    );
                  }}
                </Form.Item>
              </div>
            ))}
          </>
        )}
      </Form.List>
    </div>
  );
};
