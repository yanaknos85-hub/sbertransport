import { MutableRefObject, useEffect } from 'react';

export const useClickOutside = <T extends HTMLElement>(
  ref: MutableRefObject<T | null>,
  callback: (e: MouseEvent) => void,
  isActive = false
) => {
  useEffect(() => {
    const onClick = (e: MouseEvent) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      if (ref.current?.contains(e.target as any)) {
        return;
      }
      callback(e);
    };

    if (ref.current && isActive) {
      document.addEventListener('click', onClick);
    }

    return () => {
      document.removeEventListener('click', onClick);
    };
  }, [ref, isActive, callback]);
};
