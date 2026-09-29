import styled from 'styled-components';
import { Button as ButtonAnt } from 'antd';
import { colors, fontFamily } from 'shared/styles/styles';

export const ListContainer = styled.div`
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: 12px;
  width: 500px;
`;

export const ListItem = styled.div<{ active: boolean}>`
  display: flex;
  flex-direction: column;
  padding: 10px;
  margin-bottom: 20px;
  position: relative;
  border: 2px solid ${props => (props.active ? `${colors.green10}` : `${colors.gray3}`)};
  border-radius: 12px;
  box-shadow: 0 20px 50px rgba(255, 255, 255, 0.5);
  background: #ffffff;
`;

export const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const HeaderContent = styled.div`
  display: flex;
  flex-direction: column;
  flex: 3 0 0;

  p {
    font-family: ${fontFamily.SBSansTextRegular};
    margin: 0;
    padding: 0;
    font-size: 14px;
    color: ${colors.gray6};
  }

  .id {
    color: ${colors.black};
    font-size: 14px;
    font-weight: 600;
    font-family: ${fontFamily.SBSansInterface};
  }
`;

export const Delivery = styled.div`
  font-size: 12px;
  color: ${colors.gray6};
`;

export const Id = styled.div`
  display: flex;
  align-items: center;
  cursor: pointer;

  span {
    margin-right: 6px;
  }
`;

export const Params = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

export const Checkbox = styled.div`
  display: flex;
  align-items: center;

  .ant-checkbox-wrapper {
    margin-left: 10px;
  }

  .ant-checkbox-inner {
    width: 16px;
    height: 16px;
    border: ${`1px solid ${colors.gray4}`};
    border-radius: 4px;
  }
`;

export const CapacityInfo = styled.p`
  margin: 0 0 0 4px;
  font-size: 10px;
`;

export const Planning = styled.div`
  display: flex;
  justify-content: flex-end;
`;

export const ListItemAddressStyled = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const ListItemInfoStyled = styled.div`
  display: flex;
  justify-content: flex-end;
  gap: 10px;
`;

export const Contractor = styled.div`
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 99%;
  color: ${colors.gray8};
  font-size: 12px;
`;

export const Price = styled.div`
  text-align: right;
  p {
    margin: 0;
    padding: 0;
  }

  p:nth-child(1) {
    font-size: 14px;
    font-family: ${fontFamily.SBSansTextRegular};
    color: ${colors.gray7};
  }

  p:nth-child(2) {
    font-weight: 700;
    font-size: 24px;
    line-height: 32px;
    color: ${colors.black90Alpha};
  }
`;

export const ButtonBlockStyled = styled.div`
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-top: 8px;
`;

export const ButtonStyled = styled(ButtonAnt)`
  padding: 19px 20px;
  color: ${props => (props.success ? colors.white : colors.black)};
  background-color: ${props => (props.success ? colors.green10 : colors.white)};
  border-radius: 8px;
  line-height: 0;
  border: ${props => (props.success ? `1px solid ${colors.green10}` : 'none')};

  &:hover {
    color: ${props => (props.success ? colors.green10 : colors.black)};
    background-color: ${colors.white};
    border: ${props => (props.success ? `1px solid ${colors.green10}` : `1px solid ${colors.black}`)};
  }
`;
