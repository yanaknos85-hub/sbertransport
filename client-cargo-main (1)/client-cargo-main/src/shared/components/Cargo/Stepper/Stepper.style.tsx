import styled from 'styled-components';

export const FirstStepDiv = styled.div`
  display: flex;
  align-items: center;
  flex: 1;
`;

export const MiddleStepDiv = styled.div`
  display: flex;
  align-items: center;
  flex: 2;
`;

export const LastStepDiv = styled.div`
  display: flex;
  align-items: center;
  flex: 1;
`;

export const StepperDiv = styled.div`
  display: flex;
  width: 100%;
`;

export const EmptyIcon = styled.div`
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #d6d6d6;
  background-color: #fff;
  font-size: 10px;
  color: #fff;
`;

export const CurrentIcon = styled.div`
    width: 20px;
  height: 20px;
  padding: 3px;
  border-radius: 50%;
  border: 2px solid #ff9a32;
  background-color: #fff;
  font-size: 10px;
  color: #fff;
`;

export const CurrentInnerIcon = styled.div`
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #ff9a32;
  font-size: 10px;
  color: #fff;
`;

export const DoneIcon = styled.div`
  display: flex;
  justify-content: center;
  width: 20px;
  height: 20px;
  padding: 4px;
  border-radius: 50%;
  background-color: #19b150;
  font-size: 10px;
  color: #fff;
`;

export const EmptyEdge = styled.div`
 flex: 1;
  height: 3px;
  background-color: #D6D6D6;
`;

export const CurrentEdge = styled.div`
 flex: 1;
  height: 3px;
  background-color: #ff9a32;
`;

export const DoneEdge = styled.div`
 flex: 1;
  height: 3px;
  background-color: #19b150;
`;
