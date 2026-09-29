import React, { ReactNode } from 'react';
import { InfoCircleFilled } from '@ant-design/icons';
import { Tooltip } from 'antd';

interface Props {
  title: string;
  tooltip?: ReactNode | string[];
}

export const HeaderWithIcon: React.FC<Props> = ({ title, tooltip }) => {
  const renderTooltipContent = () => {
    if (!tooltip) return null;
    
    if (Array.isArray(tooltip)) {
      return (
        <div>
          {tooltip.map((line, index) => (
            <div key={index}>
              {line}
            </div>
          ))}
        </div>
      );
    }
    
    return tooltip;
  };

  const tooltipContent = renderTooltipContent();

  return (
    <span>
      {title}
      {tooltip ? (
        <Tooltip
          title={tooltipContent}
          color="#fff"
          overlayStyle={{
            maxWidth: 'none',
          }}
          overlayInnerStyle={{ 
            color: '#737373',
            padding: '12px 16px',
            display: 'inline-block',
            minWidth: '200px',
          }}
          placement="top"
        >
          <InfoCircleFilled style={{ 
            color: '#CCCCCC', 
            fontSize: '16px', 
            position: 'relative', 
            top: 2, 
            left: 5, 
            cursor: 'help' 
          }} />
        </Tooltip>
      ) : (
        <InfoCircleFilled style={{ 
          color: '#CCCCCC', 
          fontSize: '16px', 
          position: 'relative', 
          top: 2, 
          left: 5, 
          cursor: 'help' 
        }} />
      )}
    </span>
  );
};