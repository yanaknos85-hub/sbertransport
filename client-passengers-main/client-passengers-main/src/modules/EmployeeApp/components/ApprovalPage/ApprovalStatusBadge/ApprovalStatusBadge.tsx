import { FC } from 'react';
import cn from 'classnames';
import { ApprovalsStatusEnum } from 'shared/models/Approval.model';

import styles from './approvalStatusBadge.module.scss';
import React from 'react';

interface ApprovalStatusBadgeProps {
  status: ApprovalsStatusEnum;
  title: string;
}

export const ApprovalStatusBadge: FC<ApprovalStatusBadgeProps> = ({ status, title }) => {
  const getStatusColor = (status: ApprovalsStatusEnum) => {
    switch (status) {
      case ApprovalsStatusEnum.APPROVED:
        return 'positive';
      case ApprovalsStatusEnum.CANCELLED:
      case ApprovalsStatusEnum.DECLINED:
        return 'error';
      case ApprovalsStatusEnum.NEW:
      case ApprovalsStatusEnum.EDIT:
        return 'change';
      default:
        return 'none';
    }
  };
  return (
    <span className={cn(styles.badge, styles[getStatusColor(status)])}>{ title }</span>
  );
};
