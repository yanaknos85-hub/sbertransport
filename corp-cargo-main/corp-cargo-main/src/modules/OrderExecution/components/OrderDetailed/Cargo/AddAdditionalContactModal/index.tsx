import React, { FC, useEffect, useState } from 'react';
import { Modal, Form, Select, Input, message } from 'antd';
import { useProfile } from 'api/profile';
import { useSearchEmployee } from 'api/employee/search';
import { UUID } from 'utils/io-ts';
import { Employee } from 'stores/Employee/Employee.interface';
import { AddAdditionalContactModalProps } from './types';
import { useTranslation } from 'i18n';
import { ValidationRules } from 'shared/fieldValidationRules';

import styles from './styles.module.scss';

const { Item } = Form;
const { Option } = Select;

export const AddAdditionalContactModal: FC<AddAdditionalContactModalProps> = ({
  visible,
  onClose,
  onConfirm,
  waypointId,
}) => {
  const [form] = Form.useForm();
  const [searchText, setSearchText] = useState('');

  const { organizationId } = useProfile().data;
  const { t } = useTranslation();

  const shouldSearch = searchText.length >= 3;
  const { data: employeeData } = useSearchEmployee(
    shouldSearch ? { fullName: searchText } : {},
    { page: 0, size: 20 },
    organizationId as UUID,
    { enabled: shouldSearch, suspense: false }
  );

  useEffect(() => {
    if (visible) {
      form.resetFields();
      setSearchText('');
    }
  }, [visible, form, waypointId]);

  const handleCancel = () => {
    form.resetFields();
    onClose();
  };

  const handleConfirm = async () => {
    try {
      const values = await form.validateFields();

      // Получаем данные сотрудника из списка
      const employee = employeeData?.content.find(e => e.id === values.employeeId);

      if (employee) {
        try {
          onConfirm([{
            employeeId: employee.id,
            fullName: `${employee.lastName} ${employee.firstName} ${employee.patronymic || ''}`.trim(),
            mobilePhone: employee.mobilePhone || '',
            waypointId: waypointId,
          }]);
          onClose();
        } catch (innerError) {
          console.error('Ошибка при добавлении контакта:', innerError);
          message.error('Произошла ошибка при добавлении контакта');
        }
      }
    } catch (error) {
      console.error('Ошибка валидации формы:', error);
    }
  };

  // Обработчик выбора сотрудника
  const handleSelectEmployee = (value: string) => {
    form.setFieldsValue({ employeeId: value });

    // Получаем данные сотрудника и устанавливаем телефон
    const employee = employeeData?.content.find(e => e.id === value);
    if (employee) {
      form.setFieldsValue({ mobilePhone: employee.mobilePhone || '' });
    }
  };

  // Обработчик поиска
  const handleSearch = (value: string) => {
    setSearchText(value);
  };

  return (
    <Modal
      visible={visible}
      title={t.Monitor.addAdditionalContactTitle}
      onCancel={handleCancel}
      onOk={handleConfirm}
      width={500}
      centered
      okText={t.global.save}
      cancelText={t.global.cancel}
    >
      <Form form={form} layout="vertical" className={styles.addAdditionalContactForm}>
        <Item
          name="employeeId"
          label={t.Monitor.addAdditionalContactEmployeeLabel}
          rules={[{ required: true, message: t.Monitor.addAdditionalContactEmployeeRequired }]}
        >
          <Select
            showSearch
            placeholder={t.Monitor.addAdditionalContactEmployeePlaceholder}
            onSelect={handleSelectEmployee}
            onSearch={handleSearch}
            filterOption={false}
            style={{ width: '100%' }}
          >
            {employeeData?.content.map((employee: Employee) => (
              <Option key={employee.id} value={employee.id}>
                {employee.lastName} {employee.firstName} {employee.patronymic || ''} ({employee.personnelNumber})
              </Option>
            ))}
          </Select>
        </Item>

        <Item
          name="mobilePhone"
          label={t.Monitor.addAdditionalContactPhoneLabel}
          rules={[
            {
              required: true,
              message: t.Monitor.addAdditionalContactPhoneRequired
            },
            ValidationRules.general.checkPhoneMask('Маска ввода должна соответствовать формату +79999999999'),
          ]}
        >
          <Input
            placeholder="+7___ ___ __ __"
          />
        </Item>
      </Form>
    </Modal>
  );
};
