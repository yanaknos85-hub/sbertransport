import React from 'react';
import { CheckOutlined } from '@ant-design/icons';

import {
  CurrentEdge,
  CurrentIcon,
  CurrentInnerIcon,
  DoneEdge,
  DoneIcon,
  EmptyEdge,
  EmptyIcon,
  FirstStepDiv,
  LastStepDiv,
  MiddleStepDiv,
  StepperDiv
} from './Stepper.style';
import { StepperProps, StepProps, StepSettings } from './Stepper.types';

const FirstStep = ({ status }: StepProps) => {
  return (
    <FirstStepDiv>
      <Icon status={status} />
      <Edge status={status} />
    </FirstStepDiv>
  );
};

const MiddleStep = ({ status }: StepProps) => {
  return (
    <MiddleStepDiv>
      <Edge status={status} />
      <Icon status={status} />
      <Edge status={status} />
    </MiddleStepDiv>
  );
};

const LastStep = ({ status }: StepProps) => {
  return (
    <LastStepDiv>
      <Edge status={status} />
      <Icon status={status} />
    </LastStepDiv>
  );
};
const dictionary = {
  first: FirstStep,
  middle: MiddleStep,
  last: LastStep,
};

const Icon = ({ status }: StepProps): JSX.Element => {
  switch (status) {
    case 'empty':
      return <EmptyIcon />;
    case 'current':
      return (
        <CurrentIcon>
          <CurrentInnerIcon />
        </CurrentIcon>
      );
    case 'done':
      return (
        <DoneIcon>
          <CheckOutlined color="#fff" size={8} />
        </DoneIcon>
      );
    default:
      return <EmptyIcon />;
  }
};

const Edge = ({ status }: StepProps): JSX.Element => {
  switch (status) {
    case 'empty':
      return <EmptyEdge />;
    case 'current':
      return <CurrentEdge />;
    case 'done':
      return <DoneEdge />;
    default:
      return <EmptyEdge />;
  }
};

const Stepper = ({ steps, active }: StepperProps) => {
  const components: StepSettings[] = [];
  components.push({ type: 'first', status: active > 0 ? 'done' : 'current' });
  for (let i = 1; i < steps - 1; i++) {
    components.push({ type: 'middle', status: active > i ? 'done' : active === i ? 'current' : 'empty' });
  }
  components.push({
    type: 'last',
    status: active < steps - 1 ? 'empty' : active === steps - 1 ? 'done' : 'current',
  });
  return (
    <StepperDiv>
      {components.map((component, index) => {
        const Component = dictionary[component.type];
        const key = `${component.type}-${component.status}-${index}`;
        return <Component key={key} status={component.status} />;
      })}
    </StepperDiv>
  );
};

export default Stepper;
