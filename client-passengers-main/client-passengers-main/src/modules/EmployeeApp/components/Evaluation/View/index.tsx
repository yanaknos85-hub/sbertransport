import { observer } from 'mobx-react';
import React from 'react';

import Process from '../Constants/Process';
import { IProduct } from '../types';
import Failure from './Failure';
import Start from './Start';
import Success from './Success';

const View: React.FC<Partial<IProduct>> = observer(props => (
  <>
    {props.process === Process.START && <Start {...props} />}

    {props.process === Process.POSITIVE && <Success />}

    {props.process === Process.NEGATIVE && <Failure />}
  </>
));

export default View;
