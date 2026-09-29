/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

export const Container = styled.div`
  width: 368px;
  border-radius: 12px;
  padding: 20px;
  background-color: var(--pure-white);
  flex: 1 0 0;

  @media (max-width: 767px) {
    width: 100%;
  }
`;

export const Total = styled.div`
  display: flex;
  justify-content: space-around;
  padding-top: 24px;
`;

export const DetailingTextLine = styled('div')<{ mb?: string; ml?: string }>`
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  color: ${props => props.color || '#909090'};
  padding: ${props => props.padding || 0};
  margin: ${props => props.m || 0};
  margin-top: ${props => props.mt || 0};
  margin-bottom: ${props => props.mb || 0};
  margin-left: ${props => props.ml || 0};
  opacity: ${props => props.opacity};
`;

export const Bottom = styled('div')<any>`
  margin-top: 28px;
`;
