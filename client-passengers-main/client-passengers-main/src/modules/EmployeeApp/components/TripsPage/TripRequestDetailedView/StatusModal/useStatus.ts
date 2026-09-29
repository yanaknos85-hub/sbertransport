import { noop } from 'utils';

import { IUseStatusProps } from './StatusModal';

export const useStatus = ({
  setVisible,
}: IUseStatusProps): {
    showModal: () => void;
    hideModal: () => void;
  } => {
  const onRequestClick = noop;

  function showModal(): void {
    setVisible(true);
    onRequestClick();
  }

  function hideModal(): void {
    setVisible(false);
  }

  return { showModal, hideModal };
};
