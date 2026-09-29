import React, {
  useState, Dispatch, FC, SetStateAction
} from 'react';
import { observer } from 'mobx-react';
import {
  Form, FormInstance, Row, Col, Select
} from 'antd';
import TextArea from 'antd/es/input/TextArea';
import { LabeledValue } from 'antd/es/select';

import { NumericInput } from 'shared/components/NumericInput';
import { ValidationRules } from 'shared/fieldValidationRules';
import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';

import { DepartmentLimitSettings, EditableFormElements, SelectLimitTransportType } from './components';
import {
  monthArray, periodTitle, reasonTitle, sumTitle
} from './constants';
import { Fields } from './LimitRequestFormController';
import { InitialRequestData } from './useLimitRequestForm';

import styles from './modal.module.scss';

interface Props {
  serviceType: string;
  formRef: FormInstance;
  initialRequestValues?: InitialRequestData;
  depSiblings: LabeledValue[];
  isEdit: boolean;
  limitType: LIMIT_TYPE;
  restrict?: string[];
  setTT?: Dispatch<SetStateAction<string>>;
  updateSiblings: () => void;
}

export const LimitRequestFormContent: FC<Props> = observer(
  ({
    serviceType, formRef, initialRequestValues, isEdit, limitType, updateSiblings, depSiblings, restrict, setTT,
  }) => {
    const [fields, setFields] = useState<Partial<Fields>>({});
    const handleValuesChange = (_: unknown, values: Partial<Fields>): void => setFields(values);

    return (
      <Form
        form={formRef}
        layout="vertical"
        name="create-request"
        size="middle"
        onValuesChange={handleValuesChange}
        initialValues={initialRequestValues}
      >
        <EditableFormElements isEdit={isEdit} initialRequestValues={initialRequestValues} />

        <SelectLimitTransportType
          serviceType={serviceType}
          restrict={restrict}
          updateSiblings={updateSiblings}
          setTransportType={setTT}
        />

        <Row gutter={24}>
          <Col span={12}>
            <Form.Item
              name="period"
              label={periodTitle}
              rules={[ValidationRules.general.required]}
            >
              <Select
                className={styles.periodSelect}
                popupClassName={styles.periodSelectDropdown}
                placeholder={periodTitle}
                onChange={updateSiblings}
                options={monthArray}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              name="sum"
              label={sumTitle}
              rules={[ValidationRules.general.required, ValidationRules.general.maxMoneyValue(1000000000)]}
            >
              <NumericInput
                autoComplete="off"
                onChange={updateSiblings}
                placeholder="Сумма"
              />
            </Form.Item>
          </Col>
        </Row>

        <DepartmentLimitSettings
          type={limitType}
          formRef={formRef}
          updateSiblings={updateSiblings}
          fields={fields}
          depSiblings={depSiblings}
        />

        <Form.Item
          name="description"
          label={reasonTitle}
          rules={[ValidationRules.general.required]}
          shouldUpdate={true}
        >
          <TextArea
            maxLength={250}
            showCount
            placeholder="Оставьте комментарий"
          />
        </Form.Item>
      </Form>
    );
  }
);
