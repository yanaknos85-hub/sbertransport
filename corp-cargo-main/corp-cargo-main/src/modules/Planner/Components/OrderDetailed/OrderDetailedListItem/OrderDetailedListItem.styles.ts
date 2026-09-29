import styled from 'styled-components';
import { colors } from 'shared/styles/styles';

export const Card = styled.div`
  padding: 16px 14px;
  margin-bottom: 12px;
  width: 98%;
  border-radius: 12px;
  box-shadow: 0 0 1px rgba(0, 0, 0, 0.12), 0 8px 16px -4px rgba(0, 0, 0, 0.12);
  background-color: ${colors.white};
`;

export const Title = styled.div`
  margin: 12px 0;
  font-size: 18px;
  font-weight: 600;
`;

export const Content = styled.div`
  display: flex;
  justify-content: flex-start;
  align-items: flex-end;
`;

export const Link = styled.a`
  margin-left: 12px;
  color: ${colors.green10};
`;

export const Status = styled.span`
  color: ${colors.gray5};
  font-size: 16px;
  font-weight: 600;
`;

export const Address = styled.span`
  margin-left: 4px;
  color: ${colors.gray10};
  font-size: 16px;
  font-weight: 600;
`;

export const List = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
`;

export const Text = styled.div`
  margin-left: 8px;
  font-size: 14px;
  color: ${colors.gray6};
`;
