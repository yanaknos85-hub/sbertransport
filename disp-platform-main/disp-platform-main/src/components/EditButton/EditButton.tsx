import React, { FC, ButtonHTMLAttributes, DetailedHTMLProps } from 'react';
import cn from 'classnames';
import styles from './editButton.module.scss';
import { ReactComponent as Pen } from 'assets/icons/pen.svg';

const EditButton: FC<DetailedHTMLProps<ButtonHTMLAttributes<HTMLButtonElement>, HTMLButtonElement>> = ({
  className,
  ...props
}) => (
  <button className={cn(styles.editButton, className)} {...props}>
    <Pen />
  </button>
);

export default EditButton;
