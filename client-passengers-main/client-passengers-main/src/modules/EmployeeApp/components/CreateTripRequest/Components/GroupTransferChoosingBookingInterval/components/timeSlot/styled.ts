import styled from 'styled-components';
import { ISelectedRange } from './TimeSlot';

const TimeSlotItem = styled.div<{ isBooked: boolean; isStart: boolean; isEnd: boolean; isSelected: boolean; isPreview: boolean }>`
  width: 100%;
  display: inline-block;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: ${props => props.isStart || props.isEnd
    ? 'rgb(16, 191, 106)'
    : props.isSelected || props.isPreview
      ? 'rgb(231, 249, 240)'
      : 'rgb(248, 248, 248)'}; 
  color: ${props => props.isBooked
    ? 'rgb(255, 171, 161)' : props.isStart || props.isEnd ? 'rgb(255, 255, 255)' : 'rgb(38, 38, 38)'};
  cursor: ${props => props.isBooked ? 'not-allowed' : 'pointer'};
  // border: 1px solid #ddd;
  border-top: ${props => props.isSelected || props.isPreview ? '1px solid rgb(16, 191, 106)' : 'none'};
  border-bottom: ${props => props.isSelected || props.isPreview ? '1px solid rgb(16, 191, 106)' : 'none'};
  box-sizing: border-box;
  border-radius: ${props => props.isStart || props.isEnd ? '8px' : '0'};
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  letter-spacing: -0.3px;
`;

const TimeSlotWrapper = styled.div<{
  isReversEnd: boolean;
  selectedRange: ISelectedRange;
  previewEnd: string;
  isBooked: boolean;
  isStart: boolean;
  isEnd: boolean;
  isSelected: boolean;
  isPreview: boolean;
}>`
  width: 20%;
  height: 38px;
  display: inline-block;
  display: flex;
  border-top: ${props => (props.isStart || props.isEnd) && props.previewEnd ? '1px solid rgb(16, 191, 106)' : props.selectedRange.start && props.selectedRange.end && (props.isStart || props.isEnd) ? '1px solid rgb(16, 191, 106)' : 'none'};
  border-bottom: ${props => (props.isStart || props.isEnd) && props.previewEnd ? '1px solid rgb(16, 191, 106)' : props.selectedRange.start && props.selectedRange.end && (props.isStart || props.isEnd) ? '1px solid rgb(16, 191, 106)' : 'none'};
  box-sizing: border-box;
  border-radius: ${props => props.isStart ? props.isReversEnd ? '0 8px 8px 0' : '8px 0 0 8px' : props.isEnd ? props.isReversEnd ? '8px 0 0 8px' : '0 8px 8px 0' : 'none'};
`;

export {
  TimeSlotItem, TimeSlotWrapper
};
