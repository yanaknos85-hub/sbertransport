import React from 'react';
import { CargoMainInnerContent } from 'shared/components/Cargo/CargoLayout';

import * as Styled from './CargoFinalStep.style';

interface SectionWrapperProps {
  title: string;
  count?: number | string;
  children: React.ReactNode;
}

export const SectionWrapper: React.FC<SectionWrapperProps> = ({
  title,
  count,
  children,
}) => {
  return (
    <CargoMainInnerContent>
      <Styled.CargosHeader>
        <Styled.CargosHeaderText>{title}</Styled.CargosHeaderText>
        {count !== undefined && (
          <Styled.CargosHeaderCount>
            {count}
          </Styled.CargosHeaderCount>
        )}
      </Styled.CargosHeader>
      {children}
    </CargoMainInnerContent>
  );
};
