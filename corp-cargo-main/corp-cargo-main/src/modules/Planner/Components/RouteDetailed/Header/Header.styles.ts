import styled from 'styled-components';
import { Button as ButtonAnt } from 'antd';
import { colors, fontFamily } from 'shared/styles/styles';

export const Header = styled.div`
  width: 100%;
`;

export const TitleBlock = styled.div<{ isPlanningFinished: boolean }>`
  display: flex;
  flex-direction: ${props => props.isPlanningFinished ? 'column' : 'row'};
  justify-content: space-between;
  align-items: flex-start;
`;

export const TextBlock = styled.div<{ isPlanningFinished: boolean }>`
  display: flex;
  flex-direction: column;
  margin-bottom: ${props => props.isPlanningFinished ? '12px' : '0'};
`;

export const HeaderTitle = styled.div`
  display: flex;
  align-items: center;
  cursor: pointer;
  span.route-id-text {
    margin-left: 16px;
    margin-right: 6px;
  }
`;

export const TextLight = styled.p`
  margin-bottom: 0;
  margin-left: 37px;
  font-family: ${fontFamily.SBSansTextRegular};
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  color: ${colors.gray8};
`;

export const Button = styled(ButtonAnt)<{ isPlanningFinished: boolean }>`
  padding: 19px 20px;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  border-radius: 8px;
  line-height: 0;
  border: none;
  margin-left: ${props => props.isPlanningFinished ? '37px' : '0'};

  &:hover {
    color: ${colors.solidBodyNormal};
    border: 1px solid ${colors.solidBodyNormal};
    background-color: ${colors.white};
  }
`;
