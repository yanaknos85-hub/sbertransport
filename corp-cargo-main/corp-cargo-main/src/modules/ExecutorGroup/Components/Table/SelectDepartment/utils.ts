import { LabeledValue } from 'antd/es/select';
import { RequestParams } from '@sber-sbertransport/mf-core';
import { Department } from 'stores/Corporate/Corporate.interface';

export const paramsGetter = (search: string, extraParams?: RequestParams): RequestParams => {
  return { ...(extraParams || {}), humanReadableId: search };
};

export const departmentToOption = (department: Department): LabeledValue => {
  return {
    value: department.id,
    label: department.humanReadableId,
    ...department,
  };
};
