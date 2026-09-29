export interface StepItemContentProps {
  buttonCancel?: boolean;
  buttonSave?: boolean;
  nextStep?: () => void;
  prevStep?: () => void;
}

export type StepItemContent = React.ReactElement<StepItemContentProps>;

export type StepItems = {
  title: string;
  content: StepItemContent;
}[];
