import { TextAreaProps } from 'antd/lib/input';
import React, { FC } from 'react';

import { StyledTextarea } from './Textarea.style';

const Textarea: FC<TextAreaProps> = props => <StyledTextarea {...props} />;

export default Textarea;
