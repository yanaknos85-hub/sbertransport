import React, { FC, useState, useEffect } from 'react';

import DatePicker from 'antd/lib/date-picker';
import Modal from 'antd/lib/modal/Modal';
import moment, { Moment } from 'moment';

import { useProfile } from 'api/profile/profile.api';
import { useDeleteRowShifts, useDeleteShifts } from 'api/schedule2.0/schedule.api';

import { DATE_FORMAT } from 'constants/app.constants';

import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';

import { useTranslation } from 'i18n';

import Checkbox from 'components/Checkbox/Checkbox';

import { useModals } from '../../../../context/modal.context';
import { useSelectedShift } from '../../../../context/selectedShift.context';

import styles from './DeleteModal.module.scss';

export const DeleteModal: FC = () => {
  const { t } = useTranslation();

  const { contractorId } = useProfile().data;

  const { selectedShift, setSelectedShift } = useSelectedShift();
  const [rowActive, setRowActive] = useState(false);
  const [date, setDate] = useState<Moment | null>(null);

  const [deleteShift, { isLoading: isDeletingOne }] = useDeleteShifts(contractorId);
  const [deleteRowShift, { isLoading: isDeletingRow }] = useDeleteRowShifts(contractorId, selectedShift?.rowId as UUID);

  const { isDeleteOpened, handleClose } = useModals();

  const closModal = () => {
    handleClose();
    setSelectedShift(null);
    setRowActive(false);
    setDate(null);
  };

  useEffect(() => {
    if (selectedShift && !date) {
      setDate(moment(selectedShift.startDate).add(1, 'day'));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedShift]);

  useEffect(() => () => setSelectedShift(null), [setSelectedShift]);

  const handleSuccess = () => {
    closModal();
  };

  const handleDelete = () => {
    if (rowActive) {
      deleteRowShift(date!.format('YYYY-MM-DD')).then(handleSuccess).catch(ignore);
      return;
    }

    deleteShift(selectedShift?.id as UUID).then(handleSuccess).catch(ignore);
  };

  return (
    <Modal
      title={t.Shifts.removeShit}
      visible={isDeleteOpened}
      className={styles.modal}
      onCancel={closModal}
      onOk={handleDelete}
      okButtonProps={{
        disabled: rowActive && !date,
        loading: isDeletingOne || isDeletingRow,
      }}
      cancelText={t.global.cancel}
      okText={t.global.delete}
    >
      <p className={styles.modalDescription}>{t.Shifts.removeShitDescription}</p>
      {selectedShift?.rowId && (
        <Checkbox checked={rowActive} onChange={e => setRowActive(e.target.checked)}>
          Удалить весь последующий ряд смен
        </Checkbox>
      )}
      {rowActive && (
        <div className={styles.date}>
          <span>с даты</span>
          <DatePicker
            value={date}
            className={styles.input}
            format={DATE_FORMAT.BASE_REVERTED_DOTS}
            placeholder="Выберите дату"
            showToday={false}
            showSecond={false}
            clearIcon={null}
            onChange={value => setDate(value!)}
            disabledDate={d => d.isBefore(moment(selectedShift?.startDate).endOf('day'))}
          />
        </div>
      )}
    </Modal>
  );
};
