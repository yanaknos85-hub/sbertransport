import React, { FC } from 'react';

interface ItemProps {
  title?: string;
  children: any;
}

const ItemComment: FC<ItemProps> = props => {
  const { title, children } = props;
  const mainClass = 'orderExecutionContainer-itemc';
  const classes = [mainClass];

  return (
    <div className={classes.join(' ')}>
      {title && <div className={`${mainClass}__title`}>{title}</div>}
      {children}
    </div>
  );
};

export default ItemComment;
