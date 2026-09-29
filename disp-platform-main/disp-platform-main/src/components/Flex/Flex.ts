import styled from 'styled-components';

interface FlexProps {
  justifyContent?: 'flex-start' | 'center' | 'flex-end' | 'space-between';
  alignItems?: 'stretch' | 'center' | 'start' | 'end';
  gap?: number;
  marginBottom?: boolean;
}

const Flex = styled.div<FlexProps>`
  display: flex;
  justify-content: ${({ justifyContent }) => justifyContent ?? 'flex-start'};
  align-items: ${({ alignItems }) => alignItems ?? 'start'};
  gap: ${({ gap }) => `${gap ?? 10}px`};
  margin-bottom: ${({ marginBottom }) => `${marginBottom ? 16 : 0}px`};
`;

export default Flex;
