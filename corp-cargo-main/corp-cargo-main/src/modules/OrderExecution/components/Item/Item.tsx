import React, { FC } from 'react';

interface ItemProps {
  title?: string;
  children: any;
}

const Item: FC<ItemProps> = props => {
  const { title, children } = props;
  const mainClass = 'orderExecutionContainer-item';
  const classes = [mainClass];

  return (
    <div className={classes.join(' ')}>
      {title && <div className={`${mainClass}__title`}>{title}</div>}
      {children}
    </div>
  );
};

export default Item;
