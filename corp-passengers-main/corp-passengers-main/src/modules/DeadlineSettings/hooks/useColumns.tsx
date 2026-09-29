import React, { useMemo } from 'react';
import { ColumnProps } from 'antd/lib/table';
import { useTranslation } from 'i18n';
import { InputWithCheckboxes } from '../components/InputWithCheckboxes/InputWithCheckboxes';
import { DeadlineColumns } from '../types/types';

export interface DeadlineSettings {
  claimStatus: string;
  deadline: string;
  description: string;
}

export const useColumns = (): DeadlineColumns => {
  const { t } = useTranslation();

  const taxiColumns = useMemo(
    (): ColumnProps<DeadlineSettings>[] => [
      {
        title: t.DeadlineSettings.Columns.claimStatus,
        dataIndex: 'claimStatus',
        key: 'taxiClaimStatus',
      },
      {
        dataIndex: 'deadline',
        title: t.DeadlineSettings.Columns.deadline,
        key: 'taxiDeadline',
        render: (value, record, index) => {
          switch (index) {
            case 0:
              return (
                <InputWithCheckboxes
                  inputName="taxiApproveInput"
                  radioName="taxiApproveRadio"
                  includeAll
                />
              );
            case 1:
              return (
                <InputWithCheckboxes
                  inputName="taxiSearchInput"
                  radioName="taxiSearchRadio"
                  includeMinutes
                />
              );
            case 2:
              return (
                <InputWithCheckboxes
                  inputName="taxiFinishedInput"
                  radioName="taxiFinishedRadio"
                  includeMinutes
                />
              );
            default:
              return null;
          }
        },
      },
      {
        dataIndex: 'description',
        title: t.DeadlineSettings.Columns.description,
        key: 'taxiDescription',
      },
    ],
    [t]
  );

  const personnelColumns = useMemo(
    (): ColumnProps<DeadlineSettings>[] => [
      {
        title: t.DeadlineSettings.Columns.claimStatus,
        dataIndex: 'claimStatus',
        key: 'personnelClaimStatus',
      },
      {
        dataIndex: 'deadline',
        title: t.DeadlineSettings.Columns.deadline,
        key: 'personnelDeadline',
        render: (value, record, index) => {
          switch (index) {
            case 0:
              return (
                <InputWithCheckboxes
                  inputName="personnelApproveInput"
                  radioName="personnelApproveRadio"
                  includeAll
                />
              );
            case 1:
              return (
                <InputWithCheckboxes
                  inputName="personnelJoinInput"
                  radioName="personnelJoinRadio"
                  includeAll
                />
              );
            case 2:
              return (
                <InputWithCheckboxes
                  inputName="personnelTripInput"
                  radioName="personnelTripRadio"
                  includeDays
                />
              );
            case 3:
              return (
                <InputWithCheckboxes
                  inputName="personnelApprovementInput"
                  radioName="personnelApprovementRadio"
                  includeAll
                />
              );
            case 4:
              return (
                <InputWithCheckboxes
                  inputName="personnelOrderPaymentInput"
                  radioName="personnelOrderPaymentRadio"
                  includeAll
                />
              );
            case 5:
              return (
                <InputWithCheckboxes
                  inputName="personnelPaymentAwaitingInput"
                  radioName="personnelPaymentAwaitingRadio"
                  includeAll
                />
              );
            default:
              return null;
          }
        },
      },
      {
        dataIndex: 'description',
        title: t.DeadlineSettings.Columns.description,
        key: 'personnelDescription',
      },
    ],
    [t]
  );

  const publicColumns = useMemo(
    (): ColumnProps<DeadlineSettings>[] => [
      {
        title: t.DeadlineSettings.Columns.claimStatus,
        dataIndex: 'claimStatus',
        key: 'publicClaimStatus',
      },
      {
        dataIndex: 'deadline',
        title: t.DeadlineSettings.Columns.deadline,
        key: 'publicDeadline',
        render: (value, record, index) => {
          switch (index) {
            case 0:
              return (
                <InputWithCheckboxes
                  inputName="publicApprovalInput"
                  radioName="publicApprovalRadio"
                  includeAll
                />
              );
            case 1:
              return (
                <InputWithCheckboxes
                  inputName="publicConfirmationInput"
                  radioName="publicConfirmationRadio"
                  includeDays
                />
              );
            case 2:
              return (
                <InputWithCheckboxes
                  inputName="publicAffirmativeInput"
                  radioName="publicAffirmativeRadio"
                  includeAll
                />
              );
            case 3:
              return (
                <InputWithCheckboxes
                  inputName="publicOrderPaymentInput"
                  radioName="publicOrderPaymentRadio"
                  includeAll
                />
              );
            case 4:
              return (
                <InputWithCheckboxes
                  inputName="publicPaymentAwaitingInput"
                  radioName="publicPaymentAwaitingRadio"
                  includeAll
                />
              );
            default:
              return null;
          }
        },
      },
      {
        dataIndex: 'description',
        title: t.DeadlineSettings.Columns.description,
        key: 'publicDescription',
      },
    ],
    [t]
  );

  const personnelLimitColumns = useMemo(
    (): ColumnProps<DeadlineSettings>[] => [
      {
        title: t.DeadlineSettings.Columns.claimStatus,
        dataIndex: 'claimStatus',
        key: 'personnelLimitClaimStatus',
      },
      {
        dataIndex: 'deadline',
        title: t.DeadlineSettings.Columns.deadline,
        key: 'personnelLimitDeadline',
        render: () => (
          <InputWithCheckboxes
            inputName="personnelLimitInput"
            radioName="personnelLimitRadio"
            includeAll
          />
        ),
      },
      {
        dataIndex: 'description',
        title: t.DeadlineSettings.Columns.description,
        key: 'personnelLimitDescription',
      },
    ],
    [t]
  );

  const depLimitColumns = useMemo(
    (): ColumnProps<DeadlineSettings>[] => [
      {
        title: t.DeadlineSettings.Columns.claimStatus,
        dataIndex: 'claimStatus',
        key: 'depLimitClaimStatus',
      },
      {
        dataIndex: 'deadline',
        title: t.DeadlineSettings.Columns.deadline,
        key: 'depLimitDeadline',
        render: () => (
          <InputWithCheckboxes
            inputName="depLimitInput"
            radioName="depLimitRadio"
            includeAll
          />
        ),
      },
      {
        dataIndex: 'description',
        title: t.DeadlineSettings.Columns.description,
        key: 'depLimitDescription',
      },
    ],
    [t]
  );

  const carsharingColumns = useMemo(
    (): ColumnProps<DeadlineSettings>[] => [
      {
        title: t.DeadlineSettings.Columns.claimStatus,
        dataIndex: 'claimStatus',
        key: 'carshareClaimStatus',
      },
      {
        dataIndex: 'deadline',
        title: t.DeadlineSettings.Columns.deadline,
        key: 'carshareDeadline',
        render: () => (
          <InputWithCheckboxes
            inputName="carshareLimitInput"
            radioName="carshareLimitRadio"
            includeAll
          />
        ),
      },
      {
        dataIndex: 'description',
        title: t.DeadlineSettings.Columns.description,
        key: 'carshareDescription',
      },
    ],
    [t]
  );

  const cargoDedicatedColumns = useMemo(
    (): ColumnProps<DeadlineSettings>[] => [
      {
        title: t.DeadlineSettings.Columns.claimStatus,
        dataIndex: 'claimStatus',
        key: 'cargoDedicatedClaimStatus',
      },
      {
        dataIndex: 'deadline',
        title: t.DeadlineSettings.Columns.deadline,
        key: 'cargoDedicatedDeadline',
        render: (value, record, index) => {
          switch (index) {
            case 0:
              return (
                <InputWithCheckboxes
                  inputName="cargoDedicatedAwaitingApprovalInput"
                  radioName="cargoDedicatedAwaitingApprovalRadio"
                  includeAll
                />
              );
            case 1:
              return (
                <InputWithCheckboxes
                  inputName="cargoDedicatedApprovedInput"
                  radioName="cargoDedicatedApprovedRadio"
                  includeAll
                />
              );
            case 2:
              return (
                <InputWithCheckboxes
                  inputName="cargoDedicatedInWorkInput"
                  radioName="cargoDedicatedInWorkRadio"
                  includeAll
                />
              );
            case 3:
              return (
                <InputWithCheckboxes
                  inputName="cargoDedicatedDeliveryConfirmationInput"
                  radioName="cargoDedicatedDeliveryConfirmationRadio"
                  includeAll
                />
              );
            case 4:
              return (
                <InputWithCheckboxes
                  inputName="cargoDedicatedTrialInput"
                  radioName="cargoDedicatedTrialRadio"
                  includeAll
                />
              );
            default:
              return null;
          }
        },
      },
      {
        dataIndex: 'description',
        title: t.DeadlineSettings.Columns.description,
        key: 'cargoDedicatedDescription',
      },
    ],
    [t]
  );

  const cargoCourierColumns = useMemo(
    (): ColumnProps<DeadlineSettings>[] => [
      {
        title: t.DeadlineSettings.Columns.claimStatus,
        dataIndex: 'claimStatus',
        key: 'cargoCourierClaimStatus',
      },
      {
        dataIndex: 'deadline',
        title: t.DeadlineSettings.Columns.deadline,
        key: 'cargoCourierDeadline',
        render: (value, record, index) => {
          switch (index) {
            case 0:
              return (
                <InputWithCheckboxes
                  inputName="cargoCourierAwaitingApprovalInput"
                  radioName="cargoCourierAwaitingApprovalRadio"
                  includeAll
                />
              );
            case 1:
              return (
                <InputWithCheckboxes
                  inputName="cargoCourierApprovedInput"
                  radioName="cargoCourierApprovedRadio"
                  includeAll
                />
              );
            case 2:
              return (
                <InputWithCheckboxes
                  inputName="cargoCourierInWorkInput"
                  radioName="cargoCourierInWorkRadio"
                  includeAll
                />
              );
            case 3:
              return (
                <InputWithCheckboxes
                  inputName="cargoCourierDeliveryConfirmationInput"
                  radioName="cargoCourierDeliveryConfirmationRadio"
                  includeAll
                />
              );
            case 4:
              return (
                <InputWithCheckboxes
                  inputName="cargoCourierTrialInput"
                  radioName="cargoCourierTrialRadio"
                  includeAll
                />
              );
            default:
              return null;
          }
        },
      },
      {
        dataIndex: 'description',
        title: t.DeadlineSettings.Columns.description,
        key: 'cargoCourierDescription',
      },
    ],
    [t]
  );

  const cargoInterregionalColumns = useMemo(
    (): ColumnProps<DeadlineSettings>[] => [
      {
        title: t.DeadlineSettings.Columns.claimStatus,
        dataIndex: 'claimStatus',
        key: 'cargoInterregionalClaimStatus',
      },
      {
        dataIndex: 'deadline',
        title: t.DeadlineSettings.Columns.deadline,
        key: 'cargoInterregionalDeadline',
        render: (value, record, index) => {
          switch (index) {
            case 0:
              return (
                <InputWithCheckboxes
                  inputName="cargoInterregionalAwaitingApprovalInput"
                  radioName="cargoInterregionalAwaitingApprovalRadio"
                  includeAll
                />
              );
            case 1:
              return (
                <InputWithCheckboxes
                  inputName="cargoInterregionalApprovedInput"
                  radioName="cargoInterregionalApprovedRadio"
                  includeAll
                />
              );
            case 2:
              return (
                <InputWithCheckboxes
                  inputName="cargoInterregionalInWorkInput"
                  radioName="cargoInterregionalInWorkRadio"
                  includeAll
                />
              );
            case 3:
              return (
                <InputWithCheckboxes
                  inputName="cargoInterregionalDeliveryConfirmationInput"
                  radioName="cargoInterregionalDeliveryConfirmationRadio"
                  includeAll
                />
              );
            case 4:
              return (
                <InputWithCheckboxes
                  inputName="cargoInterregionalTrialInput"
                  radioName="cargoInterregionalTrialRadio"
                  includeAll
                />
              );
            default:
              return null;
          }
        },
      },
      {
        dataIndex: 'description',
        title: t.DeadlineSettings.Columns.description,
        key: 'cargoInterregionalDescription',
      },
    ],
    [t]
  );

  return {
    taxi: taxiColumns,
    personnel: personnelColumns,
    carsharing: carsharingColumns,
    public: publicColumns,
    personnelLimit: personnelLimitColumns,
    depLimit: depLimitColumns,
    cargoDedicated: cargoDedicatedColumns,
    cargoCourier: cargoCourierColumns,
    cargoInterregional: cargoInterregionalColumns,
  };
};
