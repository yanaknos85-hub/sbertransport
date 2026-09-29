/* eslint-disable @typescript-eslint/no-explicit-any */
import { Dispatch, useCallback } from 'react';
import { useForm, FormInstance } from 'antd/lib/form/Form';

export const useFiltersForm = (
  onApplyFilters: Dispatch<any>
): { onFinish: (values?: any) => void; form: FormInstance<any>; resetFields: () => void } => {
  const [form] = useForm();

  const onFinish = useCallback(
    (values: any = form.getFieldsValue(true)): void => {
      onApplyFilters(values);
    },
    [onApplyFilters, form]
  );

  const resetFields = useCallback(() => {
    form.resetFields();
    onFinish();
  }, [form, onFinish]);

  return {
    onFinish, form, resetFields,
  };
};
