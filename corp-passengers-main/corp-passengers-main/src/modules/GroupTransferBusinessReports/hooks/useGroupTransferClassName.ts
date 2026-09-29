import { useGroupTransferTransportClasses } from 'api/passengers-transport-classes';
import { VALUE_NOT_FOUND } from '../constants/groupTransfer.constants';

export const useGroupTransferClassName = (groupTransferClass: string | undefined | null) => {
  const { data: groupTransferClasses } = useGroupTransferTransportClasses();
  const groupTransferClassName = groupTransferClasses
    .find(item => item.value === groupTransferClass)?.rusName || VALUE_NOT_FOUND;

  return groupTransferClassName;
};
