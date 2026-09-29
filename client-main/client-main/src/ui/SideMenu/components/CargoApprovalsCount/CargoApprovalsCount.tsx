import React, { FC } from 'react';
import { Tag } from 'antd';
import { useCargoApprovalsCount, useCargoCompensationApprovalsCount } from 'api/approvals';
import { CargoApprovalCounterType } from './constants';

interface Props {
  type: CargoApprovalCounterType;
}

export const CargoApprovalsCount: FC<Props> = ({ type }) => {
  const cargoApprovalsCount = useCargoApprovalsCount()?.data ?? {
    singleCount: 0, regularCount: 0, relocationCount: 0,
  };
  const cargoCompensationCount = useCargoCompensationApprovalsCount()?.data ?? { compensationCount: 0 };

  const getTotalCount = () => {
    switch (type) {
      case CargoApprovalCounterType.cargos:
        return cargoApprovalsCount.singleCount + cargoApprovalsCount.relocationCount + cargoCompensationCount.compensationCount;
      case CargoApprovalCounterType.regularCargos:
        return cargoApprovalsCount.regularCount;
      default:
        return 0;
    }
  };

  const totalCount = getTotalCount();

  if (totalCount === 0) {
    return null;
  }

  return (
    <Tag color="var(--warning-color)">
      {totalCount}
    </Tag>
  );
};

export default CargoApprovalsCount;
