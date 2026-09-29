import { useEffect, useRef, useState } from 'react';

export const useIsOverflowing = <T extends HTMLElement>() => {
  const [isOverflowing, setIsOverflowing] = useState(false);
  const ref = useRef<T>(null);

  useEffect(() => {
    if (!ref.current) return;

    const checkOverFlow = () => {
      const node = ref.current!;
      const { width } = node.getBoundingClientRect();
      const innerWidth = node.scrollWidth;

      setIsOverflowing(innerWidth > width);
    };

    checkOverFlow();

    window.addEventListener('resize', checkOverFlow);

    return () => {
      window.removeEventListener('resize', checkOverFlow);
    };
  }, []);

  return { isOverflowing, ref };
};
