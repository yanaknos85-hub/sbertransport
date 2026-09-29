import React, { FC } from 'react';
import { Form, InputNumber } from 'antd';
import { FormInstance } from 'antd/es/form/Form';

import { ValidationRules } from 'shared/fieldValidationRules';
import { fieldTitles, passengerCountTitle } from '../../constants';

import Minus from 'shared/components/Images/Minus.svg';
import Plus from 'shared/components/Images/Plus.svg';
import '../../override.scss';

interface PassengerInformationProps {
  form: FormInstance;
}

export const PassengerInformation: FC<PassengerInformationProps> = () => (
  <div className="wrapper_groupTransfer">
    <span className="groupTransfer_title_item">{passengerCountTitle}</span>
    <Form.Item
      key={fieldTitles.passengerCount}
      name={fieldTitles.passengerCount}
      rules={[ValidationRules.general.required, ValidationRules.general.minMoneyValue()]}
    >
      <InputNumber
        min={0}
        max={19}
        type="number"
        className="numberCost"
        formatter={value => Number(value).toFixed()}
        controls={
          {
            upIcon: <img src={Plus} alt="plus" />,
            downIcon: <img src={Minus} alt="minus" />,
          }
        }
      />
    </Form.Item>
  </div>
);
