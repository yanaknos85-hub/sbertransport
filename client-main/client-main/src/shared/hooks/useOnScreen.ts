import { useEffect, useState } from 'react';

export function useOnScreen(ref: React.MutableRefObject<HTMLInputElement>): boolean {
  const [isIntersecting, setIntersecting] = useState(false);

  const observer = new IntersectionObserver(([entry]) => setIntersecting(entry.isIntersecting));

  useEffect(() => {
    if (ref.current) {
      observer.observe(ref.current);
    }

    return (): void => {
      observer.disconnect();
    };
  }, [observer, ref]);

  return isIntersecting;
}
