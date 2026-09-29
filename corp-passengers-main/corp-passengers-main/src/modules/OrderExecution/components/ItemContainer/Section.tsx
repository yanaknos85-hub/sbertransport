import React from 'react';

import './styles.scss';

interface FeedTableHeadProps {
  title?: string | JSX.Element;
  className?: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  children: any;
}

const ItemContainer: React.FC<FeedTableHeadProps> = props => {
  const {
    children, className, title,
  } = props;
  const mainClass = 'orderExecutionContainer-section';
  const classes = [mainClass];

  if (className) {
    classes.push(className);
  }

  return (
    <div className={classes.join(' ')}>
      {title && <div className={`${mainClass}__title`}>{title}</div>}
      <div className={`${mainClass}__content`}>{children}</div>
    </div>
  );
};

export default ItemContainer;
