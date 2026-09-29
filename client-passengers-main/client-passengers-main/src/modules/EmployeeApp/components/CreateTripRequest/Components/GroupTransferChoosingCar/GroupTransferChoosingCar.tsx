import { Form } from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useEffect, useState } from 'react';
import { FormInstance } from 'antd/es/form/Form';
import { Moment } from 'moment';

import { ChoosingDesiredDate } from './components/choosingDesiredDate/ChoosingDesiredDate';
import { AvailableCars } from './components/availableCars/AvailableCars';

import styles from './groupTransferChoosingCar.module.scss';
import './override.scss';

interface GroupTransferChoosingCarProps {
  groupTransferForm: FormInstance;
  form: FormInstance;
  setVisibleGroupTransferChoosingCar: React.Dispatch<React.SetStateAction<boolean>>;
  setVisibleGroupTransferChoosingBookingInterval: React.Dispatch<React.SetStateAction<boolean>>;
  setVisiblevisibleGroupTransferForm: React.Dispatch<React.SetStateAction<boolean>>;
}

export const GroupTransferChoosingCar: FC<GroupTransferChoosingCarProps> = observer(({
  groupTransferForm,
  form,
  setVisibleGroupTransferChoosingCar,
  setVisibleGroupTransferChoosingBookingInterval,
  setVisiblevisibleGroupTransferForm,
}) => {
  const [switchBookingTime, setSwitchBookingTime] = useState(false);
  const [dateBooking, setDateBooking] = useState<Moment>();

  useEffect(() => {
    return () => setVisibleGroupTransferChoosingCar(false);
  }, []);

  return (
    <Form
      form={groupTransferForm}
      layout="vertical"
      name="create-request"
      size="middle"
    >
      <div className={styles.wrapper_groupTransferChoosingCar}>
        <ChoosingDesiredDate
          setSwitchBookingTime={setSwitchBookingTime}
          form={form}
          setDateBooking={setDateBooking}
        />
        <AvailableCars
          setVisibleGroupTransferChoosingBookingInterval={setVisibleGroupTransferChoosingBookingInterval}
          setVisibleGroupTransferChoosingCar={setVisibleGroupTransferChoosingCar}
          setVisiblevisibleGroupTransferForm={setVisiblevisibleGroupTransferForm}
          form={groupTransferForm}
          switchBookingTime={switchBookingTime}
          dateBooking={dateBooking}
        />
      </div>
    </Form>
  );
});
