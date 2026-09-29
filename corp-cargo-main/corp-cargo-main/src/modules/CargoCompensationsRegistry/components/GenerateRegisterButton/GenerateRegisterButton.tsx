import React, { FC, useState } from 'react';
import { message, Modal } from 'antd';
import { useTranslation } from 'i18n';
import { useCreateTaskMutation } from 'modules/CargoCompensationsRegistry/hooks/useCreateTaskMutation';
import { isEmptyFilter } from 'modules/CargoCompensationsRegistry/utils';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { ExportButton } from './ExportButton';
import { ColumnVisibilitySettings, TransformedFilterValues } from '../../types';

import styles from './ExportButton.module.scss';

interface GenerateRegisterButtonProps {
  disabled?: boolean;
  filterValues?: TransformedFilterValues;
  columnVisibility?: ColumnVisibilitySettings;
  formatType?: string;
}

export const GenerateRegisterButton: FC<GenerateRegisterButtonProps> = ({ filterValues, disabled }) => {
  const { t } = useTranslation();
  const { logger } = useAppStoreContext();
  const [isVisible, setIsVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const { createTask, isLoading: isCreatingTask } = useCreateTaskMutation();

  const handleOk = async () => {
    if (!filterValues || isEmptyFilter(filterValues)) {
      message.error(t.Forms.RegistryXLSModal.filtersRequired);
      return;
    }

    setIsLoading(true);

    try {
      await createTask(filterValues);
      setIsVisible(false);
    } catch (error) {
      logger.toMessage('error', t.Forms.RegistryXLSModal.importError);
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenModal = () => {
    if (disabled) {
      message.warning(t.Forms.RegistryXLSModal.noDataWarning);
      return;
    }
    setIsVisible(true);
  };

  return (
    <>
      <ExportButton onClick={handleOpenModal} />
      <Modal
        visible={isVisible}
        onOk={handleOk}
        onCancel={() => setIsVisible(false)}
        confirmLoading={isLoading || isCreatingTask}
        okText={t.global.confirm}
        cancelText={t.global.cancel}
      >
        <p className={styles.warning}>{t.Forms.RegistryXLSModal.compensationImportWarning}</p>
        <p>{t.Forms.RegistryXLSModal.proceedImport}</p>
      </Modal>
    </>
  );
};
