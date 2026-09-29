import './override.scss';

import { Divider } from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useEffect } from 'react';

import { EmployeeAppLinks, EmployeeAppLinksTitles } from 'modules/EmployeeApp/EmployeeApp.constants';

import PageLayout from 'shared/components/PageLayout/PageLayout';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import { UUID } from 'utils/io-ts';

import { SharingBarsElement } from './LimitPageComponents/SharingBarsElement';
import { TableWithStatistics } from './LimitPageComponents/TableWithStatistics';
import { LimitRequestPeriods } from './LimitRequestModal/constants';
import { LimitRequestModal } from './LimitRequestModal/LimitRequestModal';

export const LimitsPage: FC = observer(() => {
  const {
    [StoreNames.limitsStore]: limits,
    [StoreNames.selfStore]: selfStore,
    [StoreNames.corporateStore]: corporateStore,
  } = useAppStoreContext();

  const title = `${EmployeeAppLinksTitles[EmployeeAppLinks.limits]} на ${LimitRequestPeriods[new Date().getMonth()]}`;
  const userDepId = selfStore.depId;
  const userDep = corporateStore.getDepartment(userDepId);
  const currentUserId = selfStore.empId;

  const isDepartmentHead = (): boolean => userDep?.departmentHeadId === currentUserId;

  useEffect(() => {
    limits.getLimitByDepartment(selfStore.depId as UUID);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [limits]);
  // FIXME react-hooks/exhaustive-deps

  const getPersonalRequestButton = (): JSX.Element | null => {
    // const depLimit = limits.depLimits.find((x) => x.year === new Date().getFullYear());
    // ToDo: Чисто бизнес требование ради релиза - когда попросят сделать отображение в зависимости от наличия лимита
    // вернуть depLimit ? (<>...</>) : null как и было
    return (
      <>
        <div className="list-header">Личный лимит</div>
        <Divider />
        <LimitRequestModal
          percent={0}
          transportType=""
          type={LIMIT_TYPE.EMPLOYEE}
        />
      </>
    );
  };

  const getPersonalSharingElem = (): JSX.Element | null => limits.currentEmployeeSharing.length > 0 ? (
    <SharingBarsElement
      title="Личный лимит"
      errorMsg="На Вас не выделен персональный лимит"
      sharing={limits.currentEmployeeSharing}
      type={LIMIT_TYPE.EMPLOYEE}
      isDepartmentHead={isDepartmentHead()}
    />
  ) : (
    getPersonalRequestButton()
  );

  const getDepSharingElement = (): JSX.Element => (
    <SharingBarsElement
      title="Лимит подразделения"
      errorMsg="На ваше подразделение лимит не выделен"
      sharing={limits.currentDepartmentSharing}
      type={LIMIT_TYPE.DEPARTMENT}
      isDepartmentHead={isDepartmentHead()}
    />
  );

  const getStatisticsElement = (): JSX.Element | null => userDep?.departmentHeadId === currentUserId ? <TableWithStatistics /> : null;

  return (
    <PageLayout title={title}>
      {getPersonalSharingElem()}
      {getDepSharingElement()}
      {getStatisticsElement()}
    </PageLayout>
  );
});
