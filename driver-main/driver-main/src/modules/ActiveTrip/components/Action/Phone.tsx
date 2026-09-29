import { FC, ReactNode } from 'react';
import { Link } from 'react-router';
import styles from './Phone.module.scss';

interface ActionProps {
  img: ReactNode;
  title: string;
  tel?: string | null;
  to?: string;
  onClick?: () => void;
}

const Action: FC<ActionProps> = ({
  img,
  title,
  tel,
  to,
  onClick,
}) => {
  if (onClick) return (
    <div className={styles.action} onClick={onClick}>
      <div className={styles.img}>{img}</div>
      <div>{title}</div>
    </div>
  );

  if (!to) return (
    <a href={`tel:${tel}`} className={styles.action}>
      <div className={styles.img}>{img}</div>
      <div>{title}</div>
    </a>
  );

  return (
    <Link to={to} className={styles.action}>
      <div className={styles.img}>{img}</div>
      <div>{title}</div>
    </Link>
  );
};

export default Action;
