import React, { useState, useEffect, FC } from 'react';
import { Avatar as AvatarAntd, Skeleton } from 'antd';

import { ReactComponent as AvatarIcon } from 'shared/components/Images/avatar.svg';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores';
import styles from './DelegateAvatar.module.scss';

interface DelegateAvatarProps {
  delegateId: string;
  size?: number;
}

const DelegateAvatar: FC<DelegateAvatarProps> = ({ delegateId, size = 48 }) => {
  const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();
  const [url, setUrl] = useState(null);
  const [isLoading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    tripStore
      .getUserAvatar(delegateId)
      .then(setUrl)
      .finally(() => {
        setLoading(false);
      });
  }, [delegateId]);

  if (isLoading) {
    return <Skeleton.Avatar active size={size} />;
  }

  return (
    <AvatarAntd
      size={size}
      src={url}
      icon={<AvatarIcon />}
      className={styles.avatar}
    />
  );
};

export default DelegateAvatar;
