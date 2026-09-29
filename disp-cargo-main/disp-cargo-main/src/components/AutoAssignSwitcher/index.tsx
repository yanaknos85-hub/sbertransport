import React, {
  ComponentProps, FC, useEffect, useState
} from 'react';
import { ignore } from 'utils/utils';
import { Switcher } from '../Switcher/Switcher';
import { Switch } from 'antd';
import withErrorBoundary from 'components/withErrorBoundary';
import { useProfile } from 'api/profile/profile.api';
import { useContractor, useToggleAutoAssign } from 'api/contractors/contractors.api';

export const AutoAssignSwitcher: FC<Omit<ComponentProps<typeof Switch>, 'ref'>> = withErrorBoundary(props => {
  const { contractorId } = useProfile().data;
  const { data, isLoading: isLoadingContractor } = useContractor(contractorId, { suspense: false });
  const { autoassign } = data ?? {};

  const [toggleAutoAssign, { isLoading }] = useToggleAutoAssign(contractorId);

  const [isOn, setIsOn] = useState(!!autoassign);

  useEffect(() => {
    setIsOn(!!autoassign);
  }, [autoassign]);

  const handleChange = (value: boolean) => {
    toggleAutoAssign(value).catch(ignore);
  };

  return (
    <Switcher
      title="Автоназначение"
      {...props}
      checked={isOn}
      onChange={handleChange}
      loading={isLoading || isLoadingContractor}
    />
  );
});
