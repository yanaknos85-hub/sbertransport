import React, { FC, useState } from 'react';
import { Popconfirm } from 'antd';

import { useTranslation } from 'i18n';
import { TRIP_STATUSES } from 'constants/trips.constants';
import { TripTypes } from 'constants/app.constants';
import { StatusSelect } from 'components/StatusSelect';

interface StatusConfirmProps {
  value: TRIP_STATUSES;
  tripMode: TripTypes;
  isEdit: boolean;
  loading: boolean;
  className: string;
  onChange: (status: TRIP_STATUSES) => void;
}

export const StatusWithConfirm: FC<StatusConfirmProps> = ({ onChange, ...props }) => {
  const { t } = useTranslation();
  const [visible, setVisible] = useState(false);
  const [selected, setSelected] = useState<TRIP_STATUSES>();

  const onChangeStatus = (status: TRIP_STATUSES) => {
    if ([TRIP_STATUSES.ORDER_CANCELLED_BY_CLIENT, TRIP_STATUSES.ORDER_CANCELLED_BY_DRIVER].includes(status)) {
      setSelected(status);
      setVisible(true);
      return;
    }
    onChange(status);
  };

  const onCancel = () => {
    setVisible(false);
    setSelected(undefined);
  };

  const onConfirm = () => {
    onChange(selected!);
    onCancel();
  };

  return (
    <Popconfirm
      visible={visible}
      title={t.Requests.Modal.EditTrip.cancelTrip}
      onConfirm={onConfirm}
      onCancel={onCancel}
      okText={t.global.yes}
      cancelText={t.global.cancel}
    >
      <StatusSelect onChange={status => onChangeStatus(status as TRIP_STATUSES)} {...props} />
    </Popconfirm>
  );
};
