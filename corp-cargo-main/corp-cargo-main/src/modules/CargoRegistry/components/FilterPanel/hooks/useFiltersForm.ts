import { Dispatch, useCallback } from 'react';
import { useForm, FormInstance } from 'antd/lib/form/Form';

// Generic тип для значений формы
export const useFiltersForm = <T extends Record<string, unknown>>(
  onApplyFilters: Dispatch<T>,
  onClearButtonActivator?: (event: T) => void
): {
  onFinish: (values?: T) => void;
  form: FormInstance<T>;
  resetFields: () => void;
} => {
  const [form] = useForm<T>();

  const onFinish = useCallback(
    (values: T = form.getFieldsValue(true) as T): void => {
      onApplyFilters(values);
    },
    [onApplyFilters, form]
  );

  const resetFields = useCallback(() => {
    form.resetFields();
    if (onClearButtonActivator) {
      // Получаем значения полей после сброса и передаём их
      const values = form.getFieldsValue(true) as T;
      onClearButtonActivator(values);
    }
    onFinish();
  }, [form, onFinish, onClearButtonActivator]);

  return {
    onFinish,
    form,
    resetFields,
  };
};
