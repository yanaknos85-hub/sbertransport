import React, { FC } from 'react';
import { RadioChangeEvent } from 'antd/lib/radio';
import { Radio } from 'antd';
import styled from 'styled-components';
import { Mode } from './types';

const StyledModeSelector = styled.div`
  padding-bottom: var(--padding-base);
`;

const options = [
  { value: 'date', label: 'День' },
  { value: 'range', label: 'Диапазон' },
  { value: 'quarter', label: 'Квартал' },
  { value: 'year', label: 'Год' },
];

export const SelectMode: FC<{ value: Mode; onChange: (mode: Mode) => void }> = ({ value, onChange }) => {
  const handleModeChange = ({ target: { value } }: RadioChangeEvent) => {
    onChange(value);
  };

  return (
    <StyledModeSelector>
      <Radio.Group
        onChange={handleModeChange}
        value={value}
        options={options}
        buttonStyle="solid"
        size="small"
      />
    </StyledModeSelector>
  );
};
