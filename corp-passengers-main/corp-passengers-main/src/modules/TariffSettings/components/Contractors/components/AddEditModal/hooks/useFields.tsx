import { useTranslation } from 'i18n';

import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { onlyNumbersRegExp, preventDefault } from 'utils';

import { ValidationRules } from 'shared/fieldValidationRules';
import CustomInput from 'shared/components/PhoneMask/inputMask';
import React, { useMemo } from 'react';
import { FormItem } from 'shared/components/FormItem';

export const useFields = (isEditable: boolean, isPasswordProtected: boolean) => {
  const { t } = useTranslation();

  const fields = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        name: 'name',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        label: t.contractors.name,
        maxLength: 128,
        rules: [
          {
            required: true,
            message: t.contractors.contractorNameFieldRequired,
          },
        ],
        isNewDesign: true,
      },
      {
        name: 'tin',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        label: t.contractors.tin,
        disabled: !isEditable,
        maxLength: 12,
        rules: [
          {
            pattern: onlyNumbersRegExp,
            message: t.contractors.digitalsMask,
          },
          {
            required: true,
            message: t.contractors.tinFieldRequired,
          },
          {
            pattern: /^((\d{10})|(\d{12}))$/,
            message: t.contractors.tinFieldLengthRequired,
          },
        ],
        isNewDesign: true,
      },
      {
        name: 'msrn',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        label: t.contractors.msrn,
        disabled: !isEditable,
        maxLength: 15,
        rules: [
          {
            pattern: onlyNumbersRegExp,
            message: t.contractors.digitalsMask,
          },
          {
            required: true,
            message: t.contractors.msrnFieldRequired,
          },
          {
            pattern: /^((\d{13})|(\d{15}))$/,
            message: t.contractors.msrnFieldLengthRequired,
          },
        ],
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.CUSTOM,
        name: 'contactPersonPhone',
        component: () => (
          <FormItem
            name="contactPersonPhone"
            label={t.contractors.contactPersonPhone}
            rules={[ValidationRules.general.checkPhoneMask()]}
            style={{ display: 'block' }}
          >
            <CustomInput
              mask="+7 (999) 999-99-99"
              placeholder="+7 (___) ___-__-__"
              isNewDesign
            />
          </FormItem>
        ),
        editable: true,
      },
      {
        name: 'contactPersonInfo',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        label: t.contractors.contactPersonInfo,
        maxLength: 250,
        isNewDesign: true,
      },
    ],
    [t, isEditable]
  );

  const dispatcherFields = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        name: 'dispatcherFirstName',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        label: t.contractors.dispatcherFirstName,
        header: t.contractors.dispatcherTitle,
        noHeaderPadding: true,
        maxLength: 128,
        rules: [
          {
            required: true,
            message: t.contractors.dispatcherFirstNameFieldRequired,
          },
        ],
        isNewDesign: true,
      },
      {
        name: 'dispatcherLastName',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        label: t.contractors.dispatcherLastName,
        maxLength: 128,
        rules: [
          {
            required: true,
            message: t.contractors.dispatcherLastNameFieldRequired,
          },
        ],
        isNewDesign: true,
      },
      {
        name: 'dispatcherPatronymic',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        label: t.contractors.dispatcherPatronymic,
        maxLength: 128,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.CUSTOM,
        name: 'dispatcherPhone',
        component: () => (
          <FormItem
            name="dispatcherPhone"
            label={t.contractors.dispatcherPhone}
            required={false}
            rules={[
              ValidationRules.general.checkPhoneMask(),
              {
                required: true,
                message: t.contractors.dispatcherPhoneFieldRequired,
              },
            ]}
          >
            <CustomInput
              mask="+7 (999) 999-99-99"
              placeholder="+7 (___) ___-__-__"
              isNewDesign
            />
          </FormItem>
        ),
        editable: true,
        isNewDesign: true,
      },
      {
        name: 'dispatcherEmail',
        fieldType: ModelFormFieldType.EMAIL,
        onPressEnter: preventDefault,
        editable: true,
        label: t.contractors.dispatcherEmail,
        maxLength: 250,
        rules: [
          {
            required: true,
            message: t.contractors.dispatcherEmailFieldRequired,
          },
        ],
        isNewDesign: true,
      },
    ],
    [t]
  );

  const XMLFields = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        name: 'contractorName',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        label: t.contractors.contractorName,
        maxLength: 250,
        header: t.contractors.integrationInstruction,
        rules: [
          {
            required: true,
            message: t.contractors.contractorLatinNameRequired,
          },
        ],
        isNewDesign: true,
      },
      {
        name: 'contractorRusName',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        label: t.contractors.contractorRusName,
        maxLength: 250,
        rules: [
          {
            required: true,
            message: t.contractors.contractorRusNameRequired,
          },
        ],
        isNewDesign: true,
      },
      {
        name: 'integrationEmail',
        fieldType: ModelFormFieldType.EMAIL,
        onPressEnter: preventDefault,
        editable: true,
        label: t.contractors.integrationEmail,
        maxLength: 250,
        rules: [
          {
            required: true,
            message: t.contractors.integrationEmailRequired,
          },
          {
            type: 'email',
            message: t.contractors.integrationEmailError,
          },
        ],
        isNewDesign: true,
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, isEditable]
  );

  const APIFields = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        name: 'APIUrl',
        fieldType: ModelFormFieldType.URL,
        onPressEnter: preventDefault,
        editable: true,
        label: 'URL',
        maxLength: 250,
        header: t.contractors.APIIntegration,
        rules: [
          {
            required: true,
            message: t.contractors.URLRequired,
          },
        ],
        isNewDesign: true,
      },
      {
        name: 'APILogin',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        editable: true,
        label: t.contractors.login,
        maxLength: 250,
        rules: [
          {
            required: true,
            message: t.contractors.loginRequired,
          },
        ],
        isNewDesign: true,
      },
      {
        name: 'APIPassword',
        fieldType: ModelFormFieldType.PASSWORD,
        onPressEnter: preventDefault,
        allowURL: true,
        editable: true,
        label: t.contractors.password,
        placeholder: isPasswordProtected ? t.contractors.newPasswordPlaceholder : '',
        rules: [
          {
            required: !isPasswordProtected,
            message: t.contractors.passwordRequired,
          },
        ],
        isNewDesign: true,
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, isEditable, isPasswordProtected]
  );

  return {
    fields,
    dispatcherFields,
    XMLFields,
    APIFields,
  };
};
