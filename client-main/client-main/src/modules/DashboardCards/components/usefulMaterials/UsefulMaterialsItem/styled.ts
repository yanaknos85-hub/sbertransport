/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

export const WrapperUsefulMaterialsItems = styled.div`
  display: flex;
  flex-wrap: wrap;
`;

export const WrapperUsefulMaterialsItemsMobile = styled.div`
  display: flex;
  width: fit-content;
`;

export const WrapperUsefulMaterialsItem = styled.div<{ cover: string }>`
  margin: 0 12px 12px 0;
  width: 160px;
  height: 160px;
  padding: 15px;
  cursor: pointer;
  border-radius: 8px;
  background-image: url(${props => (props.cover ? require(`../../../../../shared/components/Images/instructions/${props.cover}`) : '')});
  background-size: cover;
`;

export const FullScreenContent = styled.div<{ background: string }>`
  position: fixed;
  width: 100%;
  height: 100%;
  top: 100%;
  left: 100%;
  transform: translate(-100%,-100%);
  background-image: ${props => (props.background ? `url(${props.background})` : '')};
  background-size: cover;
  z-index: 2;
  overflow: auto;
`;

export const UsefulMaterialsItemTitle = styled.p`
  color: rgb(255, 255, 255);
  font-family: SB Sans Text;
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  letter-spacing: -0.15px;
  white-space: pre-line;
`;

export const NoInstructions = styled.div`
  width: 100%;
  height: 160px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: rgb(168, 171, 179);
  font-family: SB Sans Interface;
  font-size: 24px;
  font-weight: 600;
  line-height: 32px;
  letter-spacing: 0px;
`;

