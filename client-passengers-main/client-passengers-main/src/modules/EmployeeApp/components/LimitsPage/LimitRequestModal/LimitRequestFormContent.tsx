
import { Form, FormInstance, Select } from 'antd';
import TextArea from 'antd/es/input/TextArea';
import { LabeledValue } from 'antd/es/select';
import React, { Dispatch, FC, SetStateAction } from 'react';

import { RUBLE_SIGN } from 'constants/constants.app';

import { NumericInput } from 'shared/components/NumericInput';
import { ValidationRules } from 'shared/fieldValidationRules';
import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';

import { DepartmentLimitSettings, EditableFormElements, SelectLimitTransportType } from './components';
import {
  monthArray, periodTitle, reasonTitle, sumTitle
} from './constants';
import { Fields } from './LimitRequestFormController';
import { InitialRequestData } from './useLimitRequestForm';

interface Props {
  formRef: FormInstance;
  initialRequestValues?: InitialRequestData;

  depSiblings: LabeledValue[];
  updateSiblings: () => void;

  isEdit: boolean;
  limitType: LIMIT_TYPE;

  setTT?: Dispatch<SetStateAction<string>>;
}

export const LimitRequestFormContent: FC<Props> = ({
  formRef,
  initialRequestValues,
  isEdit,
  limitType,
  updateSiblings,
  depSiblings,
  setTT,
}) => {
  const [fields, setFields] = React.useState<Partial<Fields>>({});
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleValuesChange = (_: any, values: Partial<Fields>): void => setFields(values);

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

      <SelectLimitTransportType updateSiblings={updateSiblings} setTransportType={setTT} />

      <Form.Item
        name="period"
        label={periodTitle}
        rules={[ValidationRules.general.required]}
      >
        <Select
          placeholder={periodTitle}
          onChange={updateSiblings}
          options={monthArray}
        />
      </Form.Item>

      <Form.Item
        name="sum"
        label={sumTitle}
        rules={[ValidationRules.general.required, ValidationRules.general.maxMoneyValue(1000000000)]}
      >
        <NumericInput
          autoComplete="off"
          suffix={RUBLE_SIGN}
          onChange={updateSiblings}
        />
      </Form.Item>

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
        <TextArea maxLength={250} />
      </Form.Item>
    </Form>
  );
};
