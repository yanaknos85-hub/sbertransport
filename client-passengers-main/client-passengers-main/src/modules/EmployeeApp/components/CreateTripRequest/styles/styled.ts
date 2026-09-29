/* eslint-disable @typescript-eslint/no-explicit-any */
import styled from 'styled-components';
import Button from 'shared/form/Button/Button';

const Item = styled.div<{ $padding?: string; $margin?: string }>`
  position: relative;

  padding: 16px 0;
`;

const ItemNewDesign = styled.div<{
  $padding?: string;
  $margin?: string;
  $position?: string;
  $bottom?: string;
  $width?: string;
}>`
  background-color: white;
  border-radius: 16px;

  width: ${props => props.$width && props.$width};
  bottom: ${props => props.$bottom && props.$bottom};
  position: ${props => props.$position && props.$position};
  margin: ${props => (props.$margin ? props.$margin : '16px')};
  padding: ${props => props.$padding && props.$padding};
`;

const Back = styled.div`
  background-color: white;
  border-radius: 16px;
  display: flex;
  justify-content: right;
`;

const PointLetter = styled.div<{ $backgroundColor: string }>`
  width: 18px;
  height: 18px;
  text-align: center;
  color: white;
  font-size: 10px;
  line-height: 12px;
  align-items: center;
  display: flex;
  justify-content: center;
  border-radius: 10px;
  font-weight: 600;
  background-color: ${props => props.$backgroundColor};
  margin-left: 8px;
`;

const WrapperPointLetter = styled.div`
  display: flex;
  align-items: center;
  margin-left: 10px;
`;

const AutocompleteText = styled.span`
  padding: 0 var(--padding-base);
  max-width: 394px;
  overflow: auto;
`;

const ButtonWrapper = styled.div`
  margin-top: 16px;
`;

const SideFake = styled('div')<any>`
  width: 460px;
  min-width: 460px;
  height: 100%;
`;

const Side = styled('div')<any>`
  z-index: 0;
  position: fixed;
  top: 64px;
  right: 0;
  bottom: 0;
  width: 482px;
  min-width: 482px;
  background: #fff;
`;
const SideInner = styled('div')<any>`
  width: 100%;
  height: 100%;
  background: #fff;
`;

const SideContent = styled('div')<any>`
  display: flex;
  flex-direction: column;
  height: 100%;
  padding-top: 24px;
  padding-bottom: 120px;
`;

const SideSections = styled('div')<any>`
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px 24px 0 24px;
  margin-top: 12px;
`;

const SideButtons = styled('div')<any>`
  position: absolute;
  bottom: 0;
  width: 100%;
  padding-bottom: 24px;
  margin-left: 24px;
  margin-right: 32px;
  height: 132px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.0001) 0%, #ffffff 44.65%, #ffffff 100%);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
`;

const SideButton = styled(Button)<any>`
  max-width: 426px;
`;

export {
  Item,
  ItemNewDesign,
  Back,
  PointLetter,
  AutocompleteText,
  ButtonWrapper,
  SideFake,
  Side,
  SideInner,
  SideContent,
  SideSections,
  SideButtons,
  SideButton,
  WrapperPointLetter
};
