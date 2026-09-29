import { useTranslation } from 'i18n';
import React, { FC, useState } from 'react';
import { Link } from 'react-router-dom';
import styles from './ServiceCard.module.scss';

interface Props {
  title?: string;
  imageSrc?: string;
  link?: string;
  isAddVariant?: boolean;
  onAdd?: () => void;
}

export const ServiceCard: FC<Props> = ({
  title,
  imageSrc,
  link,
  isAddVariant,
  onAdd,
}) => {
  const [isLoaded, setIsLoaded] = useState(false);
  const { t } = useTranslation();

  const handleLoad = () => setIsLoaded(true);

  if (isAddVariant) {
    return (
      <button
        type="button"
        className={styles.containerAdd}
        onClick={onAdd}
      >
        <div className={styles.add} />
        <p className={styles.cardTitle}>{t.Services.addNewService}</p>
      </button>
    );
  }

  const getChild = () => (
    <>
      {imageSrc && (
        <img
          className={styles.cardImage}
          src={imageSrc}
          alt={title}
          onLoad={handleLoad}
          style={{ opacity: +isLoaded }}
        />
      )}
      <p className={styles.cardTitle}>{title}</p>
    </>
  );

  return link ? (
    <Link className={styles.container} to={link}>
      {getChild()}
    </Link>
  ) : (
    <div className={styles.container}>
      {getChild()}
    </div>
  );
};
