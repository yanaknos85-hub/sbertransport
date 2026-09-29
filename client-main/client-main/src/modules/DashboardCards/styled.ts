/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

import Panel from 'shared/ui/Panel';

export const Cards = styled.div<{ direction?: 'row' | 'column' }>`
  display: flex;
  flex-wrap: wrap;
  flex-direction: ${({ direction }) => (direction ? direction : 'row')};
`;

export const StyledCard = styled(Panel) <{ accentColor: string }>`
  width: 23.9%;
  height: 300px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  color: var(--ship-cove);
  font-size: 14px;
  line-height: 22px;
  box-sizing: border-box;

  @media (max-width: 1003px) {
    max-width: 100%;
  }

  @media (max-width: 1810px) {
    width: 23.5%;
  }

  a,
  a:visited,
  a:active {
    color: inherit;
  }
`;

export const StyledCardMobile = styled(Panel) <{ accentColor: string }>`
  display: flex;
  justify-content: space-between;
  padding: 8px;

  color: var(--ship-cove);
  font-size: 14px;
  line-height: 22px;
  box-sizing: border-box;

  a,
  a:visited,
  a:active {
    color: inherit;
  }
`;

export const CardBody = styled.div`
  margin-top: 24px;
`;

export const StyledTitle = styled.div<{ margin: string }>`
  display: flex;
  font-size: 20px;
  letter-spacing: 0px;
  line-height: 28px;
  font-weight: 600;
  color: black;
  justify-content: space-between;
  align-items: flex-end;
  font-family: "SB Sans Interface";
  margin: ${props => props.margin};
`;

export const Header = styled.div`
  display: flex;
  justify-content: space-between;
`;

export const LimitStatus = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const StyledColumnLeft = styled.div`
  // flex: 1 0;
  display: flex;
  flex-direction: column;

  a {
    padding: 9px 20px !important;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  p {
    margin-bottom: 0;
  }
`;

export const StyledColumnRight = styled.div<any>`
  // flex: 1 0;
  height: 100%;
  display: flex;
  width: 100%;
  justify-content: space-between;
  align-items: flex-end;
  pointer-events: ${props => props.disabled && 'none'};
`;

export const Bager = styled.div<{ $color?: string }>`
  width: 8px;
  height: 8px;
  border-radius: 4px;
  margin: 0 8px 0 0;

  background-color: ${props => props.$color};
`;

export const Price = styled.div`
  font-family: "SB Sans Text";
  font-size: 18px;
  font-weight: 500;
  line-height: 20px;
  letter-spacing: -1.15px;
  color: black;
  padding: 0 6px 12px 0;
  text-align: left;
`;

export const RequestLimit = styled.div`
  color: var(--jade);
`;

export const Description = styled.div`
  display: flex;
  height: 100%;
  flex-direction: column;
  // align-items: flex-end;
  // height: 237px;
  font-size: 16px;
  line-height: 22px;
  letter-spacing: -0.154px;
  color: #adadad;
  // gap: var(--padding-large);

  ul {
    padding: 0 0 0 20px;
    max-width: 237px;
  }
`;

export const TaxiIcon = styled.div<any>`
  display: flex;
  width: 85px;
  height: 57.5px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
`;

export const PersonalIcon = styled.div<any>`
  display: flex;
  width: 157.5px;
  height: 99.77px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
`;

export const CargoIcon = styled.div<any>`
  display: flex;
  width: 85px;
  height: 75px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
`;

export const CarshIcon = styled.div<any>`
  display: flex;
  width: 157.5px;
  height: 98.77px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
`;

export const PublicIcon = styled.div<any>`
  display: flex;
  width: 157.5px;
  height: 99.14px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
`;

export const CoopIcon = styled.div<any>`
  display: flex;
  width: 160px;
  height: 90px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
`;

export const TruckIcon = styled.div<any>`
  display: flex;
  width: 86px;
  height: 70px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
`;

export const ParkingIcon = styled.div<any>`
  display: flex;
  width: 67px;
  height: 75px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
`;

export const RepairIcon = styled.div<any>`
  display: flex;
  width: 70px;
  height: 75px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
`;

export const QuotaManagement = styled.div<any>`
  display: flex;
  width: 160px;
  height: 120px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
`;

export const WrapperStyledTitle = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 8px;
`;

export const WrapperOpenMainContent = styled.div`
  margin: 0 8px 8px 0;
  display: flex;
`;

export const OpenMainContent = styled.span`
  margin-right: 8px;
  color: rgb(16, 191, 106);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  letter-spacing: -0.3px;
  cursor: pointer;
`;

export const NoActiveApplications = styled.div`
  display: flex;
  justify-content: center;
  margin-top: 16px;
`;

export const NoActiveApplicationsTitle = styled.span`
  color: rgb(168, 171, 179);
  font-family: SB Sans Interface;
  font-size: 24px;
  font-weight: 600;
  line-height: 32px;
  letter-spacing: 0px;
`;

export const WrapperTitle = styled.div`
  margin: 24px 0px 8px 8px;
  display: flex;
`;

export const StyledWidgetsTitle = styled.div`
  display: flex;
  font-size: 20px;
  letter-spacing: 0px;
  line-height: 28px;
  font-weight: 600;
  color: black;
  justify-content: space-between;
  font-family: 'SB Sans Interface';
  align-items: center;
`;

export const WrapperOpenWidgetsMainContent = styled.div`
  margin: 0 8px 8px 0;
  display: flex;
  height: 40px;
  align-items: center;
`;

export const WrapperStyledWidgetsTitle = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
`;

export const WidgetCard = styled.div<{ direction?: 'column' | 'row' }>`
  display: flex;
  margin: 0 8px;
  flex-wrap: wrap;

  flex-direction: ${({ direction }) => (direction ? direction : 'row')};
`;

export const WrapperUsefulMaterials = styled.div`
  padding: 12px 12px 24px 12px;
  background: rgb(255, 255, 255);
  border-radius: 12px;
  margin: 0 8px;

  .ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn {
    color: rgb(38, 38, 38);
    font-family: SB Sans Text;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    letter-spacing: -0.3px;
  }

  .ant-tabs-tab-btn {
    color: rgb(115, 115, 115);
    font-family: SB Sans Text;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    letter-spacing: -0.3px;
  }
`;

export const WrapperUsefulMaterialsMobile = styled.div`
  padding: 12px;
  margin-bottom: 12px;
  background: rgb(255, 255, 255);

  overflow-x: scroll;
  scrollbar-width: thin;
`;
