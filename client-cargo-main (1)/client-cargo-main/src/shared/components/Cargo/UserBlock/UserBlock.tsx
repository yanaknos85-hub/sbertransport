import * as React from 'react';
import { UserOutlined } from '@ant-design/icons';
import { EmployeeModel } from '@sber-sbertransport/mf-core';
import { Avatar } from 'antd';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import { CargoRequestStatusesEnum, CargoRequestStatusesType } from 'constants/CargoRequestStatuses.constants';

import styles from './styles.module.scss';

const UserBlock: React.FC<any> = ({
  author,
  innerElement,
  driverData,
  status,
  departmentName,
}: {
  author: EmployeeModel;
  innerElement: React.FC<any>;
  driverData: string;
  status: CargoRequestStatusesType;
  departmentName: string;
}) => {
  const { [StoreNames.mappedStore]: mappedStore } = useAppStoreContext();
  const authorDetailed = mappedStore.mapEmployeeFields(new EmployeeModel(author));

  return (
    <div className={styles.mainDiv}>
      {driverData
      && status !== CargoRequestStatusesEnum.CARGO_AWAITING_APPROVAL
      && status !== CargoRequestStatusesEnum.CARGO_APPROVED ? (
        <p>{driverData}</p>
        ) : (
          <>
            <div>
              <Avatar size="large" icon={<UserOutlined />} />
            </div>
            <div className={styles.authorInfoDiv}>
              <div className={styles.fullNameString}>{authorDetailed.fullNameString}</div>
              {/* ToDo: узнать зачем оно надо было?! <div className={styles.position}>{authorDetailed.position}</div> */}
              <div className={styles.personnelNumber}>
                Таб.
                {authorDetailed.personnelNumber}
                <div>{departmentName}</div>
              </div>
            </div>
            <div className={styles.buttonsDiv}>{innerElement}</div>
          </>
        )}
    </div>
  );
};

export default UserBlock;
