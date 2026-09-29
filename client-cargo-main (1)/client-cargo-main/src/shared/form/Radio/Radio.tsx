import React, { FC } from 'react';
import { RadioProps } from 'antd/lib/radio';

import { StyledRadio } from './Radio.style';

const Radio: FC<RadioProps> = props => <StyledRadio {...props} />;

export default Radio;
