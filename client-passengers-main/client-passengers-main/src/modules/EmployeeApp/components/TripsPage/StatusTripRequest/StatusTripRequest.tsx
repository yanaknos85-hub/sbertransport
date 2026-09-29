import React, { useEffect, useState } from 'react';
import { Popover } from 'antd';
import { InfoCircleOutlined } from '@ant-design/icons';
import cn from 'classnames';

import {
  TripRequestStatusesCarSharing,
  TripRequestStatusesCarSharingTitles,
  TripRequestStatusesGroupTransfer,
  TripRequestStatusesGroupTransferTitles,
  TripRequestStatusesPersonal,
  TripRequestStatusesPersonalTitles,
  TripRequestStatusesPublic,
  TripRequestStatusesPublicTitles,
  TripRequestStatusesTaxi,
  TripRequestStatusesTaxiTitles,
  TripRequestStatuses,
  TripStatusesChangeableEnum,
  TripStatusesFinalEnum,
  TripStatusesPositiveEnum,
  tripStatusesEnum,
  StatusesHintText
} from 'modules/EmployeeApp/TripRequestStatuses.constants';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import styles from './styles.module.scss';

interface Props {
  withHint?: boolean;
  status?: TripRequestStatuses;
  type?: TransportTypeEnum;
}

export const StatusTripRequest = ({
  withHint,
  status,
  type,
}: Props): JSX.Element => {
  const [statusTrip, setStatusTrip] = useState<{ color: string | undefined; text: string }>({ color: '', text: '' });

  const getPersonalTripColor = (status: TripRequestStatusesTaxi | TripRequestStatusesPersonal | TripRequestStatusesPublic | TripRequestStatusesCarSharing | TripRequestStatusesGroupTransfer) => {
    if (TripStatusesChangeableEnum[status as TripStatusesChangeableEnum]) {
      return 'change';
    } else if (TripStatusesFinalEnum[status as TripStatusesFinalEnum]) {
      return 'error';
    } else if (TripStatusesPositiveEnum[status as TripStatusesPositiveEnum]) {
      return 'positive';
    } else if (tripStatusesEnum[status as tripStatusesEnum]) {
      return 'tripStatus';
    }
  };

  const getButtonProps = (tripType: TransportTypeEnum | undefined, status: TripRequestStatusesTaxi | TripRequestStatusesPersonal | TripRequestStatusesPublic | TripRequestStatusesCarSharing | TripRequestStatusesGroupTransfer) => {
    switch (tripType) {
      case TransportTypeEnum['TAXI']:
      {
        const color = getPersonalTripColor(status);
        return { color, text: TripRequestStatusesTaxiTitles[status as TripRequestStatusesTaxi] };
      }
      case TransportTypeEnum['PERSONAL']:
      {
        const color = getPersonalTripColor(status);
        return { color, text: TripRequestStatusesPersonalTitles[status as TripRequestStatusesPersonal] };
      }
      case TransportTypeEnum['CARSHARING']:
      {
        const color = getPersonalTripColor(status);
        return { color, text: TripRequestStatusesCarSharingTitles[status as TripRequestStatusesCarSharing] };
      }
      case TransportTypeEnum['PUBLIC']:
      {
        const color = getPersonalTripColor(status);
        return { color, text: TripRequestStatusesPublicTitles[status as TripRequestStatusesPublic] };
      }
      case TransportTypeEnum['GROUP_TRANSFER']:
      {
        const color = getPersonalTripColor(status);
        return { color, text: TripRequestStatusesGroupTransferTitles[status as TripRequestStatusesGroupTransfer] };
      }
      default:
        return { color: undefined, text: TripRequestStatusesPublicTitles[status as TripRequestStatusesPublic] };
    }
  };

  useEffect(() => {
    status && setStatusTrip(getButtonProps(type, status));
  }, []);

  const button = (
    <div className={cn(styles.status, styles[`status_${statusTrip?.color}`])}>
      <span>{statusTrip && statusTrip?.text}</span>
      {withHint && <InfoCircleOutlined />}
    </div>
  );

  return withHint ? (
    <Popover
      content={StatusesHintText}
      overlayClassName={styles.overlay}
      placement="left"
    >
      {button}
    </Popover>
  ) : button;
};
