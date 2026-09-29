import { useTranslation } from 'i18n';
import { Form, Input } from 'antd';
import React, { useMemo } from 'react';
import CustomInput from 'shared/components/PhoneMask/inputMask';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { onlyNumbersRegExp, preventDefault, settingsPhoneNumber } from 'utils';

export const useFields: (isEditable: boolean, isStatusActive: boolean) => ModelFormFieldProps[] = (
  isEditable,
  isStatusActive
) => {
  const { t } = useTranslation();

  return useMemo<ModelFormFieldProps[]>(() => {
    const fields: ModelFormFieldProps[] = [
      {
        name: 'organizationCode',
        fieldType: ModelFormFieldType.NUMBER,
        onPressEnter: preventDefault,
        editable: true,
        description: t.Forms.organizationDetailedForm.organizationalUnitCode,
        maxLength: 4,
        rules: [
          {
            pattern: /^\d+$/,
            message: t.Organizations.onlyDigits,
          },
        ],
      },
      {
        name: 'officialName',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        description: t.Forms.organizationDetailedForm.officialName,
        maxLength: 128,
        rules: [
          {
            required: true,
            message: t.Organizations.fieldRequired,
          },
        ],
      },
      {
        name: 'address',
        // TODO: добавить автокомплит
        // fieldType: ModelFormFieldType.ADDRESS_AUTO_COMPLETE,
        fieldType: ModelFormFieldType.TEXT,
        editable: true,
        description: t.Forms.organizationDetailedForm.address,
        disabled: !isEditable,
        rules: [
          {
            required: true,
            message: t.Organizations.fieldRequired,
          },
        ],
      },
      {
        fieldType: ModelFormFieldType.CUSTOM,
        name: 'phone',
        description: t.Contacts.PHONE,
        component: () => (
          <Form.Item name="phone">
            <CustomInput {...settingsPhoneNumber.input} />
          </Form.Item>
        ),
        editable: true,
      },
      {
        name: 'email',
        fieldType: ModelFormFieldType.EMAIL,
        editable: true,
        autoComplete: 'false',
        onPressEnter: preventDefault,
        required: false,
        description: t.Contacts.EMAIL,
        disabled: !isEditable,
      },
      {
        name: 'site',
        disabled: !isEditable,
        fieldType: ModelFormFieldType.CUSTOM,
        description: t.Contacts.SITE,
        component: () => (
          <Form.Item name="site">
            <Input />
          </Form.Item>
        ),
        editable: true,
      },
      {
        name: 'msrn',
        fieldType: ModelFormFieldType.NUMBER,
        onPressEnter: preventDefault,
        required: true,
        editable: true,
        description: t.Forms.organizationDetailedForm.msrn,
        maxLength: 15,
        rules: [
          {
            pattern: onlyNumbersRegExp,
            message: t.Organizations.digitalsMask,
          },
          {
            required: true,
            message: t.Organizations.fieldRequired,
          },
          {
            pattern: /^((\d{13})|(\d{15}))$/,
            message: t.contractors.msrnFieldLengthRequired,
          },
        ],
      },
      {
        name: 'tid',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        required: false,
        editable: true,
        description: t.Forms.organizationDetailedForm.tid,
        maxLength: 12,
        rules: [
          {
            pattern: onlyNumbersRegExp,
            message: t.Organizations.digitalsMask,
          },
          {
            required: true,
            message: t.Organizations.fieldRequired,
          },
          {
            pattern: /^((\d{10})|(\d{12}))$/,
            message: t.Organizations.tinFieldLengthRequired,
          },
        ],
      },
      {
        name: 'status',
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        options: [
          { value: 'ACTIVE', label: 'Активна' },
          { value: 'INACTIVE', label: 'Не активна' },
        ],
        editable: true,
        description: t.Forms.organizationDetailedForm.status,
        disabled: true,
        defaultValue: isStatusActive ? 'ACTIVE' : 'INACTIVE',
      },
      {
        name: 'easupId',
        fieldType: ModelFormFieldType.TEXT,
        editable: true,
        autoComplete: 'false',
        onPressEnter: preventDefault,
        description: t.Forms.organizationDetailedForm.easupId,
        disabled: !isEditable,
        rules: [
          {
            pattern: onlyNumbersRegExp,
            message: t.Organizations.digitalsMask,
          },
        ],
      },
    ];

    const idField: ModelFormFieldProps = {
      name: 'id',
      fieldType: ModelFormFieldType.TEXT,
      onPressEnter: preventDefault,
      required: false,
      editable: true,
      disabled: !isEditable,
      description: t.Forms.organizationDetailedForm.id,
      maxLength: 128,
    };

    !isEditable && fields.unshift(idField);
    return fields;
  }, [isEditable, t, isStatusActive]);
};
