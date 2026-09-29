import { useEffect, useState } from 'react';
import { CheckboxChangeEvent } from 'antd/es/checkbox';
import { StoreNames } from 'ioc/ioc.storeNames';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { CompensationRequestModel } from 'stores/Compensations/models/CargoRequest.model';

export const useCompensationRequestItem = (request: CompensationRequestModel) => {
  const {
    [StoreNames.compensationStore]: compensationStore,
  } = useAppStoreContext();

  const { checkedListApproval, setCheckedListApproval } = compensationStore;
  const [checkedItem, setCheckedItem] = useState(false);

  useEffect(() => {
    setCheckedItem(!!checkedListApproval?.find(item => item.id === request.id));
  }, [checkedListApproval, request.id]);

  const isActivePage = window.location.pathname.includes('active');
  const isCheckBoxVisible = compensationStore.compensationList?.content?.length > 1;

  const onChange = (e: CheckboxChangeEvent, id: string) => {
    setCheckedItem(e.target.checked);
    if (e.target.checked) {
      const routeToAdd = compensationStore.compensationList?.content.find(route => route.id === id);
      if (routeToAdd) {
        setCheckedListApproval([...checkedListApproval, routeToAdd]);
      }
    } else {
      setCheckedListApproval([...checkedListApproval.filter(item => item.id !== id)]);
    }
  };

  return {
    checkedItem,
    isActivePage,
    isCheckBoxVisible,
    onChange,
    compensationStore,
  };
};
