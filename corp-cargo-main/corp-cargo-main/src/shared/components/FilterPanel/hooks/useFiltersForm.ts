import { Dispatch, useCallback } from 'react';
import { useForm, FormInstance } from 'antd/lib/form/Form';

export const useFiltersForm = <T extends Record<string, unknown>>(
  onApplyFilters: Dispatch<T>
): { onFinish: (values?: T) => void; form: FormInstance<T>; resetFields: () => void } => {
  const [form] = useForm<T>();

  const onFinish = useCallback(
    (values: T = form.getFieldsValue(true) as T): void => {
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
