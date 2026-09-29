import React, { useState } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { Button, Modal } from 'antd';

import { RUBLE_SIGN } from 'constants/constants.app';
import { NumericInput } from 'shared/components/NumericInput';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ReactComponent as Pen } from 'shared/icons/pen.svg';
import { ILimitsStore, LIMIT_REQUEST_STATUS } from 'stores/Limits/Limit.interface';
import { UUID } from 'utils/io-ts';
import styles from './styles.module.scss';

const EditLimitRequestForm = ({
  parentLimitId,
  startSum,
  limitsStore,
  status,
  viewDisabled,
  refresh,
}: {
  parentLimitId: UUID;
  startSum: number;
  limitsStore: ILimitsStore;
  status: LIMIT_REQUEST_STATUS;
  viewDisabled: boolean;
  refresh?: () => void;
}): JSX.Element => {
  const { logger } = useAppStoreContext();
  const history = useHistory();
  const match = useRouteMatch();
  const { reqId } = match.params;

  const [modalVisible, setModalVisible] = useState(false);
  const [isLoading, setLoading] = useState(false);
  const [sum, setSum] = useState<number>(startSum / 100);

  const handleCancel = (): void => {
    setModalVisible(false);
  };

  const checkAndSetSum = (newSum: number): void => {
    setSum(newSum);
    setLoading(true);

    limitsStore
      .approveLimitRequest({
        requestId: parentLimitId,
        sum,
        approvalState: 'APPROVED',
      })
      .then(() => {
        setModalVisible(false);
        reqId ? history.goBack() : refresh?.();
      })
      .catch(error => logger.toMessage('error', error.response.data.message))
      .finally(() => setLoading(false));
  };

  const save = (e: any): void => {
    checkAndSetSum(sum);
    e.preventDefault();
    e.stopPropagation();
  };

  const footer: JSX.Element = (
    <div className={styles.footer}>
      <Button
        loading={isLoading}
        className={styles.footerButton}
        onClick={save}
      >
        Подтвердить
      </Button>
    </div>
  );

  return (
    <>
      {status === LIMIT_REQUEST_STATUS.INIT && (
        <Button
          className={styles.editBtn}
          disabled={status !== LIMIT_REQUEST_STATUS.INIT || viewDisabled}
          onClick={(e): void => {
            e.stopPropagation();
            setModalVisible(true);
          }}
          icon={<Pen />}
        />
      )}
      <Modal
        centered={true}
        title="Введите сумму"
        visible={modalVisible}
        footer={footer}
        onCancel={handleCancel}
        destroyOnClose={true}
      >
        <div>
          <NumericInput
            value={sum}
            suffix={RUBLE_SIGN}
            onChange={(_ = 0): void => setSum(_)}
          />
        </div>
      </Modal>
    </>
  );
};

export default EditLimitRequestForm;
