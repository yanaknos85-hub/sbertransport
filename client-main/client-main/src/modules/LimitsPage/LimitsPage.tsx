import React, {
  FC, useEffect, useMemo, useState
} from 'react';
import { observer } from 'mobx-react';
import { Tabs } from 'antd';

import { SpinWrapped } from 'shared/components';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

import { DepartmentLimit } from './DepartmentLimit/DepartmentLimit';
import { PersonalLimit } from './PersonalLimit/PersonalLimit';
import { Wrapper, DepartmentHeadWrapper, TabsStyled } from './LimitsPage.styled';
import { LIMIT_SERVICE_TYPE, LimitSharing } from 'stores/Limits/Limit.interface';
import { toRubles } from 'utils';
import { useQueryParams } from 'utils/useQueryParams';
import { useHistory } from '@sber-sbertransport/mf-core';

export const LimitsPage: FC = observer(() => {
  const {
    [StoreNames.selfStore]: { selfEmployee },
    [StoreNames.limitsStore]: limitsStore,
    [StoreNames.transportTypesStore]: transportTypesStore,
  } = useAppStoreContext();

  const history = useHistory();
  const queries = useQueryParams();

  const initialActiveTab = queries?.['activeKey'] ?? 'department';
  const [limitTab, setLimitTab] = useState(initialActiveTab);

  const handleOnTabChange = (value: string) => {
    setLimitTab(value);
  };
  const [activeType, setActiveType] = useState(LIMIT_SERVICE_TYPE.PASSENGER);

  const {
    limitsIsLoaded, currentDepartmentSharing, currentEmployeeSharing, responsibles,
  } = limitsStore;

  const { isDepartmentHead } = selfEmployee;

  useEffect(() => {
    history.replace(history.location.pathname);
  }, []);

  useEffect(() => {
    limitsStore.initStore();
  }, [limitsStore]);

  useEffect(() => {
    if (isDepartmentHead) {
      transportTypesStore.initStore();
    }
  }, [isDepartmentHead, transportTypesStore]);

  const restrict = useMemo(
    () => currentDepartmentSharing
      .filter(({ limitSharingPerPeriodDTO }: LimitSharing) => toRubles(limitSharingPerPeriodDTO?.balance ?? 0))
      .map(({ transportType }) => transportType),
    [currentDepartmentSharing]
  );

  const onChangeServiceType = (type: LIMIT_SERVICE_TYPE) => {
    setActiveType(type);
  };

  return (
    <Wrapper>
      {!isDepartmentHead
      && (
      <TabsStyled activeKey={limitTab} onChange={handleOnTabChange}>
        <Tabs.TabPane tab="Подразделения" key="department">
          {limitsIsLoaded ? (
            <DepartmentLimit
              sharing={currentDepartmentSharing}
              isDepartmentHead={isDepartmentHead}
              responsibles={responsibles[activeType]}
              onChangeServiceType={onChangeServiceType}
            />
          ) : (
            <SpinWrapped />
          )}
        </Tabs.TabPane>
        <Tabs.TabPane tab="Личный" key="personal">
          {limitsIsLoaded ? <PersonalLimit sharing={currentEmployeeSharing} restrict={restrict} /> : <SpinWrapped />}
        </Tabs.TabPane>
      </TabsStyled>
      )}
      {!!isDepartmentHead
      && (
      <DepartmentHeadWrapper>
        {limitsIsLoaded ? (
          <DepartmentLimit
            sharing={currentDepartmentSharing}
            isDepartmentHead={isDepartmentHead}
            responsibles={responsibles[activeType]}
            onChangeServiceType={onChangeServiceType}
          />
        ) : (
          <SpinWrapped />
        )}
      </DepartmentHeadWrapper>
      )}
    </Wrapper>
  );
});
