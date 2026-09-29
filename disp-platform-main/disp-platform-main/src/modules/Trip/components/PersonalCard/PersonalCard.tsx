import React from 'react';
import { Avatar } from 'antd';
import Title from 'antd/lib/typography/Title';
import { FC, ReactNode } from 'react';
import Flex from 'components/Flex/Flex';
import styles from './personalCard.module.scss';

interface PersonalCardProps {
  title: string;
  desc1: string | ReactNode;
  desc2?: string | ReactNode;
  src?: string;
  alt?: string;
}

const PersonalCard: FC<PersonalCardProps> = ({
  title,
  desc1,
  desc2,
  src,
  alt,
}) => (
  <Flex alignItems="center">
    <Avatar
      size={40}
      src={src}
      alt={alt}
    >
      {title.split(' ')[0]?.[0]}
      {' '}
      {title.split(' ')[1]?.[0]}
    </Avatar>
    <div>
      <Title className={styles.title} level={5}>{title}</Title>
      <div>
        {desc1}
        {' '}
        {desc2 && (
        <span>
          |
          {desc2}
        </span>
        )}
      </div>
    </div>
  </Flex>
);

export default PersonalCard;
