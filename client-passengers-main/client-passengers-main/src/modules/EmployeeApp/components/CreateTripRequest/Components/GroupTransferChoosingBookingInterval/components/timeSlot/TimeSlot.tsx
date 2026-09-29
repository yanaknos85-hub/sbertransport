import React, { FC } from 'react';

import {
  TimeSlotItem, TimeSlotWrapper
} from './styled';

export interface ISelectedRange {
  start: string | null;
  end: string | null;
}

interface PassengerInformationProps {
  time: string;
  isBooked: boolean | undefined;
  isSelected: boolean;
  isPreview: boolean;
  isStart: boolean;
  isEnd: boolean;
  onClick: (time: any) => void;
  onMouseEnter: () => void;
  previewEnd: string | null;
  selectedRange: ISelectedRange;
  isReversEnd: boolean | null;
}

export const TimeSlot: FC<PassengerInformationProps> = ({
  time,
  isBooked,
  isSelected,
  isPreview,
  isStart,
  isEnd,
  onClick,
  onMouseEnter,
  previewEnd,
  selectedRange,
  isReversEnd,
}) => {
  return (
    <TimeSlotWrapper
      isStart={isStart}
      isEnd={isEnd}
      isPreview={isPreview}
      previewEnd={previewEnd}
      selectedRange={selectedRange}
      isReversEnd={isReversEnd}
    >
      <TimeSlotItem
        onClick={onClick}
        onMouseEnter={onMouseEnter}
        isBooked={isBooked}
        isStart={isStart}
        isEnd={isEnd}
        isSelected={isSelected}
        isPreview={isPreview}
        className={isPreview && 'isPreview'}
      >
        {time}
      </TimeSlotItem>
    </TimeSlotWrapper>
  );
};

