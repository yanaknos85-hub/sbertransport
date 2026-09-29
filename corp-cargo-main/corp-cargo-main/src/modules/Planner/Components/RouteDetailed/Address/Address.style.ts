import styled from 'styled-components';
import { colors } from 'shared/styles/styles';


export const Card = styled.div`
  display: flex;
  justify-content: space-between;
  padding: 7px;
  margin-bottom: 4px;
  border-radius: 12px;
  background-color: ${colors.white};
  border: 1px solid rgba(0, 0, 0, 0.06);
  cursor: grab;
`;

export const Content = styled.div`
  display: flex;
  justify-content: space-between;
  flex: 4 0 0;
  width: 100%
`;

export const LeftSide = styled.div`
  display: flex;
  align-items: center;
  flex: 1;
`;

export const LetterElement = styled.div`
  padding: 6px
`;

export const Info = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex: 5;
`;

export const Bucket = styled.div`
  display: flex;
  align-items: center;
  flex: 2;
  justify-content: flex-end;
  cursor: pointer;
  max-width: 24px;
  height: 24px;
`;

export const IdElement = styled.div`
  display: flex
`;

export const Type = styled.div`
  margin-left: 4px;
  color: rgb(173, 173, 173);
`;

export const AddressTitle = styled.div`
  display: flex;
  align-items: center;
  color: rgb(173, 173, 173);
`;

export const InfoWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
`;

export const Link = styled.a`
  margin-left: 12px;
  color: ${colors.green10};
`;

export const Status = styled.span`
  color: ${colors.gray5};
  font-size: 14px;
  font-weight: 600;
`;

export const Address = styled.div`
  color: ${colors.gray10};
  font-size: 14px;
  font-weight: 600;
  max-width: 284px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
`;

export const IconBlock = styled.div`
  display: flex;
  align-items: center;
  margin-top: 8px;
`;

export const Text = styled.div`
  margin-left: 8px;
  font-size: 14px;
  color: ${colors.gray6};
`;

export const Text2 = styled.p`
  font-size: 10px;
  color: ${colors.green10};
`;

export const AddressCircle = styled.div`
  background: #c2c2c2;
  font-family: 'SB Sans Interface', serif, sans-serif;
  font-style: normal;
  width: 18px;
  height: 18px;
  text-align: center;
  color: white;
  font-size: 10px;
  line-height: 12px;
  -webkit-box-align: center;
  align-items: center;
  display: flex;
  -webkit-box-pack: center;
  justify-content: center;
  border-radius: 10px;
  font-weight: 600;
  z-index: 2;
`;

export const AddressCircleFirst = styled(AddressCircle)`
  background: #10bf6a;
`;

export const AddressCircleLast = styled(AddressCircle)`
  background: #6979f7;
`;
