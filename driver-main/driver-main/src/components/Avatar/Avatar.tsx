import { FC } from 'react';
import cn from 'classnames';
import AntdAvatar, { AvatarProps } from 'antd-mobile/es/components/avatar';

import UserIcon from 'assets/icons/user.svg';
import styles from './Avatar.module.scss';

interface IAvatarProps extends Omit<AvatarProps, 'src'> {
  src?: string | void | undefined;
  size?: number;
}

const Avatar: FC<IAvatarProps> = ({
  src = UserIcon, className, size = 64,
}) => (
  <AntdAvatar
    src={src}
    className={cn(styles.avatar, [className])}
    style={{ minWidth: size, minHeight: size }}
    alt="avatar"
  />
);

export default Avatar;
