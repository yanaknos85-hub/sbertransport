import React, { FC } from 'react';

import { ISections } from 'modules/RedesignHome/types/Home.types';

interface IChartsData {
  sections: ISections[];
  gap?: number;
  size?: number;
  children?: React.ReactNode;
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const ThinPieChart: FC<IChartsData> = ({
  sections, gap = 2, size = 200, children,
}) => {
  const radius = (size - 10) / 2;
  const circumference = 2 * Math.PI * radius;
  const totalPercentage = sections && sections.reduce((sum, sec) => sum + sec.percentage, 0);
  let accumulatedOffset = 0;
  const gapSize = (gap / 100) * circumference;
  const totalGaps = gapSize * sections.length;

  return (
    <div style={{
      position: 'relative', width: size, height: size,
    }}
    >
      <svg
        width={size}
        height={size}
        viewBox={`0 0 ${size} ${size}`}
      >
        {sections.map((section, index) => {
          const sectionLength = (section.percentage / totalPercentage) * (circumference - totalGaps);
          const dashArray = `${sectionLength} ${circumference - sectionLength}`;
          const dashOffset = index === 1 ? circumference - accumulatedOffset
            : circumference - accumulatedOffset;

          accumulatedOffset += sections.length === index + 1 ? sectionLength - gapSize : sectionLength + gapSize;

          return (
            <circle
              key={index}
              cx={size / 2}
              cy={size / 2}
              r={radius}
              fill="none"
              stroke={section.color}
              strokeWidth={7}
              strokeDasharray={dashArray}
              strokeDashoffset={dashOffset}
              strokeLinecap="round"
              transform={`rotate(-90 ${size / 2} ${size / 2})`}
            />
          );
        })}
      </svg>
      <div
        style={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {children}
      </div>
    </div>
  );
};
