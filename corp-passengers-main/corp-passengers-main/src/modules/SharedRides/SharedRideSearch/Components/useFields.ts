import { useMemo } from 'react';
import { useTranslation } from 'i18n';

import { useTransportTypes } from 'api/transport-types';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';

const useTransportTypeOptions = (restrict?: string[]) => {
  const { data: transportTypes } = useTransportTypes();
  const availableTransportTypes = transportTypes.filter(({ name }) => !restrict || restrict.includes(name));
  return availableTransportTypes.map(({ name: value, rusName: label }) => ({ label, value }));
};

export const useFields = (): ModelFormFieldProps[] => {
  const { t } = useTranslation();
  const transportTypeOptions = useTransportTypeOptions();

  return useMemo(
    () => [
      {
        name: 'passengers',
        title: t.Forms.sharedRidesSearch.passengers,
        description: t.Forms.sharedRidesSearch.passengers,
        fieldType: ModelFormFieldType.NUMBER,
        required: true,
        editable: true,
      },
      {
        name: 'tariffId',
        title: t.Forms.sharedRidesSearch.tariffId,
        description: t.Forms.sharedRidesSearch.tariffId,
        fieldType: ModelFormFieldType.SELECT,
        required: true,
        editable: true,
        options: transportTypeOptions,
      },
    ],
    [t, transportTypeOptions]
  );
};
