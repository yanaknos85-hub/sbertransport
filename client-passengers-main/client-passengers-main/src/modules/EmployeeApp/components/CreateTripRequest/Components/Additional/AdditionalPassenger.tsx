import { Form, Radio } from 'antd';
import { observer } from 'mobx-react';
import React from 'react';

import { DepartmentEmployeeAutoComplete } from 'shared/components/EmployeeDynamicAutoComplete';

import { CreateRequestLinks, CreateRequestLinksTitles } from '../../constants/CreateRequest.constants';

import styles from './additionalPassenger.module.scss';

export const AdditionalPassenger = observer(({ disabled }: { disabled: boolean }) => (
  <>
    <p className={styles.stylesPassenger}>Кто пассажир?</p>
    <Form.Item name="passenger">
      <Radio.Group disabled={disabled}>
        <Radio className={styles.radio_date} value="me">{CreateRequestLinksTitles[CreateRequestLinks.iWillGo]}</Radio>
        <Radio className={styles.radio_date} value="notme">{CreateRequestLinksTitles[CreateRequestLinks.coworkerWillGo]}</Radio>
      </Radio.Group>
    </Form.Item>
    <Form.Item
      noStyle={true}
      shouldUpdate={(prevValues, currentValues): boolean => prevValues.passenger !== currentValues.passenger}
    >
      {({ getFieldValue }): JSX.Element | null => {
        return getFieldValue('passenger') === 'notme' ? (
          <Form.Item name="employee">
            <DepartmentEmployeeAutoComplete placeholder={CreateRequestLinksTitles.createForAnotherPlaceholder} />
          </Form.Item>
        ) : null;
      }}
    </Form.Item>
  </>
));
