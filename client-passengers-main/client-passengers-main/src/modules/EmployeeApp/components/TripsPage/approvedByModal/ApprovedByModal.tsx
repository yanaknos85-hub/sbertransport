/* eslint-disable no-unsafe-optional-chaining */
import { Modal } from 'antd';
import { observer } from 'mobx-react';
import React, { FC } from 'react';

import Close from 'shared/components/Images/Close.svg';
import { Delegate } from 'stores/Delegates/Delegates.interface';
import { IDepartmentHead } from 'stores/Corporate/Corporate.interface';
import { TripRequestModel } from 'stores/Trip/models';
import { formatFullNameWithoutDots } from 'utils/formatFullName';
import UserInformation from './UserInformation/UserInformation';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores';

import styles from './approvedByModal.module.scss';

interface ApprovedByModalProps {
  visible: boolean;
  onCancel: () => void;
  delegates: Delegate[];
  supervisor?: IDepartmentHead;
  request: TripRequestModel;
  checkAwaitingApproval?: boolean;
}

export const ApprovedByModal: FC<ApprovedByModalProps> = observer(
  ({
    visible,
    onCancel,
    delegates,
    supervisor,
    request,
    checkAwaitingApproval,
  }): JSX.Element => {
    const { [StoreNames.corporateStore]: corporateStore } = useAppStoreContext();
    const filteredDelegates = delegates
      .filter(({
        transportType,
      }) => (
        transportType === request.transportType)
      );
    const findPositionNameById = positionId => {
      const foundObject = corporateStore.positions.find(item => item.id === positionId);

      return foundObject ? foundObject.positionName : '';
    };

    return (
      <div onClick={e => e.stopPropagation()}>
        <Modal
          open={visible}
          onCancel={onCancel}
          className={styles.approvedByModal}
          footer={false}
        >
          <div className="cardWrapper">
            <div className="header">
              <div className="numberApplication">
                Согласующие
              </div>
              <div className="cancel" onClick={onCancel}>
                <img alt="Close" src={Close} />
              </div>
            </div>
            <div className={styles.approvedByModal_description}>
              {checkAwaitingApproval ? 'Вашу заявку могут согласовать:' : request.approvedBy.userId ? 'Заявка согласована:' : 'Вашу заявку могут согласовать:'}
            </div>
            <div>
              {request.approvedBy.userId && !checkAwaitingApproval
                ? (
                  <div className={styles.approved}>
                    <UserInformation
                      position={findPositionNameById(request.approvedBy.positionId)}
                      id={request.approvedBy.id}
                      fio={request.approvedBy.fullName}
                    />
                  </div>
                )
                : (
                  <div className={styles.approved}>
                    {filteredDelegates.length === 0
                      ? supervisor && (
                      <UserInformation
                        id={supervisor.id}
                        fio={`${supervisor.lastName} ${supervisor.firstName} ${supervisor.patronymic}`}
                        position=""
                      />
                      )
                      : filteredDelegates.map(
                        ({
                          delegateEmployee: {
                            userId,
                            lastName,
                            firstName,
                            patronymic,
                          },
                        }) => (
                          <div>
                            <UserInformation
                              id={userId}
                              fio={formatFullNameWithoutDots(firstName, lastName, patronymic)}
                              position=""
                            />
                          </div>
                        ))}
                  </div>
                )}
            </div>
          </div>
        </Modal>
      </div>
    );
  }
);
