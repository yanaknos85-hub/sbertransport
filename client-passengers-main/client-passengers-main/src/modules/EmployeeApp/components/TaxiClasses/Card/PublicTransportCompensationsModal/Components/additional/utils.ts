import { FormInstance } from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';

import { tripsInfoTitle } from '../constants';

export const getFieldValue = (form: FormInstance, field: FormListFieldData, fieldTitle: string): number | undefined => {
  const test = form.getFieldValue(tripsInfoTitle);
  const need = test[field.key];
  return need?.[fieldTitle];
};
