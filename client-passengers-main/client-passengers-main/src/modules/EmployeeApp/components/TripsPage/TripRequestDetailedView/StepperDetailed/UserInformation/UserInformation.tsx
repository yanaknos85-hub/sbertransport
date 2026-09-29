import React, { useEffect, useState } from 'react';
import './overwrite.scss';
import User from 'shared/components/Images/user.png';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

interface Props {
  id: string;
  fio: string;
}

const UserInformation = ({ id, fio }: Props): JSX.Element => {
  const [avatar, setAvatar] = useState<string>('');
  const {
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();

  useEffect(() => {
    tripStore.getUserAvatart(id)
      .then(setAvatar);
  }, []);

  return (
    <div className="informationinInitiatorWrapper">
      <div className="informationinInitiatorMain">
        <img alt="avatar" src={avatar ? avatar : User} />
        <div className="informationinInitiatorInfo">
          <span className="informationinInitiatorInfoName">
            {fio}
          </span>
        </div>
      </div>
    </div>
  );
};

export default UserInformation;
