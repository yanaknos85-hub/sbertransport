import React, { FC, ButtonHTMLAttributes, DetailedHTMLProps } from 'react';
import cn from 'classnames';
import styles from './actionButton.module.scss';
import { ReactComponent as Pen } from 'shared/icons/pen.svg';
import { ReactComponent as Delete } from 'shared/icons/delete.svg';

interface ActionButtonProps extends DetailedHTMLProps<ButtonHTMLAttributes<HTMLButtonElement>, HTMLButtonElement> {
  iconType?: 'edit' | 'delete';
}

const ActionButton: FC<ActionButtonProps> = ({
  className, iconType, ...props
}) => (
  <button className={cn(styles.actionButton, className)} {...props}>
    {iconType === 'delete' ? <Delete /> : <Pen />}
  </button>
);

export default ActionButton;
