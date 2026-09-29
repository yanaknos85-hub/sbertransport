import type { FC } from 'react';

import { APPS_URL } from 'constants/constants.app';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';

const MobileAppRef: FC = () => {
  const { isMobile, isIos } = usePlatformDetect();

  if (isMobile) {
    window.location.replace(isIos ? APPS_URL.IOS : APPS_URL.ANDROID);
  }

  return null;
};

export default MobileAppRef;
