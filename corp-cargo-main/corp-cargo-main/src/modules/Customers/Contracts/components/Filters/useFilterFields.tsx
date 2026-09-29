import { useMemo } from 'react';
import { useTranslation } from 'i18n';
import { useParams } from 'react-router-dom';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { useFields } from '../../hooks/useFields';
import { ContractTypes } from 'constants/constants.app';

export const useFilterFields = (modalStateType: string) => {
  const { t } = useTranslation();
  const excludedFields = ['contractNumber', 'vatValue', 'nds', 'sum', 'uvhd'];
  const formIncomeFields = useFields({
    modalStateType, regionIds: [],
  })
    .incomeFields.filter(field => !excludedFields.includes(field.name));

  const formOutcomeFields = useFields({
    modalStateType, regionIds: [],
  })
    .outcomeFields.filter(field => !excludedFields.includes(field.name));

  const { contractType } = useParams<{ contractType: ContractTypes }>();

  return useMemo<ModelFormFieldProps[]>(
    () => [
      ...(contractType === ContractTypes.INCOME ? formIncomeFields : formOutcomeFields).map(field => ({
        ...field,
        required: false,
        allowClear: true,
        allowEmpty: [true, true] as [boolean, boolean],
      })),
      {
        fieldType: ModelFormFieldType.SELECT,
        name: 'active',
        label: t.Contracts.active,
        allowClear: true,
        editable: true,
        options: [
          {
            label: t.Contracts.activated,
            value: 'true',
          },
          {
            label: t.Contracts.deactivated,
            value: 'false',
          },
        ],
        isNewDesign: true,
      },
    ],
    [t, formIncomeFields, formOutcomeFields, contractType]
  );
};
