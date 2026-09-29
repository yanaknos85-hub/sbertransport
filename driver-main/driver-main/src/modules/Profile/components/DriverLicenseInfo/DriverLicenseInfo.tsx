import { FC } from 'react';
import { useNavigate } from 'react-router';

import { DriverSelf } from 'api/services/DispatcherRoom/DispatcherRoom.types';
import { routes } from 'constants/routes.constants';

import { ReactComponent as DriverLicenseIcon } from 'assets/icons/driver-license.svg';
import { ReactComponent as EditIcon } from 'assets/icons/edit.svg';
import styles from './DriverLicenseInfo.module.scss';

interface DriverLicenseInfoProps {
  driver: DriverSelf;
}

const DriverLicenseInfo: FC<DriverLicenseInfoProps> = ({ driver }) => {
  const navigate = useNavigate();

  const goToEditLicense = () => {
    navigate(routes.DriverLicense);
  };

  return (
    <div className={styles.driverLicense}>
      <DriverLicenseIcon />
      <div className={styles.licenseNumber}>
        <span>{driver.driverLicenseNumber}</span>
        <span className={styles.licenseLabel}>Водительское удостоверение</span>
      </div>
      <EditIcon className={styles.editIcon} onClick={goToEditLicense} />
    </div>
  );
};

export default DriverLicenseInfo;
