import { useState } from 'react';
import { useDefaultColumnVisibilitySettings } from 'api/registry';
import { useSaveUsersAttributes, useUserSettings } from 'api/public-register-search';
import { UsersAttributes } from 'api/register-search';

export const useColumnVisibilitySettings = (userId: string) => {
  const { data: defaultColumns } = useDefaultColumnVisibilitySettings();
  const { refetch: refetchUserSettings, data: userSettings } = useUserSettings(userId);
  const [saveColumnVisibilitySettings, { isLoading: isSaving }] = useSaveUsersAttributes(userId);
  const [userColumnVisibilitySettings, setUserColumnVisibilitySettings] = useState<UsersAttributes | undefined>();

  const handleSaveColumnVisibilitySettings = (key: Record<string, boolean | undefined> | undefined) => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    saveColumnVisibilitySettings({ ...key, requestRatingVisible: false } as any).then(() => refetchUserSettings());
  };

  return {
    defaultColumns,
    userSettings,
    isSaving,
    userColumnVisibilitySettings,
    setUserColumnVisibilitySettings,
    handleSaveColumnVisibilitySettings,
  };
};
