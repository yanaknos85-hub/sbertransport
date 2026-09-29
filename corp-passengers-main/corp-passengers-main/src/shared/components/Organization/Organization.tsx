import * as React from 'react';
import AccessControl from 'shared/components/AccessControl';
import { SelectOrganization } from 'shared/components/SelectOrganization';
import { useRole } from 'utils/useRole';
import { UUID } from 'utils/io-ts';
import { ReactComponent as DownArrowIcon } from 'shared/assets/svg/down-arrow.svg';
import { useOrganizationContext } from 'context/Organization.context';
import styles from './Organization.module.scss';
import { rolesCanSwitchOrg } from 'constants/constants.app';
import { SelectExecutorGroup } from '../SelectExecutorGroup';
import { SelectFlag } from '../SelectFlag';

export const Organization = () => {
  const {
    setOrganizationId, organizationId, executorGroupId, setExecutorGroupId, isOrganization, setIsOrganization,
  } = useOrganizationContext();
  const userRoles = useRole();
  const options = ['По группам исполнителей', 'По организации'];
  let isShowingNewHeader = false;

  if (window.location.href.includes('reports/registry/passengers') || window.location.href.includes('order-execution/passengers')) {
    isShowingNewHeader = true;
  } else {
    isShowingNewHeader = false;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleChangeOrg = (value: any) => {
    setOrganizationId(value as UUID);
  };

  const handleChangeExecutor = value => {
    setExecutorGroupId(value);
  };

  const handleChange = () => {
    setIsOrganization(prev => !prev);
  };

  return (
    <AccessControl
      userPermissions={userRoles}
      allowedPermissions={rolesCanSwitchOrg}
    >
      <div className={styles.container}>
        {!!isShowingNewHeader
        && (
        <SelectFlag
          suffixIcon={<DownArrowIcon />}
          onChange={handleChange}
          defaultValue={options[!isOrganization ? 0 : 1]}
          options={options}
          placeholder="Все группы исполнителей"
        />
        )}
        {!!isOrganization || !isShowingNewHeader
          ? (
            <SelectOrganization
              suffixIcon={<DownArrowIcon />}
              onChange={handleChangeOrg}
              defaultValue={organizationId}
            />
          )
          : (
            <SelectExecutorGroup
              suffixIcon={<DownArrowIcon />}
              onChange={handleChangeExecutor}
              defaultValue={executorGroupId}
              placeholder="Все группы исполнителей"
            />
          )}
      </div>
    </AccessControl>
  );
};
