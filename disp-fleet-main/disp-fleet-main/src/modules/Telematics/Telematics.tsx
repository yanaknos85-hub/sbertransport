import { useLayoutEffect, FC } from 'react';
import { TELEMATICS_LINK } from 'constants/app.constants';

const Telematics: FC = () => {
  useLayoutEffect(() => {
    window.location.replace(TELEMATICS_LINK);
  }, []);

  return null;
};

export default Telematics;
