import React, { useState } from 'react';
import type { SelectProps, SelectValue } from 'antd/es/select';
import { useLocation } from 'react-router-dom';

import AccessControl from 'shared/components/AccessControl';
import { SelectOrganization } from 'shared/components/SelectOrganization';
import { useRole } from 'utils/useRole';
import { ReactComponent as DownArrowIcon } from 'shared/assets/svg/down-arrow.svg';
import { useOrganizationContext } from 'context/Organization.context';
import styles from './Organization.module.scss';
import { rolesCanSwitchOrg } from 'constants/constants.app';
import { SelectExecutorGroup } from '../SelectExecutorGroup';
import { SelectFlag } from '../SelectFlag';
import {
  ORDER_EXECUTION_PASSENGERS,
  REGISTRY_PASSENGERS
} from 'constants/constants.routes';
import { useCargoRoute } from 'shared/hooks/useCargoRoute';

export const Organization = () => {
  const {
    setOrganizationId,
    organizationId,
    setOrganizationName,
    organizationName,
    executorGroupId,
    setExecutorGroupId,
    isOrganization,
    setIsOrganization,
    emptyExecutorGroup,
    setEmptyExecutorGroup,
    allExecutorGroups,
    setAllExecutorGroups,
  } = useOrganizationContext();

  const location = useLocation();
  const { isCargoAny, isEtrnTab } = useCargoRoute();

  const [defaultOption] = useState(() => organizationId && organizationName ? {
    id: organizationId,
    officialName: organizationName,
  } : undefined);

  const userRoles = useRole();
  const options = ['По группам исполнителей', 'По организации'];

  const isPassengersPage = location.pathname.includes(REGISTRY_PASSENGERS)
    || location.pathname.includes(ORDER_EXECUTION_PASSENGERS);

  const isShowingNewHeader
    = (isPassengersPage || isCargoAny) && !isEtrnTab;

  const handleChangeOrg: SelectProps<SelectValue>['onChange'] = (value, option) => {
    setOrganizationId(value?.toString());
    if (!Array.isArray(option) && 'children' in option) {
      setOrganizationName(option.children.toString());
    }
  };

  const handleChangeExecutor = (value: SelectValue) => {
    if (Array.isArray(value)) {
      setExecutorGroupId(value as string[]);
    } else if (value === undefined) {
      setExecutorGroupId([]);
    } else {
      setExecutorGroupId([value as string]);
    }
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
        {isShowingNewHeader && (
          <SelectFlag
            suffixIcon={<DownArrowIcon />}
            onChange={handleChange}
            defaultValue={options[!isOrganization ? 0 : 1]}
            options={options}
            placeholder="Все группы исполнителей"
          />
        )}
        {isOrganization || !isShowingNewHeader ? (
          <SelectOrganization
            suffixIcon={<DownArrowIcon />}
            onChange={handleChangeOrg}
            defaultValue={organizationId}
            defaultOption={defaultOption}
          />
        ) : (
          <SelectExecutorGroup
            suffixIcon={<DownArrowIcon />}
            onChange={handleChangeExecutor}
            defaultValue={executorGroupId}
            value={executorGroupId}
            emptyActive={emptyExecutorGroup}
            onEmptyChange={setEmptyExecutorGroup}
            allActive={allExecutorGroups}
            onAllChange={setAllExecutorGroups}
            placeholder="Все группы исполнителей"
          />
        )}
      </div>
    </AccessControl>
  );
};
