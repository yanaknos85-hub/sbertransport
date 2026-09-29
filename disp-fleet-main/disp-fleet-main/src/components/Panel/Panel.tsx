import React, { FC, ReactNode } from 'react';
import cn from 'classnames';
import styles from './Panel.module.scss';

const Panel: FC<{
  title?: string | ReactNode;
  titleWithoutPadding?: boolean;
  contentWithoutPadding?: boolean;
  smallVerticalPadding?: boolean;
  cardPadding?: boolean;
  fullHeight?: boolean;
  marginTop?: boolean;
  marginBottom?: boolean;
  inner?: boolean;
  actions?: ReactNode;
  contentWidth?: boolean;
  className?: string;
}> = ({
  title,
  titleWithoutPadding,
  contentWithoutPadding,
  smallVerticalPadding = false,
  cardPadding = false,
  fullHeight = false,
  marginTop = false,
  marginBottom = false,
  inner = false,
  contentWidth = false,
  actions,
  children,
  className,
}) => (
  <div className={cn(styles.panel, {
    [styles.fullHeight]: fullHeight,
    [styles.marginTop]: marginTop,
    [styles.marginBottom]: marginBottom,
    [styles.inner]: inner,
    [styles.contentWidth]: contentWidth,
  }, className)}
  >
    {(title || actions) && (
    <div className={cn(styles.title, {
      [styles.withoutPadding]: titleWithoutPadding,
      [styles.smallVerticalPadding]: smallVerticalPadding,
      [styles.cardPadding]: cardPadding,
    })}
    >
      {title && <h2 className={styles.panelTitle}>{title}</h2>}
      <div>{actions}</div>
    </div>
    )}
    <div className={cn(styles.content, {
      [styles.withoutPadding]: contentWithoutPadding,
      [styles.smallVerticalPadding]: smallVerticalPadding,
      [styles.cardPadding]: cardPadding,
    })}
    >
      {children}
    </div>
  </div>
);

export default Panel;
