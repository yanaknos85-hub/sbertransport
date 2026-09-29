
import { Button, Form } from 'antd';
import Modal from 'antd/lib/modal/Modal';
import React, { FC, useEffect } from 'react';

import { SYSTEM_MESSAGES } from 'constants/constants.app';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useModalState } from 'shared/hooks/useModal';
import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';
import { LimitEmpRequest, LimitSendRequest } from 'stores/Limits/LimitsRequest.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import { getYear } from 'utils';

import { UUID } from 'utils/io-ts';

import { LimitRequestLevel, minLimitPercent } from './constants';
import { LimitRequestFormController } from './LimitRequestFormController';

import styles from './modal.module.scss';

export const LimitRequestModal: FC<{
  isEdit?: boolean;
  type: LIMIT_TYPE;
  transportType: string;
  percent: number;
  mainButton?: boolean;
}> = ({
  isEdit, type, transportType, percent, mainButton,
}) => {
  const [modal, modalActions] = useModalState();
  const [form] = Form.useForm();
  const {
    [StoreNames.limitsRequestStore]: limitsRequestStore,
    [StoreNames.limitsStore]: limitStore,
    [StoreNames.selfStore]: selfStore,
    logger,
  } = useAppStoreContext();

  useEffect(() => {
    limitStore.getLimitByDepartment(selfStore.depId as UUID);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [limitStore]);
  // FIXME react-hooks/exhaustive-deps

  const [tt, setTT] = React.useState<string>(transportType);

  const cancelHandler = (): void => {
    form.resetFields();
    setTT(transportType);
    form.setFieldsValue({ transportType });
    modalActions.hide();
  };

  const getRequestData = (
    period: number,
    // eslint-disable-next-line no-shadow, @typescript-eslint/no-shadow
    transportType: string,
    // FIXME no-shadow
    departments: UUID[] | string[],
    sum: number,
    level: LimitRequestLevel,
    description: string
  ): LimitEmpRequest | LimitSendRequest => ({
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
  });

  const onSuccessRequest = (): void => {
    limitStore.initStore();
    limitStore.getLimitRequestsStats();
    logger.toMessage('info', SYSTEM_MESSAGES.limitRequestCreateSuccess);
    form.submit();
    form.resetFields();
    modalActions.hide();
  };

  const sendDepRequest = (data: LimitSendRequest): void => {
    limitsRequestStore
      .addLimitRequest(data)
      .then(res => res === 200 && onSuccessRequest())
      .catch(error => logger.toMessage('error', error.response?.data?.message || error.message));
  };

  const sendEmpRequest = (data: LimitEmpRequest): void => {
    limitsRequestStore
      .addEmpLimitRequest(data)
      .then(res => res === 200 && onSuccessRequest())
      .catch(error => logger.toMessage('error', error.response?.data?.message || error.message));
  };

  const okHandler = (): void => {
    form.validateFields().then(
      () => {
        // eslint-disable-next-line no-shadow, @typescript-eslint/no-shadow
        const {
          period, transportType, departments, sum, level, description,
        } = form.getFieldsValue();
        // FIXME no-shadow
        const data = getRequestData(period, transportType, [departments], sum, level, description);
        if (level) {
          sendDepRequest(data as LimitSendRequest);
        } else {
          sendEmpRequest(data as LimitEmpRequest);
        }
      },
      () => {
        logger.toMessage('error', 'Проверьте заполнение полей формы!');
      }
    );
  };

  const footer: JSX.Element = (
    <div className={styles.footer}>
      <Button type="primary" onClick={okHandler}>
        Подтвердить
      </Button>
      <Button danger={true} onClick={cancelHandler}>
        Отменить запрос
      </Button>
    </div>
  );

  const getFormTitle = (): string => {
    const titleDep = 'Создание заявки на пополнение лимита подразделения Владельцем подразделения';
    const titleEmp = 'Создание заявки на личный лимит';

    return type === LIMIT_TYPE.DEPARTMENT ? titleDep : titleEmp;
  };

  return (
    <>
      <div className={mainButton ? styles.buttonsContainerMainPage : styles.buttonsContainer}>
        <Button size="middle" onClick={modalActions.show}>
          Запросить лимит
        </Button>
        {type === LIMIT_TYPE.DEPARTMENT && percent < minLimitPercent && (
          <Button
            size="middle"
            onClick={(): void => {
              logger.toMessage('warning', 'Механизм push-уведомлений находится в разработке');
              // TODO реализовать
            }}
            className={styles.mgLeft}
          >
            Уведомить
          </Button>
        )}
      </div>
      <Modal
        title={(isEdit && 'Изменить заявку') || getFormTitle()}
        visible={modal}
        onCancel={cancelHandler}
        onOk={okHandler}
        footer={footer}
      >
        <LimitRequestFormController
          isEdit={false}
          formRef={form}
          type={type}
          defaultTransportType={tt}
          setTT={setTT}
        />
      </Modal>
    </>
  );
};
