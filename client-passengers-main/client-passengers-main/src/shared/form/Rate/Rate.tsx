import { RateProps } from 'antd/lib/rate';
import React, { FC } from 'react';

import { StyledRate } from './Rate.style';

const Rate: FC<RateProps> = props => <StyledRate {...props} />;

export default Rate;
