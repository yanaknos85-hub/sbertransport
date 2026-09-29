import React from 'react';
import { FormInstance } from 'antd';

import { ModelFormField } from 'shared/models/ModelDetail/ModelFormField';

import { useFilterFields } from '../../hooks/useFIlterFields';

import { FilterFormValues } from '../../types';

export const FilterModalFields = ({ filterForm }: { filterForm: FormInstance<FilterFormValues> }) => {
  const fields = useFilterFields();

  return (
    <>
      {fields.map((props, index) => (
        <ModelFormField
          form={filterForm}
          key={index}
          {...props}
        />
      ))}
    </>
  );
};
