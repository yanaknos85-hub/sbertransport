import React, { FC, useEffect } from 'react';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { Tag } from 'antd';
import { useInitStore } from 'ioc/ioc.hooks';
import { useYandexTaxiApprovalsList } from 'api/yandexTaxi/yandex-taxi.api';

export enum TaxiApprovalCounterType {
  taxi = 'taxi',
  yandex = 'yandex',
  total = 'total',
}

interface Props {
  type: TaxiApprovalCounterType;
}

export const TaxiApprovalsCount: FC<Props> = ({ type }) => {
  const {
    countActiveApprovals,
    loadCountActiveApprovals,
  } = useAppStoreContext().tripStore;
  const { isSelfEmployeeLoaded } = useInitStore();
  const yandexTaxiApprovalsQty = useYandexTaxiApprovalsList({
    suspense: false,
    enabled: type === TaxiApprovalCounterType.yandex,
  })?.data?.page?.total ?? 0;

  useEffect(() => {
    if (type === TaxiApprovalCounterType.taxi && isSelfEmployeeLoaded) {
      loadCountActiveApprovals();
      window.addEventListener('approve', loadCountActiveApprovals);
      window.addEventListener('decline', loadCountActiveApprovals);

      return () => {
        window.removeEventListener('approve', loadCountActiveApprovals);
        window.removeEventListener('decline', loadCountActiveApprovals);
      };
    }
  }, [isSelfEmployeeLoaded]);

  return (
    <Tag color="var(--warning-color)">
      {type === TaxiApprovalCounterType.taxi ? countActiveApprovals
        : yandexTaxiApprovalsQty}
    </Tag>
  );
};

export default observer(TaxiApprovalsCount);
