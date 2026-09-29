import { useTranslation } from 'i18n';
import React, { FC } from 'react';
import { Link } from 'react-router-dom';

import styles from './HotButton.module.scss';

interface VariantAddProps {
  title?: never;
  image?: never;
  link?: never;
  onClick: () => void;
  isAddVariant: boolean;
}

interface DefaultProps {
  title: string;
  image: JSX.Element;
  link?: string;
  onClick?: never;
  isAddVariant?: never;
}

type Props = DefaultProps | VariantAddProps;

export const HotButton: FC<Props> = ({
  title, image, onClick, isAddVariant, link = '',
}) => {
  const { t } = useTranslation();

  if (isAddVariant) {
    return (
      <button
        type="button"
        className={styles.containerAdd}
        onClick={onClick}
      >
        <div className={styles.add} />
        <p className={styles.cardTitle}>{t.HotButtons.newButton}</p>
      </button>
    );
  }

  return (
    <Link className={styles.container} to={link || ''}>
      <p className={styles.cardTitle}>{title}</p>
      <div className={styles.cardImage}>{image}</div>
    </Link>
  );
};
