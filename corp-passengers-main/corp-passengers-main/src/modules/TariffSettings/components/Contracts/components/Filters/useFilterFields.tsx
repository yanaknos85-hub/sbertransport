import { useMemo } from 'react';
import { useTranslation } from 'i18n';

import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { useFields } from '../../hooks/useFields';

export const useFilterFields = (modalStateType: string) => {
  const { t } = useTranslation();
  const excludedFields = ['contractNumber', 'vatValue', 'nds', 'sum', 'uvhd'];
  const formFields = useFields({
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    vatChecked: true, setVatChecked: () => {}, modalStateType,
  })
    .fields.filter(field => !excludedFields.includes(field.name));

  return useMemo<ModelFormFieldProps[]>(
    () => [
      ...formFields.map(field => ({
        ...field,
        required: false,
        allowClear: true,
        allowempty: [true, true] as [boolean, boolean],
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
    [t, formFields]
  );
};
