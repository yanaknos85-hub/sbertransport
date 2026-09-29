import { useTranslation } from 'i18n';
import { ModelFormFieldType, ModelFormFieldProps } from 'shared/models/ModelDetail/ModelFormField';
import { useMemo } from 'react';
import { preventDefault } from 'utils';

export const useFields: (isEditable: boolean) => ModelFormFieldProps[] = isEditable => {
  const { t } = useTranslation();

  return useMemo<ModelFormFieldProps[]>(() => {
    const fields: ModelFormFieldProps[] = [
      {
        name: 'name',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        required: true,
        editable: true,
        description: t.Forms.Geo.name,
        maxLength: 128,
        rules: [
          {
            required: true,
            message: t.contractors.contractorNameFieldRequired,
          },
        ],
      },
      {
        name: 'code',
        fieldType: ModelFormFieldType.NUMBER,
        onPressEnter: preventDefault,
        editable: true,
        required: true,
        description: t.Forms.Geo.code,
        disabled: !isEditable,
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
  }, [isEditable, t]);
};
