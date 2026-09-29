import React, { useMemo } from 'react';
import { useTranslation } from 'i18n';
import { useRouteMatch } from 'react-router-dom';
import { ColumnProps } from 'antd/lib/table';

import { useGroupTransferTransportClasses } from 'api/passengers-transport-classes';
import { SortFields, TableRecord } from '../types/types';

import { fullNameLastFirstPat } from '../utils/fullNameLastFirstPat';
import { CellLink } from 'modules/Registry/components/Cells/CellLink/CellLink';
import { formatTime } from 'utils/formatTime';

import { GroupTransferStatusNames, GroupTransferStatuses, VALUE_NOT_FOUND } from '../constants/groupTransfer.constants';

import { RegistriesColumn } from 'shared/styles/styles';

export type TripRegistryColumnProps = ColumnProps<TableRecord> &
  ({ sorting: true; sortProperty: SortFields } | { sorting?: false; sortProperty?: never }) & { checked?: boolean };

export const useColumns = (): TripRegistryColumnProps[] => {
  const match = useRouteMatch();
  const { t } = useTranslation();
  const { data: groupTransferClasses } = useGroupTransferTransportClasses();

  const getGroupTransferClassName = (groupTransferClass: string | undefined) => {
    return (
      groupTransferClass
        ? groupTransferClasses.find(item => item.value === groupTransferClass)?.rusName || VALUE_NOT_FOUND
        : VALUE_NOT_FOUND
    );
  };
  const renderString = (value: string | undefined) => value || VALUE_NOT_FOUND;
  const renderDate = (value: number | undefined) => formatTime(value);

  return useMemo<TripRegistryColumnProps[]>(
    () => [
      {
        dataIndex: 'humanReadableId',
        title: t.Forms.informationAttributesOfRegistries.requestIdVisible,
        key: 'humanReadableId',
        fixed: 'left',
        width: 156,
        render: (humanReadableId: string, record): JSX.Element => (
          <CellLink to={`${match.path}/${record.id}`}>{humanReadableId}</CellLink>
        ),
      },
      {
        dataIndex: 'groupTransferClass',
        title: t.Forms.informationAttributesOfRegistries.groupTransferClassVisible,
        key: 'groupTransferClass',
        width: 200,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="200px">{getGroupTransferClassName(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'costCenter',
        title: t.Forms.informationAttributesOfRegistries.mvzVisible,
        key: 'costCenter',
        width: 110,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="110px">{renderString(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'desiredDate',
        title: t.Forms.informationAttributesOfRegistries.desiredDateVisible,
        key: 'desiredDate',
        width: 255,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="255px">{renderDate(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'deadline',
        title: t.Forms.informationAttributesOfRegistries.deadlineVisible,
        checked: false,
        key: 'deadline',
        width: 320,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="320px">{renderDate(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'driverArrivedDatetime',
        title: t.Forms.informationAttributesOfRegistries.driverArrivedDatetimeVisible,
        checked: false,
        key: 'driverArrivedDatetime',
        width: 307,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="307px">{renderDate(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'contractorName',
        title: t.Forms.informationAttributesOfRegistries.counterpartyNameVisible,
        checked: false,
        key: 'contractorName',
        width: 300,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="300px">{renderString(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'approveDate',
        title: t.Forms.informationAttributesOfRegistries.approveDateVisible,
        key: 'approveDate',
        width: 211,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="211px">{renderDate(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'authorFIO',
        title: t.Forms.informationAttributesOfRegistries.passengerFioVisible,
        key: 'authorFIO',
        width: 286,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="286px">{renderString(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'status',
        title: t.Forms.informationAttributesOfRegistries.requestStatusVisible,
        key: 'status',
        width: 232,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="232px">
            {column && (column in GroupTransferStatuses)
              ? GroupTransferStatusNames[column as GroupTransferStatuses]
              : VALUE_NOT_FOUND}
          </RegistriesColumn>
        ),
      },
      {
        dataIndex: 'expectedCost',
        key: 'expectedCost',
        title: t.Forms.informationAttributesOfRegistries.plannedPriceVisible,
        align: 'right',
        width: 206,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="206px">{renderString(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'tripFactPrice',
        key: 'tripFactPrice',
        title: t.Forms.informationAttributesOfRegistries.tripFactPriceVisible,
        align: 'right',
        width: 229,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="229px">{renderString(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'expectedDistance',
        key: 'expectedDistance',
        title: t.Forms.informationAttributesOfRegistries.expectedDistanceVisible,
        align: 'right',
        width: 208,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="208px">{renderString(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'tripFactDistance',
        key: 'tripFactDistance',
        title: t.Forms.informationAttributesOfRegistries.drivingLengthVisible,
        align: 'right',
        width: 229,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="229px">{renderString(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'organizationalUnitCode',
        key: 'organizationalUnitCode',
        title: t.Forms.informationAttributesOfRegistries.organizationalUnitCode,
        align: 'right',
        width: 166,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="166px">{renderString(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'vehicle',
        key: 'vehicle',
        title: t.Forms.informationAttributesOfRegistries.carVisible,
        width: 166,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="166px">
            {
            `${column?.brand || VALUE_NOT_FOUND} ${column?.model || VALUE_NOT_FOUND} ${column?.stateNumber || VALUE_NOT_FOUND}`
            }
          </RegistriesColumn>
        ),
      },
      {
        dataIndex: 'driver',
        key: 'driver',
        title: t.Forms.informationAttributesOfRegistries.driverFullName,
        width: 200,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="200px">{fullNameLastFirstPat(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'driverPhone',
        key: 'driverPhone',
        title: t.Forms.informationAttributesOfRegistries.driverPhone,
        width: 200,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="200px">{renderString(column)}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'passengerCount',
        key: 'passengerCount',
        title: t.Forms.informationAttributesOfRegistries.passengersCountVisible,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="200px">{column}</RegistriesColumn>
        ),
      },
      {
        dataIndex: 'commentForDriver',
        key: 'commentForDriver',
        title: t.Forms.informationAttributesOfRegistries.commentForDriverVisible,
        render: (column): JSX.Element => (
          <RegistriesColumn minWidth="200px">{renderString(column)}</RegistriesColumn>
        ),
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, match]
  );
};
