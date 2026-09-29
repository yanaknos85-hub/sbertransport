import React, { useState, FC } from 'react';
import { Modal } from 'antd';
import { FormInstance, useForm } from 'antd/lib/form/Form';
import { useTranslation } from 'i18n';

import { FiltersButton } from '../FiltersButton/FiltersButton';
import { resetFormFields } from '../../lib/handlers';
import styles from './ModalFilters.module.scss';

export interface Props<T> {
  setQuery: (dispatcher: Partial<T>) => void;
  query: Partial<T>;
  formJSX: (form: FormInstance, initialValues: T, onFinish: (values: T) => void) => JSX.Element;
}

export const ModalFilters: FC<Props<Record<string, unknown>>> = ({
  setQuery, query, formJSX,
}) => {
  const { t } = useTranslation();
  const [visible, setVisible] = useState(false);
  const [form] = useForm();

  const showModal = () => setVisible(true);
  const hideModal = () => setVisible(false);

  const handleSuccess = () => {
    form.submit();
    hideModal();
  };

  const handleDrop = (e: React.MouseEvent<HTMLElement, MouseEvent>) => {
    const target = e.target as HTMLElement;

    // Сбрасываем фильтры только при нажатии на кнопку сброса
    if (target.innerText === t.global.drop) {
      resetFormFields(form);
      setQuery({});
    }

    hideModal();
  };

  return (
    <>
      <Modal
        title={t.global.filters}
        className={styles.modal}
        visible={visible}
        onCancel={handleDrop}
        onOk={handleSuccess}
        okText={t.global.success}
        cancelText={t.global.drop}
        cancelButtonProps={{ className: styles.cancelButton }}
      >
        {formJSX(form, query, setQuery)}
      </Modal>
      <FiltersButton onClick={showModal} />
    </>
  );
};
