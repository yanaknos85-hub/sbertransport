import styled from 'styled-components';

export const LoadingProgress = styled.div<{ progress: number }>`
  background: #b7ecd2;
  height: 4px;
  margin-top: 8px;
  overflow: hidden;
  border-radius: 12px;

  &:after {
    content: '';
    display: block;
    background: #10bf6a;
    height: 100%;
    width: ${({ progress }) => progress || 1}%;
    border-radius: 12px;
    transition: width 0.3s ease-in-out;
  }
`;
