import { MutableRefObject, useEffect } from 'react';

export const useOnClickOutside = function useOnClickOutside(
  ref: MutableRefObject<HTMLElement>,
  onClick: (ev: MouseEvent) => void
) {
  useEffect(() => {
    const onClickHandler = (ev: MouseEvent) => {
      if (!ref.current || ref.current.contains(ev.target as Node)) {
        return;
      }

      onClick(ev);
    };

    document.addEventListener('mousedown', onClickHandler);

    return () => {
      document.removeEventListener('mousedown', onClickHandler);
    };
  }, [onClick, ref]);

  return null;
};
