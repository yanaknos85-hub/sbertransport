import { useTranslation } from 'i18n';
import { ContractorHandbookTitles, ContractorHandbooksNames } from 'modules/Contractors/Contractors.constants';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { onlyNumbersRegExp, preventDefault } from 'utils';

export const useFilterFields = (): ModelFormFieldProps[] => {
  const { t } = useTranslation();

  return [
    {
      fieldType: ModelFormFieldType.CUSTOM_TEXT,
      name: ContractorHandbooksNames.name,
      label: ContractorHandbookTitles.name,
      editable: true,
      isNewDesign: true,
    },
    {
      fieldType: ModelFormFieldType.TEXT,
      name: ContractorHandbooksNames.tin,
      label: ContractorHandbookTitles.tin,
      onPressEnter: preventDefault,
      editable: true,
      description: t.contractors.tin,
      minLength: 10,
      maxLength: 12,
      rules: [
        {
          pattern: onlyNumbersRegExp,
          message: t.contractors.digitalsMask,
        },
        {
          pattern: /^((\d{10})|(\d{12}))$/,
          message: t.contractors.tinFieldLengthRequired,
        },
      ],
      isNewDesign: true,
    },
    {
      fieldType: ModelFormFieldType.TEXT,
      name: ContractorHandbooksNames.msrn,
      label: ContractorHandbookTitles.msrn,
      onPressEnter: preventDefault,
      description: t.contractors.msrn,
      editable: true,
      maxLength: 15,
      rules: [
        {
          pattern: onlyNumbersRegExp,
          message: t.contractors.digitalsMask,
        },
        {
          pattern: /^((\d{13})|(\d{15}))$/,
          message: t.contractors.msrnFieldLengthRequired,
        },
      ],
      isNewDesign: true,
    },
  ];
};
