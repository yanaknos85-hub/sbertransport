import React, { FC, useState, useEffect } from 'react';
import { Modal } from '@sber-sbertransport/ui-kit/src';
import { Spin } from 'antd';
import { useTranslation } from 'i18n';
import { Button } from 'components/Button';
import { ReactComponent as SuccessIcon } from 'assets/icons/success.svg';
import { ReactComponent as AttentionIcon } from 'assets/icons/attention.svg';

import { TEwbShiftResponse } from 'api/shifts/shifts.types';
import { useEwbWebsocket } from 'api/shifts/shifts.api';
import { TMassCreateFirstTitleItem } from 'api/shifts/shifts.types';
import { Shift } from 'api/shifts/shifts.types';
import styles from './Results.module.scss';

interface Props {
  visible: boolean;
  firstTitleResult: TMassCreateFirstTitleItem[] | undefined;
  selectedShifts: Shift[];
  onClose: () => void;
}

const Results: FC<Props> = ({
  visible,
  firstTitleResult,
  selectedShifts,
  onClose,
}) => {
  const { modal } = useTranslation().t.Waybill;
  const [results, setResults] = useState<TEwbShiftResponse[]>([]);
  const [copied, setCopied] = useState(false);

  const { lastMessage } = useEwbWebsocket();

  const isSuccess = results.every(r => r.success);
  const hasErrors = results.some(r => !r.success);
  const errors = results.filter(r => !r.success);

  const handleCopy = () => {
    const tableData = errors.map(error => {
      const shift = selectedShifts.find(s => s.id === error.shiftId);
      return `${shift?.vehicleStateNumber || ''}\t${error.errorText || 'Неизвестная ошибка'}`;
    });

    const header = 'Госномер\tОшибка';
    const tableText = [header, ...tableData].join('\n');

    navigator.clipboard.writeText(tableText).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    });
  };

  const isComplete = firstTitleResult
    ? results.length === firstTitleResult.length
    : false;

  useEffect(() => {
    if (lastMessage && lastMessage.data && visible) {
      setResults(prev => {
        const existingShiftIds = prev.map(r => r.shiftId);
        if (!existingShiftIds.includes(lastMessage.data.shiftId)) {
          return [...prev, lastMessage.data];
        }
        return prev;
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lastMessage, visible]);

  return (
    <Modal
      open={visible}
      closable={false}
      footer={(
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button type="primary" onClick={onClose}>
            {modal.okText}
          </Button>
        </div>
      )}
      destroyOnClose
      title={modal.resultsTitleCreate}
      width={567}
    >
      <div className={styles.resultsWrapper}>
        {!isComplete && (
          <div className={styles.spinWrapper}>
            <Spin />
          </div>
        )}

        <div className={`${styles.statusBox} ${isSuccess ? styles.success : styles.error}`}>
          <div className={styles.statusContent}>
            {isSuccess ? (
              <SuccessIcon className={styles.statusIcon} />
            ) : (
              <AttentionIcon className={styles.statusIcon} />
            )}
            <p className={styles.statusText}>
              Создано ЭПЛ: {results.filter(r => r.success).length} из {firstTitleResult?.length}
            </p>
          </div>
        </div>

        {hasErrors && (
          <table className={styles.errorsTable}>
            <thead>
              <tr>
                <th className={styles.columnStateNumber}>Госномер</th>
                <th>Ошибка</th>
                <th className={styles.columnActions}>
                  {isComplete && (
                    <div
                      className={styles.copyButton}
                      onClick={handleCopy}
                    >
                      {copied ? 'Скопировано' : 'Копировать'}
                    </div>
                  )}
                </th>
              </tr>
            </thead>
            <tbody>
              {errors.map((error, index) => {
                const shift = selectedShifts.find(s => s.id === error.shiftId);
                return (
                  <tr key={error.shiftId + index}>
                    <td className={styles.columnStateNumber}>{shift?.vehicleStateNumber}</td>
                    <td>{error.errorText || 'Неизвестная ошибка'}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </Modal>
  );
};

export default Results;
