import { useState } from 'react';
import { useGetUsersAllAttributes, UsersAttributes, useSaveUsersAttributes } from 'api/register-search';
import { useProfile } from 'api/profile';
import { useDefaultColumnVisibilitySettings } from 'api/registry';

export const useUserAttributes = () => {
  const { userId } = useProfile().data;

  const usersAttributes = useGetUsersAllAttributes(userId!).data;

  const [settingAttributes, setSettingAttributes] = useState<UsersAttributes | undefined>();

  const { data: defaultColumns } = useDefaultColumnVisibilitySettings();

  const [saveSettingAttributes] = useSaveUsersAttributes(userId!);

  const handleSave = (key: Record<string, boolean> | undefined) => {
    saveSettingAttributes({ ...key, requestRatingVisible: false } as any);
  };

  return {
    usersAttributes,
    settingAttributes,
    setSettingAttributes,
    defaultColumns,
    handleSave,
  };
};
