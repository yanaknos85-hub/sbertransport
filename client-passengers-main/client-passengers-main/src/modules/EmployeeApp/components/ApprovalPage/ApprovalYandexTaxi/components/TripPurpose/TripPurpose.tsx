import React, { FC } from 'react';
import { Skeleton } from 'antd';
import usePurpose from 'shared/hooks/purpose/usePurpose';

export interface TripPurposeProps {
  purposeId: string;
  className?: string;
}

export const TripPurpose: FC<TripPurposeProps> = ({
  purposeId, className,
}) => {
  const { inProgress, purpose } = usePurpose(purposeId);

  return (
    <Skeleton
      loading={inProgress}
      paragraph={false}
      title={{ width: 100 }}
    >
      <span className={className}>{purpose?.label || ''}</span>
    </Skeleton>
  );
};
