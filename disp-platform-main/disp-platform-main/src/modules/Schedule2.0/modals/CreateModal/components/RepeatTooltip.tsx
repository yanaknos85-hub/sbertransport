import React, { FC } from 'react';

import { Tooltip } from 'antd';

import { ReactComponent as QuestionMark } from 'assets/icons/question.svg';

const RepeatTooltip: FC = () => (
  <Tooltip title="Настроить расписание повторяющихся смен" placement="right">
    <QuestionMark />
  </Tooltip>
);

export default RepeatTooltip;
