import React, { FC, useEffect, useState } from 'react';
import { Form, Radio } from 'antd';

import { PlusOutlined } from '@ant-design/icons';
import { useForm } from 'antd/es/form/Form';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useTranslation } from 'i18n';
import { preventDefault } from 'utils';
import { Button } from 'shared/components/Button/Button';
import Input from 'shared/components/Inputs/Input/Input';
import {
  conditionSelectOptions,
  TripPurposesHandbookTexts,
  TripPurposesHandbookTextsCyrillic,
  weekdaysSelectOptions
} from '../constants/TripPurposes.constants';
import { InnerTripPurposeFormFields } from './InnerTripPurposeFormFields';
import { addCondition as addNewCondition, fillFormOnUpdate } from '../utils/utils';

import { useFormValues } from '../hooks/useFormValues';
import { useFormActions } from '../hooks/useFormActions';
import { useFormOptions } from '../hooks/useFormOptions';

import styles from '../styles/TripPurposeForm.module.scss';

export const TripPurposeForm: FC<{ handleClose?: () => void; id?: string }> = ({ id }) => {
  const {
    t: {
      global,
      SettingsTripRules: {
        tripPurposes: { updatePurposeForm },
      },
    },
  } = useTranslation();
  const [form] = useForm();

  const isCreateNewPurpose = !id || id === 'adding';
  const [isAllValid, setIsAllValid] = useState(!isCreateNewPurpose);

  const formTitle = isCreateNewPurpose ? updatePurposeForm.titleCreate : updatePurposeForm.titleUpdate;

  const {
    initialConditions,
    initialName,
    purposeId,
    conditions,
    setConditions,
    organizationId,
    purposeType,
  } = useFormValues(id);

  const { attributeOptions } = useFormOptions({ organizationId });

  const {
    busy, goBack, onFinish,
  } = useFormActions({
    form, organizationId, purposeId,
  });

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleChange: React.FormEventHandler<HTMLFormElement> = (e: any) => {
    const requiredFieldNames = ['name'];
    const isRequiredTouched = form.isFieldsTouched(requiredFieldNames);
    const hasErrors = form.getFieldsError().filter(({ errors }) => errors.length).length === 0;
    const isRequiredInvalid = e.target.value.length < 1 && e.target.required;

    setIsAllValid(isRequiredTouched && hasErrors && !isRequiredInvalid);
  };

  useEffect(() => purposeId && fillFormOnUpdate(form, initialName || '', initialConditions, purposeType || ''), [
    form,
    initialName,
    initialConditions,
    purposeId,
    purposeType,
  ]);

  const renderFormItems = () => (
    <>
      <Form.Item
        className={styles.nameField}
        label={TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.name]}
        name="name"
        rules={[{ required: true, message: TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.nameRequired] }]}
      >
        <Input
          maxLength={128}
          onPressEnter={preventDefault}
          required
        />
      </Form.Item>
      <Form.Item
        className={styles.nameField}
        label={TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.targetType]}
        name="purposeType"
      >
        <Radio.Group
          name="purposeType"
          defaultValue={purposeType ?? 'CORPORATE'}
          onChange={e => {
            form.setFieldsValue({ purposeType: e.target.value });
          }}
        >
          <Radio.Button value="CORPORATE">{updatePurposeForm.purposeTypeCorporate}</Radio.Button>
          <Radio.Button value="PERSONAL" disabled>
            {updatePurposeForm.purposeTypePersonal}
          </Radio.Button>
        </Radio.Group>
      </Form.Item>
    </>
  );

  const renderConditions = () => (
    <div className={styles.conditionSections}>
      {conditions.map(condition => (
        <InnerTripPurposeFormFields
          key={condition.indexNo}
          index={condition.indexNo}
          conditionSelectOptions={conditionSelectOptions}
          weekdaysSelectOptions={weekdaysSelectOptions}
          attributeOptions={attributeOptions}
          innerFormInitialValues={condition}
          onDelete={() => setConditions(conditions.filter(({ indexNo }) => indexNo !== condition.indexNo))}
        />
      ))}
    </div>
  );

  const renderAddConditionButton = () => (
    <div className={styles.addButtonWrapper}>
      <Button
        type="default"
        icon={<PlusOutlined />}
        onClick={() => setConditions(addNewCondition(conditions))}
        className={styles.addButton}
      >
        {TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.addCondition]}
      </Button>
    </div>
  );

  const renderSubmitButtons = () => (
    <div className={styles.mainButtonsWrapper}>
      <Button
        className={styles.cancelButton}
        size="middle"
        onClick={goBack}
      >
        {global.cancel}
      </Button>
      <Button
        disabled={!isAllValid}
        size="middle"
        onClick={form.submit}
        className={styles.saveButton}
      >
        {global.save}
      </Button>
    </div>
  );

  return (
    <div className={styles.mainWrapper}>
      <div className={styles.formTitle}>{formTitle}</div>
      {isCreateNewPurpose && <div className={styles.formSubtitle}>{updatePurposeForm.subtitleCreate}</div>}
      <Form
        form={form}
        className={styles.formWrapper}
        onFinish={onFinish}
        onChange={handleChange}
        layout="vertical"
      >
        {renderFormItems()}
        {renderConditions()}
        {renderAddConditionButton()}
        {renderSubmitButtons()}
      </Form>
      {busy && <SpinWrapped mask />}
    </div>
  );
};
