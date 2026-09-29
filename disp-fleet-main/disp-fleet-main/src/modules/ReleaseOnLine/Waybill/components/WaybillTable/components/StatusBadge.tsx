import React, { FC } from 'react';

import styled from 'styled-components';

import Flex from 'components/Flex/Flex';

import { ReactComponent as NotPassed } from 'assets/icons/cancel.svg';
import { ReactComponent as Passed } from 'assets/icons/passed.svg';

interface Props {
  passed: boolean;
}

const CircleIcon = styled.div<{ passed: boolean }>`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: ${({ passed }) => (passed ? '#52C41A' : '#FF4D4F')};
  color: white;
  font-size: 10px;
`;

const StatusBadge: FC<Props> = ({ passed }) => {
  return (
    <Flex alignItems="center" gap={8}>
      <CircleIcon passed={passed}>
        {passed ? <Passed /> : <NotPassed />}
      </CircleIcon>
      {passed ? 'Пройдено' : 'Не пройдено'}
    </Flex>
  );
};

export default StatusBadge;
