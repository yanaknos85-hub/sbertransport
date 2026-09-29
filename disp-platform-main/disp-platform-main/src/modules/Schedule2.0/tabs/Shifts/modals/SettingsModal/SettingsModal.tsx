import React, { FC, useEffect, useState } from 'react';

import { Modal, InputNumber } from 'antd';

import { useTranslation } from 'i18n';

import { useModals } from 'modules/Schedule2.0/context/modal.context';
import { useWorkload } from 'modules/Schedule2.0/tabs/Shifts/context/analyticsWorkload.context';
import { useEstimatedHours } from 'modules/Schedule2.0/tabs/Shifts/context/estimatedHours.context';

import Checkbox from 'components/Checkbox/Checkbox';

import styles from './SettingsModal.module.scss';

export const SettingsModal: FC = () => {
  const { t } = useTranslation();
  const { isSettingsOpened, handleClose } = useModals();

  const { estimatedHours, setEstimatedHours } = useEstimatedHours();

  const { visibleVehicleWorkload, setVisibleVehicleWorkload } = useWorkload();

  const [duration, setDuration] = useState(estimatedHours);
  const [visible, setVisible] = useState(visibleVehicleWorkload);

  useEffect(() => {
    setDuration(estimatedHours);
  }, [estimatedHours]);

  const handleSave = () => {
    setEstimatedHours(duration);
    setVisibleVehicleWorkload(visible);
    handleClose();
  };

  const handleChange = (value: number | null) => {
    if (value && value > 0 && value <= 24) {
      setDuration(value);
    }
  };

  return (
    <Modal
      title={t.global.settings}
      visible={isSettingsOpened}
      className={styles.modal}
      cancelText={t.global.cancel}
      okText={t.global.save}
      cancelButtonProps={{ className: styles.cancelButton }}
      onCancel={handleClose}
      onOk={handleSave}
    >
      <p className={styles.modalDescription}>
        {t.Shifts.estimatedHours}
      </p>
      <InputNumber value={duration} onChange={handleChange} />

      <Checkbox checked={visible} onChange={e => setVisible(e.target.checked)}>
        <span>Отображать загруженность автомобилей</span>
      </Checkbox>
    </Modal>
  );
};
