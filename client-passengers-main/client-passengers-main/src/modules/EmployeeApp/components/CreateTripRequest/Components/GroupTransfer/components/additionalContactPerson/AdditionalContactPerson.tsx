import '../../override.scss';
import {
  Divider, Input, Switch, Form
} from 'antd';
import { observer } from 'mobx-react';
import React, { FC } from 'react';

import additionalContactPersonStyles from './additionalContactPerson.module.scss';
import {
  additionalContactPersonTitle, clientFullName, fieldTitles, telephone
} from '../../constants';
import { ValidationRules } from 'shared/fieldValidationRules';

export const AdditionalContactPerson: FC = observer(
  (): JSX.Element => {
    return (
      <div className="wrapper_groupTransfer">
        <Divider />
        <div className={additionalContactPersonStyles.wrapper_switch}>
          <span className={additionalContactPersonStyles.title}>{additionalContactPersonTitle}</span>
          <Switch checked disabled />
        </div>
        <div className={additionalContactPersonStyles.wrapper_clientInfo}>
          <span className="groupTransfer_title_item">{clientFullName}</span>
          <Form.Item
            key={fieldTitles.clientFullName}
            name={fieldTitles.clientFullName}
            rules={[ValidationRules.general.checkClientFullName()]}
          >
            <Input placeholder="Укажите ФИО" />
          </Form.Item>
          <span className="groupTransfer_title_item">{telephone}</span>
          <Form.Item
            key={fieldTitles.clientInfoPhone}
            name={fieldTitles.clientInfoPhone}
            rules={[ValidationRules.general.required, ValidationRules.general.checkPhoneMask()]}
          >
            <Input className={additionalContactPersonStyles.telephone_title} placeholder="Укажите номер телефона" />
          </Form.Item>
        </div>
        <Divider />
      </div>
    );
  }
);
