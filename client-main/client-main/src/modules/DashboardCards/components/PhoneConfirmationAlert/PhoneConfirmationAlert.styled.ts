import styled from 'styled-components';

export const Container = styled.div`
  display: flex;
  border-radius: 8px;
  padding: 16px;
  gap: 12px;
  background-color: #fff5eb;
  border: 1px solid #ffe1c2;
  margin: 0 8px;
  cursor: pointer;

  svg {
    width: 24px;
    height: 24px;
  }
`;

export const Title = styled.h2`
  font-family: 'SB Sans Text';
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
  letter-spacing: -0.3px;
  color: var(--gray-10);
`;

export const Description = styled.p`
  font-family: 'SB Sans Text';
  font-size: 14px;
  line-height: 22px;
  letter-spacing: -0.3px;
  color: var(--gray9);
  margin: 4px 0 0;
`;
