import React, { useEffect, useState } from 'react';
import ReactDOM from 'react-dom';
import moment from 'moment';

import { DATE_FORMAT } from 'constants/constants.app';

import ClientDayBanner from 'shared/images/banners/Banner_Client-Day-2025.jpg';

import {
  Overlay
} from './styledAuthBanner';

const AuthBanner: React.FC = () => {
  const [showBanner, setShowBanner] = useState(false);
  const hasSeenBanner = localStorage.getItem('authBannerShown');
  const today = moment().format(DATE_FORMAT.BASE);
  const bannerDate = '2025-03-19';

  useEffect(() => {
    if (!hasSeenBanner && today === bannerDate) {
      localStorage.setItem('authBannerShown', 'true');
      setShowBanner(true);
    }
  }, []);

  const handleClose = () => {
    setShowBanner(false);
  };

  const handleClickBanner = e => {
    e.stopPropagation();
    handleClose();
  };

  if (!showBanner) return null;

  return ReactDOM.createPortal(
    <Overlay onClick={handleClose}>
      <div onClick={handleClickBanner}>
        <img src={ClientDayBanner} alt="День Клиента" />
      </div>
    </Overlay>,
    document.body
  );
};

export default AuthBanner;
