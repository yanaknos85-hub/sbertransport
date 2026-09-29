import { useForm } from 'antd/lib/form/Form';
import {
  useApprovalSettings,
  useDefaultGroupTransferApprovalSettings,
  useDefaultOtherApprovalSettings,
  useDefaultPublicApprovalSettings,
  useDefaultTaxiApprovalSettings,
  useSaveGroupTransferApprovalSettings,
  useSaveOtherApprovalSettings,
  useSavePublicApprovalSettings,
  useSaveTaxiApprovalSettings,
  useUpdateGroupTransferApprovalSettings,
  useUpdateOtherApprovalSettings,
  useUpdatePublicApprovalSettings,
  useUpdateTaxiApprovalSettings
} from 'api/approval-settings';
import { Dispatch, SetStateAction, useCallback } from 'react';
import { Employee } from 'stores/Employee/Employee.interface';
import { GeoZones } from 'stores/GeoZones/GeoZones.interface';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { TripPurpose } from 'stores/TripPurposes/TripPurpose.interface';

import { UUID } from 'utils/io-ts';
import { FormInstance } from 'antd/es/form';
import { ApprovalsRecord, FormValues } from '../types/types';
import { mapFromValuesToApprovalsQuery, mapSettingsToRecords } from '../utils/utils';

export const useTableForm = (
  profile: Employee,
  purposes: Record<string, TripPurpose>,
  geoZones: GeoZones[],
  transportType: TransportTypes,
  state: ApprovalsRecord[],
  setState: Dispatch<SetStateAction<ApprovalsRecord[]>>,
  approvalId?: UUID | null,
  setRebootSettings?: React.Dispatch<React.SetStateAction<boolean>>
): {
    form: FormInstance<FormValues>;
    setFormValues: (values: Record<string, unknown>) => void;
    onFinish: (values?: FormValues) => void;
    onCancel: () => void;
    onDefault: () => void;
  } => {
  const [form] = useForm<FormValues>();

  const { organizationId } = profile;

  const { data: approvalSettings } = useApprovalSettings(organizationId, transportType);

  const [saveTaxiApprovalSettings] = useSaveTaxiApprovalSettings();
  const [savePublicApprovalSettings] = useSavePublicApprovalSettings();
  const [saveGroupTransferApprovalSettings] = useSaveGroupTransferApprovalSettings();
  const [saveOtherApprovalSettings] = useSaveOtherApprovalSettings();

  const [updateTaxiApprovalSettings] = useUpdateTaxiApprovalSettings(approvalId);
  const [updatePublicApprovalSettings] = useUpdatePublicApprovalSettings(approvalId);
  const [updateGroupTransferApprovalSettings] = useUpdateGroupTransferApprovalSettings(approvalId);
  const [updateOtherApprovalSettings] = useUpdateOtherApprovalSettings(organizationId, transportType);

  const [defaultTaxiApprovalSettings] = useDefaultTaxiApprovalSettings(organizationId, approvalId);
  const [defaultPublicApprovalSettings] = useDefaultPublicApprovalSettings(organizationId, approvalId);
  const [defaultGroupTransferApprovalSettings] = useDefaultGroupTransferApprovalSettings(organizationId, approvalId);
  const [defaultOtherApprovalSettings] = useDefaultOtherApprovalSettings(organizationId, transportType);

  const onFinish = useCallback(
    (values: FormValues = form.getFieldsValue(true)): void => {
      // Это строки которые реально существуют!
      const existRows = state.map(({ rowId }) => rowId);

      // Это values с этими строками!
      // Антовская форма сама не вычищает поля которые удалены
      const filteredValues = Object.keys(values)
        .filter(field => {
          const rowId = field.toString().split('_')[1] as UUID;
          return rowId ? existRows.includes(rowId) : true;
        })
        .reduce((acc, key) => ({ ...acc, [key]: values[key] }), {} as typeof values);

      const prepared = mapFromValuesToApprovalsQuery(filteredValues, profile, purposes, geoZones);

      if (approvalId) {
        if (prepared.transportType === TransportTypes.TAXI) {
          updateTaxiApprovalSettings(prepared);
        } else if (prepared.transportType === TransportTypes.PUBLIC) {
          updatePublicApprovalSettings(prepared);
        } else if (prepared.transportType === TransportTypes.GROUP_TRANSFER) {
          updateGroupTransferApprovalSettings(prepared);
        } else {
          updateOtherApprovalSettings(prepared);
        }
        return;
      }

      if (prepared.transportType === TransportTypes.TAXI) {
        saveTaxiApprovalSettings({ query: prepared });
      } else if (prepared.transportType === TransportTypes.PUBLIC) {
        savePublicApprovalSettings({ query: prepared });
      } else if (prepared.transportType === TransportTypes.GROUP_TRANSFER) {
        saveGroupTransferApprovalSettings({ query: prepared });
      } else {
        saveOtherApprovalSettings({ query: prepared });
      }
    },
    [
      form,
      state,
      approvalId,
      geoZones,
      profile,
      purposes,
      saveOtherApprovalSettings,
      savePublicApprovalSettings,
      saveTaxiApprovalSettings,
      updateOtherApprovalSettings,
      updatePublicApprovalSettings,
      updateTaxiApprovalSettings,
    ]
  );

  const onCancel = () => {
    setState(mapSettingsToRecords(geoZones, approvalSettings?.approvalSettings));
  };

  const onDefault = () => {
    if (transportType === TransportTypes.TAXI) {
      defaultTaxiApprovalSettings();
    } else if (transportType === TransportTypes.PUBLIC) {
      defaultPublicApprovalSettings();
    } else if (transportType === TransportTypes.GROUP_TRANSFER) {
      defaultGroupTransferApprovalSettings();
    } else {
      defaultOtherApprovalSettings();
    }

    setRebootSettings && setRebootSettings(true);
  };

  const setFormValues = (values: Partial<FormValues>) => {
    form.setFieldsValue(values);
  };

  return {
    form,
    setFormValues,
    onFinish,
    onCancel,
    onDefault,
  };
};
