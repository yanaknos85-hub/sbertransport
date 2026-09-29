import { useState } from 'react';

import { Actions, useModalState } from '../../hooks/useModal';

interface ReasonModal {
  visible: boolean;
  actions: Actions;
  reasonText: string;
  setReasonText: React.Dispatch<React.SetStateAction<string>>;
}

export const useReasonModal = (): ReasonModal => {
  const [visible, actions] = useModalState();
  const [reasonText, setReasonText] = useState('');

  return {
    visible,
    actions,
    reasonText,
    setReasonText,
  };
};
