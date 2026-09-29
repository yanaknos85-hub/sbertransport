import React, { FC, ReactElement } from 'react';
import styled from 'styled-components';

const TotalItem = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
`;

const UnitBlock = styled.div`
  display: flex;
  justify-content: center;
`;

const UnitText = styled.span`
  font-weight: 600;
  color: var(--gray-10);
`;

const UnitDescription = styled.p`
  margin-bottom: var(--margin-base);
  color: var(--gray-7);
  font-size: var(--fz-14);
  font-weight: 300;
`;

interface Props {
  img: ReactElement;
  data: number | string | undefined;
  units?: string;
  description: string;
  tag?: ReactElement;
}

const CargoTotalItem: FC<Props> = ({
  img, data, units, description, tag,
}) => (
  <TotalItem>
    <UnitBlock>{img}</UnitBlock>
    <UnitBlock>
      <UnitText>
        {data}
        {' '}
        {units}
        {tag ? tag : ''}
      </UnitText>
    </UnitBlock>
    <UnitDescription>{description}</UnitDescription>
  </TotalItem>
);

export default CargoTotalItem;
