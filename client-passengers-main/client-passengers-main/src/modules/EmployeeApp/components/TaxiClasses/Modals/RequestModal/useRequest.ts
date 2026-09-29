
import { IUseRequestProps } from './RequestModal';

export const useRequest = ({
  setVisible,
  onRequestClick,
}: IUseRequestProps): {
    showModal: () => void;
    hideModal: () => void;
  } => {
  function showModal(): void {
    setVisible(true);
    onRequestClick();
  }

  function hideModal(): void {
    setVisible(false);
  }

  return { showModal, hideModal };
};
