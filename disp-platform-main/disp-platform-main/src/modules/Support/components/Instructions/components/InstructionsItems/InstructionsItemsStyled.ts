import styled from 'styled-components';

export const InstructionsContainer = styled.div`
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
`;

export const Instruction = styled.div<{ cover: string }>`
  width: 186px;
  height: 160px;
  border-radius: 8px;
  padding: 12px 10px 0;
  cursor: pointer;
  background-image: url(${props => props.cover ? require(`assets/images/instructions/${props.cover}`) : ''});
  background-size: cover;
`;

export const InstructionTitle = styled.p`
  font-family: 'SB Sans Text', sans-serif;
  font-size: 14px;
  font-weight: 500;
  line-height: 18px;
  letter-spacing: -0.15px;
  color: #262626;
  margin: 0;
`;

export const FullScreenContent = styled.div`
  position: fixed;
  width: 100%;
  height: 100%;
  top: 100%;
  left: 100%;
  transform: translate(-100%, -100%);
  background-image: url(${() => require(`assets/images/backgroundInstruction.png`)});
  background-size: cover;
  z-index: 10002;
  overflow: auto;
`;
