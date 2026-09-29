import { FC } from 'react';
import ImageUploader, { ImageUploadItem } from 'antd-mobile/es/components/image-uploader';
import Skeleton from 'antd-mobile/es/components/skeleton';

import { useContractor, useUpdateAvatar, useUserAvatar } from 'api/services/DispatcherRoom/DispatcherRoom.query';
import { DriverSelf } from 'api/services/DispatcherRoom/DispatcherRoom.types';
import Avatar from 'components/Avatar/Avatar';
import { getFullName } from 'utils/formattors/getFullName';

import { ReactComponent as StarIcon } from 'assets/icons/star.svg';
import styles from './UserInfo.module.scss';

interface UserInfoProps {
  driver: DriverSelf;
}

const UserInfo: FC<UserInfoProps> = ({ driver }) => {
  const { data: avatarUrl, isRefetching } = useUserAvatar(driver.id);

  const { data: contractor } = useContractor(driver.contractorId, {
    enabled: !!driver.contractorId,
  });

  const { mutate: updateAvatar, isPending } = useUpdateAvatar();

  const onUpload = (file: File) => {
    updateAvatar(file);
  };

  const isLoading = isRefetching || isPending;

  return (
    <div className={styles.user}>
      <div className={styles.info}>
        <div className={styles.user}>
          <span className={styles.userName}>{getFullName(driver)}</span>
          <span className={styles.userRole}>Водитель</span>
          {contractor?.name && <span className={styles.userAutoPark}>{`Автопарк «${contractor.name}»`}</span>}
        </div>

        <div className={styles.avatarWrapper}>
          {isLoading ? <Skeleton animated className={styles.skeleton} /> : <Avatar src={avatarUrl} />}

          <ImageUploader
            className={styles.imageUploader}
            upload={onUpload as (file: File) => Promise<ImageUploadItem>}
            beforeUpload={onUpload as (file: File) => Promise<null>}
            deletable={false}
            preview={false}
          />
        </div>
      </div>

      <div className={styles.statistics}>
        <div className={styles.rating}>
          <span>{(driver.rating || 0) / 100}</span>
          <StarIcon />
        </div>
        <span className={styles.ratingLabel}>рейтинг</span>
      </div>
    </div>
  );
};

export default UserInfo;
