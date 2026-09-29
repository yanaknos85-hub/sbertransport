import React from 'react';
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
  img: any;
  data: number | string | undefined;
  units?: string;
  description: string;
  tag?: React.ReactElement;
}

const CargoTotalItemMulitple: React.FC<Props> = ({ ...props }) => (
  <TotalItem>
    <UnitBlock>{props.img}</UnitBlock>
    <UnitBlock>
      <UnitText>
        {props.data}
        {' '}
        {props.units}
        {props.tag ? props.tag : ''}
      </UnitText>
    </UnitBlock>
    <UnitDescription>{props.description}</UnitDescription>
  </TotalItem>
);

export default CargoTotalItemMulitple;
