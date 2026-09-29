/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

export const Header = styled('header')`
  display: flex;
  flex-direction: column;
  padding: 16px;
  background-color: #fff;
  border-radius: 15px;
`;

export const Inner = styled('div')`
  display: flex;
`;

export const Icon = styled('div')`
  margin-right: 10px;
`;

export const Content = styled('div')`
  flex-grow: 1;
  display: flex;
  flex-direction: column;
`;

export const Title = styled('h3')`
  font-size: 24px;
  font-family: 'SB Sans Interface SemiBold', serif, sans-serif;
  line-height: 28px;
  letter-spacing: 0;
  margin: 0;

  @media (max-width: 767px) {
    font-size: 16px;
  }
`;

export const Step = styled('div')`
  font-family: 'SB Sans Text', serif, sans-serif;
  color: #909090;
  font-size: 16px;
`;
export const StepContainer = styled('div')`
  display: flex;
  align-items: center;
  margin-top: 10px;
`;

export const StepDate = styled('div')`
  font-family: 'SB Sans Text', serif, sans-serif;
  color: #000000;
  font-size: 14px;
  font-weight: 600;
`;

export const StepTitle = styled('span')`
  margin-right: 8px; 
`;

