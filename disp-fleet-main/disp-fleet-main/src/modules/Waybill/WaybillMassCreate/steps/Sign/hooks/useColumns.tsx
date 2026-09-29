import { useMemo } from 'react';
import type { ColumnType } from 'antd/lib/table';

import { useTranslation } from 'i18n';

interface SignDataSourceItem {
  key: string;
  humanReadableId: string;
  startDate?: string;
  driverFullName?: string;
  driverPersonnelNumber?: string;
  drivingLicenseSeries?: string;
  drivingLicenseNumber?: string;
  drivingLicenseIssueDate?: string;
  finishDate?: string;
  transportationType?: string;
  communicationType?: string;
  organizationName?: string;
  organizationOgrn?: string;
  organizationTin?: string;
  stateNumber?: string;
  vehicleBrand?: string;
  vehicleModel?: string;
  transportType?: string;
  documentDate?: string;
}

export const useColumns = () => {
  const { createMass: i18 } = useTranslation().t.Waybill;

  const columns: ColumnType<SignDataSourceItem>[] = useMemo(() => {
    return [
      {
        title: i18.humanReadableId,
        dataIndex: 'humanReadableId',
        key: 'humanReadableId',
        width: 170,
      },
      {
        title: i18.documentDate,
        dataIndex: 'documentDate',
        key: 'documentDate',
        width: 140,
      },
      {
        title: `${i18.startDate}`,
        dataIndex: 'startDate',
        key: 'startDate',
        width: 140,
      },
      {
        title: `${i18.driverFullName} (${i18.driverPersonnelNumber})`,
        dataIndex: 'driverFullName',
        key: 'driverFullName',
        width: 315,
        render: (value: string, record: SignDataSourceItem) => {
          const personnelNumber = record.driverPersonnelNumber;
          return personnelNumber ? `${value} (${personnelNumber})` : value;
        },
      },
      {
        title: i18.drivingLicenseSeries,
        dataIndex: 'drivingLicenseSeries',
        key: 'drivingLicenseSeries',
        width: 92,
      },
      {
        title: i18.drivingLicenseNumber,
        dataIndex: 'drivingLicenseNumber',
        key: 'drivingLicenseNumber',
        width: 100,
      },
      {
        title: i18.drivingLicenseIssueDate,
        dataIndex: 'drivingLicenseIssueDate',
        key: 'drivingLicenseIssueDate',
        width: 133,
      },
      {
        title: i18.finishDate,
        dataIndex: 'finishDate',
        key: 'finishDate',
        width: 140,
      },
      {
        title: i18.transportationType,
        dataIndex: 'transportationType',
        key: 'transportationType',
        width: 195,
      },
      {
        title: i18.communicationType,
        dataIndex: 'communicationType',
        key: 'communicationType',
        width: 195,
      },
      {
        title: i18.organizationName,
        dataIndex: 'organizationName',
        key: 'organizationName',
        width: 145,
      },
      {
        title: i18.organizationOgrn,
        dataIndex: 'organizationOgrn',
        key: 'organizationOgrn',
        width: 145,
      },
      {
        title: i18.organizationTin,
        dataIndex: 'organizationTin',
        key: 'organizationTin',
        width: 110,
      },
      {
        title: i18.stateNumber,
        dataIndex: 'stateNumber',
        key: 'stateNumber',
        width: 125,
      },
      {
        title: i18.vehicleBrand,
        dataIndex: 'vehicleBrand',
        key: 'vehicleBrand',
        width: 125,
      },
      {
        title: i18.vehicleModel,
        dataIndex: 'vehicleModel',
        key: 'vehicleModel',
        width: 125,
      },
      {
        title: i18.transportType,
        dataIndex: 'transportType',
        key: 'transportType',
        width: 110,
      },
    ];
  }, [i18]);

  return { columns };
};
