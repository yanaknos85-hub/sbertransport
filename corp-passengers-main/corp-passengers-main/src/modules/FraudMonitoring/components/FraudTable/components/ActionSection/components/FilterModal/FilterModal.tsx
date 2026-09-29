import React, { Suspense } from 'react';
import { Form } from 'antd';
import { useTranslation } from 'i18n';
import { useForm } from 'antd/es/form/Form';

import { Button } from 'shared/components/Button/Button';
import { Modal } from 'shared/components/Modal/Modal';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';

import { FilterFormValues } from '../../types';
import { FilterModalProps } from './types';

import { FilterModalFields } from '../FilterModalFields/FilterModalFields';

import styles from '../../styles.module.scss';

export const FilterModal = ({
  isOpened,
  initialValues,
  onClose: handleClose,
  onReset: handleReset,
  onSubmit: handleSubmit,
}: FilterModalProps) => {
  const { t } = useTranslation();
  const [filterForm] = useForm<FilterFormValues>();

  const handleFilterFormSubmit = (filterFormValues: FilterFormValues) => {
    handleSubmit({ ...filterFormValues });
  };

  const handleApplyFilters = () => {
    filterForm.submit();
    handleClose();
  };

  const ModalFooter = [
    <Button type="link" onClick={handleClose}>
      {t.global.close}
    </Button>,
    <Button
      type="text"
      danger
      onClick={handleReset}
    >
      {t.global.reset}
    </Button>,
    <Button type="primary" onClick={handleApplyFilters}>
      {t.global.search}
    </Button>,
  ];

  return (
    <Modal
      visible={isOpened}
      onCancel={handleClose}
      className={styles.modal}
      centered
      title={t.global.filters}
      footer={ModalFooter}
    >
      <Suspense fallback={<SpinWrapped />}>
        <Form
          form={filterForm}
          onFinish={handleFilterFormSubmit}
          initialValues={initialValues}
        >
          <FilterModalFields filterForm={filterForm} />
        </Form>
      </Suspense>
    </Modal>
  );
};

