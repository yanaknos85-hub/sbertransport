/* eslint-disable @typescript-eslint/no-explicit-any */

import { Divider, Form, Radio } from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue } from 'antd/es/select';
import { observer } from 'mobx-react';
import React, {
  Dispatch, FC, SetStateAction, useEffect
} from 'react';

import { AvailablePublicTTCompensationType } from 'api/trip-requests';

import { ValidationRules } from 'shared/fieldValidationRules';

import { compensationTypeLabel, compensationTypeTitle } from '../constants';

interface Props {
  field: FormListFieldData;
  compensationType: string;
  setCompensationType: Dispatch<SetStateAction<string>>;
  availablePublicCompensation: AvailablePublicTTCompensationType[];
  index: number;
  resetFields: (index: number, selectedCompensationType: string) => void;
}

export const CompensationTypeSelect: FC<Props> = observer(
  ({
    field, compensationType, setCompensationType, availablePublicCompensation, index, resetFields,
  }) => {
    const [compensations, setCompensations] = React.useState<LabeledValue[]>([]);
    const [compensationTypeRadio, setCompensationTypeRadio] = React.useState<JSX.Element[]>();

    useEffect(() => {
      const res = availablePublicCompensation.map(y => ({
        value: y.name,
        label: y.rusName,
      }));
      setCompensations(res);
    }, [availablePublicCompensation]);

    const getRadio = (array: LabeledValue[]): JSX.Element[] => array.map(item => (
      <Radio key={item.value} value={item.value}>
        {item.label}
      </Radio>
    ));

    useEffect(() => {
      const res = getRadio(compensations);
      setCompensationTypeRadio(res);
    }, [compensations]);

    const compensationTypeChange = (e: any): void => {
      setCompensationType(e.target.value);
      resetFields(index, e.target.value);
    };

    return (
      <>
        <Form.Item
          {...field}
          key={compensationTypeTitle}
          // @ts-ignore
          fieldKey={[field.fieldKey, compensationTypeTitle]}
          name={[field.name, compensationTypeTitle]}
          label={compensationTypeLabel}
          rules={[ValidationRules.general.required]}
          initialValue={compensationType}
        >
          <Radio.Group onChange={(e: any): void => compensationTypeChange(e)}>{compensationTypeRadio}</Radio.Group>
        </Form.Item>
        <Divider />
      </>
    );
  }
);
