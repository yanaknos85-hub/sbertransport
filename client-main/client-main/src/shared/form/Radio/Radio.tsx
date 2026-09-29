import { RadioProps } from 'antd/lib/radio';
import React, { FC } from 'react';

import { StyledRadio } from './Radio.style';

const Radio: FC<RadioProps> = props => <StyledRadio {...props} />;

export default Radio;
