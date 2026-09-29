
export interface StepperProps {
  steps: number;
  active: number;
}

export interface StepSettings {
  type: 'first' | 'middle' | 'last';
  status: 'done' | 'current' | 'empty';
}

export interface StepProps {
  status: 'done' | 'current' | 'empty';
}
