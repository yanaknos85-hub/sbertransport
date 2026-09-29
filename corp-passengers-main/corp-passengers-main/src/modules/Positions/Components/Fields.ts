import { useMemo } from 'react';
import { useTranslation } from 'i18n';

import { useOrganizations } from 'api/organizations';

import { taxiClassOptions } from 'modules/NewTariffs/constants/Tariffs.constants';
import { ModelFormFieldType, ModelFormFieldProps } from 'shared/models/ModelDetail/ModelFormField';
import { preventDefault } from 'utils';

export const useFields: (isOrgEditable: boolean) => ModelFormFieldProps[] = isOrgEditable => {
  const { t } = useTranslation();
  const { byId: organizationsById } = useOrganizations().data;

  return useMemo<ModelFormFieldProps[]>(
    () => [
      {
        name: 'active',
        fieldType: ModelFormFieldType.BOOLEAN,
        onInputKeyDown: preventDefault,
        required: true,
        editable: true,
        description: t.Positions.Active,
        trueLabel: t.Positions['Active.True'],
        falseLabel: t.Positions['Active.False'],
        placeholder: t.Positions.ActivePlaceholder,
        allowClear: true,
      },
      {
        name: 'humanReadableId',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        required: false,
        editable: isOrgEditable,
        description: t.Positions.PositionId,
        maxLength: 20,
      },
      {
        name: 'positionName',
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        required: true,
        editable: true,
        description: t.Positions.PositionName,
        maxLength: 255,
      },
      {
        name: 'organizationId',
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        editable: isOrgEditable,
        description: t.Positions.Organization,
        options: Object.values(organizationsById).map(({ id, officialName }) => ({ value: id, label: officialName })),
        placeholder: t.Positions.OrganizationPlaceholder,
        allowClear: true,
      },
      {
        name: 'selfApproved',
        fieldType: ModelFormFieldType.BOOLEAN,
        onInputKeyDown: preventDefault,
        editable: true,
        required: true,
        description: t.Positions.SelfApproved,
        trueLabel: t.Positions['SelfApproved.True'],
        falseLabel: t.Positions['SelfApproved.False'],
        placeholder: t.Positions.SelfApprovedPlaceholder,
        allowClear: true,
      },
      {
        name: 'availableClasses',
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        editable: true,
        mode: 'multiple',
        description: t.Positions.AvailableClasses,
        options: taxiClassOptions,
        placeholder: t.Positions.AvailableClassesPlaceholder,
        allowClear: true,
      },
    ],
    [t, organizationsById, isOrgEditable]
  );
};
