import React from 'react';
import type { FC } from 'react';
import cn from 'classnames';
import { useTranslation } from 'i18n';

import styles from './Confirm.module.scss';
import { Modal } from '@sber-sbertransport/ui-kit/src';
import { CreationPayload } from '../../WaybillCreate.interface';

interface Props {
  visible: boolean;
  data: CreationPayload | null;
  onConfirm: () => void;
  onCancel: () => void;
}

const ConfirmModal: FC<Props> = ({
  visible,
  data,
  onConfirm,
  onCancel,
}) => {
  const { confirm: i18 } = useTranslation().t.Waybill.modal;

  return (
    <Modal
      open={visible}
      width={1224}
      title={i18.modalTitle}
      okText={i18.okText}
      onOk={onConfirm}
      onCancel={onCancel}
    >
      <div>
        <span className={styles.question}>{i18.question}</span>

        <div className={styles.waybill}>
          <div className={styles.waybill__title}>{i18.titleOne}</div>

          <div className={styles.waybill__details}>
            <div className={styles.block}>
              <div className={styles.block__header}>{i18.common.title}</div>

              <div className={styles.block__body}>
                <div className={styles.item}>
                  <span className={styles.item__label}>{i18.common.humanReadableId}</span>

                  <span className={styles.item__value}>{data?.humanReadableId}</span>
                </div>

                <div className={styles.item}>
                  <span className={styles.item__label}>{i18.common.ewbDate}</span>

                  <span className={styles.item__value}>{data?.ewbDate}</span>
                </div>

                <div className={styles.item}>
                  <span className={styles.item__label}>{i18.common.ewbId}</span>

                  <span className={styles.item__value}>{data?.ewbId}</span>
                </div>

                <div className={styles.item}>
                  <span className={cn(styles.item__label, styles.item__label__short)}>
                    {i18.common.startDate}
                  </span>

                  <span className={styles.item__value}>{data?.startDate}</span>
                </div>

                <div className={styles.item}>
                  <span className={cn(styles.item__label, styles.item__label__short)}>
                    {i18.common.finishDate}
                  </span>

                  <span className={styles.item__value}>{data?.finishDate}</span>
                </div>

                <div className={styles.item}>
                  <span className={styles.item__label}>{i18.common.transportationType}</span>

                  <span className={styles.item__value}>{data?.transportationType}</span>
                </div>

                <div className={styles.item}>
                  <span className={styles.item__label}>{i18.common.communicationType}</span>

                  <span className={styles.item__value}>{data?.communicationType}</span>
                </div>

                <div className={styles.item}>
                  <span className={styles.item__label}>{i18.common.organizationName}</span>

                  <span className={styles.item__value}>{data?.organizationName}</span>
                </div>

                <div className={styles.item}>
                  <span className={styles.item__label}>{i18.common.ogrn}</span>

                  <span className={styles.item__value}>{data?.ogrn}</span>
                </div>

                <div className={styles.item}>
                  <span className={styles.item__label}>{i18.common.tin}</span>

                  <span className={styles.item__value}>{data?.tin}</span>
                </div>
              </div>
            </div>

            <div>
              <div className={styles.block}>
                <div className={styles.block__header}>{i18.organization.title}</div>

                <div className={styles.block__body}>
                  <div className={styles.item}>
                    <span className={styles.item__label}>{i18.organization.organizationName}</span>

                    <span className={styles.item__value}>{data?.organization.organizationName}</span>
                  </div>

                  <div className={styles.item}>
                    <span className={styles.item__label}>{i18.organization.subjectCode}</span>

                    <span className={styles.item__value}>{data?.organization.subjectCode}</span>
                  </div>

                  <div className={styles.item}>
                    <span className={styles.item__label}>{i18.organization.ogrn}</span>

                    <span className={styles.item__value}>{data?.organization.ogrn}</span>
                  </div>

                  <div className={styles.item}>
                    <span className={styles.item__label}>{i18.organization.tin}</span>

                    <span className={styles.item__value}>{data?.organization.tin}</span>
                  </div>
                </div>
              </div>

              <div className={styles.block}>
                <div className={styles.block__header}>{i18.transport.title}</div>

                <div className={styles.block__body}>
                  <div className={styles.item}>
                    <span className={styles.item__label}>{i18.transport.stateNumber}</span>

                    <span className={styles.item__value}>{data?.transport.stateNumber}</span>
                  </div>

                  <div className={styles.item}>
                    <span className={styles.item__label}>{i18.transport.brand}</span>

                    <span className={styles.item__value}>{data?.transport.brand}</span>
                  </div>

                  <div className={styles.item}>
                    <span className={styles.item__label}>{i18.transport.model}</span>

                    <span className={styles.item__value}>{data?.transport.model}</span>
                  </div>

                  <div className={styles.item}>
                    <span className={styles.item__label}>{i18.transport.type}</span>

                    <span className={styles.item__value}>{data?.transport.type}</span>
                  </div>
                </div>
              </div>

              <div className={styles.block}>
                <div className={styles.block__header}>{i18.driver.title}</div>

                <div className={styles.block__body}>
                  <div className={styles.item}>
                    <div className={styles.item__wrap}>
                      <span className={styles.item__label}>{i18.driver.fullName}</span>
                      <span className={styles.item__label}>{`(${i18.driver.personnelNumber})`}</span>
                    </div>

                    <div className={cn(styles.item__wrap, styles.item__wrap__right)}>
                      <span className={styles.item__value}>{data?.driver.fullName}</span>
                      <span className={styles.item__value}>{`(${data?.driver.personnelNumber})`}</span>
                    </div>
                  </div>

                  <div className={styles.item}>
                    <span className={cn(styles.item__label, styles.item__label__short)}>
                      {i18.driver.number}
                    </span>

                    <span className={styles.item__value}>{data?.driver.number}</span>
                  </div>

                  <div className={styles.item}>
                    <span className={cn(styles.item__label, styles.item__label__short)}>
                      {i18.driver.series}
                    </span>

                    <span className={styles.item__value}>{data?.driver.series}</span>
                  </div>

                  <div className={styles.item}>
                    <span className={cn(styles.item__label, styles.item__label__short)}>
                      {i18.driver.issueDate}
                    </span>

                    <span className={styles.item__value}>{data?.driver.issueDate}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Modal>
  );
};

export default ConfirmModal;
