import { EditOutlined } from '@ant-design/icons';
import { Button, Modal } from 'antd';
import * as React from 'react';
import { useRouteMatch } from 'react-router-dom';

import { RUBLE_SIGN } from 'constants/constants.app';

import { ILimitsStore, LIMIT_REQUEST_STATUS, LimitRequestSavingObject } from 'stores/Limits/Limit.interface';

import { NumericInput } from 'shared/components/NumericInput';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { UUID } from 'utils/io-ts';

import styles from './styles.module.scss';

const EditLimitRequestForm = ({
  parentlimitId,
  startSum,
  limitsStore,
  status,
  viewDisabled,
}: {
  parentlimitId: UUID;
  startSum: number;
  limitsStore: ILimitsStore;
  status: LIMIT_REQUEST_STATUS;
  viewDisabled: boolean;
}): JSX.Element => {
  const { logger, configStore } = useAppStoreContext();

  const [modalVisible, setModalVisible] = React.useState(false);

  const [sum, setSum] = React.useState<number>(startSum);

  const handleCancel = (): void => {
    setModalVisible(false);
  };

  const match = useRouteMatch<any>();
  const { reqId } = match.params;

  const checkAndSetSum = (newSum: number): void => {
    setSum(newSum);

    const data = {
      requestId: parentlimitId,
      sum,
      approvalState: 'APPROVED',
    } as LimitRequestSavingObject;

    limitsStore
      .approveLimitRequest(data)
      .then(() => {
        setModalVisible(false);
        if (reqId) {
          configStore.history.goBack();
        }
      })
      .catch(error => logger.toMessage('error', error.response.data.message));
  };

  const save = (e: any): void => {
    checkAndSetSum(sum);
    e.preventDefault();
    e.stopPropagation();
  };

  const getIconButton = (): JSX.Element => (
    <Button
      type="link"
      disabled={status !== LIMIT_REQUEST_STATUS.INIT || viewDisabled}
      onClick={(e): void => {
        e.stopPropagation();
        setModalVisible(true);
      }}
      icon={<EditOutlined />}
    />
  );

  const getButtonWithTitle = (): JSX.Element => (
    <Button
      disabled={status !== LIMIT_REQUEST_STATUS.INIT || viewDisabled}
      onClick={(e): void => {
        e.stopPropagation();
        setModalVisible(true);
      }}
    >
      Скорректировать
    </Button>
  );

  const getButton = (): JSX.Element => (reqId ? getButtonWithTitle() : getIconButton());

  const footer: JSX.Element = (
    <div className={styles.footer}>
      <Button className={styles.footerButton} onClick={save}>
        Подтвердить
      </Button>
    </div>
  );

  return (
    <>
      {getButton()}
      <Modal
        centered
        title="Введите сумму"
        visible={modalVisible}
        footer={footer}
        onCancel={handleCancel}
        destroyOnClose
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
