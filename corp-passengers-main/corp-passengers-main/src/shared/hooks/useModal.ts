import { useState } from 'react';

export interface Actions {
  show: () => void;
  hide: () => void;
  toggle: (value?: boolean | undefined) => void;
}

export const useModalState = (initial = false): [boolean, Actions] => {
  const [visible, setVisible] = useState(initial);

  return [
    visible,
    {
      show: (): void => setVisible(true),
      hide: (): void => setVisible(false),
      toggle: (): void => setVisible(!visible),
    },
  ];
};
