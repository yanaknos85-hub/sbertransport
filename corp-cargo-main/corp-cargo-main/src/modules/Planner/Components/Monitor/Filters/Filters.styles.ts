import styled from 'styled-components';
import { colors } from 'shared/styles/styles';
import { Space } from 'antd';

export const Container = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  margin-bottom: 12px;
  border-radius: 12px;
  background-color: ${colors.white};
`;

export const Handlers = styled.div`
  display: flex;
  justify-content: center;
  width: 50%;
  align-items: center;
`;

export const Icons = styled(Space)`
  display: flex;
  justify-content: center;
  align-items: center;
`;
