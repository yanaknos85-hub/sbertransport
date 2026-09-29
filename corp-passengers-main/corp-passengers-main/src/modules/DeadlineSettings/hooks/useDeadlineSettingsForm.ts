import { useProfile } from 'api/profile';
import {
  Dispatch, SetStateAction, useState, useEffect
} from 'react';
import { useDeadlineSettings } from 'api/deadline-settings';
import { FormInstance } from 'antd/es/form';
import { DeadlineSettingsType } from 'stores/DeadlineSettings/DeadlineSettings.interface';
import { useForm } from 'antd/lib/form/Form';
import { fillFormInitialValues } from '../utils/utils';

export const useDeadlineSettingsForm = (): {
  form: FormInstance;
  deadlineSettings: DeadlineSettingsType;
  busy: boolean;
  setBusy: Dispatch<SetStateAction<boolean>>;
  settingsId?: string;
  setSettingsId: Dispatch<SetStateAction<string | undefined>>;
} => {
  const [form] = useForm();
  const { organizationId } = useProfile().data;
  // @ts-ignore
  const { deadlineSettings } = useDeadlineSettings(organizationId).data;
  const [busy, setBusy] = useState(true);
  const [settingsId, setSettingsId] = useState<string>();

  useEffect(() => {
    fillFormInitialValues(form, deadlineSettings);
    setSettingsId(deadlineSettings.id);
    setBusy(false);
  }, [deadlineSettings, form, setBusy, setSettingsId]);

  return {
    form,
    deadlineSettings,
    busy,
    setBusy,
    settingsId,
    setSettingsId,
  };
};
