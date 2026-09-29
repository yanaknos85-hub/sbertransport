import React from 'react';
import { InputNumber, Checkbox } from 'antd';
import { tabPaneStyles } from '../../styles/tariffTabStyles';

interface CostValue { enabled: boolean; cost?: number | string | null }

export const CostInput: React.FC<{
  value?: CostValue;
  onChange?: (value: CostValue) => void;
  maxValue?: number;
}> = ({
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  value: { cost, enabled } = { cost: 0, enabled: false }, onChange = () => {}, maxValue,
}) => (
  <div style={tabPaneStyles.costInput}>
    <InputNumber
      disabled={!enabled}
      value={cost as number}
      onChange={cost => onChange({ cost, enabled })}
      min={0}
      step={0.01}
      max={maxValue}
    />
    <Checkbox checked={enabled} onChange={({ target: { checked: enabled } }) => onChange({ cost, enabled })} />
  </div>
);
