import { EmployeeDetailedModel } from '@sber-sbertransport/mf-core';

import moment from 'moment';
import { useCallback } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { DATE_FORMAT } from 'constants/constants.app';

import { DelegateModel } from 'stores/Delegates/Delegates.interface';

import { StoreNames } from 'stores/StoreNames.enum';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { Delegates, DelegatesCyrillic } from './Delegates.constants';

export const useDelegateCard = (): {
  spesificDelegate: EmployeeDetailedModel;
  delegatesByDelegateId?: DelegateModel;
  delegateId: any;
  rusName: string;
  goDelegatesList: () => void;
  delegatePeriod: string;
} => {
  const {
    [StoreNames.configStore]: configStore,
    [StoreNames.mappedStore]: mappedStore,
    [StoreNames.delegatesStore]: delegatesStore,
    [StoreNames.transportTypesStore]: transportTypesStore,
  } = useAppStoreContext();
  const { history } = configStore;
  const match = useRouteMatch<any>();
  const { id } = match.params;
  const delegatesByDelegateId = delegatesStore.delegates.find(el => el.id === id);

  const {
    startDate, endDate, delegateId,
  } = (delegatesByDelegateId || {}) as DelegateModel;

  const transportType = delegatesStore.delegates.find(el => el.id === id)?.transportType;

  const rusName: string = transportTypesStore.rusNamesByTransportType[transportType || ''];

  const spesificDelegate = mappedStore.employeeListByOrgDetailed[delegateId];

  const goDelegatesList = useCallback((): void => history.push('../delegates'), [history]);
  const delegatePeriod
    = startDate && endDate
      ? `C ${moment(startDate).format(DATE_FORMAT.BASE_REVERTED)} по ${moment(endDate).format(
        DATE_FORMAT.BASE_REVERTED
      )}`
      : DelegatesCyrillic[Delegates.noDelegateDates];

  return {
    spesificDelegate,
    delegatesByDelegateId,
    delegateId,
    rusName,
    goDelegatesList,
    delegatePeriod,
  };
};
