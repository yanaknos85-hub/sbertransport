import * as React from 'react';
import { useLocation } from 'react-router-dom';
import AccessControl from 'shared/components/AccessControl';
import { SelectOrganization } from 'shared/components/SelectOrganization';
import { useRole } from 'utils/useRole';
import { UUID } from 'utils/io-ts';
import { ReactComponent as DownArrowIcon } from 'shared/assets/svg/down-arrow.svg';
import { useOrganizationContext } from 'context/Organization.context';
import { rolesCanSwitchOrg } from 'constants/constants.app';
import { SelectExecutorGroup } from '../SelectExecutorGroup';
import { SelectFlag } from '../SelectFlag';

import styles from './Organization.module.scss';

export const Organization = () => {
  const {
    setOrganizationId,
    organizationId,
    executorGroupId,
    setExecutorGroupId,
    isOrganization,
    setIsOrganization,
    emptyExecutorGroup,
    setEmptyExecutorGroup,
    allExecutorGroups,
    setAllExecutorGroups,
  } = useOrganizationContext();

  const userRoles = useRole();
  const location = useLocation();

  const options = ['По группам исполнителей', 'По организации'];

  const isShowingNewHeader = location.pathname.includes('reports/registry/cargo/orders')
    || location.pathname.includes('reports/registry/cargo/routes')
    || location.pathname.includes('order-execution')
    || location.pathname.includes('multi-logistics');

  // Для OrderExecution: если таб "Расписания" (template), показываем только селект организации
  const isTemplateTab = location.pathname.includes('/template');

  // Для ЭТрН всегда показываем только селект организации (без переключателя)
  const searchParams = new URLSearchParams(location.search);
  const isEtrnTab = searchParams.get('tab') === 'etrn';

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleChangeOrg = (value: any) => {
    setOrganizationId(value as UUID);
  };

  const handleChangeExecutor = value => {
    setExecutorGroupId(value);
  };

  const handleChange = () => {
    setIsOrganization(!isOrganization);
  };

  return (
    <AccessControl
      userPermissions={userRoles}
      allowedPermissions={rolesCanSwitchOrg}
    >
      <div className={styles.container}>
        {/* Показываем переключатель только если не template и не etrn */}
        {isShowingNewHeader && !isTemplateTab && !isEtrnTab && (
          <SelectFlag
            suffixIcon={<DownArrowIcon />}
            onChange={handleChange}
            defaultValue={options[!isOrganization ? 0 : 1]}
            options={options}
            placeholder="Все группы исполнителей"
          />
        )}
        {/* Показываем только селект организации для template или etrn */}
        {(isShowingNewHeader && (isTemplateTab || isEtrnTab))
          ? (
            <SelectOrganization
              suffixIcon={<DownArrowIcon />}
              onChange={handleChangeOrg}
              defaultValue={organizationId}
            />
          )
          : isShowingNewHeader
            ? (
              <>
                {isOrganization ? (
                  <SelectOrganization
                    suffixIcon={<DownArrowIcon />}
                    onChange={handleChangeOrg}
                    defaultValue={organizationId}
                  />
                ) : (
                  <SelectExecutorGroup
                    suffixIcon={<DownArrowIcon />}
                    onChange={handleChangeExecutor}
                    onEmptyChange={setEmptyExecutorGroup}
                    onAllChange={setAllExecutorGroups}
                    emptyActive={emptyExecutorGroup}
                    allActive={allExecutorGroups}
                    defaultValue={executorGroupId}
                    value={executorGroupId}
                    placeholder="Все группы исполнителей"
                  />
                )}
              </>
            )
            : (
              <SelectOrganization
                suffixIcon={<DownArrowIcon />}
                onChange={handleChangeOrg}
                defaultValue={organizationId}
              />
            )}
      </div>
    </AccessControl>
  );
};
