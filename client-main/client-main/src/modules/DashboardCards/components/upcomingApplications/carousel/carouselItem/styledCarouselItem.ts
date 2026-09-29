import styled from 'styled-components';

export const CarouselItemElement = styled.div`
  flex: 0 0 calc(25% - 10px);
  margin-right: 10px;
  cursor: pointer;
  padding: 12px;
  background: #fff;
  border-radius: 10px;
  -webkit-user-select: none;
  -khtml-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  
  & img {
    max-width: 100%;
  }

  &:last-child {
    margin-right: 0;
  }

  .ant-divider {
    margin: 8px 0;
  }
`;

export const TransportTypeWrapper = styled.div`
  display: flex;
  align-items: center;
  margin-top: 8px;
`;

export const TransportPictureWrapper = styled.div`
  display: flex;
  flex-direction: row;
  border-radius: 50%;
  width: 36px;
  height: 24px;
  overflow: hidden;
  margin-right: 12px;
`;

export const TransportType = styled.img`
  display: inline-block;
  background-size: cover;
  width: 36px;
  height: 23px;
  transform: translateX(7px);
`;

export const Date = styled.span`
  color: rgb(38, 38, 38);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  letter-spacing: -0.3px;
`;

export const CoopTrip = styled.span`
  color: rgb(144, 144, 144);
  font-family: SB Sans Interface;
  font-size: 12px;
  font-weight: 400;
  line-height: 16px;
  letter-spacing: 0.2px;
`;

export const Purpose = styled.span`
  color: rgb(144, 144, 144);
  font-family: SB Sans Interface;
  font-size: 12px;
  font-weight: 400;
  line-height: 16px;
  letter-spacing: 0.2px;
`;

export const TransportTypeTitle = styled.span`
  color: rgb(38, 38, 38);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  letter-spacing: -0.3px;
`;

export const TripInfo = styled.div`
  display: flex;
  flex-direction: column;
`;
