import { Button, Form } from 'antd';
import Modal from 'antd/lib/modal/Modal';
import React, { FC, useState, useEffect } from 'react';
import { SYSTEM_MESSAGES } from 'constants/constants.app';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useModalState } from 'shared/hooks/useModal';
import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';
import { LimitEmpRequest, LimitSendRequest } from 'stores/Limits/LimitsRequest.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { getYear } from 'utils';
import { UUID } from 'utils/io-ts';
import { LimitRequestLevel } from './constants';
import { LimitRequestFormController } from './LimitRequestFormController';
import { ReactComponent as CloseIcon } from 'components/Evaluation/static/icons/closeIcon.svg';
import styles from './modal.module.scss';

export const LimitRequestModal: FC<{
  isEdit?: boolean;
  type?: LIMIT_TYPE;
  serviceType?: string;
  transportType?: string;
  percent?: number;
  mainButton?: boolean;
  restrict?: string[];
  requestButtonName?: string;
  onSendRequest?: () => void;
}> = ({
  isEdit,
  type = LIMIT_TYPE.EMPLOYEE,
  serviceType,
  transportType,
  mainButton,
  restrict,
  requestButtonName = 'Запросить лимит',
  onSendRequest,
}) => {
  const [modal, modalActions] = useModalState();
  const [form] = Form.useForm();
  const {
    [StoreNames.limitsRequestStore]: limitsRequestStore,
    [StoreNames.transportTypesStore]: transportTypesStore,
    logger,
  } = useAppStoreContext();
  const [isLoading, setLoading] = useState(false);

  const { getAvailableTransportTypes, getAvailableTransportTypesByService } = transportTypesStore;

  useEffect(() => {
    if (modal) {
      if (serviceType) {
        getAvailableTransportTypesByService(serviceType);
      } else {
        getAvailableTransportTypes();
      }
    }
  }, [modal, serviceType, getAvailableTransportTypes, getAvailableTransportTypesByService]);

  // Зачем вообще это запрашивать в модалке ???
  // useEffect(() => {
  //   if (modal) {
  //     limitStore.getLimitByDepartment(selfStore.depId as UUID);
  //   }
  // }, [modal, selfStore.depId, limitStore]);

  const [tt, setTT] = React.useState<string>(transportType);

  const cancelHandler = (): void => {
    form.resetFields();
    setTT(transportType);
    form.setFieldsValue({ transportType });
    modalActions.hide();
  };

  const getRequestData = (
    period: number,
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
    logger.toMessage('info', SYSTEM_MESSAGES.limitRequestCreateSuccess);
    form.submit();
    form.resetFields();
    modalActions.hide();

    if (onSendRequest) {
      onSendRequest();
    }
  };

  const sendDepRequest = (data: LimitSendRequest): void => {
    setLoading(true);

    limitsRequestStore
      .addLimitRequest(data)
      .then(res => res === 200 && onSuccessRequest())
      .catch(error => logger.toMessage('error', error.response?.data?.message || error.message))
      .finally(() => setLoading(false));
  };

  const sendEmpRequest = (data: LimitEmpRequest): void => {
    setLoading(true);

    limitsRequestStore
      .addEmpLimitRequest(data)
      .then(res => res === 200 && onSuccessRequest())
      .catch(error => logger.toMessage('error', error.response?.data?.message || error.message))
      .finally(() => setLoading(false));
  };

  const okHandler = (): void => {
    form.validateFields().then(
      () => {
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
      <Button type="text" onClick={cancelHandler}>
        Отменить
      </Button>
      <Button
        type="primary"
        onClick={okHandler}
        loading={isLoading}
      >
        Запросить
      </Button>
    </div>
  );

  const getFormTitle = (): string => {
    const titleDep = 'Создание заявки на пополнение лимита подразделения Владельцем подразделения';
    const titleEmp = 'Создание заявки на личный лимит';

    return type === LIMIT_TYPE.DEPARTMENT ? titleDep : titleEmp;
  };

  const isDisabled = restrict && transportType && !restrict.includes(transportType);

  return (
    <>
      <div className={mainButton ? styles.buttonsContainerMainPage : styles.buttonsContainer}>
        <Button
          size="middle"
          onClick={modalActions.show}
          disabled={isDisabled}
        >
          {requestButtonName}
        </Button>

        {/* Механизм push-уведомлений находится в разработке */}
        {/* {type === LIMIT_TYPE.DEPARTMENT && percent < minLimitPercent && (
          <Button
            size="middle"
            onClick={() => {}}
            className={styles.mgLeft}
          >
            Уведомить
          </Button>
        )} */}
      </div>
      <Modal
        width={560}
        title={(isEdit && 'Изменить заявку') || getFormTitle()}
        visible={modal}
        onCancel={cancelHandler}
        onOk={okHandler}
        closeIcon={<CloseIcon />}
        cancelButtonProps={{ style: { display: 'block' } }}
        footer={footer}
        className={styles.modal}
      >
        <LimitRequestFormController
          isEdit={false}
          serviceType={serviceType}
          restrict={restrict}
          formRef={form}
          type={type}
          defaultTransportType={tt}
          setTT={setTT}
        />
      </Modal>
    </>
  );
};
