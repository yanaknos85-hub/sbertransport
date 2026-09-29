import React, { FC } from 'react';

import styled from 'styled-components';

import {
  EvacuationStatus,
  MaintenanceStatus,
  MaintenanceTabs,
  MAINTENANCE_STATUSES,
  RegistrationStatus,
  RepairStatus,
  TireStatus,
  WashingStatus
} from 'api/maintenance/maintenance.constants';

interface Props {
  status: string;
  type: MaintenanceTabs;
}

const StatusContainer = styled.div<{ $status: string }>`
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 16px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  background-color: ${({ $status }) => {
    switch ($status) {
      case WashingStatus.EXPECTED_WASH:
      case RepairStatus.EXPECTED_AT_THE_SERVICE_STATION:
      case MaintenanceStatus.WAITING_FOR_MAINTENANCE:
      case TireStatus.WAITING_FOR_TIRE:
        return '#FFF7E6';
      case WashingStatus.GOING_WASH:
      case RepairStatus.TOW_TRUCK_IS_COMING:
      case TireStatus.TRUCK_ON_WAY:
      case MaintenanceStatus.TRUCK_ON_WAY:
      case RepairStatus.PERFORMER_ON_THE_WAY:
      case EvacuationStatus.CAR_IS_ON_THE_WAY:
        return '#E6FFFB';
      case WashingStatus.FINISHED:
      case RepairStatus.FINISHED:
      case TireStatus.FINISHED:
      case EvacuationStatus.FINISHED:
      case MaintenanceStatus.FINISHED:
      case RegistrationStatus.FINISHED:
      case EvacuationStatus.CAR_DELIVERED_TO_DESTINATION:
      case TireStatus.CAR_PASSED_TRUCK:
      case MaintenanceStatus.CAR_PASSED_TRUCK:
      case RepairStatus.CAR_WAS_TRANSFERRED_TO_A_TOW_TRUCK:
        return '#F6FFED';
      case WashingStatus.CANCELLED:
      case RepairStatus.CANCELED:
      case TireStatus.CANCELED:
      case EvacuationStatus.CANCELED:
      case MaintenanceStatus.CANCELED:
      case RegistrationStatus.CANCELED:
      case RepairStatus.SEARCH_FOR_A_TOW_TRUCK:
      case EvacuationStatus.SEARCH_FOR_A_TOW_TRUCK:
      case TireStatus.SEARCH_TRUCK:
      case MaintenanceStatus.SEARCH_TRUCK:
        return '#FFF1F0';
      case RepairStatus.ISSUING_A_CAR:
      case TireStatus.ISSUING_A_CAR:
      case MaintenanceStatus.ISSUING_A_CAR:
      case RegistrationStatus.ISSUING_A_CAR:
      case RepairStatus.CAR_WAS_ACCEPTED_AT_THE_SERVICE_STATION:
      case TireStatus.ACCEPTING:
      case MaintenanceStatus.ACCEPTING:
      case EvacuationStatus.TOW_TRUCK_FILED:
        return '#F0F5FF';
      case RepairStatus.DIAGNOSTICS:
      case MaintenanceStatus.DIAGNOSTICS:
      case RepairStatus.DETERMINATION_OF_THE_LIST_OF_WORKS:
      case TireStatus.DEFINING_WORKS:
      case RepairStatus.WORK_ORDER_APPROVAL:
      case TireStatus.APPROVE_WORKS_COST:
      case MaintenanceStatus.APPROVE_WORKS_COST:
        return '#E6F7FF';
      case RepairStatus.WORK:
      case TireStatus.CARRY_OUT:
      case MaintenanceStatus.CARRY_OUT:
      case RepairStatus.REGISTRATION_AT_THE_SERVICE_STATION:
      case TireStatus.RECORD_FOR_TIRE:
      case MaintenanceStatus.RECORD_FOR_MAINTENANCE:
      case RepairStatus.REGISTRATION_FOR_FIELD_SERVICE:
      case RegistrationStatus.DOCUMENT_TRANSFER:
      case RegistrationStatus.PAYMENT_OF_GOVERNMENT_FEE:
      case RegistrationStatus.TRAFFIC_POLICE_INSPECTION:
        return '#FFFBE6';
      default:
        return '#F5F5F5';
    }
  }};
  color: ${({ $status }) => {
    switch ($status) {
      case WashingStatus.EXPECTED_WASH:
      case RepairStatus.EXPECTED_AT_THE_SERVICE_STATION:
      case MaintenanceStatus.WAITING_FOR_MAINTENANCE:
      case TireStatus.WAITING_FOR_TIRE:
        return '#FA8C16';
      case WashingStatus.GOING_WASH:
      case RepairStatus.TOW_TRUCK_IS_COMING:
      case TireStatus.TRUCK_ON_WAY:
      case MaintenanceStatus.TRUCK_ON_WAY:
      case RepairStatus.PERFORMER_ON_THE_WAY:
      case EvacuationStatus.CAR_IS_ON_THE_WAY:
        return '#00B8D9';
      case WashingStatus.FINISHED:
      case RepairStatus.FINISHED:
      case TireStatus.FINISHED:
      case EvacuationStatus.FINISHED:
      case MaintenanceStatus.FINISHED:
      case RegistrationStatus.FINISHED:
      case EvacuationStatus.CAR_DELIVERED_TO_DESTINATION:
      case TireStatus.CAR_PASSED_TRUCK:
      case MaintenanceStatus.CAR_PASSED_TRUCK:
      case RepairStatus.CAR_WAS_TRANSFERRED_TO_A_TOW_TRUCK:
        return '#52C41A';
      case WashingStatus.CANCELLED:
      case RepairStatus.CANCELED:
      case TireStatus.CANCELED:
      case EvacuationStatus.CANCELED:
      case MaintenanceStatus.CANCELED:
      case RegistrationStatus.CANCELED:
      case RepairStatus.SEARCH_FOR_A_TOW_TRUCK:
      case EvacuationStatus.SEARCH_FOR_A_TOW_TRUCK:
      case TireStatus.SEARCH_TRUCK:
      case MaintenanceStatus.SEARCH_TRUCK:
        return '#FF4D4F';
      case RepairStatus.ISSUING_A_CAR:
      case TireStatus.ISSUING_A_CAR:
      case MaintenanceStatus.ISSUING_A_CAR:
      case RegistrationStatus.ISSUING_A_CAR:
      case RepairStatus.CAR_WAS_ACCEPTED_AT_THE_SERVICE_STATION:
      case TireStatus.ACCEPTING:
      case MaintenanceStatus.ACCEPTING:
      case EvacuationStatus.TOW_TRUCK_FILED:
        return '#5353FF';
      case RepairStatus.DIAGNOSTICS:
      case MaintenanceStatus.DIAGNOSTICS:
      case RepairStatus.DETERMINATION_OF_THE_LIST_OF_WORKS:
      case TireStatus.DEFINING_WORKS:
      case RepairStatus.WORK_ORDER_APPROVAL:
      case TireStatus.APPROVE_WORKS_COST:
      case MaintenanceStatus.APPROVE_WORKS_COST:
        return '#1890FF';
      case RepairStatus.WORK:
      case TireStatus.CARRY_OUT:
      case MaintenanceStatus.CARRY_OUT:
      case RepairStatus.REGISTRATION_AT_THE_SERVICE_STATION:
      case TireStatus.RECORD_FOR_TIRE:
      case MaintenanceStatus.RECORD_FOR_MAINTENANCE:
      case RepairStatus.REGISTRATION_FOR_FIELD_SERVICE:
      case RegistrationStatus.DOCUMENT_TRANSFER:
      case RegistrationStatus.PAYMENT_OF_GOVERNMENT_FEE:
      case RegistrationStatus.TRAFFIC_POLICE_INSPECTION:
        return '#D4B106';
      default:
        return '#8C8C8C';
    }
  }};
`;

const StatusLabel: FC<Props> = ({ status, type }) => {
  const statusLabelMap = new Map(
    MAINTENANCE_STATUSES[type].map(({ value, label }) => [value, label])
  );

  return (
    <StatusContainer $status={status}>
      {statusLabelMap.get(status) ?? status}
    </StatusContainer>
  );
};

export default StatusLabel;
