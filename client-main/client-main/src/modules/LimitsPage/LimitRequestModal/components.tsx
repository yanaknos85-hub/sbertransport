import { Form, Select } from 'antd';
import { LabeledValue } from 'antd/es/select';
import { FormInstance } from 'antd/lib/form';
import moment from 'moment';
import React, { Dispatch, FC, SetStateAction } from 'react';
import { observer } from 'mobx-react';

import { DATE_FORMAT } from 'constants/constants.app';

import { ValidationRules } from 'shared/fieldValidationRules';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import { LimitsPage, LimitsPageTitles } from '../Limits.constants';
import { LimitRequestLevel } from './constants';
import { Fields } from './LimitRequestFormController';

interface EditableFormElementProps {
  isEdit: boolean;
  initialRequestValues: any;
}

export const EditableFormElements: FC<EditableFormElementProps> = ({ isEdit, initialRequestValues }) => isEdit ? (
  <>
    <Form.Item name="creationTime" label="Дата создания заявки">
      <span>{moment(initialRequestValues.creationTime).format(DATE_FORMAT.DATE_WITH_TIME)}</span>
    </Form.Item>

    <Form.Item name="humanReadableId" label="ID заявки на лимит">
      <span>{initialRequestValues.humanReadableId}</span>
    </Form.Item>

    <Form.Item name="fullName" label="ФИО">
      <span>{initialRequestValues.fullName}</span>
    </Form.Item>
  </>
) : null;

interface DepartmentLimitSettingsProps {
  type: LIMIT_TYPE;
  formRef: FormInstance;
  updateSiblings: () => void;
  fields: Partial<Fields>;
  depSiblings: LabeledValue[];
}
export const DepartmentLimitSettings: FC<DepartmentLimitSettingsProps> = ({
  formRef,
  updateSiblings,
  fields,
  depSiblings,
  type,
}) => {
  const isDepartmentLimit = type === LIMIT_TYPE.DEPARTMENT;

  return isDepartmentLimit ? (
    <>
      <Form.Item
        name="level"
        label="Запросить у"
        rules={[ValidationRules.general.required]}
        shouldUpdate={true}
      >
        <Select onChange={updateSiblings}>
          <Select.Option value="parent">Вышестоящее подразделение</Select.Option>
          <Select.Option value="siblings">Смежные подразделения</Select.Option>
        </Select>
      </Form.Item>

      {(fields.level === LimitRequestLevel.SIBLINGS
      || formRef.getFieldValue('level') === LimitRequestLevel.SIBLINGS) && (
        /*
            В рамках MVP (TRANSPORT-4151) было принято решение
            отключить возможность запросов на пополнение лимитов через несколько подразделений
          */
        <Form.Item
          name="departments"
          label="Получатели"
          rules={[ValidationRules.general.required]}
          shouldUpdate={true}
        >
          <Select
            showSearch={true}
            allowClear={true}
            options={depSiblings}
          />
        </Form.Item>
      )}
    </>
  ) : null;
};

interface SelectTransportTypesProps {
  serviceType: string;
  restrict?: string[];
  updateSiblings: () => void;
  setTransportType?: Dispatch<SetStateAction<string>>;
}

export const SelectLimitTransportType: FC<SelectTransportTypesProps> = observer(
  ({
    serviceType, restrict, updateSiblings, setTransportType,
  }): JSX.Element => {
    const { [StoreNames.transportTypesStore]: transportTypesStore } = useAppStoreContext();

    const { availableTransportTypesByService, availableTransportTypes } = transportTypesStore;

    const dataTransportTypes = serviceType ? availableTransportTypesByService : availableTransportTypes;

    const transportTypesOptions: LabeledValue[] = (
      restrict ? dataTransportTypes.filter(({ name }) => restrict.includes(name)) : dataTransportTypes
    ).map(({ name, rusName }) => ({ label: rusName, value: name }));

    const handleChange = (type: string): void => {
      if (setTransportType) {
        setTransportType(type);
      }
      updateSiblings();
    };

    return (
      <Form.Item
        name="transportType"
        label={LimitsPageTitles[LimitsPage.transportType]}
        rules={[ValidationRules.general.required]}
      >
        <Select
          placeholder={LimitsPageTitles[LimitsPage.transportType]}
          onChange={handleChange}
          options={transportTypesOptions}
        />
      </Form.Item>
    );
  }
);
