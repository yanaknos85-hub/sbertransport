import { TableField } from 'modules/FraudMonitoring/fraudMonitoring.constants';

export const autoApprovalTitle = {
  true: 'Да',
  false: 'Нет',
};

export const columnsWidth: Record<TableField, number> = {
  humanReadableId: 156,
  transportType: 203,
  fraudMarkers: 93,
  desiredDate: 260,
  passenger: 286,
  approvalDate: 159,
  approver: 286,
  autoApproval: 218,
  plannedCost: 206,
  actualCost: 229,
  departureAddress: 300,
  intermediatePointsCount: 270,
  destinationAddress: 300,
  status: 180,
  department: 260,
  purpose: 260,
};
