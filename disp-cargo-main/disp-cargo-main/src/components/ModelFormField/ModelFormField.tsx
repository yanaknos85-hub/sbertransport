import React, { Dispatch, FC } from 'react';
import { FormInstance, Rule } from 'antd/lib/form';
import { InputProps, TextAreaProps } from 'antd/lib/input';
import { DatePicker, InputNumber } from 'antd';
import { LabeledValue, SelectProps } from 'antd/lib/select';

// import { WaypointModel } from 'stores/Geo/models/Waypoint.model';
import { TRangePickerArg } from 'types/Types';

import { RadioChangeEvent } from 'antd/lib/radio';
import TextField from './FieldTypes/TextField';
import BooleanField from './FieldTypes/BooleanField';
import CheckboxGroupField from './FieldTypes/CheckboxGroupField';
// import AddressAutoCompleteField from './FieldTypes/AddressAutoCompleteField';
import DateField from './FieldTypes/DateField';
import DateRangeField from './FieldTypes/DateRangeField';
import SelectField from './FieldTypes/SelectField';
import RadioGroupField from './FieldTypes/RadioGroupField';
import NumericField from './FieldTypes/NumericField';
import SliderField from './FieldTypes/SliderField';
import { SliderBaseProps } from 'antd/lib/slider';

export enum ModelFormFieldType {
  TEXT,
  EMAIL,
  PASSWORD,
  SELECT,
  NUMBER,
  CUSTOM,
  BOOLEAN,
  CHECKBOX_GROUP,
  RADIO_GROUP,
  // ADDRESS_AUTO_COMPLETE,
  DATE,
  DATE_RANGE,
  SLIDER,
}

interface BaseFieldProps {
  name: string;
  description?: string;
  editable: boolean;
  required?: boolean;
  disabled?: boolean;
  label?: string;
  rules?: Rule[];
  className?: string;
  header?: string;
}

export interface TextAreaFieldProps extends BaseFieldProps, Omit<TextAreaProps, 'name'> {
  fieldType: ModelFormFieldType.TEXT | ModelFormFieldType.PASSWORD | ModelFormFieldType.EMAIL;
}

export interface TextFieldProps extends BaseFieldProps, Omit<InputProps, 'form' | 'name'> {
  fieldType: ModelFormFieldType.TEXT | ModelFormFieldType.PASSWORD | ModelFormFieldType.EMAIL;
  placeholder?: string;
  rules?: Rule[];
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export interface SelectFieldProps extends BaseFieldProps, Omit<SelectProps<any>, 'options'> {
  fieldType: ModelFormFieldType.SELECT;
  options: LabeledValue[] | (() => LabeledValue[]);
  placeholder?: string;
}

export interface CustomFieldProps extends BaseFieldProps {
  fieldType: ModelFormFieldType.CUSTOM;
  component: ({ form }: { form: FormInstance }) => React.ReactElement;
  onChange?: (e: HTMLElement) => void;
}

export interface CheckboxGroupFieldProps extends BaseFieldProps {
  fieldType: ModelFormFieldType.CHECKBOX_GROUP;
  options: LabeledValue[];
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onChange?: (e: any) => void;
}

export interface RadioGroupFieldProps extends BaseFieldProps {
  fieldType: ModelFormFieldType.RADIO_GROUP;
  options: LabeledValue[];
  onChange?: (e: RadioChangeEvent) => void;
  initialValue?: string;
}

export type DateFieldProps = BaseFieldProps &
  Omit<React.ComponentProps<typeof DatePicker>, 'value'> & {
    fieldType: ModelFormFieldType.DATE;
    onChange?: Dispatch<moment.Moment | undefined>;
    placeholder?: string;
    format?: string;
    showTime?: boolean;
  };

export type DateRangeFieldProps = BaseFieldProps &
  Omit<React.ComponentProps<typeof DatePicker.RangePicker>, 'value'> & {
    fieldType: ModelFormFieldType.DATE_RANGE;
    onChange?: Dispatch<TRangePickerArg>;
    placeholder?: [string, string];
    showTime?: boolean;
  };

export interface NumberFieldProps
  extends BaseFieldProps, Omit<React.ComponentProps<typeof InputNumber>, 'form' | 'name'> {
  fieldType: ModelFormFieldType.NUMBER;
}

export interface BooleanFieldProps extends BaseFieldProps {
  fieldType: ModelFormFieldType.BOOLEAN;
  trueLabel?: string;
  falseLabel?: string;
  unsetLabel?: string;
  placeholder?: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onChange?: (e: any) => void;
}

// export interface AddressAutocompleteFieldProps extends BaseFieldProps {
//   fieldType: ModelFormFieldType.ADDRESS_AUTO_COMPLETE;
//   onAddressSelect?: ((val: WaypointModel, name?: string) => void) | undefined;
//   placeholder?: string;
//   onChange?: (e: HTMLElement) => void;
// }

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export interface SliderFieldProps extends BaseFieldProps, SliderBaseProps {
  fieldType: ModelFormFieldType.SLIDER;
}
export type ModelFormFieldProps =
  | TextFieldProps
  | NumberFieldProps
  | SelectFieldProps
  | CheckboxGroupFieldProps
  | RadioGroupFieldProps
  // | AddressAutocompleteFieldProps
  | DateFieldProps
  | DateRangeFieldProps
  | BooleanFieldProps
  | SliderFieldProps
  | CustomFieldProps;

type ModelFormFieldComponent<T extends BaseFieldProps> = FC<T>;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const ModelFormFieldsByType: Record<ModelFormFieldType, ModelFormFieldComponent<any>> = {
  [ModelFormFieldType.TEXT]: props => <TextField {...props} />,
  [ModelFormFieldType.EMAIL]: props => <TextField {...props} type="email" />,
  [ModelFormFieldType.PASSWORD]: props => <TextField {...props} type="password" />,
  [ModelFormFieldType.BOOLEAN]: props => <BooleanField {...props} />,
  [ModelFormFieldType.CHECKBOX_GROUP]: CheckboxGroupField,
  [ModelFormFieldType.RADIO_GROUP]: RadioGroupField,
  [ModelFormFieldType.SELECT]: props => <SelectField {...props} />,
  [ModelFormFieldType.NUMBER]: props => <NumericField {...props} />,
  // [ModelFormFieldType.ADDRESS_AUTO_COMPLETE]: props => <AddressAutoCompleteField {...props} />,
  [ModelFormFieldType.DATE]: props => <DateField {...props} />,
  [ModelFormFieldType.DATE_RANGE]: props => <DateRangeField {...props} />,
  [ModelFormFieldType.SLIDER]: props => <SliderField {...props} />,
  [ModelFormFieldType.CUSTOM]: (_: ModelFormFieldProps) => {
    throw new Error('WE SHOULD NEVER GET HERE');
  },
};

export const ModelFormField: FC<ModelFormFieldProps & { form: FormInstance }> = props => {
  if (props.fieldType === ModelFormFieldType.CUSTOM) {
    return props.component({ form: props.form });
  }
  return ModelFormFieldsByType[props.fieldType](props);
};
