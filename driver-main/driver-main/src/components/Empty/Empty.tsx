import { FC, ReactNode } from 'react';
import cn from 'classnames';

import notFound from 'assets/images/not-found.png';
import styles from './Empty.module.scss';

interface EmptyProps {
  title: string;
  description: string;
  className?: string;
  children?: ReactNode;
}

const Empty: FC<EmptyProps> = ({
  title, description, className, children,
}) => {
  return (
    <div className={cn(styles.container, [className])}>
      <img
        className={styles.image}
        src={notFound}
        alt="not-found"
      />
      <section className={styles.textSide}>
        <strong className={styles.title}>{title}</strong>
        <p className={styles.description}>{description}</p>
      </section>
      {children}
    </div>
  );
};

export default Empty;
