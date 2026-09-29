import { useEffect, useState } from 'react';

const MAX_MOBILE_WIDTH = 767;

const isMobileDevice = (windowWidth: number) => windowWidth <= MAX_MOBILE_WIDTH;

export const useMobile = () => {
  const [isMobile, setIsMobile] = useState(isMobileDevice(window.innerWidth));

  useEffect(() => {
    const detectMobile = () => {
      setIsMobile(isMobileDevice(window.innerWidth));
    };

    window.addEventListener('resize', detectMobile);

    return () => {
      window.removeEventListener('resize', detectMobile);
    };
  }, []);

  return isMobile;
};
