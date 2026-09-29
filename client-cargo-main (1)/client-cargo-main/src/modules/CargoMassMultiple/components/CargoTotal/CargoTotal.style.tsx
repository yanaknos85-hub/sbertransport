/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

interface DetailingTextLine {
  m?: string;
  mt?: string;
  mb?: string;
  color?: string;
  padding?: string;
  opacity?: string;
}

export const Container = styled.div`
  width: 368px;
  border-radius: 12px;
  padding: 20px;
  background-color: var(--pure-white);
  flex: 1 0 0;
`;

export const Total = styled.div`
  display: flex;
  justify-content: space-around;
  padding-top: 24px;
`;

export const Detailing = styled('div')`
  opacity: 0.7;
`;

export const DetailingTextLine = styled('div')<DetailingTextLine>`
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  color: ${props => props.color || '#909090'};
  padding: ${props => props.padding || 0};
  margin: ${props => props.m || 0};
  margin-top: ${props => props.mt || 0};
  margin-bottom: ${props => props.mb || 0};
  opacity: ${props => props.opacity};
`;
