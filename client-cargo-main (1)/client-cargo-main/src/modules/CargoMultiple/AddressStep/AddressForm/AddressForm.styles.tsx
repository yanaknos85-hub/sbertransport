import styled from 'styled-components';

export const SideSections = styled('div')`
  padding: 16px;
  margin-top: 12px;
  background-color: #fff;
  border-radius: 16px;
`;

export const Section = styled('div')`

  &:first-child {
    border-bottom: 1px solid rgba(38, 38, 38, 0.08);
  }

  &:first-child + & {
    margin-top: 24px;
`;

export const ErrorsWrapper = styled('div')`
  margin-bottom: 8px;
`;
