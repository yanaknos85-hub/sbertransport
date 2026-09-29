import { Modal } from 'antd';
import { useTranslation } from 'i18n';
import React, { FC } from 'react';

import Checkbox from 'shared/form/Checkbox/Checkbox';

import styles from './ModalCheckbox.module.scss';

interface Props {
  title: string;
  description: string;
  isModalVisible: boolean;
  closeModal: () => void;
  saveNew: () => void;
  formActive: Set<string>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  toggleForm: (item: any) => void;
  checkboxes: {
    id: string;
    titleKey: string;
  }[];
  translationModuleKey: 'HotButtons' | 'Widgets';
}

export const ModalCheckbox: FC<Props> = ({
  title,
  description,
  isModalVisible,
  closeModal,
  saveNew,
  formActive,
  toggleForm,
  translationModuleKey,
  checkboxes,
}) => {
  const { t } = useTranslation();
  const translationModule = t[translationModuleKey];

  return (
    <Modal
      className={styles.modal}
      title={title}
      visible={isModalVisible}
      onCancel={closeModal}
      destroyOnClose
      okText={translationModule.Modal.ok}
      cancelText={translationModule.Modal.cancel}
      onOk={saveNew}
    >
      <p className={styles.modalDescription}>{description}</p>

      <div className={styles.checkboxes}>
        {checkboxes.map(({ id, titleKey }) => (
          <div className={styles.checkbox} key={id}>
            <Checkbox
              className={styles.checkboxMark}
              id={id}
              checked={formActive.has(id)}
              onChange={() => toggleForm(id)}
            />
            <label className={styles.checkboxLabel} htmlFor={id}>
              {translationModule[titleKey as keyof typeof translationModule]}
            </label>
          </div>
        ))}
      </div>
    </Modal>
  );
};
