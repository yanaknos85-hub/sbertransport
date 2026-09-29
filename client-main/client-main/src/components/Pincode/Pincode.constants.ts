import { PincodeProps } from './Pincode.types';

export const pinInputDefaultProps: Partial<PincodeProps> = {
  type: 'number',
  mask: false,
  showState: true,
  size: 'md',
  autoFocus: false,
  autoTab: true,
  borderColor: '#adadad',
  errorBorderColor: '#ff5743',
  errorBackgroundColor: '#FFEEEC',
  focusBorderColor: '#10BF6A',
  validBorderColor: '#10BF6A',
  validBackgroundColor: '#E7F9F0',
  containerStyle: {},
  inputStyle: {},
  autoComplete: 'off',
  placeholder: 'o',
};
