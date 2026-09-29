import React, { FC } from 'react';
import { TextAreaProps } from 'antd/lib/input';

import { StyledTextarea } from './Textarea.style';

const Textarea: FC<TextAreaProps> = props => <StyledTextarea {...props} />;

export default Textarea;
