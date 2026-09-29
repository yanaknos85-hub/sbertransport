import { useProfile } from 'api/profile';
import {
  useCreateDeadlineSettings,
  useDeadlineSettings,
  useSetDefaultDeadlineSettings,
  useUpdateDeadlineSettings
} from 'api/deadline-settings';
import { DeadlineSettingsType } from 'stores/DeadlineSettings/DeadlineSettings.interface';
import { FormInstance } from 'antd/es/form';
import { Dispatch, SetStateAction } from 'react';
import { UUID } from 'utils/io-ts';
import { fillFormInitialValues, prepareToSubmitForm } from '../utils/utils';

export const useFormActions = (form: FormInstance, setBusy: Dispatch<SetStateAction<boolean>>, settingsId?: string) => {
  const { organizationId } = useProfile().data;
  // @ts-ignore
  const { deadlineSettings } = useDeadlineSettings(organizationId).data;
  // @ts-ignore
  const [createSettings] = useCreateDeadlineSettings(organizationId);
  const [updateSettings] = useUpdateDeadlineSettings();
  const [setDefaults] = useSetDefaultDeadlineSettings();

  const onFinish = () => {
    setBusy(true);
    const data = form.getFieldsValue();
    const submitted = prepareToSubmitForm(data, organizationId, deadlineSettings, settingsId);
    settingsId
      ? updateSettings({
        deadlineSettings: submitted as DeadlineSettingsType,
        // @ts-ignore
        organizationId,
        settingId: settingsId as UUID,
      })
        .then(() => setBusy(false))
        .catch(() => setBusy(false))
      : createSettings({
        deadlineSettings: submitted,
        // @ts-ignore
        organizationId,
      })
        .then(() => setBusy(false))
        .catch(() => setBusy(false));
  };

  const onCancel = () => {
    setBusy(true);
    fillFormInitialValues(form, deadlineSettings);
    setBusy(false);
  };

  const onDefault = () => {
    setBusy(true);
    // @ts-ignore
    setDefaults({ organizationId, settingId: settingsId as UUID })
      .then(() => setBusy(false))
      .catch(() => setBusy(false));
  };

  return {
    onFinish,
    onCancel,
    onDefault,
  };
};
