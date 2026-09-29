import { useEffect, RefObject } from 'react';

/**
 * Автоматически скроллит контейнер вниз при изменении списка сообщений.
 */
export const useAutoScroll = (
  containerRef: RefObject<HTMLDivElement | null>,
  deps: unknown[]
): void => {
  useEffect(() => {
    containerRef.current?.scrollTo({
      top: containerRef.current.scrollHeight,
      behavior: 'smooth',
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);
};
