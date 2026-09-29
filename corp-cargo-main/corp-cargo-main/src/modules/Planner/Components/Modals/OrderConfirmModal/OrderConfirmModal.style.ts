import styled from 'styled-components';
import { Button as ButtonAnt } from 'antd';
import { colors, fontFamily } from 'shared/styles/styles';

export const ModalContainer = styled.div`
  display: flex;
  flex-direction: column;
`;

export const Title = styled.h3`
  font-size: 20px;
  font-weight: 600;
  font-family: ${fontFamily.SBSansInterface};
  margin-bottom: 8px;
`;

export const NameRouteTitle = styled.h4`
  color: ${colors.black};
  font-size: 16px;
  font-family: ${fontFamily.SBSansTextRegular};
`;

export const SubTitle = styled.h4`
  color: ${colors.gray6};
  font-size: 14px;
`;

export const RouteList = styled.ul`
  list-style-type: none;
  padding: 0;
  li {
    color: ${colors.solidBodyNormal};
    font-size: 14px;
  }
`;

export const Link = styled.a`
  color: ${colors.solidBodyNormal};

  &:hover {
    color: ${colors.black};
  }
`;

export const ButtonOk = styled(ButtonAnt)`
  padding: 21px 0;
  width: 352px;
  margin-bottom: 12px;
  line-height: 0;
  color: ${colors.white};
  background-color: ${colors.solidBodyNormal};
  border-radius: 8px;
  border: none;
  outline: none;

  &:hover {
    color: ${colors.solidBodyNormal};
    background-color: ${colors.white};
  }
`;

export const ButtonCancel = styled(ButtonAnt)`
  padding: 21px 0;
  width: 352px;
  line-height: 0;
  color: ${colors.gray10};
  background: ${colors.bgDark};
  border-radius: 8px;
  border: none;
  outline: none;

  &:hover {
    background-color: ${colors.gray9};
    color: ${colors.white};
  }
`;

export const ButtonsContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
`;
