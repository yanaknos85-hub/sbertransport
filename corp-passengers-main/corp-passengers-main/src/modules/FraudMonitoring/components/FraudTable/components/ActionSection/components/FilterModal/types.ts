import { FilterFormValues } from '../../types';

export interface FilterModalProps {
  isOpened: boolean;
  initialValues: FilterFormValues;
  onClose: VoidFunction;
  onReset: VoidFunction;
  onSubmit: (values: FilterFormValues) => void;
}
