
import React, { useEffect, useState } from 'react';
import User from 'shared/components/Images/user.png';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import { IRequestsModal } from 'stores/Trip/Trip.interface';
import { formatFullName } from 'utils/formatFullName';
import modalInformationTrip from '../modalInformationTrip.module.scss';

interface PassengersItemProps {
  item: IRequestsModal;
}

const PassengersItem: React.FC<PassengersItemProps> = ({ item }) => {
  const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();

  const [avatar, setAvatar] = useState<string>('');

  useEffect(() => {
    tripStore.getUserAvatart(item.employee?.userId).then(setAvatar);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className={modalInformationTrip.informationPassengersWrapper}>
      <div className={modalInformationTrip.informationPassengersMain}>
        <img alt="avatar" src={avatar || User} />
        <div className={modalInformationTrip.informationPassengersInfo}>
          <span className={modalInformationTrip.informationPassengersInfoName}>
            {formatFullName(item?.employee?.firstName, item?.employee?.lastName, item?.employee?.patronymic)}
          </span>
          <span className={modalInformationTrip.informationPassengersInfoPosition}>{item.employee?.positionName}</span>
          <span className={modalInformationTrip.informationPassengersInfoNumber}>
            {`Таб. №${item.employee?.personnelNumber}`}
          </span>
        </div>
      </div>
    </div>
  );
};

export default PassengersItem;
