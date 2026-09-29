import React, { FC } from 'react';
import { Link } from 'react-router-dom';

import styles from './RedesignHotButton.module.scss';

interface VariantAddProps {
  title?: never;
  image?: never;
  link?: never;
  onClick: () => void;
  isAddVariant: boolean;
}

interface DefaultProps {
  title?: string;
  image?: string;
  link?: string;
  onClick?: never;
  isAddVariant?: never;
}

type Props = DefaultProps | VariantAddProps;

export const RedesignHotButton: FC<Props> = ({
  title, image, link = '',
}) => {
  return (
    <Link className={styles.container} to={link || ''}>
      <p className={styles.cardTitle}>{title}</p>
      <img
        src={image}
        alt={title}
        className={styles.cardImage}
      />
    </Link>
  );
};
