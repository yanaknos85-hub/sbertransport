import { DeadlineSettingsType } from 'stores/DeadlineSettings/DeadlineSettings.interface';
import { FormInstance } from 'antd/es/form';

export type SettingUnit = 'MINUTES' | 'HOURS' | 'DAYS';

interface SettingsFormData {
  carshareLimitInput: number;
  carshareLimitRadio: SettingUnit;
  depLimitInput: number;
  depLimitRadio: SettingUnit;
  personnelApproveInput: number;
  personnelApproveRadio: SettingUnit;
  personnelApprovementInput: number;
  personnelApprovementRadio: SettingUnit;
  personnelJoinInput: number;
  personnelJoinRadio: SettingUnit;
  personnelLimitInput: number;
  personnelLimitRadio: SettingUnit;
  personnelTripInput: number;
  personnelTripRadio: SettingUnit;
  personnelOrderPaymentInput: number;
  personnelOrderPaymentRadio: SettingUnit;
  personnelPaymentAwaitingInput: number;
  personnelPaymentAwaitingRadio: SettingUnit;
  publicAffirmativeInput: number;
  publicAffirmativeRadio: SettingUnit;
  publicApprovalInput: number;
  publicApprovalRadio: SettingUnit;
  publicConfirmationInput: number;
  publicConfirmationRadio: SettingUnit;
  publicOrderPaymentInput: number;
  publicOrderPaymentRadio: SettingUnit;
  publicPaymentAwaitingInput: number;
  publicPaymentAwaitingRadio: SettingUnit;
  taxiApproveInput: number;
  taxiApproveRadio: SettingUnit;
  taxiFinishedInput: number;
  taxiFinishedRadio: SettingUnit;
  taxiSearchInput: number;
  taxiSearchRadio: SettingUnit;
  cargoDedicatedAwaitingApprovalInput: number;
  cargoDedicatedAwaitingApprovalRadio: SettingUnit;
  cargoDedicatedApprovedInput: number;
  cargoDedicatedApprovedRadio: SettingUnit;
  cargoDedicatedInWorkInput: number;
  cargoDedicatedInWorkRadio: SettingUnit;
  cargoDedicatedDeliveryConfirmationInput: number;
  cargoDedicatedDeliveryConfirmationRadio: SettingUnit;
  cargoDedicatedTrialInput: number;
  cargoDedicatedTrialRadio: SettingUnit;
  cargoCourierAwaitingApprovalInput: number;
  cargoCourierAwaitingApprovalRadio: SettingUnit;
  cargoCourierApprovedInput: number;
  cargoCourierApprovedRadio: SettingUnit;
  cargoCourierInWorkInput: number;
  cargoCourierInWorkRadio: SettingUnit;
  cargoCourierDeliveryConfirmationInput: number;
  cargoCourierDeliveryConfirmationRadio: SettingUnit;
  cargoCourierTrialInput: number;
  cargoCourierTrialRadio: SettingUnit;
  cargoInterregionalAwaitingApprovalInput: number;
  cargoInterregionalAwaitingApprovalRadio: SettingUnit;
  cargoInterregionalApprovedInput: number;
  cargoInterregionalApprovedRadio: SettingUnit;
  cargoInterregionalInWorkInput: number;
  cargoInterregionalInWorkRadio: SettingUnit;
  cargoInterregionalDeliveryConfirmationInput: number;
  cargoInterregionalDeliveryConfirmationRadio: SettingUnit;
  cargoInterregionalTrialInput: number;
  cargoInterregionalTrialRadio: SettingUnit;
}

export const prepareToSubmitForm = (
  formData: SettingsFormData,
  orgId: string,
  currentDeadlineSettings: DeadlineSettingsType,
  settingsId?: string
): Omit<DeadlineSettingsType, 'id'> | DeadlineSettingsType => ({
  organizationId: orgId,
  taxiAwaitingApprovalDeadline: formData.taxiApproveInput
    ? {
      unit: formData.taxiApproveRadio,
      value: formData.taxiApproveInput,
    }
    : currentDeadlineSettings.taxiAwaitingApprovalDeadline,
  taxiAwaitingSearchDeadline: formData.taxiSearchInput
    ? {
      unit: formData.taxiSearchRadio,
      value: formData.taxiSearchInput,
    }
    : currentDeadlineSettings.taxiAwaitingSearchDeadline,
  taxiTripFinishedDeadline: {
    unit: formData.taxiFinishedRadio,
    value: formData.taxiFinishedInput,
  },
  personalAwaitingApprovalDeadline: formData.personnelApproveInput
    ? {
      unit: formData.personnelApproveRadio,
      value: formData.personnelApproveInput,
    }
    : currentDeadlineSettings.personalAwaitingApprovalDeadline,
  personalAwaitingSharedRideApprovalDeadline: formData.personnelJoinInput
    ? {
      unit: formData.personnelJoinRadio,
      value: formData.personnelJoinInput,
    }
    : currentDeadlineSettings.personalAwaitingSharedRideApprovalDeadline,
  personalTripInProgressDeadline: formData.personnelTripInput
    ? {
      unit: formData.personnelTripRadio,
      value: formData.personnelTripInput,
    }
    : currentDeadlineSettings.personalTripInProgressDeadline,
  personalAwaitingTripApprovalDeadline: formData.personnelApprovementInput
    ? {
      unit: formData.personnelApprovementRadio,
      value: formData.personnelApprovementInput,
    }
    : currentDeadlineSettings.personalAwaitingTripApprovalDeadline,
  personalOrderPaymentFormationDeadline: formData.personnelOrderPaymentInput
    ? {
      unit: formData.personnelOrderPaymentRadio,
      value: formData.personnelOrderPaymentInput,
    }
    : currentDeadlineSettings.personalOrderPaymentFormationDeadline,
  personalPaymentAwaitingDeadline: formData.personnelPaymentAwaitingInput
    ? {
      unit: formData.personnelPaymentAwaitingRadio,
      value: formData.personnelPaymentAwaitingInput,
    }
    : currentDeadlineSettings.personalPaymentAwaitingDeadline,
  publicAwaitingApprovalDeadline: formData.publicApprovalInput
    ? {
      unit: formData.publicApprovalRadio,
      value: formData.publicApprovalInput,
    }
    : currentDeadlineSettings.publicAwaitingApprovalDeadline,
  publicTripConfirmationDeadline: formData.publicConfirmationInput
    ? {
      unit: formData.publicConfirmationRadio,
      value: formData.publicConfirmationInput,
    }
    : currentDeadlineSettings.publicTripConfirmationDeadline,
  publicAwaitingAffirmativeDeadline: formData.publicAffirmativeInput
    ? {
      unit: formData.publicAffirmativeRadio,
      value: formData.publicAffirmativeInput,
    }
    : currentDeadlineSettings.publicAwaitingAffirmativeDeadline,
  publicOrderPaymentFormationDeadline: formData.publicOrderPaymentInput
    ? {
      unit: formData.publicOrderPaymentRadio,
      value: formData.publicOrderPaymentInput,
    }
    : currentDeadlineSettings.publicOrderPaymentFormationDeadline,
  publicPaymentAwaitingDeadline: formData.publicPaymentAwaitingInput
    ? {
      unit: formData.publicPaymentAwaitingRadio,
      value: formData.publicPaymentAwaitingInput,
    }
    : currentDeadlineSettings.publicPaymentAwaitingDeadline,
  employeeLimitDeadline: formData.personnelLimitInput
    ? {
      unit: formData.personnelLimitRadio,
      value: formData.personnelLimitInput,
    }
    : currentDeadlineSettings.employeeLimitDeadline,
  departmentLimitDeadline: formData.depLimitInput
    ? {
      unit: formData.depLimitRadio,
      value: formData.depLimitInput,
    }
    : currentDeadlineSettings.departmentLimitDeadline,
  carsharingJoinDeadline: formData.carshareLimitInput
    ? {
      unit: formData.carshareLimitRadio,
      value: formData.carshareLimitInput,
    }
    : currentDeadlineSettings.carsharingJoinDeadline,
  cargoDedicatedAwaitingApprovalDeadline: formData.cargoDedicatedAwaitingApprovalInput
    ? {
      unit: formData.cargoDedicatedAwaitingApprovalRadio,
      value: formData.cargoDedicatedAwaitingApprovalInput,
    }
    : currentDeadlineSettings.cargoDedicatedAwaitingApprovalDeadline,
  cargoDedicatedApprovedDeadline: formData.cargoDedicatedApprovedInput
    ? {
      unit: formData.cargoDedicatedApprovedRadio,
      value: formData.cargoDedicatedApprovedInput,
    }
    : currentDeadlineSettings.cargoDedicatedApprovedDeadline,
  cargoDedicatedInWorkDeadline: formData.cargoDedicatedInWorkInput
    ? {
      unit: formData.cargoDedicatedInWorkRadio,
      value: formData.cargoDedicatedInWorkInput,
    }
    : currentDeadlineSettings.cargoDedicatedInWorkDeadline,
  cargoDedicatedDeliveryConfirmationDeadline: formData.cargoDedicatedDeliveryConfirmationInput
    ? {
      unit: formData.cargoDedicatedDeliveryConfirmationRadio,
      value: formData.cargoDedicatedDeliveryConfirmationInput,
    }
    : currentDeadlineSettings.cargoDedicatedDeliveryConfirmationDeadline,
  cargoDedicatedTrialDeadline: formData.cargoDedicatedTrialInput
    ? {
      unit: formData.cargoDedicatedTrialRadio,
      value: formData.cargoDedicatedTrialInput,
    }
    : currentDeadlineSettings.cargoDedicatedTrialDeadline,
  cargoCourierAwaitingApprovalDeadline: formData.cargoCourierAwaitingApprovalInput
    ? {
      unit: formData.cargoCourierAwaitingApprovalRadio,
      value: formData.cargoCourierAwaitingApprovalInput,
    }
    : currentDeadlineSettings.cargoCourierAwaitingApprovalDeadline,
  cargoCourierApprovedDeadline: formData.cargoCourierApprovedInput
    ? {
      unit: formData.cargoCourierApprovedRadio,
      value: formData.cargoCourierApprovedInput,
    }
    : currentDeadlineSettings.cargoCourierApprovedDeadline,
  cargoCourierInWorkDeadline: formData.cargoCourierInWorkInput
    ? {
      unit: formData.cargoCourierInWorkRadio,
      value: formData.cargoCourierInWorkInput,
    }
    : currentDeadlineSettings.cargoCourierInWorkDeadline,
  cargoCourierDeliveryConfirmationDeadline: formData.cargoCourierDeliveryConfirmationInput
    ? {
      unit: formData.cargoCourierDeliveryConfirmationRadio,
      value: formData.cargoCourierDeliveryConfirmationInput,
    }
    : currentDeadlineSettings.cargoCourierDeliveryConfirmationDeadline,
  cargoCourierTrialDeadline: formData.cargoCourierTrialInput
    ? {
      unit: formData.cargoCourierTrialRadio,
      value: formData.cargoCourierTrialInput,
    }
    : currentDeadlineSettings.cargoCourierTrialDeadline,
  cargoInterregionalAwaitingApprovalDeadline: formData.cargoInterregionalAwaitingApprovalInput
    ? {
      unit: formData.cargoInterregionalAwaitingApprovalRadio,
      value: formData.cargoInterregionalAwaitingApprovalInput,
    }
    : currentDeadlineSettings.cargoInterregionalAwaitingApprovalDeadline,
  cargoInterregionalApprovedDeadline: formData.cargoInterregionalApprovedInput
    ? {
      unit: formData.cargoInterregionalApprovedRadio,
      value: formData.cargoInterregionalApprovedInput,
    }
    : currentDeadlineSettings.cargoInterregionalApprovedDeadline,
  cargoInterregionalInWorkDeadline: formData.cargoInterregionalInWorkInput
    ? {
      unit: formData.cargoInterregionalInWorkRadio,
      value: formData.cargoInterregionalInWorkInput,
    }
    : currentDeadlineSettings.cargoInterregionalInWorkDeadline,
  cargoInterregionalDeliveryConfirmationDeadline: formData.cargoInterregionalDeliveryConfirmationInput
    ? {
      unit: formData.cargoInterregionalDeliveryConfirmationRadio,
      value: formData.cargoInterregionalDeliveryConfirmationInput,
    }
    : currentDeadlineSettings.cargoInterregionalDeliveryConfirmationDeadline,
  cargoInterregionalTrialDeadline: formData.cargoInterregionalTrialInput
    ? {
      unit: formData.cargoInterregionalTrialRadio,
      value: formData.cargoInterregionalTrialInput,
    }
    : currentDeadlineSettings.cargoInterregionalTrialDeadline,
  ...(settingsId && { settingsId }),
});

export const fillFormInitialValues = (form: FormInstance, settings: DeadlineSettingsType): void => {
  const initialValues: SettingsFormData = {
    carshareLimitInput: settings.carsharingJoinDeadline.value,
    carshareLimitRadio: settings.carsharingJoinDeadline.unit,
    depLimitInput: settings.departmentLimitDeadline.value,
    depLimitRadio: settings.departmentLimitDeadline.unit,
    personnelApproveInput: settings.personalAwaitingApprovalDeadline.value,
    personnelApproveRadio: settings.personalAwaitingApprovalDeadline.unit,
    personnelApprovementInput: settings.personalAwaitingTripApprovalDeadline.value,
    personnelApprovementRadio: settings.personalAwaitingTripApprovalDeadline.unit,
    personnelJoinInput: settings.personalAwaitingSharedRideApprovalDeadline.value,
    personnelJoinRadio: settings.personalAwaitingSharedRideApprovalDeadline.unit,
    personnelLimitInput: settings.employeeLimitDeadline.value,
    personnelLimitRadio: settings.employeeLimitDeadline.unit,
    personnelTripInput: settings.personalTripInProgressDeadline.value,
    personnelTripRadio: settings.personalTripInProgressDeadline.unit,
    personnelOrderPaymentInput: settings.personalOrderPaymentFormationDeadline.value,
    personnelOrderPaymentRadio: settings.personalOrderPaymentFormationDeadline.unit,
    personnelPaymentAwaitingInput: settings.personalPaymentAwaitingDeadline.value,
    personnelPaymentAwaitingRadio: settings.personalPaymentAwaitingDeadline.unit,
    publicAffirmativeInput: settings.publicAwaitingAffirmativeDeadline.value,
    publicAffirmativeRadio: settings.publicAwaitingAffirmativeDeadline.unit,
    publicApprovalInput: settings.publicAwaitingApprovalDeadline.value,
    publicApprovalRadio: settings.publicAwaitingApprovalDeadline.unit,
    publicConfirmationInput: settings.publicTripConfirmationDeadline.value,
    publicConfirmationRadio: settings.publicTripConfirmationDeadline.unit,
    publicOrderPaymentInput: settings.publicOrderPaymentFormationDeadline.value,
    publicOrderPaymentRadio: settings.publicOrderPaymentFormationDeadline.unit,
    publicPaymentAwaitingInput: settings.publicPaymentAwaitingDeadline.value,
    publicPaymentAwaitingRadio: settings.publicPaymentAwaitingDeadline.unit,
    taxiApproveInput: settings.taxiAwaitingApprovalDeadline.value,
    taxiApproveRadio: settings.taxiAwaitingApprovalDeadline.unit,
    taxiFinishedInput: settings.taxiTripFinishedDeadline.value,
    taxiFinishedRadio: settings.taxiTripFinishedDeadline.unit,
    taxiSearchInput: settings.taxiAwaitingSearchDeadline.value,
    taxiSearchRadio: settings.taxiAwaitingSearchDeadline.unit,
    cargoDedicatedAwaitingApprovalInput: settings.cargoDedicatedAwaitingApprovalDeadline.value,
    cargoDedicatedAwaitingApprovalRadio: settings.cargoDedicatedAwaitingApprovalDeadline.unit,
    cargoDedicatedApprovedInput: settings.cargoDedicatedApprovedDeadline.value,
    cargoDedicatedApprovedRadio: settings.cargoDedicatedApprovedDeadline.unit,
    cargoDedicatedInWorkInput: settings.cargoDedicatedInWorkDeadline.value,
    cargoDedicatedInWorkRadio: settings.cargoDedicatedInWorkDeadline.unit,
    cargoDedicatedDeliveryConfirmationInput: settings.cargoDedicatedDeliveryConfirmationDeadline.value,
    cargoDedicatedDeliveryConfirmationRadio: settings.cargoDedicatedDeliveryConfirmationDeadline.unit,
    cargoDedicatedTrialInput: settings.cargoDedicatedTrialDeadline.value,
    cargoDedicatedTrialRadio: settings.cargoDedicatedTrialDeadline.unit,
    cargoCourierAwaitingApprovalInput: settings.cargoCourierAwaitingApprovalDeadline.value,
    cargoCourierAwaitingApprovalRadio: settings.cargoCourierAwaitingApprovalDeadline.unit,
    cargoCourierApprovedInput: settings.cargoCourierApprovedDeadline.value,
    cargoCourierApprovedRadio: settings.cargoCourierApprovedDeadline.unit,
    cargoCourierInWorkInput: settings.cargoCourierInWorkDeadline.value,
    cargoCourierInWorkRadio: settings.cargoCourierInWorkDeadline.unit,
    cargoCourierDeliveryConfirmationInput: settings.cargoCourierDeliveryConfirmationDeadline.value,
    cargoCourierDeliveryConfirmationRadio: settings.cargoCourierDeliveryConfirmationDeadline.unit,
    cargoCourierTrialInput: settings.cargoCourierTrialDeadline.value,
    cargoCourierTrialRadio: settings.cargoCourierTrialDeadline.unit,
    cargoInterregionalAwaitingApprovalInput: settings.cargoInterregionalAwaitingApprovalDeadline.value,
    cargoInterregionalAwaitingApprovalRadio: settings.cargoInterregionalAwaitingApprovalDeadline.unit,
    cargoInterregionalApprovedInput: settings.cargoInterregionalApprovedDeadline.value,
    cargoInterregionalApprovedRadio: settings.cargoInterregionalApprovedDeadline.unit,
    cargoInterregionalInWorkInput: settings.cargoInterregionalInWorkDeadline.value,
    cargoInterregionalInWorkRadio: settings.cargoInterregionalInWorkDeadline.unit,
    cargoInterregionalDeliveryConfirmationInput: settings.cargoInterregionalDeliveryConfirmationDeadline.value,
    cargoInterregionalDeliveryConfirmationRadio: settings.cargoInterregionalDeliveryConfirmationDeadline.unit,
    cargoInterregionalTrialInput: settings.cargoInterregionalTrialDeadline.value,
    cargoInterregionalTrialRadio: settings.cargoInterregionalTrialDeadline.unit,
  };
  form.setFieldsValue({ ...initialValues });
};
