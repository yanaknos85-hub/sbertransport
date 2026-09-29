import React, { FC } from 'react';

import styled from 'styled-components';

import { WaybillStatus, WaybillStatusesNames } from 'api/waybill/waybill.constants';

interface Props {
  status: WaybillStatus;
}

const StatusContainer = styled.div<{ $status: WaybillStatus }>`
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 16px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  background-color: ${({ $status }) => {
    switch ($status) {
      case WaybillStatus.EWB_CREATED:
        return '#E6F7FF';
      case WaybillStatus.MEDIC_IN_PROGRESS:
        return '#FFF7E6';
      case WaybillStatus.MEDIC_DECLINED:
        return '#FFF1F0';
      case WaybillStatus.TELEMECH_IN_PROGRESS:
        return '#E6FFFB';
      case WaybillStatus.DRIVER_CANCELED:
        return '#FFF1F0';
      case WaybillStatus.TELEMECH_DECLINED:
        return '#FFF1F0';
      case WaybillStatus.ON_THE_LINE:
        return '#F6FFED';
      case WaybillStatus.KORUS_DECLINED:
        return '#FFF1F0';
      case WaybillStatus.IN_GARAGE:
        return '#F0F5FF';
      case WaybillStatus.EWB_CLOSED:
        return '#E6FFFB';
      case WaybillStatus.EXPIRED:
        return '#FFF1F0';
      case WaybillStatus.EWB_CANCELLED:
        return '#FFF1F0';
      default:
        return '#F5F5F5';
    }
  }};
  color: ${({ $status }) => {
    switch ($status) {
      case WaybillStatus.EWB_CREATED:
        return '#1890FF';
      case WaybillStatus.MEDIC_IN_PROGRESS:
        return '#FA8C16';
      case WaybillStatus.MEDIC_DECLINED:
        return '#FF4D4F';
      case WaybillStatus.TELEMECH_IN_PROGRESS:
        return '#00B8D9';
      case WaybillStatus.DRIVER_CANCELED:
        return '#FF4D4F';
      case WaybillStatus.TELEMECH_DECLINED:
        return '#FF4D4F';
      case WaybillStatus.ON_THE_LINE:
        return '#52C41A';
      case WaybillStatus.KORUS_DECLINED:
        return '#FF4D4F';
      case WaybillStatus.IN_GARAGE:
        return '#5353FF';
      case WaybillStatus.EWB_CLOSED:
        return '#00B8D9';
      case WaybillStatus.EXPIRED:
        return '#FF4D4F';
      case WaybillStatus.EWB_CANCELLED:
        return '#FF4D4F';
      default:
        return '#8C8C8C';
    }
  }};
`;

const Status: FC<Props> = ({ status }) => {
  return (
    <StatusContainer $status={status}>
      {WaybillStatusesNames[status]}
    </StatusContainer>
  );
};

export default Status;
