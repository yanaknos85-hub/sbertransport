import React, { FC } from 'react';
import { Button, Form, Input } from 'antd';
import Modal from 'antd/lib/modal/Modal';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useModalState } from 'shared/hooks/useModal';

import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';
import { LimitEmpRequest, LimitSendRequest } from 'stores/Limits/LimitsRequest.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { SYSTEM_MESSAGES } from 'constants/constants.app';
import { getYear } from 'utils/index';
import { UUID } from 'utils/io-ts';

import { LimitRequestLevel } from '../../../LimitRequestModal/constants';
import { LimitRequestFormController } from '../../../LimitRequestModal/LimitRequestFormController';
import { ISpentActionsType } from '../../../useDetailedLimitsMapper';
import { isInitRequest } from '../../../utils';

import styles from '../../../LimitRequestModal/modal.module.scss';

const UserLimitRequestApproveModal: FC<{
  isEdit: boolean;
  type: LIMIT_TYPE;
  transportType: string;
  request: ISpentActionsType;
  goToList: () => void;
}> = ({
  type, transportType, request, isEdit, goToList,
}) => {
  const [modal, modalActions] = useModalState();
  const [form] = Form.useForm();
  const { [StoreNames.limitsStore]: limitStore, logger } = useAppStoreContext();

  const setReason = (value: string): void => {
    request.description = value;
  };

  const [tt, setTT] = React.useState<string>(transportType);

  const onSuccess = (): void => {
    modalActions.hide();
    logger.toMessage('info', SYSTEM_MESSAGES.limitRequestEditSuccess);
    form.resetFields();
    limitStore.getLimitRequestsStats();
    goToList();
  };

  const sendDepRequest = (sendRequest: LimitSendRequest): void => {
    limitStore.changeDepLimitRequest(sendRequest, request.id).then(() => {
      onSuccess();
    });
  };

  const sendChangeDepRequest = (
    period: number,
    // eslint-disable-next-line no-shadow
    transportType: string,
    // FIXME no-shadow
    sum: number,
    description: string,
    level: string,
    departments?: UUID[]
  ): void => {
    if (level === LimitRequestLevel.SIBLINGS && !departments) {
      logger.toMessage('error', 'Пожалуйста, заполните поле Получатели!');
    } else {
      const sendRequest = {
        year: getYear(period),
        period,
        transportType,
        ...(level && {
          askTargets: level?.toUpperCase() ?? LimitRequestLevel.PARENT.toUpperCase(),
        }),
        sum,
        description,
        ...(level
        && level === LimitRequestLevel.SIBLINGS && {
          departments: departments?.filter(
            (item: string, index: number, array: string[]) => array.indexOf(item) === index
          ),
        }),
      } as LimitSendRequest;
      sendDepRequest(sendRequest);
    }
  };

  const sendChangeEmpRequest = (data: Partial<LimitEmpRequest>): void => {
    const sendRequest = {
      period: data.period,
      transportType: data.transportType,
      sum: data.sum,
      description: data.description,
      year: getYear(data.period as number),
    } as LimitEmpRequest;
    limitStore.changeEmpLimitRequest(sendRequest, request.id).then(() => {
      onSuccess();
    });
  };

  const okChangeHandler = (): void => {
    form.validateFields().then(
      () => {
        // eslint-disable-next-line no-shadow
        const {
          period, transportType, sum, description, level,
        } = form.getFieldsValue();
        // FIXME no-shadow
        if (request.limitType === LIMIT_TYPE.DEPARTMENT) {
          sendChangeDepRequest(period, transportType, sum, description, level, form.getFieldValue('departments'));
        } else {
          sendChangeEmpRequest({
            period, transportType, sum, description,
          });
        }
      },
      () => {
        logger.toMessage('error', 'Проверьте заполнение полей формы!');
      }
    );
  };

  const cancelChangeHandler = (): void => {
    form.resetFields();
    setTT(transportType);
    form.setFieldsValue({ transportType });
    modalActions.hide();
  };

  // two functions for the "cancel" modal window
  const cancelOkHandler = (data: { requestId: UUID; description: string }): void => {
    limitStore.cancelLimitRequest(data);
    goToList();
  };

  const footer: JSX.Element = (
    <div className={styles.footer}>
      <Button type="primary" onClick={okChangeHandler}>
        Подтвердить
      </Button>
      <Button danger={true} onClick={cancelChangeHandler}>
        Отменить изменения
      </Button>
    </div>
  );

  const changeRequestModal: JSX.Element = (
    <>
      <Button
        disabled={
          (request?.limitBalance ? request.sum >= request?.limitBalance : false) || !isInitRequest(request.status)
        }
        size="middle"
        onClick={modalActions.show}
      >
        Изменить
      </Button>
      <Modal
        title="Изменить заявку"
        visible={modal}
        onCancel={cancelChangeHandler}
        onOk={okChangeHandler}
        footer={footer}
      >
        <LimitRequestFormController
          formRef={form}
          type={type}
          isEdit={true}
          request={request}
          defaultTransportType={tt}
          setTT={setTT}
        />
      </Modal>
    </>
  );

  const cancelRequestModal: JSX.Element = (
    <>
      <Button
        disabled={!isInitRequest(request.status)}
        size="middle"
        onClick={modalActions.show}
        danger={true}
      >
        Отменить
      </Button>
      <Modal
        title="Отмена заявки"
        open={modal}
        onOk={(): void => {
          cancelOkHandler({ requestId: request.id as UUID, description: request.description ?? '' });
        }}
        onCancel={(): void => {
          modalActions.hide();
          form.resetFields();
        }}
        okText="Подтвердить отмену"
        cancelText="Вернуться"
        okButtonProps={{ danger: true }}
      >
        <Form
          form={form}
          layout="vertical"
          name="edit-request"
          size="middle"
          onValuesChange={(): void => {
            /* */
          }}
          initialValues={{ reason: '' }}
        >
          <Form.Item name="reason">
            <p>Пожалуйста, укажите причину отмены:</p>
            <Input.TextArea
              onChange={(e): void => setReason(e.target.value)}
              placeholder="Причина"
              autoSize={{ minRows: 3, maxRows: 5 }}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );

  return isEdit ? changeRequestModal : cancelRequestModal;
};

export default UserLimitRequestApproveModal;
