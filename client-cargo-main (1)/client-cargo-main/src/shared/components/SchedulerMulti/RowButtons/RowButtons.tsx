import React, { FC, useState } from 'react';
import styled from 'styled-components';

import Cell from '../Cell/Cell';
import Row, { RowItem } from '../Row/Row';

const Label = styled('div')`
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-size: 12px;
  line-height: 16px;
  letter-spacing: 0.2px;
  color: #262626;
  margin-bottom: 8px;
`;

const Component: FC<{
  initialValue?: number[];
  label: string;
  names?: string[];
  values: number[];
  onChange?: (vals: number[]) => void;
  period?: number[];
  isPeriod?: boolean;
}> = ({
  initialValue, label, names, values, onChange, isPeriod, period,
}) => {
  const [activeValues, setActiveValues] = useState<number[]>(period || initialValue || []);

  const handleOnChange = (value: number) => {
    let newValues: number[];

    if (activeValues.includes(value)) {
      newValues = activeValues.filter(val => val !== value);
    } else {
      newValues = [...activeValues, value];
    }
    newValues.sort();
    setActiveValues(newValues);

    if (onChange) {
      onChange(newValues);
    }
  };

  return (
    <div>
      <Label>{label}</Label>
      <Row>
        {values.map((val, i) => (
          <RowItem key={values[i] + i} onClick={isPeriod ? null : () => handleOnChange(val)}>
            <Cell isPeriod={isPeriod} active={activeValues.includes(val)}>
              {(names && names[i]) || val}
            </Cell>
          </RowItem>
        ))}
      </Row>
    </div>
  );
};

export default Component;
