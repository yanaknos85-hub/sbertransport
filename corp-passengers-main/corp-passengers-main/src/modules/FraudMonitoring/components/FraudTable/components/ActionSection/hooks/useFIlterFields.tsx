import React, { useMemo } from 'react';
import { Form } from 'antd';
import { useTranslation } from 'i18n';

import { useProfile } from 'api/profile';
import { useActiveTripPurposes } from 'api/purposes';
import { useGetAvailableTransportTypes } from 'api/transport-types';

import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { ValidationRules } from 'shared/fieldValidationRules';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';

import { convertPurposesToOptions } from 'utils/purposesOptions';
import { convertTransportTypesToOptions } from '../utils';

import { formValues, selectTransportTypes } from '../constants';

import styles from '../styles.module.scss';

export const useFilterFields = () => {
  const {
    t: {
      Forms: { fraudFilterFields: fraudFilterFieldsLables },
    },
  } = useTranslation();

  const { organizationId } = useProfile().data;
  const { purposes } = useActiveTripPurposes(organizationId).data;
  const transportTypes = useGetAvailableTransportTypes(organizationId).data;

  const purposesOptions = useMemo(() => convertPurposesToOptions(purposes), [purposes]);
  const transportTypesOptions = useMemo(
    () => convertTransportTypesToOptions(transportTypes, selectTransportTypes),
    [transportTypes]
  );

  const fields = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        name: formValues.transportType,
        label: fraudFilterFieldsLables.transportType,
        fieldType: ModelFormFieldType.SELECT,
        mode: 'multiple',
        editable: true,
        showArrow: true,
        allowClear: true,
        isNewDesign: true,
        className: styles.selectField,
        placeholder: fraudFilterFieldsLables.transportType,
        options: transportTypesOptions,
      },
      {
        name: formValues.passengerName,
        label: fraudFilterFieldsLables.passengerName,
        fieldType: ModelFormFieldType.TEXT,
        editable: true,
        allowClear: true,
        rules: [ValidationRules.general.minMaxLength(3, 50)],
        className: styles.textField,
        placeholder: fraudFilterFieldsLables.passengerName,
      },
      {
        name: formValues.tripDate,
        fieldType: ModelFormFieldType.CUSTOM,
        editable: true,
        component: () => (
          <Form.Item
            name={formValues.tripDate}
            label={fraudFilterFieldsLables.tripDate}
            className={styles.dateField}
          >
            <NewDateInput
              quarterDisabled
              withAvialableFuture
              placeholder={fraudFilterFieldsLables.tripDate}
            />
          </Form.Item>
        ),
      },
      {
        name: formValues.approverName,
        label: fraudFilterFieldsLables.approverName,
        fieldType: ModelFormFieldType.TEXT,
        editable: true,
        allowClear: true,
        rules: [ValidationRules.general.minMaxLength(3, 50)],
        className: styles.textField,
        placeholder: fraudFilterFieldsLables.approverName,
      },
      {
        name: formValues.approveDate,
        fieldType: ModelFormFieldType.CUSTOM,
        editable: true,
        component: () => (
          <Form.Item
            name={formValues.approveDate}
            label={fraudFilterFieldsLables.approveDate}
            className={styles.dateField}
          >
            <NewDateInput
              quarterDisabled
              withAvialableFuture
              placeholder={fraudFilterFieldsLables.approveDate}
            />
          </Form.Item>
        ),
      },
      {
        name: formValues.purposes,
        label: fraudFilterFieldsLables.purpose,
        fieldType: ModelFormFieldType.SELECT,
        mode: 'multiple',
        editable: true,
        showArrow: true,
        allowClear: true,
        isNewDesign: true,
        className: styles.selectField,
        placeholder: fraudFilterFieldsLables.purpose,
        options: purposesOptions,
      },
    ],
    [purposesOptions, fraudFilterFieldsLables, transportTypesOptions]
  );

  return fields;
};
