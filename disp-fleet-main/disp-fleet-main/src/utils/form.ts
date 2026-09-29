import { FormInstance } from 'antd/lib/form';

/**
 * Устанавливает все поля формы в undefined (сбрасывает значения)
 * @param form - экземпляр формы Ant Design
 * @returns объект с undefined значениями для всех полей формы
 */
export const getEmptyFieldValues = (form: FormInstance) => {
  const formValues = form.getFieldsValue();

  return Object.keys(formValues).reduce((acc, fieldName) => {
    acc[fieldName] = undefined;
    return acc;
  }, {});
};
